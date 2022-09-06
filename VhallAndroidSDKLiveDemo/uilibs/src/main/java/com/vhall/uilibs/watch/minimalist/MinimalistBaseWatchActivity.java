package com.vhall.uilibs.watch.minimalist;

import static com.vhall.uilibs.interactive.RtcInternal.REQUEST_PUSH;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchLive;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.WebinarInfoDataSource;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.dialog.OutDialog;
import com.vhall.uilibs.util.ActivityUtils;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.emoji.InputView;
import com.vhall.uilibs.watch.minimalist.MiniBaseCallBack.SimpleChatCallback;
import com.vhall.uilibs.widget.PressLikeView;
import com.vhall.uilibs.widget.pushView.PushViewUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 极简模式观看
 */
public abstract class MinimalistBaseWatchActivity extends AppCompatActivity implements View.OnClickListener {

    protected Param params;
    protected WebinarInfo webinarInfo;
    protected PressLikeView pressLikeView;
    protected ImageView iv_like, iv_host_avatar, iv_user_avatar, iv_close, iv_gift, iv_push, iv_mic, mSwitchCamera, mSwitchAudio, mSwitchVideo;
    protected TextView tv_chat, tv_like_num, tv_host_name, tv_look_num, tv_hot_num, tv_push, tv_mic, tv_title;
    protected RecyclerView recyclerView;
    protected MiniChatAdapter chatAdapter;
    protected List<Integer> imageUrls = new ArrayList<>();
    protected WatchMiniFragment watchMiniFragment;
    protected RtcMiniFragment rtcMiniFragment;

    protected InputView inputView;
    protected PushViewUtils pushView;
    protected FrameLayout push_view;
    protected WatchLive watchLive;
    //点赞总数
    protected int likeNum = 0;
    protected int clickLikeNumber = 0;

    protected Random random = new Random();
    public GetCodeCount micCount;
    protected OutDialog showInvited;
    protected boolean isMic = false;
    protected boolean hasVideoOpen = false;
    protected boolean hasAudioOpen = false;
    protected Group group_public;
    protected String bigShowUserId;

    protected int online; //真实在线人数
    protected int onlineVirtual; //虚拟在线人数

    protected int pv; //热度
    protected int pvVirtual; //虚拟热度

    protected String hands_up;  // 是否开启举手   1开启举手，0未开启举手


    public static final int CAMERA_VIDEO = 2; //摄像头
    public static final int CAMERA_AUDIO = 1; //麦克风
    public static final int CAMERA_DEVICE_OPEN = 1;
    public static final int CAMERA_DEVICE_CLOSE = 0;

