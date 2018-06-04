package com.vhall.uilibs.watch;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.opengl.GL_Preview_YUV;
import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.Watch;
import com.vhall.business.WatchLive;
import com.vhall.business.data.WebinarInfo;

import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.Survey;
import com.vhall.business.data.source.SurveyDataSource;

import com.vhall.uilibs.Param;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.vhalllive.common.Constants;
import com.vhall.vhalllive.playlive.GLPlayInterface;
import com.vhall.uilibs.R;

//TODO 投屏相关
//import com.vhall.business_support.Watch_Support;
//import com.vhall.business_support.dlna.DeviceDisplay;
//import com.vhall.business_support.WatchLive;
//import org.fourthline.cling.android.AndroidUpnpService;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * 观看直播的Presenter
 */
public class WatchLivePresenter implements WatchContract.LivePresenter, ChatContract.ChatPresenter {
    private static final String TAG = "WatchLivePresenter";
    private Param params;
    private WatchContract.LiveView liveView;

    WatchContract.DocumentView documentView;
    WatchContract.WatchView watchView;
    ChatContract.ChatView chatView;
    ChatContract.ChatView questionView;

    public boolean isWatching = false;
    private WatchLive watchLive;

    int[] scaleTypes = new int[]{Constants.DrawMode.kVHallDrawModeAspectFit.getValue(), Constants.DrawMode.kVHallDrawModeAspectFill.getValue(), Constants.DrawMode.kVHallDrawModeNone.getValue()};
    int currentPos = 0;
    private int scaleType = Constants.DrawMode.kVHallDrawModeAspectFit.getValue();

    private GLPlayInterface mPlayView;

    public WatchLivePresenter(WatchContract.LiveView liveView, WatchContract.DocumentView documentView, ChatContract.ChatView chatView, ChatContract.ChatView questionView, WatchContract.WatchView watchView, Param param) {
        this.params = param;
        this.liveView = liveView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.questionView = questionView;
        this.chatView = chatView;
        this.watchView.setPresenter(this);
        this.liveView.setPresenter(this);
        this.chatView.setPresenter(this);
        this.questionView.setPresenter(this);
    }

    @Override
    public void start() {
        getWatchLive().setVRHeadTracker(true);
        getWatchLive().setScaleType(Constants.DrawMode.kVHallDrawModeAspectFit.getValue());
        initWatch();
    }

    @Override
    public void onWatchBtnClick() {
        if (isWatching) {
            stopWatch();
        } else {
            if (getWatchLive().isAvaliable()) {
                startWatch();
            } else {
                initWatch();
            }
        }
    }

    @Override
    public void showChatView(boolean emoji, InputUser user, int limit) {
        watchView.showChatView(emoji, user, limit);
    }

