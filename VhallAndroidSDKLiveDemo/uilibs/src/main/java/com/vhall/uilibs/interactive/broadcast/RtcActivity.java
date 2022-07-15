package com.vhall.uilibs.interactive.broadcast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vhall.beautify.VHBeautifyKit;
import com.vhall.beautifykit.control.FaceBeautyControlView;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business_interactive.InterActive;
import com.vhall.net.NetBroadcastReceiver;
import com.vhall.net.NetUtil;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.beautysource.FaceBeautyDataFactory;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.interactive.RtcInternal;
import com.vhall.uilibs.interactive.broadcast.config.RtcConfig;
import com.vhall.uilibs.interactive.broadcast.present.IBroadcastContract;
import com.vhall.uilibs.interactive.broadcast.present.RtcH5Present;
import com.vhall.uilibs.interactive.dialog.ChooseDocDialog;
import com.vhall.uilibs.interactive.dialog.OutDialog;
import com.vhall.uilibs.interactive.dialog.OutDialogBuilder;
import com.vhall.uilibs.interactive.dialog.UserListNewDialog;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.CommonUtil;
import com.vhall.uilibs.util.DensityUtils;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.RenViewUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.util.UserManger;
import com.vhall.uilibs.util.VhallGlideUtils;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;
import com.vhall.vhss.CallBack;
import com.vhall.vhss.data.RoleNameData;
import com.vhall.vhss.data.WebinarInfoData;

import org.vhwebrtc.SurfaceViewRenderer;

import static android.support.constraint.ConstraintSet.PARENT_ID;
import static com.vhall.uilibs.interactive.RtcInternal.REQUEST_PUSH;

/**
 * 互动界面  仿制 微吼直播
 * 只能处理RTC 主播/嘉宾
 */
public class RtcActivity extends FragmentActivity implements View.OnClickListener, IBroadcastContract.IBroadcastView {
    private final static String KEY_PARAMS = "params";
    private final static String KEY_WEBINAR_INFO = "webinarInfo";
    private final static String KEY_IS_GUEST = "isGuest";

    /**
     * 主讲人发起互动
     *
     * @param context
     * @param info
     * @param param
     */
    public static void startActivity(Context context, WebinarInfo info, Param param) {
        Intent intent = new Intent(context, RtcActivity.class);
        intent.putExtra(KEY_WEBINAR_INFO, info);
        intent.putExtra(KEY_PARAMS, param);
        context.startActivity(intent);
    }

    /**
     * 嘉宾进入互动
     *
     * @param context
     * @param isGuest
     * @param webinarInfoData
     */
    public static void startActivity(Context context, boolean isGuest, WebinarInfo webinarInfoData) {
        Intent intent = new Intent(context, RtcActivity.class);
        intent.putExtra(KEY_IS_GUEST, isGuest);
        intent.putExtra(KEY_WEBINAR_INFO, webinarInfoData);
        context.startActivity(intent);
    }

    private CountDownTimer mCount;
    private TextView tvTimeNum, tvStart, tvMic, tvTime, tvLookNum, tvErrorName;
    private ImageView ivDoc;
    private ProgressBar progressBar;
    private FrameLayout flBroadcast, flDoc;
    private ConstraintLayout clStart, clOver, clError;
    private boolean isGuest = false, isRtc = false;
    private ImageView ivTopAvatar, ivOverAvatar, ivChangeCamera, ivCamera, ivVoice, ivBeauty, ivMic;
    private Group groupPlay, groupDocClear, groupDoc, groupUser, groupDocEdit, groupDocView;
    private RtcFragment broadcastRtcFragment;
    private DocFragment docFragment;
    private RecyclerView recyclerView;

    private OutDialog outDialog;
    private UserListNewDialog mangerDialog;
    private String mainId = "1";

    private ConstraintLayout.LayoutParams playerViewLayoutParams;
    private VHRenderView mainLocalView;
    private boolean forbidBroadcast = true;
    private Group group_a, group_v;
    /**
     * 是否上麦
     */
    private boolean isPublic = false;

    /**
     * 上麦 美颜等开关
     */
    private boolean isMic = false, isVoice = true, isCamera = true, showBeauty = true;
    /**
     * 是否清屏
     */
    private boolean showChatView = true;
    private IBroadcastContract.IBroadcastPresent broadcastPresent;
    private ChatAdapter adapter = new ChatAdapter();
    private long time = 0;
    private NetUtil netUtil;

    /**
     * webinar_type 直播类型 1-音频、2-视频、3-互动
     * webinar_show_type 横竖屏
     * webinar_id 直播id
     */
    private String webinar_type, webinar_id;
    private int webinar_show_type;

    private InterActive mInterActive;


