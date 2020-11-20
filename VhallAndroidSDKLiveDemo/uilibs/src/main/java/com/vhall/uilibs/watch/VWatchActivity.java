package com.vhall.uilibs.watch;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.VhallSDK;
import com.vhall.business.data.Survey;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.WebinarInfoDataSource;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.chat.VChatFragment;
import com.vhall.uilibs.interactive.InteractiveActivity;
import com.vhall.uilibs.util.ActivityUtils;
import com.vhall.uilibs.util.CircleView;
import com.vhall.uilibs.util.DevicePopu;
import com.vhall.uilibs.util.ExtendTextView;
import com.vhall.uilibs.util.InvitedDialog;
import com.vhall.uilibs.util.MessageLotteryData;
import com.vhall.uilibs.util.ShowLotteryDialog;
import com.vhall.uilibs.util.SignInDialog;
import com.vhall.uilibs.util.SurveyPopu;
import com.vhall.uilibs.util.SurveyPopuVss;
import com.vhall.uilibs.util.SurveyView;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.uilibs.util.emoji.InputView;
import com.vhall.uilibs.util.emoji.KeyBoardManager;
import com.vhall.uilibs.util.handler.WeakHandler;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import static com.vhall.uilibs.util.SurveyView.EVENT_JS_BACK;
import static com.vhall.uilibs.util.SurveyView.EVENT_PAGE_LOADED;

/**
 * 观看页的Activity
 */
public class VWatchActivity extends FragmentActivity implements WatchContract.WatchView {

    private FrameLayout contentChat;
    private LinearLayout ll_detail;
    private CircleView mHand;
    private ExtendTextView tv_notice;
    private TextView tvOnlineNum;
    private Param param;
    private int type;
    private WatchContract.WatchView watchView;