    @Override
    public void sendChat(String text) {
        if (!VhallSDK.isLogin()) {
            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        getWatchLive().sendChat(text, new RequestCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String reason) {
                Log.e(TAG, " reason == " + reason);
                //chatView.showToast(reason);
            }
        });
    }

    @Override
    public void sendCustom(JSONObject text) {
        if (!VhallSDK.isLogin()) {
            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        getWatchLive().sendCustom(text, new RequestCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String reason) {
                chatView.showToast(reason);
            }
        });
    }

    @Override
    public void sendQuestion(String content) {
        if (!VhallSDK.isLogin()) {
            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        getWatchLive().sendQuestion(content, new RequestCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int errorCode, String reason) {
                questionView.showToast(reason);
            }
        });
    }

    @Override
    public void onLoginReturn() {
        initWatch();
    }

    @Override
    public void onFreshData() {

    }

    @Override
    public void showSurvey(String surveyid) {
        if (!VhallSDK.isLogin()) {
            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        VhallSDK.getSurveyInfo(surveyid, new SurveyDataSource.SurveyInfoCallback() {
            @Override
            public void onSuccess(Survey survey) {
                watchView.showSurvey(survey);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                chatView.showToast(errorMsg);
            }
        });
    }

    boolean force = false;

    @Override
    public void onSwitchPixel(int level) {
        if (getWatchLive().getDefinition() == level && !force) {
            return;
        }
        force = false;
        getWatchLive().setPCSwitchDefinition();
        if (watchView.getActivity().isFinishing()) {
            return;
        }
    }

    @Override
    public void onMobileSwitchRes(int res) {
        if (getWatchLive().getDefinition() == res && !force) {
            return;
        }
        if (isWatching) {
            stopWatch();
        }
        force = false;
        getWatchLive().setDefinition(res);
    }


    @Override
    public int setScaleType() {
        scaleType = scaleTypes[(++currentPos) % scaleTypes.length];
        getWatchLive().setScaleType(scaleType);
        liveView.setScaleButtonText(scaleType);
        return scaleType;
    }

    @Override
    public int changeOriention() {
        return watchView.changeOrientation();
    }

    @Override
    public void onDestory() {
        getWatchLive().destory();
    }

    @Override
    public void submitLotteryInfo(String id, String lottery_id, String nickname, String phone) {
        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(lottery_id)) {
            VhallSDK.submitLotteryInfo(id, lottery_id, nickname, phone, new RequestCallback() {
                @Override
                public void onSuccess() {
                    watchView.showToast("信息提交成功");
                }

                @Override
                public void onError(int errorCode, String reason) {

                }
            });
        }
    }

    @Override
    public int getCurrentPixel() {
        return getWatchLive().getDefinition();
    }

    @Override
    public int getScaleType() {
        if (getWatchLive() != null) {
            return getWatchLive().getScaleType();
        }
        return -1;
    }

    @Override
    public void setHeadTracker() {
        if (!getWatchLive().isVR()) {
            watchView.showToast("当前活动为非VR活动，不可使用陀螺仪");
            return;
        }
        getWatchLive().setVRHeadTracker(!getWatchLive().isVRHeadTracker());
        liveView.reFreshView();
    }

    @Override
    public boolean isHeadTracker() {
        return getWatchLive().isVRHeadTracker();
    }

    @Override
    public void initWatch() {
        //游客ID及昵称 已登录用户可传空
        TelephonyManager telephonyMgr = (TelephonyManager) watchView.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String customeId = telephonyMgr.getDeviceId();
        String customNickname = Build.BRAND + "手机用户";
        VhallSDK.initWatch(params.watchId, customeId, customNickname, params.key, getWatchLive(), WebinarInfo.LIVE, new RequestCallback() {
            @Override
            public void onSuccess() {
                if (watchView.getActivity().isFinishing())
                    return;
                liveView.showRadioButton(getWatchLive().getDefinitionAvailable());
                chatView.clearChatData();
                getChatHistory();
            }

            @Override
            public void onError(int errorCode, String msg) {
                watchView.showToast(msg);
            }

        });
    }

    @Override
    public void startWatch() {
        getWatchLive().start();
    }


    @Override
    public void stopWatch() {
        if (isWatching) {
            getWatchLive().stop();
            isWatching = false;
            liveView.setPlayPicture(isWatching);
        }
    }

    public WatchLive getWatchLive() {
        if (watchLive == null) {
            WatchLive.Builder builder = new WatchLive.Builder()
                    .context(watchView.getActivity().getApplicationContext())
                    .containerLayout(liveView.getWatchLayout())
                    .bufferDelay(params.bufferSecond)
                    .callback(new WatchCallback())
                    .messageCallback(new MessageEventCallback())
                    .connectTimeoutMils(5000)
                    .chatCallback(new ChatCallback());
            watchLive = builder.build();
        }
        //狄拍builder
//        if (watchLive == null) {
//            WatchLive.Builder builder = new WatchLive.Builder()
//                    .context(watchView.getActivity().getApplicationContext())
//                    .bufferDelay(params.bufferSecond)
//                    .callback(new WatchCallback())
//                    .messageCallback(new MessageEventCallback())
//                    .connectTimeoutMils(5000)
//                    .playView(mPlayView = new VRPlayView(watchView.getActivity().getApplicationContext()))//todo 添加到自定义布局中，非new
//                    .chatCallback(new ChatCallback());
//            watchLive = builder.build();
//            liveView.getWatchLayout().addView((VRPlayView) mPlayView, 640, 480);
//            ((VRPlayView) mPlayView).getHolder().setFixedSize(640, 480);
//        }
        return watchLive;
    }

    //签到
    @Override
    public void signIn(String signId) {
        if (!VhallSDK.isLogin()) {
            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        VhallSDK.performSignIn(params.watchId, signId, new RequestCallback() {
            @Override
            public void onSuccess() {
                watchView.showToast("签到成功");
                watchView.dismissSignIn();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                watchView.showToast(errorMsg);
            }
        });
    }

    //提交问卷 需要先登录且watch已初始化完成
    @Override
    public void submitSurvey(Survey survey, String result) {
        if (survey == null)
            return;
        if (!VhallSDK.isLogin()) {
            watchView.showToast("请先登录！");
            return;
        }
        VhallSDK.submitSurveyInfo(getWatchLive(), survey.surveyid, result, new RequestCallback() {
            @Override
            public void onSuccess() {
                watchView.showToast("提交成功！");
                watchView.dismissSurvey();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                watchView.showToast(errorMsg);
                if (errorCode == 10821)
                    watchView.dismissSurvey();
            }
        });
    }


    //TODO 投屏相关
//
//    @Override
//    public void dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service) {
//        getWatchLive().dlnaPost(deviceDisplay, service, new Watch_Support.DLNACallback() {
//
//            @Override
//            public void onError(int errorCode) {
//                watchView.showToast("投屏失败，errorCode:" + errorCode);
//            }
//
//            @Override
//            public void onSuccess() {
//                watchView.showToast("投屏成功!");
//                stopWatch();
//            }
//
//        });
//    }
//
//    @Override
//    public void showDevices() {
//        watchView.showDevices();
//    }
//
//    @Override
//    public void dismissDevices() {
//        watchView.dismissDevices();
//    }

    /**
     * 观看过程中事件监听
     */
    private class WatchCallback implements Watch.WatchEventCallback {
        @Override
        public void onError(int errorCode, String errorMsg) {
            switch (errorCode) {
                case WatchLive.ERROR_CONNECT:
                    Log.e(TAG, "ERROR_CONNECT  ");
                    isWatching = false;
                    liveView.showLoading(false);
                    liveView.setPlayPicture(isWatching);
                    watchView.showToast(errorMsg);
                    break;
                default:
                    watchView.showToast(errorMsg);
            }
        }

        @Override
        public void onStateChanged(int stateCode) {
            switch (stateCode) {
                case WatchLive.STATE_CONNECTED:
                    Log.e(TAG, "STATE_CONNECTED  ");
                    isWatching = true;
                    liveView.setPlayPicture(isWatching);
                    break;
                case WatchLive.STATE_BUFFER_START:
                    Log.e(TAG, "STATE_BUFFER_START  ");
                    if (isWatching)
                        liveView.showLoading(true);
                    break;
                case WatchLive.STATE_BUFFER_STOP:
                    Log.e(TAG, "STATE_BUFFER_STOP  ");
                    liveView.showLoading(false);
                    break;
                case WatchLive.STATE_STOP:
                    Log.e(TAG, "STATE_STOP  ");
                    isWatching = false;
                    liveView.showLoading(false);
                    liveView.setPlayPicture(isWatching);
                    break;
            }
        }

        @Override
        public void onVhallPlayerStatue(boolean playWhenReady, int playbackState) {
            // 播放器状态回调  只在看回放时使用
        }

        @Override
        public void uploadSpeed(String kbps) {
            liveView.setDownSpeed("速率" + kbps + "/kbps");
        }

        @Override
        public void videoInfo(int width, int height) {
            if (mPlayView != null) {
                mPlayView.init(width, height);
                // INIT STUF
            }
        }

    }

    /**
     * 观看过程消息监听
     */
    private class MessageEventCallback implements MessageServer.Callback {
        @Override
        public void onEvent(MessageServer.MsgInfo messageInfo) {
            switch (messageInfo.event) {
                case MessageServer.EVENT_DISABLE_CHAT://禁言
                    break;
                case MessageServer.EVENT_KICKOUT://踢出
                    break;
                case MessageServer.EVENT_OVER://直播结束
                    watchView.showToast("直播已结束");
                    stopWatch();
                    break;
                case MessageServer.EVENT_PERMIT_CHAT://解除禁言
                    break;
                case MessageServer.EVENT_DIFINITION_CHANGED:
                    Log.e(TAG, "EVENT_DIFINITION_CHANGED PC 端切换分辨率");
                    liveView.showRadioButton(getWatchLive().getDefinitionAvailable());
                    onSwitchPixel(WatchLive.DPI_DEFAULT);
//                    if (!getWatchLive().isDifinitionAvailable(getWatchLive().getDefinition())) {
//                        onSwitchPixel(WatchLive.DPI_DEFAULT);
//                    }
                    break;
                case MessageServer.EVENT_START_LOTTERY://抽奖开始
                    watchView.showLottery(messageInfo);
                    break;
                case MessageServer.EVENT_END_LOTTERY://抽奖结束
                    watchView.showLottery(messageInfo);
                    break;
                case MessageServer.EVENT_NOTICE:
                    watchView.showNotice(messageInfo.content);
                    break;
                case MessageServer.EVENT_SIGNIN: //签到消息
                    if (!TextUtils.isEmpty(messageInfo.id) && !TextUtils.isEmpty(messageInfo.sign_show_time)) {
                        watchView.showSignIn(messageInfo.id, parseTime(messageInfo.sign_show_time, 30));
                    }
                    break;
                case MessageServer.EVENT_QUESTION: // 问答开关
                    watchView.showToast("问答功能已" + (messageInfo.status == 0 ? "关闭" : "开启"));
                    break;
                case MessageServer.EVENT_SURVEY://问卷
                    ChatServer.ChatInfo chatInfo = new ChatServer.ChatInfo();
                    chatInfo.event = "survey";
                    chatInfo.id = messageInfo.id;
                    chatView.notifyDataChanged(chatInfo);
                    break;
                case MessageServer.EVENT_CLEARBOARD:
                case MessageServer.EVENT_DELETEBOARD:
                case MessageServer.EVENT_INITBOARD:
                case MessageServer.EVENT_PAINTBOARD:
                case MessageServer.EVENT_SHOWBOARD:
                    documentView.paintBoard(messageInfo);
                    break;
                case MessageServer.EVENT_CHANGEDOC://PPT翻页消息
                case MessageServer.EVENT_CLEARDOC:
                case MessageServer.EVENT_PAINTDOC:
                case MessageServer.EVENT_DELETEDOC:
                    Log.e(TAG, " event " + messageInfo.event);
                    documentView.paintPPT(messageInfo);
                    break;
                case MessageServer.EVENT_RESTART:
                    force = true;
                    //onSwitchPixel(WatchLive.DPI_DEFAULT);
                    break;
            }

        }

        public int parseTime(String str, int defaultTime) {
            int currentTime = 0;
            try {
                currentTime = Integer.parseInt(str);
                if (currentTime == 0) {
                    return defaultTime;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return currentTime;
        }

        @Override
        public void onMsgServerConnected() {

        }

        @Override
        public void onConnectFailed() {
//            getWatchLive().connectMsgServer();
        }

        @Override
        public void onMsgServerClosed() {

        }
    }

    private class ChatCallback implements ChatServer.Callback {
        @Override
        public void onChatServerConnected() {
        }

        @Override
        public void onConnectFailed() {
//            getWatchLive().connectChatServer();
        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    chatView.notifyDataChanged(chatInfo);
                    liveView.addDanmu(chatInfo.msgData.text);
                    break;
                case ChatServer.eventCustomKey:
                    chatView.notifyDataChanged(chatInfo);
                    break;
                case ChatServer.eventOnlineKey:
                    chatView.notifyDataChanged(chatInfo);
                    break;
                case ChatServer.eventOfflineKey:
                    chatView.notifyDataChanged(chatInfo);
                    break;
                case ChatServer.eventQuestion:
                    questionView.notifyDataChanged(chatInfo);
                    break;
            }
        }

        @Override
        public void onChatServerClosed() {
        }
    }

    private void getChatHistory() {
        getWatchLive().acquireChatRecord(true, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                chatView.notifyDataChanged(list);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
                Log.e(TAG, "onFailed->" + errorcode + ":" + messaage);
            }
        });
    }

    //狄拍自定义渲染
    public class VRPlayView extends GL_Preview_YUV implements GLPlayInterface {
        public VRPlayView(Context var1) {
            super(var1);
        }

        public VRPlayView(Context var1, AttributeSet var2) {
            super(var1, var2);
        }

        public void setDrawMode(int model) {
            super.setDrawMode(model);
        }

        public void setIsHeadTracker(boolean head) {
            super.setIsHeadTracker(head);
        }

        public boolean init(int width, int height) {
            super.setPreviewW(width);
            super.setPreviewH(height);
            super.setIsFlip(true);
            super.setColorFormat(19);
            mIsReady.set(true);
            return false;
        }

        public void playView(byte[] YUV) {
            if (this.isReady()) {
                this.setdata(YUV);
            }

        }

        public boolean isReady() {
            return mIsReady.get();
        }

        public void release() {
            this.setRelease();
        }
    }
}