    protected int likePermission, giftPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minimalist_watch);
        params = (Param) getIntent().getSerializableExtra("param");
        initView();
        initData();
        initInputView();
    }

    private void initView() {
        initLike();
        initGiftPush();
        iv_gift = findViewById(R.id.iv_gift);
        tv_chat = findViewById(R.id.tv_chat);
        iv_mic = findViewById(R.id.iv_mic);
        tv_mic = findViewById(R.id.tv_mic);
        tv_title = findViewById(R.id.tv_title);
        iv_host_avatar = findViewById(R.id.iv_host_avatar);
        iv_user_avatar = findViewById(R.id.iv_user_avatar);
        tv_host_name = findViewById(R.id.tv_host_name);
        tv_like_num = findViewById(R.id.tv_like_num);
        tv_look_num = findViewById(R.id.tv_look_num);
        tv_hot_num = findViewById(R.id.tv_hot_num);
        iv_close = findViewById(R.id.iv_close);
        mSwitchCamera = findViewById(R.id.iv_switch_camera);
        mSwitchVideo = findViewById(R.id.iv_switch_video);
        mSwitchAudio = findViewById(R.id.iv_switch_audio);
        group_public = findViewById(R.id.group_public);

        mSwitchVideo.setOnClickListener(this);
        mSwitchCamera.setOnClickListener(this);
        mSwitchAudio.setOnClickListener(this);
        iv_mic.setOnClickListener(this);
        tv_mic.setOnClickListener(this);
        tv_chat.setOnClickListener(this);
        iv_gift.setOnClickListener(this);
        iv_close.setOnClickListener(this);
        recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new MiniChatAdapter(this);
        recyclerView.setAdapter(chatAdapter);

    }

    protected void updateBigScreen(String mainId) {
        bigShowUserId = mainId;
        if (rtcMiniFragment != null) {
            rtcMiniFragment.updateBigShow(mainId);
        }
    }

    protected CountDownTimer likeNumTimer = new CountDownTimer(2000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            pushLikeNum(clickLikeNumber);
            clickLikeNumber = 0;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (inputView != null) {
            inputView.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (inputView != null) {
            inputView.destroyed();
        }
    }

    private void initData() {
        VhallSDK.initWatch(params.watchId, "", "", params.key, WebinarInfo.LIVE, params.k_id, new WebinarInfoDataSource.LoadWebinarInfoCallback() {
            @Override
            public void onWebinarInfoLoaded(String jsonStr, WebinarInfo data) {
                if (isFinishing()) {
                    return;
                }
                webinarInfo = data;
                //预告状态
                if (webinarInfo.status == WebinarInfo.BESPEAK) {
                    showToast("还没开始直播");
                    finish();
                    return;
                }
                if (webinarInfo.getWebinarInfoData() == null) {
                    showToast("只支持化蝶活动");
                    finish();
                    return;
                }
                params.webinar_id = webinarInfo.webinar_id;
                watchMiniFragment = WatchMiniFragment.newInstance();
                initChatHistory();
                watchMiniFragment.initWebinarInfo(data, params, new MessageEventCallback(), new ChatCallback());
                ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                        watchMiniFragment, R.id.contentVideo);

                VhallSDK.permissionsCheck(webinarInfo.webinar_id, webinarInfo.hostId, new RequestDataCallbackV2<String>() {
                    @Override
                    public void onSuccess(String data) {
                        JSONObject permissions = null;
                        try {
                            permissions = new JSONObject(data);
                            likePermission = Integer.parseInt(permissions.optString("ui.watch_hide_like"));
                            giftPermission = Integer.parseInt(permissions.optString("ui.hide_gifts"));
                            if (likePermission == 0) {
                                iv_like.setVisibility(View.GONE);
                                tv_like_num.setVisibility(View.GONE);
                            }
                            if (giftPermission == 0) {
                                iv_gift.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dealWebinarData();
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        dealWebinarData();
                    }
                });

                VhallSDK.getRoomLike(webinarInfo.vss_room_id, new RequestDataCallbackV2<Integer>() {
                    @Override
                    public void onSuccess(Integer data) {
                        int a = data;
                        Log.e("vhall", a + "");
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        Log.e("vhall", errorMsg + "getRoomLike");
                    }
                });
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                showToast(errorMsg);
                finish();
            }
        });
    }

    protected abstract void initChatHistory();

    private void dealWebinarData() {
        likeNum = webinarInfo.like_num;
        hands_up = webinarInfo.hands_up;
        if (likeNum == 0) {
            tv_like_num.setVisibility(View.GONE);
        }

        if ("0".equals(hands_up)) {
            iv_mic.setVisibility(View.GONE);
        }
        setLikeNum(webinarInfo.like_num);
        tv_host_name.setText(BaseUtil.getLimitString(webinarInfo.hostName, 8));
        online = webinarInfo.online;
        onlineVirtual = webinarInfo.onlineVirtual;
        pv = webinarInfo.pv;
        tv_title.setText(webinarInfo.subject);
        pvVirtual = webinarInfo.pvVirtual;
        setLookNum();
        RequestOptions requestOptions = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.drawable.icon_default_avatar);
        Glide.with(this).load(VhallSDK.getUserAvatar()).apply(requestOptions).into(iv_user_avatar);
        Glide.with(this).load(webinarInfo.hostAvatar).apply(requestOptions).into(iv_host_avatar);
        if (webinarInfo.onlineShow == 0) {
            tv_look_num.setVisibility(View.GONE);
        }
        if (webinarInfo.pvShow == 0) {
            tv_hot_num.setVisibility(View.GONE);
        }
        if (webinarInfo.getWebinarInfoData() != null && webinarInfo.getWebinarInfoData().roomToolsStatusData != null) {
            bigShowUserId = webinarInfo.getWebinarInfoData().roomToolsStatusData.main_screen;
        }
    }

    public void setLookNum() {
        int pvNum = this.pv + pvVirtual;
        int onlineNum = online + onlineVirtual;
        tv_look_num.setText(String.valueOf(onlineNum > 999 ? "999+" : onlineNum));
        tv_hot_num.setText(String.valueOf(pvNum > 999 ? "999+" : pvNum));
    }

    public void setLikeNum(int num) {
        if (likePermission == 1 && num > 0) {
            tv_like_num.setVisibility(View.VISIBLE);
        } else {
            tv_like_num.setVisibility(View.GONE);
        }
        tv_like_num.setText(String.valueOf(num > 999 ? "999+" : num));
    }

    public abstract void initInputView();

    public abstract void initGiftPush();

    public abstract void initLike();

    public abstract void pushLikeNum(int num);

    public void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    //开始申请互动倒计时
    public abstract void startMicCount();

    //显示上麦邀请
    public abstract void showInvited();

    //进入互动房间
    public abstract void enterInteractive();

    //离开互动房间
    public abstract void leaveInteractive();

    //自己开关音视频接口调用
    public abstract void switchDevice(int device, int type);

    //更新UI
    public abstract void updateVideoFrame(int status);

    //更新UI
    public abstract void updateAudioFrame(int status);

    //更新互动流的视频开关
    public abstract void switchVideoFrame(int status);

    //更新互动流的音频开关
    public abstract void switchAudioFrame(int status);


    public class GetCodeCount extends CountDownTimer {

        GetCodeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tv_mic.setText(String.format("%ss", String.valueOf(millisUntilFinished / 1000 + 1)));
        }

        @Override
        public void onFinish() {
            if ("0".equals(hands_up)) {
                iv_mic.setVisibility(View.GONE);
            } else {
                iv_mic.setVisibility(View.VISIBLE);
            }
            tv_mic.setText("");
            tv_mic.setVisibility(View.GONE);
        }
    }

    /**
     * 观看过程消息监听
     */
    private class MessageEventCallback extends MiniBaseCallBack.SimpleMessageEventCallback {
        @Override
        public void onEvent(MessageServer.MsgInfo messageInfo) {
            switch (messageInfo.event) {
                case MessageServer.EVENT_KICKOUT://踢出
                    showToast("您已被踢出");
                    finish();
                    break;
                case MessageServer.EVENT_OVER://直播结束
                    showToast("直播已结束");
                    finish();
                    break;
                case MessageServer.EVENT_DISABLE_CHAT://禁言
                    showToast("您已被禁言");
                    break;
                case MessageServer.EVENT_PERMIT_CHAT://解除禁言
                    showToast("您已被解除禁言");
                    break;
                case MessageServer.EVENT_CHAT_FORBID_ALL://全员禁言
                    break;
                case MessageServer.EVENT_NOTICE:
                    break;

                case MessageServer.EVENT_INTERACTIVE_ALLOW_MIC:
                    //主持人 同意上麦
                    enterInteractive();
                    break;
                case MessageServer.EVENT_INTERACTIVE_ALLOW_HAND:
                    showToast(messageInfo.status == 0 ? "举手按钮关闭" : "举手按钮开启");
                    hands_up = String.valueOf(messageInfo.status);
                    if (messageInfo.status == 0 && !isMic) {
                        iv_mic.setVisibility(View.GONE);
                        tv_mic.setVisibility(View.GONE);
                    } else {
                        iv_mic.setVisibility(View.VISIBLE);
                    }
                    break;
                case MessageServer.EVENT_INTERACTIVE_DOWN_MIC://下麦
                    leaveInteractive();
                    break;
                case MessageServer.EVENT_INVITED_MIC://被邀请上麦
                    showInvited();
                    break;

                case MessageServer.EVENT_PRAISE_TOTAL:
                    likeNum = messageInfo.likeNum;
                    if (likePermission == 1)
                        tv_like_num.setVisibility(View.VISIBLE);
                    setLikeNum(messageInfo.likeNum);
                    break;

                case MessageServer.EVENT_GIFT_SEND_SUCCESS:
                    ChatMessageData chatMessageData = new ChatMessageData();
                    chatMessageData.msgInfo = messageInfo;
                    chatAdapter.addData(chatMessageData);
                    if (chatAdapter.getItemCount() > 0)
                        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

                    pushView.intoShowEnter(push_view, messageInfo.giftInfoData);
                    break;
                case MessageServer.EVENT_VRTC_BIG_SCREEN_SET:
                    //更新互动大屏幕显示
                    updateBigScreen(messageInfo.roomJoinId);
                    break;
                case MessageServer.EVENT_SWITCH_DEVICE:
                    /**
                     * 新增 收到消息 切换自己设备
                     */
                    if (messageInfo.device == CAMERA_AUDIO) { // 麦克风
                        switchAudioFrame(messageInfo.status);
                        updateAudioFrame(messageInfo.status);
                    } else { //2摄像头
                        switchVideoFrame(messageInfo.status);
                        updateVideoFrame(messageInfo.status);
                    }
                    break;

                case MessageServer.EVENT_VRTC_SPEAKER_SWITCH:
                    //互动设置为主讲人
                    showToast(BaseUtil.getLimitString(messageInfo.roomJoinId) + "已被设为主讲人");
                    break;
                default:
                    break;
            }
        }
    }

    private void addChatData(ChatServer.ChatInfo chatInfo) {
        ChatMessageData chatMessageData1 = new ChatMessageData();
        chatMessageData1.chatInfo = chatInfo;
        chatAdapter.addData(chatMessageData1);
        if (chatAdapter.getItemCount() > 0)
            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
    }

    private class ChatCallback extends SimpleChatCallback {
        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    addChatData(chatInfo);
                    break;
                case ChatServer.eventOnlineKey:
                    pv++;
                case ChatServer.eventOfflineKey:
                    if (chatInfo.onlineData != null) {
                        online = chatInfo.onlineData.concurrent_user;
                    }
                    addChatData(chatInfo);
                    setLookNum();
                    break;
                case ChatServer.eventVirtualUpdate:
                    if (chatInfo.virtualNumUpdateData != null) {
                        onlineVirtual += chatInfo.virtualNumUpdateData.update_online_num;
                        pvVirtual += chatInfo.virtualNumUpdateData.update_pv;
                    }
                    setLookNum();
                    break;

                default:
                    break;
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PUSH) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            } else {
                showToast("未获取到音视频设备权限，请前往获取权限");
            }
        }
    }

}