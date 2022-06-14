package com.vhall.uilibs.watch;

import static com.vhall.business.ErrorCode.ERROR_LOGIN_MORE;

import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RelativeLayout;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchLive;
import com.vhall.business.common.Constants;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.Survey;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.SurveyDataSource;
import com.vhall.business_support.dlna.DMCControl;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.play.impl.VHVideoPlayerView;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.chat.VChatFragment;
import com.vhall.uilibs.util.emoji.InputUser;

import org.fourthline.cling.android.AndroidUpnpService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//TODO 投屏相关


/**
 * 观看直播的Presenter
 */
class VWatchLivePresenter implements WatchContract.LivePresenter, ChatContract.ChatPresenter {
    private static final String TAG = "WatchLivePresenter";
    private Param params;
    private WebinarInfo webinarInfo;
    private WatchContract.LiveView liveView;

    WatchContract.DocumentView documentView;
    WatchContract.WatchView watchView;
    ChatContract.ChatView chatView;
    public boolean isWatching = false;
    private WatchLive watchLive;

    int[] scaleTypes = new int[]{Constants.DrawMode.kVHallDrawModeAspectFit.getValue(), Constants.DrawMode.kVHallDrawModeAspectFill.getValue(), Constants.DrawMode.kVHallDrawModeNone.getValue()};
    int currentPos = 0;
    private int scaleType = Constants.DrawMode.kVHallDrawModeAspectFit.getValue();

    private VHVideoPlayerView mPlayView;
    private boolean isHand = false;
    private int isHandStatus = 1;

    CountDownTimer onHandDownTimer;
    private int durationSec = 30; // 举手上麦倒计时


    public VWatchLivePresenter(WatchContract.LiveView liveView, WatchContract.DocumentView documentView, ChatContract.ChatView chatView, WatchContract.WatchView watchView, Param param, WebinarInfo webinarInfo) {
        this.params = param;
        this.webinarInfo = webinarInfo;
        this.liveView = liveView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.chatView = chatView;
        this.watchView.setPresenter(this);
        this.liveView.setPresenter(this);
        this.chatView.setPresenter(this);
    }