    public WatchPlaybackFragment playbackFragment;
    public VWatchLiveFragment liveFragment;
    public VChatFragment chatFragment;
    InputView inputView;
    public int chatEvent = VChatFragment.CHAT_EVENT_CHAT;
    private SurveyPopuVss popuVss;
    private SurveyPopu popu;
    private SignInDialog signInDialog;
    private ShowLotteryDialog lotteryDialog;
    private InvitedDialog invitedDialog;
    WatchContract.WatchPresenter mPresenter;
    private Fragment docFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.v_watch_activity);
        fragmentManager = getSupportFragmentManager();
        param = (Param) getIntent().getSerializableExtra("param");
        type = getIntent().getIntExtra("type", VhallUtil.WATCH_LIVE);
        docFragment = fragmentManager.findFragmentById(R.id.contentDoc);
        chatFragment = (VChatFragment) getSupportFragmentManager().findFragmentById(R.id.contentChat);
        initView();
        if (chatFragment == null) {
            chatFragment = VChatFragment.newInstance(type, false);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), chatFragment, R.id.contentChat);
        }

        watchView = this;

        initWatch(param);
    }


    public void initWatch(Param params) {
        String customeId = Build.BOARD + Build.DEVICE + Build.SERIAL;
        String customNickname = Build.BRAND + "手机用户";
        int watchType;
        if (type == VhallUtil.WATCH_LIVE) {
            watchType = WebinarInfo.LIVE;
        } else {
            watchType = WebinarInfo.VIDEO;
        }
        VhallSDK.initWatch(params.watchId, customeId, customNickname, params.key, watchType, new WebinarInfoDataSource.LoadWebinarInfoCallback() {
            @Override
            public void onWebinarInfoLoaded(String jsonStr, WebinarInfo webinarInfo) {
                param.webinar_id = webinarInfo.webinar_id;
                if(webinarInfo.status == WebinarInfo.BESPEAK)//预告状态
                    watchView.showToast("还没开始直播");
                /**
                 *
                 * 重要说明
                 * 房间类型为H5是 webinarInfo 中vssToken，vssRoomId 有值，必需使用H5播放器播放，使用 PresenterVss
                 * 房间类型为Flash时   vssToken，vssRoomId 空，必需使用Flash播放器播放 Presenter
                 */
                //敏感词过滤信息，发送聊天、评论通用
                if (webinarInfo.filters != null && webinarInfo.filters.size() > 0) {
                    param.filters.clear();
                    param.filters.addAll(webinarInfo.filters);
                }
                if (docFragment == null) {
                    docFragment = new DocumentFragment();
                    fragmentManager.beginTransaction().add(R.id.contentDoc, docFragment).commit();
                }
                if (liveFragment == null && type == VhallUtil.WATCH_LIVE) {
                    //直播间，公告信息
                    if (webinarInfo.notice != null && !TextUtils.isEmpty(webinarInfo.notice.content)) {
                        param.noticeContent = webinarInfo.notice.content;
                    }
                    liveFragment = VWatchLiveFragment.newInstance();
                    ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), liveFragment, R.id.contentVideo);
                    new VWatchLivePresenter(liveFragment, (WatchContract.DocumentView) docFragment, chatFragment, watchView, param, webinarInfo);
                } else if (playbackFragment == null && type == VhallUtil.WATCH_PLAYBACK) {
                    playbackFragment = WatchPlaybackFragment.newInstance();
                    ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), playbackFragment, R.id.contentVideo);
                    new WatchPlaybackPresenter(playbackFragment, (WatchContract.DocumentView) docFragment, chatFragment, watchView, param, webinarInfo);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                showToast(errorMsg);
                if (liveFragment == null && type == VhallUtil.WATCH_LIVE) {
                    liveFragment = VWatchLiveFragment.newInstance();
                    ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), liveFragment, R.id.contentVideo);
                    docFragment = new DocumentFragment();
                    fragmentManager.beginTransaction().replace(R.id.contentDoc, docFragment).commit();
                    new VWatchLivePresenter(liveFragment, (WatchContract.DocumentView) docFragment, chatFragment, watchView, param, null);
                }
                if (playbackFragment == null && type == VhallUtil.WATCH_PLAYBACK) {
                    playbackFragment = WatchPlaybackFragment.newInstance();
                    ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), playbackFragment, R.id.contentVideo);
                    docFragment = new DocumentFragment();
                    fragmentManager.beginTransaction().replace(R.id.contentDoc, docFragment).commit();
                    new WatchPlaybackPresenter(playbackFragment, (WatchContract.DocumentView) docFragment, chatFragment, watchView, param, null);
                }
            }
        });
    }


    private void initView() {
        tvOnlineNum = findViewById(R.id.tv_online_num);

        inputView = new InputView(this, KeyBoardManager.getKeyboardHeight(this), KeyBoardManager.getKeyboardHeightLandspace(this));
        inputView.add2Window(this);
        inputView.setClickCallback(new InputView.ClickCallback() {
            @Override
            public void onEmojiClick() {

            }
        });
        inputView.setOnSendClickListener(new InputView.SendMsgClickListener() {
            @Override
            public void onSendClick(String msg, InputUser user) {
                if (chatFragment != null && chatEvent == VChatFragment.CHAT_EVENT_CHAT) {
                    chatFragment.performSend(msg, chatEvent);
                }
            }
        });
        inputView.setOnHeightReceivedListener(new InputView.KeyboardHeightListener() {
            @Override
            public void onHeightReceived(int screenOri, int height) {
                if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    KeyBoardManager.setKeyboardHeight(VWatchActivity.this, height);
                } else {
                    KeyBoardManager.setKeyboardHeightLandspace(VWatchActivity.this, height);
                }
            }
        });
        ll_detail       = findViewById(R.id.ll_detail);
        contentChat     = findViewById(R.id.contentChat);

        mHand = this.findViewById(R.id.image_hand);
        mHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击上麦, 再次点击下麦
                if (mPresenter != null) {
                    mPresenter.onRaiseHand();
                }
            }
        });

        tv_notice = this.findViewById(R.id.tv_notice);
        tv_notice.setDrawableClickListener(new ExtendTextView.DrawableClickListener() {
            @Override
            public void onDrawableClick(int position) {
                switch (position) {
                    case ExtendTextView.DRAWABLE_RIGHT:
                        dismissNotice();
                        break;
                }
            }
        });
        if (type == VhallUtil.WATCH_LIVE) {
//            questionBtn.setVisibility(View.VISIBLE);
            contentChat.setVisibility(View.VISIBLE);
        }
        if (type == VhallUtil.WATCH_PLAYBACK) {
            contentChat.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showChatView(boolean isShowEmoji, InputUser user, int contentLengthLimit) {
        if (contentLengthLimit > 0) {
            inputView.setLimitNo(contentLengthLimit);
        }
        inputView.show(isShowEmoji, user);
    }

    @Override
    public void showSignIn(String signId, int startTime) {
        if (signInDialog == null) {
            signInDialog = new SignInDialog(this);
        }
        signInDialog.setSignInId(signId);
        signInDialog.setCountDownTime(startTime);
        signInDialog.setOnSignInClickListener(new SignInDialog.OnSignInClickListener() {
            @Override
            public void signIn(String signId) {
                mPresenter.signIn(signId);
            }
        });
        signInDialog.show();
    }

    @Override
    public void dismissSignIn() {
        if (signInDialog != null) {
            signInDialog.dismiss();
        }
    }

    @Override
    public void showSurvey(String url, String title) {
        if (popuVss == null) {
            popuVss = new SurveyPopuVss(this);
            popuVss.setListener(new SurveyView.EventListener() {
                @Override
                public void onEvent(int eventCode, final String eventMsg) {
                    switch (eventCode) {
                        case EVENT_PAGE_LOADED:
                            //页面加载完成
                            break;
                        case EVENT_JS_BACK:
                            //数据回调
                            new WeakHandler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    popuVss.dismiss();
                                    // mPresenter.submitSurvey(eventMsg);
                                }
                            });
                            break;
                        default:
                            break;
                    }
                }
            });
        }
        popuVss.loadView(url, title);
        popuVss.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.NO_GRAVITY, 0, 0);
    }

    @Override
    public void showSurvey(Survey survey) {
        if (popu == null) {
            popu = new SurveyPopu(this);
            popu.setOnSubmitClickListener(new SurveyPopu.OnSubmitClickListener() {
                @Override
                public void onSubmitClick(Survey survey1, String result) {
                    mPresenter.submitSurvey(survey1, result);
                }
            });
        }
        popu.setSurvey(survey);
        popu.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.NO_GRAVITY, 0, 0);
    }

    @Override
    public void dismissSurvey() {
        if (popuVss != null) {
            popuVss.dismiss();
        }
        if (popu != null) {
            popu.dismiss();
        }
    }
    //显示问答
    @Override
    public void showQAndA(){
    }

    //隐藏问答
    public void dismissQAndA(){
    }

    @Override
    public int changeOrientation() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            ll_detail.setVisibility(View.GONE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ll_detail.setVisibility(View.VISIBLE);
        }
        return getRequestedOrientation();
    }

    @Override
    public void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToast(int toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void showLottery(MessageLotteryData lotteryData) {
        if (lotteryDialog == null) {
            lotteryDialog = new ShowLotteryDialog(this);
        }
        lotteryDialog.setMessageInfo(lotteryData);
        lotteryDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        lotteryDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        lotteryDialog.show();
    }

    @Override
    public void enterInteractive() { // 进入互动房间
        Intent intent = new Intent(this, InteractiveActivity.class);
        intent.putExtra("param", param);
        startActivity(intent);
    }

    @Override
    public void refreshHand(int second) {
        mHand.setTextAndInvalidate(second);
    }

    @Override
    public void showInvited() {
        if (invitedDialog == null) {
            invitedDialog = new InvitedDialog(this);
            invitedDialog.setPositiveOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //同意上麦
                    mPresenter.replyInvite(1);
                    enterInteractive();
                    invitedDialog.dismiss();
                    //发送同意上麦信息
                }
            });
            invitedDialog.setNegativeOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.replyInvite(2);
                    invitedDialog.dismiss();
                    //发送拒绝上麦信息
                }
            });
        }
        invitedDialog.setRefuseInviteListener(new InvitedDialog.RefuseInviteListener() {
            @Override
            public void refuseInvite() {
                mPresenter.replyInvite(2);
            }
        });
        invitedDialog.show();
    }

    @Override
    public void setOnlineNum(int onlineNum) {
        tvOnlineNum.setText("在线人数：" + onlineNum);
    }


    @Override
    public void showNotice(String content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        tv_notice.setText(content);
        tv_notice.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissNotice() {
        tv_notice.setVisibility(View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        inputView.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            changeOrientation();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onUserLeaveHint() {
        if (null != inputView) {
            inputView.dismiss();
        }
        super.onUserLeaveHint();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setPresenter(WatchContract.WatchPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //TODO 投屏相关
//    ------------------------------------------------------投屏相关--------------------------------------------------
    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
    private DevicePopu devicePopu;
    private AndroidUpnpService upnpService;

    protected class BrowseRegistryListener extends DefaultRegistryListener {

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {

        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {

        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            if (device.getType().getNamespace().equals("schemas-upnp-org") && device.getType().getType().equals("MediaRenderer")) {
                deviceAdded(device);
            }
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
//            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
//            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (devicePopu == null) {
                        devicePopu = new DevicePopu(VWatchActivity.this);
                        devicePopu.setOnItemClickListener(new OnItemClick());
                    }
                    devicePopu.deviceAdded(device);
                }
            });
        }

        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (devicePopu == null) {
                        devicePopu = new DevicePopu(VWatchActivity.this);
                        devicePopu.setOnItemClickListener(new OnItemClick());
                    }
                    devicePopu.deviceRemoved(device);
                }
            });
        }
    }

    class OnItemClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final DeviceDisplay deviceDisplay = (DeviceDisplay) parent.getItemAtPosition(position);
            devicePopu.setDmcControl(mPresenter.dlnaPost(deviceDisplay, upnpService));
        }
    }

    @Override
    public void showDevices() {
        if (devicePopu == null) {
            devicePopu = new DevicePopu(this);
            devicePopu.setOnItemClickListener(new OnItemClick());
        }
        devicePopu.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    @Override
    public void dismissDevices() {
        if (devicePopu != null)
            devicePopu.dismiss();
    }
}