    private long mLocalBeginTime = 0;
    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //记录首次开播本地时间
            if (time == 0) {
                mLocalBeginTime = System.currentTimeMillis();
            }
            tvTime.setText(CommonUtil.converLongTimeToStr(time += 1000));
            sendEmptyMessageDelayed(1, 1000);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.rtc_live_activity_layout);
        monitorNetWork();
        initData();
        //只能处理rtc 信息
        if (TextUtils.isEmpty(webinar_id) || !isRtc) {
            baseShowToast("错误数据");
            finish();
            return;
        }

        initView();
        //设置屏幕方向   // 0 竖屏 1横屏
        if (webinar_show_type == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            initBeautifyData(0);
        } else {
            initBeautifyData(1);
        }
    }

    private WebinarInfo webinarInfo;


    private void initData() {
        RtcConfig.setInteractive(null);
        isGuest = getIntent().getBooleanExtra(KEY_IS_GUEST, false);
        webinarInfo = (WebinarInfo) getIntent().getSerializableExtra(KEY_WEBINAR_INFO);
        if (webinarInfo != null) {
            webinar_type = webinarInfo.getWebinarInfoData().getWebinar().mode;
            webinar_id = webinarInfo.webinar_id;
            webinar_show_type = webinarInfo.getWebinarInfoData().webinar_show_type;
            //进入房间初始化值
            roleNameData = webinarInfo.roleNameData;
            if (broadcastPresent != null && roleNameData != null) {
                broadcastPresent.updateHostRoleName(roleNameData.host_name);
            }
            isRtc = TextUtils.equals("3", webinar_type);
            if (isRtc) {
                RenViewUtils.updateRoleName(roleNameData);
            }
            if (adapter != null) {
                adapter.updateRoleName(roleNameData);
            }
        }
    }

    private void monitorNetWork() {
        netUtil = new NetUtil(getApplicationContext(), new NetBroadcastReceiver.NetChangeListener() {
            @Override
            public void onChangeListener(int status) {
                if (RtcInternal.isNetworkConnected(getApplicationContext())) {
                    hideLoadProgress();
                    //防止网络中断之间 修改昵称
                    VhallSDK.getRoleName(webinar_id, new RequestDataCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            if (result instanceof RoleNameData) {
                                roleNameData = (RoleNameData) result;
                                if (broadcastPresent != null) {
                                    broadcastPresent.updateHostRoleName(roleNameData.host_name);
                                }
                                if (isRtc) {
                                    if (RenViewUtils.updateRoleName(roleNameData))
                                        broadcastRtcFragment.updateViewHandler();
                                }
                                if (adapter != null) {
                                    adapter.updateRoleName(roleNameData);
                                }
                            }
                        }

                        @Override
                        public void onError(int errorCode, String errorMsg) {

                        }
                    });
                } else {
                    baseShowToast("当前网络异常");
                    finish();
                    return;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (broadcastPresent != null) {
            broadcastPresent.hintInputView();
        }
    }

    private void baseShowToast(final String txt) {
        ToastUtil.showToast(txt);
    }

    protected void initView() {

        group_a = findViewById(R.id.group_a);
        group_v = findViewById(R.id.group_v);

        mainLocalView = findViewById(R.id.render_main_view);
        mainLocalView.init(null, null);
        mainLocalView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
        tvTimeNum = findViewById(R.id.tv_time_num);
        tvErrorName = findViewById(R.id.tv_error_name);
        tvLookNum = findViewById(R.id.tv_look_num);
        recyclerView = findViewById(R.id.recycle_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        ivTopAvatar = findViewById(R.id.liver_top_avatar);
        ivOverAvatar = findViewById(R.id.over_avatar);
        tvStart = findViewById(R.id.tv_start);
        tvTime = findViewById(R.id.tv_time);
        progressBar = findViewById(R.id.pr_time);
        clStart = findViewById(R.id.cl_start);
        clOver = findViewById(R.id.cl_over);
        clError = findViewById(R.id.cl_error);
        ivChangeCamera = findViewById(R.id.iv_change_camera);
        ivCamera = findViewById(R.id.iv_camera);
        ivDoc = findViewById(R.id.iv_doc);
        ivVoice = findViewById(R.id.iv_voice);
        tvMic = findViewById(R.id.tv_mic);
        ivMic = findViewById(R.id.iv_mic);

        flBroadcast = findViewById(R.id.fragment_broadcast);
        flDoc = findViewById(R.id.fragment_doc);
        ivBeauty = findViewById(R.id.iv_beauty);
        groupPlay = findViewById(R.id.group_play);
        groupUser = findViewById(R.id.group_user);
        groupDocClear = findViewById(R.id.group_doc_clear);
        groupDocEdit = findViewById(R.id.group_doc_edit);
        groupDocView = findViewById(R.id.group_doc_choose);
        groupDoc = findViewById(R.id.group_doc);
        groupUser.setVisibility(View.GONE);
        groupDoc.setVisibility(View.GONE);
        groupDocClear.setVisibility(View.GONE);

        playerViewLayoutParams = new ConstraintLayout.LayoutParams(((ConstraintLayout.LayoutParams) flBroadcast.getLayoutParams()));
        initListener();

        if (isGuest) {
            if (webinarInfo.getWebinarInfoData() == null || webinarInfo.getWebinarInfoData().getWebinar() == null || webinarInfo.getWebinarInfoData().getInteract() == null) {
                baseShowToast("错误数据");
                finish();
                return;
            } else {
                webinar_id = webinarInfo.getWebinarInfoData().webinar.id;
                webinar_type = webinarInfo.getWebinarInfoData().webinar.mode;
                webinar_show_type = webinarInfo.getWebinarInfoData().webinar_show_type;
                RtcInternal.report(webinarInfo.getWebinarInfoData().join_info.third_party_user_id, webinar_id, getApplicationContext());
            }

            tvStart.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            tvTimeNum.setVisibility(View.GONE);
            clStart.setVisibility(View.GONE);
            groupPlay.setVisibility(View.VISIBLE);
            groupUser.setVisibility(View.VISIBLE);
            groupDocClear.setVisibility(View.VISIBLE);
            groupDocView.setVisibility(View.VISIBLE);
            ivMic.setVisibility(View.VISIBLE);
            tvTime.setText(webinarInfo.getWebinarInfoData().getWebinar().getSubject());
            tvErrorName.setText(BaseUtil.getLimitString(webinarInfo.getWebinarInfoData().getWebinar().getUserinfo().getNickname()));

            String url = UserManger.judgePic(webinarInfo.getWebinarInfoData().getWebinar().getUserinfo().getAvatar());
            VhallGlideUtils.loadImage(RtcActivity.this, url, R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivTopAvatar);
            VhallGlideUtils.loadImage(RtcActivity.this, url, R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivOverAvatar);
        }

        initRtc();

        docFragment = DocFragment.getInstance(webinar_type, String.valueOf(webinar_show_type));
        if (isGuest) {
            processGuest();
        }else {
            RtcInternal.isGrantedPermissionRtc(this, REQUEST_PUSH);
        }
        //活动显示类型  0 竖屏 1横屏
        broadcastPresent.initInputView();

        showFragment(R.id.fragment_doc, docFragment);

        mInterActive = broadcastRtcFragment.getInteractive();
    }

    private void initListener() {
        ivChangeCamera.setOnClickListener(this);
        ivCamera.setOnClickListener(this);
        findViewById(R.id.iv_close).setOnClickListener(this);
        findViewById(R.id.iv_mic).setOnClickListener(this);
        findViewById(R.id.tv_mic).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_beauty).setOnClickListener(this);
        findViewById(R.id.iv_change_camera).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.tv_finish).setOnClickListener(this);
        findViewById(R.id.tv_chat).setOnClickListener(this);
        findViewById(R.id.iv_user_list).setOnClickListener(this);
        findViewById(R.id.cl_over).setOnClickListener(this);
        ivVoice.setOnClickListener(this);
        findViewById(R.id.iv_beauty).setOnClickListener(this);
        findViewById(R.id.iv_doc).setOnClickListener(this);
        findViewById(R.id.iv_doc_clear).setOnClickListener(this);
        findViewById(R.id.iv_doc_edit).setOnClickListener(this);
        tvStart.setOnClickListener(this);
    }

    //处理嘉宾
    private void processGuest() {
        initMainId(webinarInfo.getWebinarInfoData().getWebinar().userinfo);
        broadcastPresent.init(webinarInfo);
        if (broadcastRtcFragment != null && isRtc) {
            if (webinarInfo.getWebinarInfoData() != null && webinarInfo.getWebinarInfoData().roomToolsStatusData != null && webinarInfo.getWebinarInfoData().roomToolsStatusData.speaker_list != null) {
                for (int i = 0; i < webinarInfo.getWebinarInfoData().roomToolsStatusData.speaker_list.size(); i++) {
                    if (webinarInfo.getWebinarInfoData().join_info.third_party_user_id.endsWith(webinarInfo.getWebinarInfoData().roomToolsStatusData.speaker_list.get(i).account_id)) {
                        mInterActive.setUserNoSpeak(null);
                    }
                }
            }
            enterRoom();
        }
        docFragment.setRoomInfo(webinarInfo.getWebinarInfoData());
    }


    private void enterRoom() {
        broadcastRtcFragment.setRoomInfo(getApplicationContext(), new CallBack() {
            @Override
            public void onSuccess(Object result) {
                broadcastRtcFragment.enterRoom();
            }

            @Override
            public void onError(int eventCode, String msg) {
                showToast(msg);
                finish();
            }
        });
    }

    private RtcFragment.UpdateMainStreamLister mUpdateMainStreamLister = new RtcFragment.UpdateMainStreamLister() {
        @Override
        public void updateMainStream(Stream mainStream) {
            if (flDoc != null && flDoc.getVisibility() == View.VISIBLE && docFragment != null) {
                if (mainLocalView.stream != null && mainLocalView.stream.hasVideo()) {
                    mainLocalView.stream.removeAllRenderView();
                }
                broadcastRtcFragment.getMainStream().removeAllRenderView();
                mainLocalView.setVisibility(View.VISIBLE);
                broadcastRtcFragment.getMainStream().addRenderView(mainLocalView);
            }
        }

        @Override
        public void setIsPublic(final boolean ispublic) {
            RtcActivity.this.isPublic = ispublic;
            if (isPublic) {
                mainLocalView.setVisibility(View.GONE);
                if (UserManger.isHost(webinarInfo.getWebinarInfoData().getJoin_info().getRole_name())) {
                    if (flDoc != null && flDoc.getVisibility() == View.VISIBLE && docFragment != null) {
                        return;
                    }
                }
            }
        }

        @Override
        public void onLiveSuccess() {
            groupPlay.setVisibility(View.VISIBLE);
        }
    };

    private void initRtc() {
        broadcastPresent = new RtcH5Present(this);

        if (roleNameData != null) {
            broadcastPresent.updateHostRoleName(roleNameData.host_name);
        }

        broadcastRtcFragment = RtcFragment.getInstance(String.valueOf(webinar_show_type), webinarInfo, broadcastPresent.getMessageCallback(), broadcastPresent.getChatCallback());
        broadcastPresent.setRtcFragmentView(broadcastRtcFragment);
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setMode(AudioManager.MODE_IN_CALL);
        //设置设备状态
        broadcastRtcFragment.setDeviceStatus(mDeviceStatus);
        broadcastRtcFragment.setUpdateMainStreamLister(mUpdateMainStreamLister);
        showFragment(R.id.fragment_broadcast, broadcastRtcFragment);
        if (!isGuest) {
            mainLocalView.setVisibility(View.VISIBLE);
            group_a.setVisibility(View.VISIBLE);
            group_v.setVisibility(View.GONE);
            //主播初始化房间
            broadcastRtcFragment.setRoomInfo(RtcActivity.this, mainLocalView, new CallBack() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onError(int eventCode, String msg) {
                    showToast(msg);
                    finish();
                }
            });
            broadcastPresent.setLocalStream(mainLocalView.getStream());
        } else {
            hideUI();
        }
    }

    private RtcFragment.IDeviceStatus mDeviceStatus = new RtcFragment.IDeviceStatus() {
        @Override
        public boolean getAudioStatus() {
            return isVoice;
        }

        @Override
        public boolean getVideoStatus() {
            return isCamera;
        }
    };


    public void showFragment(int id, Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(id, fragment);
        transaction.commit();
    }


    @Override
    public Activity getActivity() {
        return RtcActivity.this;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PUSH) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            } else {
                baseShowToast("未获取到音视频设备权限，请前往获取权限");
            }
        }
    }

    private GetCodeCount micCount;

    private void startMicCount() {
        ivMic.setVisibility(View.GONE);
        tvMic.setVisibility(View.VISIBLE);
        if (micCount == null) {
            micCount = new GetCodeCount(30 * 1000, 1000);
        }
        micCount.start();
    }


    @Override
    public void setMic(boolean isMic) {
        ivMic.setVisibility(View.VISIBLE);
        tvMic.setText("");
        tvMic.setVisibility(View.GONE);
        if (micCount != null) {
            micCount.cancel();
        }
        this.isMic = isMic;
        if (isMic) {
            ivMic.setBackgroundResource(R.mipmap.icon_mic_down);
            showUI();
        } else {
            ivMic.setBackgroundResource(R.mipmap.icon_mic_up);
            hideUI();
        }
        if (flDoc != null && flDoc.getVisibility() == View.VISIBLE) {
            ivMic.setVisibility(View.GONE);
            hideUI();
        }
    }

    @Override
    public void updateVideoFrame(boolean isCheck) {
        isCamera = isCheck;
        if (isCheck) {
            ivCamera.setBackgroundResource(R.mipmap.icon_camera_open);
            baseShowToast("已打开摄像头");
        } else {
            ivCamera.setBackgroundResource(R.mipmap.icon_camera_close);
            baseShowToast("已关闭摄像头");
        }
    }

    @Override
    public void updateAudioFrame(boolean isCheck) {
        isVoice = isCheck;
        if (isCheck) {
            ivVoice.setBackgroundResource(R.mipmap.icon_voice_open);
            baseShowToast("已打开麦克风");
        } else {
            ivVoice.setBackgroundResource(R.mipmap.icon_voice_close);
            baseShowToast("已关闭麦克风");
        }
    }

    @Override
    public void refreshUserList() {
        if (mangerDialog != null && mangerDialog.isVisible()) {
            mangerDialog.refreshUserList();
        }
    }

    @Override
    public void refreshStream(final String userId, final int voice, final int camera) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (broadcastRtcFragment != null) {
                    broadcastRtcFragment.updatePic(userId, voice, camera);
                }
            }
        });
    }

    @Override
    public void updateMain(String mainId) {
        this.mainId = mainId;
        if (mangerDialog != null) {
            mangerDialog.setMainId(mainId);
        }
        if (broadcastRtcFragment != null) {
            broadcastRtcFragment.setMainId(mainId);
        }
        if (docFragment != null) {
            docFragment.setMainId(mainId, webinarInfo.getWebinarInfoData().getJoin_info().getThird_party_user_id());
        }
        if (flDoc.getVisibility() == View.VISIBLE) {
            if (mainId.equals(webinarInfo.getWebinarInfoData().getJoin_info().getThird_party_user_id())) {
                groupDocEdit.setVisibility(View.VISIBLE);
                groupDocView.setVisibility(View.VISIBLE);
            } else {
                groupDocView.setVisibility(View.GONE);
                groupDocEdit.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void notifyDataChangedChat(MessageChatData data) {
        if (adapter != null) {
            adapter.addData(data);
            if (adapter.getItemCount()>0)
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private int pv = 1, uv = 1;

    @Override
    public void showLookNum(int pv, int uv) {
        this.pv = pv;
        this.uv = uv;
        tvLookNum.setText(String.format("%s人观看", BaseUtil.intChange2Str(uv)));
    }

    @Override
    public void forbidBroadcast() {
        if (forbidBroadcast) {
            finish();
        }
    }

    private RoleNameData roleNameData = new RoleNameData("主持人", "嘉宾", "助理");

    @Override
    public void notifyRoleName(String type, String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        switch (type) {
            //主持人
            case "1":
                if (!TextUtils.equals(roleNameData.host_name, name)) {
                    refreshUserList();
                    roleNameData.host_name = name;
                    if (broadcastPresent != null) {
                        broadcastPresent.updateHostRoleName(roleNameData.host_name);
                    }
                    if (isRtc) {
                        broadcastRtcFragment.updateRoleName(roleNameData);
                    }
                }
                break;
            //助理
            case "3":
                if (!TextUtils.equals(roleNameData.assistant_name, name)) {
                    refreshUserList();
                    roleNameData.assistant_name = name;
                }
                break;
            //嘉宾
            case "4":
                if (!TextUtils.equals(roleNameData.guest_name, name)) {
                    refreshUserList();
                    roleNameData.guest_name = name;
                    if (isRtc) {
                        broadcastRtcFragment.updateRoleName(roleNameData);
                    }
                }
                break;
            default:
                break;
        }
        if (adapter != null)
            adapter.updateRoleName(roleNameData);
    }

    public void userNoSpeaker(String userId) {
        if (TextUtils.equals(userId, mainId)) {
            if (!TextUtils.equals(mainId, String.valueOf(webinarInfo.user_id)) && UserManger.isHost(webinarInfo.role_name)) {
                RtcConfig.getInterActive().setMainSpeaker(webinarInfo.user_id, new RequestCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        showToast(errorMsg);
                    }
                });
            }
        }
    }

    @Override
    public void showToast(String content) {
        if (clStart != null && clStart.getVisibility() == View.VISIBLE && TextUtils.equals(content, "您已被设为主讲人")) {
            return;
        }
        baseShowToast(content);
    }

    class GetCodeCount extends CountDownTimer {

        GetCodeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tvMic.setText(String.format("%ss", String.valueOf(millisUntilFinished / 1000 + 1)));
        }

        @Override
        public void onFinish() {
            ivMic.setVisibility(View.VISIBLE);
            tvMic.setText("");
            tvMic.setVisibility(View.GONE);
            if (flDoc != null && flDoc.getVisibility() == View.VISIBLE) {
                ivMic.setVisibility(View.GONE);
            }

        }
    }

    private void showLoadProgress() {

    }

    public void hideLoadProgress() {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_start) {
            startLive();
        } else if (v.getId() == R.id.tv_chat) {
            broadcastPresent.showInputView();
        } else if (v.getId() == R.id.iv_doc_clear) {
            if (showChatView) {
                groupDocClear.setVisibility(View.GONE);
                if (mainId.equals(webinarInfo.getWebinarInfoData().getJoin_info().getThird_party_user_id())) {
                    groupDocView.setVisibility(View.GONE);
                }
                findViewById(R.id.iv_doc_clear).setBackgroundResource(R.mipmap.icon_doc_clear_open);
                if (isRtc && docFragment != null) {
                    mainLocalView.setVisibility(View.GONE);
                } else {
                    flBroadcast.setVisibility(View.GONE);
                }
            } else {
                if (mainId.equals(webinarInfo.getWebinarInfoData().getJoin_info().getThird_party_user_id())) {
                    groupDocView.setVisibility(View.VISIBLE);
                }
                groupDocClear.setVisibility(View.VISIBLE);
                findViewById(R.id.iv_doc_clear).setBackgroundResource(R.mipmap.icon_doc_clear_close);
                if (isRtc && docFragment != null) {
                    mainLocalView.setVisibility(View.VISIBLE);
                } else {
                    flBroadcast.setVisibility(View.VISIBLE);
                }
            }
            showChatView = !showChatView;
        } else if (v.getId() == R.id.iv_mic) {
            if (RtcInternal.isGrantedPermissionRtc(this, REQUEST_PUSH)) {
                if (!isMic) {
                    if (!broadcastPresent.canSpeak()) {
                        showToast("您已被禁言");
                        return;
                    }
                    rtcApply();
                } else {
                    rtcDownMic();
                }
            }
        } else if (v.getId() == R.id.tv_mic) {
            mInterActive.cancelApply(new RequestCallback() {
                @Override
                public void onSuccess() {
                    showToast("已取消上麦申请");
                    if (micCount != null) {
                        micCount.onFinish();
                    }
                }

                @Override
                public void onError(int eventCode, String msg) {
                    showToast(msg);
                }
            });
        } else if (v.getId() == R.id.tv_finish) {
            finish();
        } else if (v.getId() == R.id.iv_change_camera) {
            if (isRtc) {
                if (broadcastPresent != null) {
                    broadcastPresent.changeCamera();
                }
            }
        } else if (v.getId() == R.id.iv_doc) {
            clickDoc();
        } else if (v.getId() == R.id.iv_doc_edit) {
            if (docFragment != null) {
                docFragment.showPop(ivDoc);
            }
        } else if (v.getId() == R.id.iv_camera) {
            if (broadcastPresent != null) {
                broadcastPresent.onSwitchVideo(!isCamera);
            }
        } else if (v.getId() == R.id.iv_voice) {
            if (broadcastPresent != null) {
                broadcastPresent.onSwitchAudio(!isVoice);
            }
        } else if (v.getId() == R.id.iv_beauty) {
            //新的美颜形式
            if (VHBeautifyKit.getInstance().setBeautifyEnable(true)) {
                changeVisibility();
            }else {
                if (beautyDialog == null) {
                    beautyDialog = new OutDialogBuilder().layout(R.layout.dialog_beauty_no_serve)
                            .build(getActivity());
                }
            }
//            showBeauty = !showBeauty;
//            if (isRtc) {
//                if (broadcastPresent != null) {
//                    broadcastPresent.showBeauty(showBeauty);
//                }
//            }
//            if (showBeauty) {
//                ivBeauty.setBackgroundResource(R.mipmap.icon_beauty_open);
//                baseShowToast("已开启美颜");
//            } else {
//                ivBeauty.setBackgroundResource(R.mipmap.icon_beauty_off);
//                baseShowToast("已关闭美颜");
//            }
        } else if (v.getId() == R.id.iv_back) {
            docBack();
        } else if (v.getId() == R.id.iv_user_list) {
            if (mangerDialog == null) {
                boolean canManger = true;
                if (isGuest) {
                    if (webinarInfo.getWebinarInfoData().getPermission() != null) {
                        canManger = webinarInfo.getWebinarInfoData().getPermission().contains("100013");
                    }
                }
                mangerDialog = UserListNewDialog.getInstance(canManger, isRtc, isGuest, webinarInfo.getWebinarInfoData());
                mangerDialog.setMainId(mainId);
            }
            if (!mangerDialog.isVisible() || !mangerDialog.isAdded()) {
                mangerDialog.show(getSupportFragmentManager(), "");
            }
        } else if (v.getId() == R.id.iv_close) {
            if (outDialog == null) {
                initOutDialog();
            }
            outDialog.show();
        }

    }

    //下麦
    private void rtcDownMic() {
        mInterActive.unpublish(new RequestCallback() {
            @Override
            public void onSuccess() {
                showToast("您已下麦");
                if (micCount != null) {
                    micCount.onFinish();
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                showToast(errorMsg);
            }
        });
    }

    private void rtcApply() {
        mInterActive.apply(new RequestCallback() {
            @Override
            public void onSuccess() {
                showToast("已发送上麦申请");
                startMicCount();
            }

            @Override
            public void onError(int eventCode, String msg) {
                showToast(msg);
            }
        });
    }

    //开始直播
    private void startLive() {
        if (RtcInternal.isGrantedPermissionRtc(this, REQUEST_PUSH)) {
            tvStart.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            tvTimeNum.setVisibility(View.VISIBLE);
            startCountDown();
        }

    }

    /**
     * 退出弹窗
     */
    private void initOutDialog() {

        OutDialogBuilder.DialogTexts texts = new OutDialogBuilder.DialogTexts("确认结束当前直播?", "继续直播", "结束直播");
        if (isGuest) {
            texts = new OutDialogBuilder.DialogTexts("确定退出直播?", "取消", "确认");
        }
        outDialog = new OutDialogBuilder()
                .title(texts.title)
                .tv1(texts.cancel)
                .tv2(texts.conform)
                .onConfirm(new OutDialog.ClickLister() {
                    @Override
                    public void click() {
                        if (!isGuest) {
                            if (tvStart.isShown() || clOver.isShown() || clError.isShown()) {
                                finish();
                            } else {
                                clickOut();
                            }
                        } else {
                            if (broadcastRtcFragment != null) {
                                broadcastRtcFragment.finish();
                            }
                            finish();
                        }
                    }
                })
                .build(RtcActivity.this);
    }


    private void clickOut() {
        if (broadcastRtcFragment != null) {
            broadcastRtcFragment.finish();
            broadcastRtcFragment.setFinish(true);
        }
        if (1 == webinarInfo.getWebinarInfoData().getWebinar_show_type()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        /**
         * 表示自己关闭直播 不处理这个时候的直播结束
         */
        forbidBroadcast = false;
        groupDocClear.setVisibility(View.GONE);
        groupDoc.setVisibility(View.GONE);
        groupUser.setVisibility(View.GONE);
        groupPlay.setVisibility(View.GONE);
        groupDocEdit.setVisibility(View.GONE);
        groupDocView.setVisibility(View.GONE);
        if (isRtc) {
            hideUI();
            ivMic.setVisibility(View.GONE);
            tvMic.setVisibility(View.GONE);
        }
        clStart.setVisibility(View.GONE);
        flDoc.setVisibility(View.GONE);
        flBroadcast.setVisibility(View.VISIBLE);
        clOver.setVisibility(View.VISIBLE);
        // 活动类型  1为音频，2视频 3 互动直播
        if (!"2".equals(webinar_type)) {
            findViewById(R.id.tv_top).setVisibility(View.VISIBLE);
        } else if (1 == webinarInfo.getWebinarInfoData().getWebinar_show_type()) {
            findViewById(R.id.tv_top).setVisibility(View.VISIBLE);
        }

        TextView time = findViewById(R.id.tv_over_time);
        TextView concurrenceNum = findViewById(R.id.tv_over_concurrence_num);
        TextView lookNum = findViewById(R.id.tv_over_look_num);
        TextView chatNum = findViewById(R.id.tv_over_chat_num);
        //本地回显示时间 更解决真实时间 当前时间减去开播时间
        time.setText(CommonUtil.converLongTimeToStr(System.currentTimeMillis() - mLocalBeginTime));
        String nickName = webinarInfo.getWebinarInfoData().join_info.nickname;
        if (TextUtils.isEmpty(nickName)) {
            nickName = webinarInfo.nick_name;
        }
        tvErrorName.setText(BaseUtil.getLimitString(nickName));
        lookNum.setText(BaseUtil.intChange2Str(uv));
        concurrenceNum.setText(BaseUtil.intChange2Str(pv));
        if (adapter != null) {
            if (ListUtils.isEmpty(adapter.getData())) {
                chatNum.setText("0");
            } else {
                chatNum.setText(BaseUtil.intChange2Str(adapter.getData().size()));
            }
        }
    }


    private void clickDoc() {
        if (flDoc != null && flDoc.getVisibility() == View.VISIBLE && mainId.equals(webinarInfo.getWebinarInfoData().getJoin_info().getThird_party_user_id())) {
            if (broadcastRtcFragment != null) {
                broadcastRtcFragment.setStop(false);
            }
            new ChooseDocDialog(getActivity(), new ChooseDocDialog.ChooseIdClickLister() {
                @Override
                public void onChooseIdClick(String docId) {
                    if (docFragment != null && !TextUtils.isEmpty(docId.trim())) {
                        docFragment.setDocId(docId);
                    }
                }
            }).show();
            return;
        }
        groupUser.setVisibility(View.GONE);
        groupPlay.setVisibility(View.GONE);
        if (isRtc) {
            hideUI();
            ivMic.setVisibility(View.GONE);
            tvMic.setText("");
            tvMic.setVisibility(View.GONE);
        }
        groupDoc.setVisibility(View.VISIBLE);
        if (mainId.equals(webinarInfo.getWebinarInfoData().getJoin_info().getThird_party_user_id())) {
            groupDocEdit.setVisibility(View.VISIBLE);
            groupDocView.setVisibility(View.VISIBLE);
        } else {
            groupDocView.setVisibility(View.GONE);
            groupDocEdit.setVisibility(View.GONE);
        }
        docFragment.setMainId(mainId, webinarInfo.getWebinarInfoData().getJoin_info().getThird_party_user_id());
        showDoc();
    }

    private void docBack() {
        groupUser.setVisibility(View.VISIBLE);
        groupPlay.setVisibility(View.VISIBLE);
        if (isRtc) {
            if (isGuest) {
                if (TextUtils.isEmpty(tvMic.getText().toString().trim())) {
                    ivMic.setVisibility(View.VISIBLE);
                } else {
                    tvMic.setVisibility(View.VISIBLE);
                }
            }
        }
        groupDocClear.setVisibility(View.VISIBLE);
        groupDoc.setVisibility(View.GONE);
        groupDocView.setVisibility(View.VISIBLE);
        groupDocEdit.setVisibility(View.GONE);
        hideDoc();
    }

    @Override
    public void onBackPressed() {
        if (flDoc.getVisibility() == View.VISIBLE) {
            docBack();
            return;
        }

        if (clOver.getVisibility() == View.VISIBLE) {
            return;
        }
        if (outDialog == null) {
            initOutDialog();
        }
        outDialog.show();
    }


    public void showDoc() {
        if (flDoc == null) {
            return;
        }
        flDoc.setVisibility(View.VISIBLE);
        if (isRtc) {
            flBroadcast.setVisibility(View.GONE);
            if (docFragment != null && broadcastRtcFragment != null && broadcastRtcFragment.getMainStream() != null) {
                broadcastRtcFragment.getMainStream().removeAllRenderView();
                mainLocalView.setVisibility(View.VISIBLE);
                mainLocalView.setZOrderOnTop(true);
                mainLocalView.setZOrderMediaOverlay(true);
                if (mainLocalView.stream != null) {
                    try {
                        //fix paas videoTracks is null reference
                        mainLocalView.stream.removeAllRenderView();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                broadcastRtcFragment.getMainStream().addRenderView(mainLocalView);
            }
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mainLocalView.getLayoutParams();
            layoutParams.width = DensityUtils.dpToPxInt(120);
            layoutParams.height = DensityUtils.dpToPxInt(120);
            layoutParams.topToTop = PARENT_ID;
            layoutParams.endToEnd = PARENT_ID;
            mainLocalView.setLayoutParams(layoutParams);

            ivMic.setVisibility(View.GONE);
            tvMic.setText("");
            tvMic.setVisibility(View.GONE);
        } else {
            mainLocalView.setVisibility(View.GONE);
            hideUI();
            // 0 活动类型  2为音频，3视频 4 互动直播
            //1-音频、2-视频、3-互动
            if ("2".equals(webinar_type)) {
                // 0 竖屏 1横屏
                if (webinarInfo.getWebinarInfoData().getWebinar_show_type() == 1) {

                    playerViewLayoutParams.width = 600;
                    playerViewLayoutParams.height = BaseUtil.getScreenHeight(getApplicationContext()) * 600 / BaseUtil.getScreenWidth(getApplicationContext());
                } else {
                    playerViewLayoutParams.width = BaseUtil.getScreenWidth(getApplicationContext()) * 600 / BaseUtil.getScreenHeight(getApplicationContext());
                    playerViewLayoutParams.height = 600;
                }

            } else {
                playerViewLayoutParams.width = 0;
                playerViewLayoutParams.height = 0;
            }
            playerViewLayoutParams.topToTop = PARENT_ID;
            playerViewLayoutParams.endToEnd = PARENT_ID;
            playerViewLayoutParams.setMargins(20, 20, 20, 20);
            flBroadcast.setVisibility(View.VISIBLE);
            flBroadcast.setLayoutParams(playerViewLayoutParams);
        }
    }

    public void hideDoc() {
        if (flDoc == null) {
            return;
        }
        flDoc.setVisibility(View.GONE);
        flBroadcast.setVisibility(View.VISIBLE);
        findViewById(R.id.iv_doc_clear).setBackgroundResource(R.mipmap.icon_doc_clear_close);
        if (docFragment != null) {
            docFragment.hintDoc();
        }
        if (isRtc) {
            broadcastRtcFragment.updateViewHandler();
            mainLocalView.setZOrderOnTop(false);
            mainLocalView.setZOrderMediaOverlay(false);
            mainLocalView.setVisibility(View.GONE);
            if (isGuest) {
                if (TextUtils.isEmpty(tvMic.getText().toString().trim())) {
                    ivMic.setVisibility(View.VISIBLE);
                } else {
                    tvMic.setVisibility(View.VISIBLE);
                }
            }
            if (isPublic) {
                showUI();
            } else {
                hideUI();
            }

        } else {
            if ("1".equals(webinar_type)) {
                findViewById(R.id.group_a).setVisibility(View.GONE);
            } else {
                findViewById(R.id.group_a).setVisibility(View.VISIBLE);
            }
        }
        playerViewLayoutParams.setMargins(0, 0, 0, 0);
        playerViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        playerViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        flBroadcast.setLayoutParams(playerViewLayoutParams);
    }

    private void showUI() {
        group_a.setVisibility(View.VISIBLE);
        group_v.setVisibility(View.VISIBLE);
    }

    private void hideUI() {
        findViewById(R.id.group_a).setVisibility(View.GONE);
        findViewById(R.id.group_v).setVisibility(View.GONE);
    }

    private void startCountDown() {
        if (mCount == null) {
            mCount = new CountDownTimer(4 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tvStart.setVisibility(View.GONE);
                    String text = String.valueOf(millisUntilFinished / 1000);
                    if ("0".equals(text)) {
                        text = "GO";
                    }
                    tvTimeNum.setText(text);
                }

                @Override
                public void onFinish() {
                    if (!isFinishing()) {
                        startLiveImpl();
                    }
                }
            };
        }
        mCount.start();
    }

    private void startLiveImpl() {
        tvErrorName.setText(BaseUtil.getLimitString(webinarInfo.nick_name));
        String imageUrl = UserManger.judgePic(webinarInfo.getWebinarInfoData().getWebinar().getUserinfo().getAvatar());
        VhallGlideUtils.loadImage(RtcActivity.this, imageUrl, R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivTopAvatar);
        VhallGlideUtils.loadImage(RtcActivity.this, imageUrl, R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivOverAvatar);

        initMainId(webinarInfo.getWebinarInfoData().getWebinar().getUserinfo());
        updateMain(mainId);
        broadcastPresent.init(webinarInfo);
        docFragment.setRoomInfo(webinarInfo.getWebinarInfoData());
        if (broadcastRtcFragment != null && isRtc) {
            broadcastRtcFragment.enterRoom();
            ivMic.setVisibility(View.GONE);
        }
        mTimerHandler.obtainMessage(1).sendToTarget();
        progressBar.setVisibility(View.GONE);
        tvTimeNum.setVisibility(View.GONE);
        clStart.setVisibility(View.GONE);
//        mainLocalView.setVisibility(View.GONE);
//        groupPlay.setVisibility(View.VISIBLE);
        groupUser.setVisibility(View.VISIBLE);
        groupDocClear.setVisibility(View.VISIBLE);
        groupDocView.setVisibility(View.VISIBLE);
    }

    private void initMainId(WebinarInfoData.WebinarBean.UserinfoBean userinfo) {
        if (webinarInfo.getWebinarInfoData().roomToolsStatusData != null && !TextUtils.isEmpty(webinarInfo.getWebinarInfoData().roomToolsStatusData.doc_permission)) {
            mainId = webinarInfo.getWebinarInfoData().roomToolsStatusData.doc_permission;
        } else {
            mainId = String.valueOf(userinfo.user_id);
        }
    }

    @Override
    protected void onDestroy() {
        //结束互动直播
        VhallSDK.finishBroadcast(webinarInfo.getWebinarInfoData().webinar.id, "", null, null);
        if (broadcastRtcFragment != null) {
            broadcastRtcFragment.finish();
        }
        if (netUtil != null)
            netUtil.release();
        if (mInterActive != null)
            mInterActive.onDestroy();
        if (broadcastPresent != null) {
            broadcastPresent.onDestroyed();
        }
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }


    // 高级美颜相关
    private FaceBeautyControlView mFaceBeautyControlView;
    private FaceBeautyDataFactory mFaceBeautyDataFactory;

    private void changeVisibility() {
        //新的美颜
        if (mFaceBeautyControlView.getVisibility() == View.VISIBLE) {
            mFaceBeautyControlView.setVisibility(View.GONE);
        } else {
            mFaceBeautyControlView.setVisibility(View.VISIBLE);
        }
    }

    private OutDialog beautyDialog;

    private void initBeautifyData(int orientation) {
        mFaceBeautyDataFactory = new FaceBeautyDataFactory(getActivity());
        mFaceBeautyControlView = findViewById(R.id.faceBeautyControlView);
        mFaceBeautyControlView.setMainTabVisibility(false, true, true, false);
        mFaceBeautyControlView.setSelectLineVisible();
        // 0 横屏 1 竖屏
        mFaceBeautyControlView.changeOrientation(orientation);
        mFaceBeautyControlView.bindDataFactory(mFaceBeautyDataFactory);
    }
}