    @Override
    public void start() {
        getWatchLive().setVRHeadTracker(true);
        initWatch();
        getWatchLive().setScaleType(Constants.DrawMode.kVHallDrawModeNone.getValue());
//        Log.e("vhall_hkl",Thread.currentThread().getName());
//        mPlayView.setDrawMode(Constants.DrawMode.kVHallDrawModeNone.getValue());
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
        if (webinarInfo != null && webinarInfo.chatforbid) {
            watchView.showToast("你被禁言了");
            return;
        }
        getWatchLive().sendChat(text, new RequestCallback() {
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
    public void sendCustom(JSONObject text) {
        if (!VhallSDK.isLogin()) {
            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        //禁言与自定义消息无关
/*        if (!canSpeak) {
            watchView.showToast("你被禁言了");
            return;
        }*/
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
            }
        });
    }

    @Override
    public void onLoginReturn() {
        initWatch();
    }


    @Override
    public void showSurvey(String url, String title) {
        if (!VhallSDK.isLogin()) {
            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        watchView.showSurvey(url, title);
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
    public void onSwitchPixel(String dpi) {
        if (getWatchLive().getDefinition().equals(dpi) && !force) {
            return;
        }
        force = false;
        getWatchLive().setPCSwitchDefinition();
        if (watchView.getActivity().isFinishing()) {
            return;
        }
    }

    @Override
    public void onMobileSwitchRes(String dpi) {
        if (getWatchLive().getDefinition() == dpi && !force) {
            return;
        }
        force = false;
        getWatchLive().setDefinition(dpi);
    }


    @Override
    public int setScaleType() {
        scaleType = scaleTypes[(++currentPos) % scaleTypes.length];
        getWatchLive().setScaleType(scaleType);
//        mPlayView.setDrawMode(scaleType);
        liveView.setScaleButtonText(scaleType);
        return scaleType;
    }

    @Override
    public int changeOriention() {
        return watchView.changeOrientation();
    }

    @Override
    public void onDestroy() {
        getWatchLive().destroy();
    }


    @Override
    public boolean setVideoBackgroundColor(int color) {
        return getWatchLive().setVideoBackgroundColor(color);
    }


    @Override
    public boolean setVideoBackgroundImage(Bitmap bitmap) {
        return getWatchLive().setVideoBackgroundImage(bitmap);
    }

    @Override
    public void submitLotteryInfo(String id, String lottery_id, String nickname, String phone) {
        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(lottery_id)) {
            VhallSDK.submitLotteryInfo(id, lottery_id, nickname, phone, "", new RequestCallback() {
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
    public String getCurrentPixel() {
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


    //无延迟 互动直播 使用
    @Override
    public void onSwitchCamera() {

    }

    //无延迟 互动直播 使用
    @Override
    public void onSwitchVideo(boolean isOpen) {

    }

    //无延迟 互动直播 使用
    @Override
    public void onSwitchAudio(boolean isOpen) {

    }

    //无延迟 互动直播 使用
    @Override
    public void onDownMic(boolean own) {

    }

    //无延迟 互动直播 使用
    @Override
    public void onUpMic() {

    }

    @Override
    public boolean getIsPlaying() {
        return getWatchLive().isPlaying();
    }

    @Override
    public void initWatch() {
        if (webinarInfo != null) {
            getWatchLive().setWebinarInfo(webinarInfo);
            operationDocument();
            liveView.showRadioButton(getWatchLive().getDefinitionAvailable());
            chatView.clearChatData();
            getChatHistory();
            getAnswerList();
            startWatch();
        }
    }

    private void getAnswerList() {
        VhallSDK.getAnswerList(params.watchId, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
//                Toast.makeText(watchView.getActivity(), messaage, Toast.LENGTH_SHORT).show();
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
            RelativeLayout watchLayout = liveView.getWatchLayout();
            WatchLive.Builder builder = new WatchLive.Builder()
                    .context(watchView.getActivity().getApplicationContext())
                    .bufferDelay(params.bufferSecond)
                    .containerLayout(watchLayout)
                    .callback(new WatchCallback())
//                    .playView(mPlayView = new VHVideoPlayerView(watchView.getActivity().getApplicationContext()))
                    .messageCallback(new MessageEventCallback())
                    .connectTimeoutMils(10000)
                    .chatCallback(new ChatCallback());
            watchLive = builder.build();
//            watchLayout.addView(mPlayView);
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
    public void submitSurvey(String result) {
    }

    @Override
    public void submitSurvey(Survey survey, String result) {
        if (survey == null) {
            return;
        }
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
                if (errorCode == 10821) {
                    watchView.dismissSurvey();
                }
            }
        });
    }

    @Override
    public void onRaiseHand() {
        getWatchLive().onRaiseHand(params.watchId, isHand ? 0 : 1, new RequestCallback() {
            @Override
            public void onSuccess() {
                if (isHand) {
                    isHand = false;
                    watchView.refreshHand(0);
                    if (onHandDownTimer != null) {
                        onHandDownTimer.cancel();
                    }
                } else {
                    Log.e(TAG, "举手成功");
                    startDownTimer(durationSec);
                    isHand = true;
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                watchView.showToast("举手失败，errorMsg:" + errorMsg);
            }
        });
    }

    @Override
    public void replyInvite(int type) {
        getWatchLive().replyInvitation(params.watchId, type, new RequestCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                watchView.showToast("上麦状态反馈异常，errorMsg:" + errorMsg);
            }
        });
    }


    //TODO 投屏相关

    @Override
    public DMCControl dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service) {
        DMCControl dmcControl = new DMCControl(deviceDisplay, service, getWatchLive().getOriginalUrl(), webinarInfo);
        return dmcControl;
    }

    @Override
    public void showDevices() {
        watchView.showDevices();
        getWatchLive().stop();
    }

    @Override
    public void dismissDevices() {
        watchView.dismissDevices();
    }

    /**
     * 观看过程中事件监听
     */
    private class WatchCallback implements VHPlayerListener {
        @Override
        public void onStateChanged(com.vhall.player.Constants.State state) {
            switch (state) {
                case START:
                    isWatching = true;
                    liveView.showLoading(false);
                    liveView.setPlayPicture(isWatching);
//                    Log.e("vhall_hkl",Thread.currentThread().getName());
                    //屏幕自适应
                    getWatchLive().setScaleType(Constants.DrawMode.kVHallDrawModeAspectFit.getValue());
//                    mPlayView.setDrawMode(Constants.DrawMode.kVHallDrawModeAspectFit.getValue());
                    break;
                case BUFFER:
                    if (isWatching) {
                        liveView.showLoading(true);
                    }
                    break;
                case STOP:
                    isWatching = false;
                    liveView.showLoading(false);
                    liveView.setPlayPicture(isWatching);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case com.vhall.player.Constants.Event.EVENT_DOWNLOAD_SPEED:
                    liveView.setDownSpeed("速率" + msg + "/kbps");
                    break;
                case com.vhall.player.Constants.Event.EVENT_DPI_CHANGED:
                    //分辨率切换
                    Log.i(TAG, msg);
                    break;
                case com.vhall.player.Constants.Event.EVENT_DPI_LIST:
                    //支持的分辨率 msg
                    try {
                        JSONArray array = new JSONArray(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case com.vhall.player.Constants.Event.EVENT_VIDEO_SIZE_CHANGED:
                    Log.i(TAG, msg);
                    break;
                case com.vhall.player.Constants.Event.EVENT_STREAM_START://发起端开始推流

                    break;
                case com.vhall.player.Constants.Event.EVENT_STREAM_STOP://发起端停止推流
                    break;

                case ERROR_LOGIN_MORE://被其他人踢出
                    watchView.showToast(msg);
                    watchView.getActivity().finish();
                default:
                    break;

            }
        }

        @Override
        public void onError(int errorCode, int innerCode, String msg) {
            switch (errorCode) {
                case com.vhall.player.Constants.ErrorCode.ERROR_CONNECT:
                    Log.e(TAG, "ERROR_CONNECT  ");
                    isWatching = false;
                    liveView.showLoading(false);
                    liveView.setPlayPicture(isWatching);
                    watchView.showToast(msg);

                    break;
                default:
                    watchView.showToast(msg);
            }
        }
    }

    /**
     * 观看过程消息监听
     */
    private class MessageEventCallback implements MessageServer.Callback {
        @Override
        public void onEvent(MessageServer.MsgInfo messageInfo) {
            Log.e(TAG, "messageInfo " + messageInfo.event);
            switch (messageInfo.event) {
                case MessageServer.EVENT_DISABLE_CHAT://禁言
                    watchView.showToast("您已被禁言");
                    break;
                case MessageServer.EVENT_KICKOUT://踢出
                    watchView.showToast("您已被踢出");
                    watchView.getActivity().finish();
                    break;
                case MessageServer.EVENT_PERMIT_CHAT://解除禁言
                    watchView.showToast("您已被解除禁言");
                    break;
                case MessageServer.EVENT_CHAT_FORBID_ALL://全员禁言
                    if (messageInfo.status == 0) {
                        //取消全员禁言
                        watchView.showToast("解除全员禁言");
                    } else {
                        //全员禁言
                        watchView.showToast("全员禁言");
                    }
                    break;
                case MessageServer.EVENT_OVER://直播结束
                    watchView.showToast("直播已结束");
                    liveView.liveFinished();
                    stopWatch();
                    break;
                case MessageServer.EVENT_DIFINITION_CHANGED:
                    Log.e(TAG, "EVENT_DIFINITION_CHANGED PC 端切换分辨率");
                    liveView.showRadioButton(getWatchLive().getDefinitionAvailable());
                    onSwitchPixel(com.vhall.player.Constants.Rate.DPI_SAME);
//                    if (!getWatchLive().isDifinitionAvailable(getWatchLive().getDefinition())) {
//                        onSwitchPixel(WatchLive.DPI_DEFAULT);
//                    }
                    break;
//                case MessageServer.EVENT_START_LOTTERY://抽奖开始
//                    watchView.showLottery(messageInfo);
//                    break;
//                case MessageServer.EVENT_END_LOTTERY://抽奖结束
//                    watchView.showLottery(messageInfo);
//                    break;
//                case MessageServer.EVENT_NOTICE:
//                    watchView.showNotice(messageInfo.content);
//                    break;
//                case MessageServer.EVENT_SIGNIN: //签到消息
//                    if (!TextUtils.isEmpty(messageInfo.id) && !TextUtils.isEmpty(messageInfo.sign_show_time)) {
//                        watchView.showSignIn(messageInfo.id, messageInfo.signTitle, parseTime(messageInfo.sign_show_time, 30));
//                    }
//                    break;
//                case MessageServer.EVENT_QUESTION: // 问答开关
//                    watchView.showToast("问答功能已" + (messageInfo.status == 0 ? "关闭" : "开启"));
//                    if (messageInfo.status == 1)
//                        watchView.showQAndA();
//                    else
//                        watchView.dismissQAndA();
//                    break;
//                case MessageServer.EVENT_SURVEY://问卷
//
//                    /**
//                     * 获取msg内容
//                     */
//
//                    MessageChatData surveyData = new MessageChatData();
//                    surveyData.event = MessageChatData.eventSurveyKey;
//                    Map<String, String> params = new HashMap<>();
//                    params.put(SurveyInternal.KEY_SURVEY_ID, messageInfo.id);
//                    params.put(SurveyInternal.KEY_ROOMID, webinarInfo.vss_room_id);
//                    params.put(SurveyInternal.KEY_WEBINAR_ID, webinarInfo.webinar_id);
//                    params.put(SurveyInternal.KEY_APPID, VssSdk.getInstance().getAppId());
//                    if (webinarInfo.getWebinarInfoData() != null) {
//                        params.put(SurveyInternal.KEY_PAAS_ACCESS_TOKEN, webinarInfo.getWebinarInfoData().interact.paas_access_token);
//                    }
//                    params.put(SurveyInternal.KEY_USER_ID, webinarInfo.user_id);
//                    params.put(SurveyInternal.KEY_TOKEN, TokenManger.getToken());
//                    params.put(SurveyInternal.KEY_INTERACT_TOKEN, TokenManger.getInteractToken());
//                    surveyData.setUrl(SurveyInternal.createSurveyUrl(params));
//
//                    surveyData.setId(messageInfo.id);
//                    chatView.notifyDataChangedChat(surveyData);
//                    break;
//                case MessageServer.EVENT_SHOWDOC://文档开关指令 1 使用文档 0 关闭文档
//                    Log.e(TAG, "onEvent:show_docType:watchType= " + messageInfo.watchType);
//                    getWatchLive().setIsUseDoc(messageInfo.watchType);
//                    operationDocument();
//                    break;
//                case MessageServer.EVENT_CLEARBOARD:
//                case MessageServer.EVENT_DELETEBOARD:
//                case MessageServer.EVENT_INITBOARD:
//                case MessageServer.EVENT_PAINTBOARD:
//                    if (getWatchLive().isUseDoc()) {
//                        documentView.paintBoard(messageInfo);
//                    }
//                    break;
//                case MessageServer.EVENT_SHOWBOARD:
//                    getWatchLive().setIsUseBoard(messageInfo.showType);
//                    if (getWatchLive().isUseDoc()) {
//                        documentView.paintBoard(messageInfo);
//                    }
//                    break;
//                case MessageServer.EVENT_CHANGEDOC://PPT翻页消息
//                case MessageServer.EVENT_CLEARDOC:
//                case MessageServer.EVENT_PAINTDOC:
//                case MessageServer.EVENT_DELETEDOC:
//                    Log.e(TAG, " event " + messageInfo.event);
//                    documentView.paintPPT(messageInfo);
//                    break;
                case MessageServer.EVENT_RESTART:
                    force = true;
                    //onSwitchPixel(WatchLive.DPI_DEFAULT);
                    watchView.showToast("已开启直播");
                    break;
                case MessageServer.EVENT_INTERACTIVE_HAND:
                    Log.e(TAG, " status " + messageInfo.status);
                    /** 互动举手消息 status = 1  允许上麦  */
                    break;
                case MessageServer.EVENT_INTERACTIVE_ALLOW_MIC:
//                    getWatchLive().disconnectMsgServer(); // 关闭watchLive中的消息
//                    replyInvite(1);
                    watchView.enterInteractive();
                    if (onHandDownTimer != null) {
                        isHand = false; //重置是否举手标识
                        onHandDownTimer.cancel();
                        watchView.refreshHand(0);
                    }
                    break;
                case MessageServer.EVENT_INTERACTIVE_ALLOW_HAND:
                    watchView.showToast(messageInfo.status == 0 ? "举手按钮关闭" : "举手按钮开启");

                    break;
                case MessageServer.EVENT_INVITED_MIC://被邀请上麦
                    watchView.showInvited();
                    break;
                default:
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
            Log.e(TAG, "MessageServer CONNECT FAILED");
//            getWatchLive().connectMsgServer();
        }

        @Override
        public void onMsgServerClosed() {

        }
    }

    /**
     * 根据文档状态选择展示
     */
    private void operationDocument() {
        if (!getWatchLive().isUseDoc()) {
            documentView.showType(2);//关闭文档
            watchView.showToast("关闭文档");
        } else {
            watchView.showToast("打开文档");
            //展示文档
            if (getWatchLive().isUseBoard()) {
                //当前为白板
                documentView.showType(1);
            } else {
                documentView.showType(0);
            }
        }
    }

    public void startDownTimer(int secondTimer) {
        onHandDownTimer = new CountDownTimer(secondTimer * 1000 + 1080, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                watchView.refreshHand((int) millisUntilFinished / 1000 - 1);
            }

            @Override
            public void onFinish() {
                onHandDownTimer.cancel();
                onRaiseHand();
            }
        }.start();
    }

    private class ChatCallback implements ChatServer.Callback {

        @Override
        public void onChatServerConnected() {
            Log.e(TAG, "CHAT CONNECTED ");
        }

        @Override
        public void onConnectFailed() {
            Log.e(TAG, "CHAT CONNECT FAILED");
        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    if (chatInfo.msgData != null && !TextUtils.isEmpty(chatInfo.msgData.target_id)) {
                        //根据target_id 不为空标记当前是不是问答私聊 是的话直接过滤
                        return;
                    }
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    liveView.addDanmu(chatInfo.msgData.text);
                    break;
                case ChatServer.eventCustomKey:
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    if (chatInfo.onlineData != null) {
                        watchView.setOnlineNum(chatInfo.onlineData.concurrent_user, 0);
                    }
                    break;
                case ChatServer.eventOnlineKey:
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    if (chatInfo.onlineData != null) {
                        watchView.setOnlineNum(chatInfo.onlineData.concurrent_user, 0);
                    }
                    break;
                case ChatServer.eventOfflineKey:
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    break;
                case ChatServer.eventQuestion:
                    break;
                default:
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
                List<MessageChatData> list1 = new ArrayList<>();
                for (ChatServer.ChatInfo chatInfo : list) {
                    list1.add(MessageChatData.getChatData(chatInfo));
                }
                chatView.notifyDataChangedChat(VChatFragment.CHAT_EVENT_CHAT, list1);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
                Log.e(TAG, "onFailed->" + errorcode + ":" + messaage);
            }
        });
    }

}

