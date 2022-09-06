package com.vhall.uilibs.watch;

import static com.vhall.business.ErrorCode.ERROR_LOGIN_MORE;

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RelativeLayout;

import com.vhall.beautify.VHBeautifyKit;
import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.Survey;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.SurveyDataSource;
import com.vhall.business.utils.SurveyInternal;
import com.vhall.business_interactive.InterActive;
import com.vhall.business_interactive.internal.VHInteractiveListener;
import com.vhall.business_support.dlna.DMCControl;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.chat.ChatFragment;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;
import com.vhall.vhss.CallBack;
import com.vhall.vhss.TokenManger;
import com.vhall.vhss.data.RoundUserListData;
import com.vhall.vhss.data.TimerInfoData;
import com.vhall.vhss.network.InteractToolsNetworkRequest;

import org.fourthline.cling.android.AndroidUpnpService;
import org.json.JSONObject;
import org.vhwebrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vhall.com.vss2.VssSdk;

//TODO 投屏相关


/**
 * 观看直播 无延时直播 的Presenter
 */
public class WatchNoDelayLivePresenter implements WatchContract.LivePresenter, ChatContract.ChatPresenter {
    private static final String TAG = "NoDelayLivePresenter";
    private Param params;
    private WebinarInfo webinarInfo;
    private WatchContract.LiveView liveView;
    private WatchContract.LiveNoDelayView liveNoDelayView;

    WatchContract.DocumentView documentView;
    WatchContract.WatchView watchView;
    ChatContract.ChatView chatView;
    ChatContract.ChatView questionView;

    public static final int CAMERA_VIDEO = 2; //摄像头
    public static final int CAMERA_AUDIO = 1; //麦克风
    public static final int CAMERA_DEVICE_OPEN = 1;
    public static final int CAMERA_DEVICE_CLOSE = 0;

    //举手中
    private boolean isHand = false;

    //是否上麦
    private boolean isPublic = false;

    CountDownTimer onHandDownTimer;
    private int durationSec = 30; // 举手上麦倒计时
    //是否可以聊天
    private boolean canSpeak = true;
    private boolean chatALLForbid = true;
    private boolean chatOwnForbid = true;
    //是否可以 发问答
    private boolean canSpeakQa = true;
    private InterActive interactive;

    public WatchNoDelayLivePresenter(WatchContract.LiveView liveView, WatchContract.LiveNoDelayView liveNoDelayView, WatchContract.DocumentView documentView, ChatContract.ChatView chatView, ChatContract.ChatView questionView, WatchContract.WatchView watchView, Param param, WebinarInfo webinarInfo) {
        this.params = param;
        this.webinarInfo = webinarInfo;
        this.liveView = liveView;
        this.liveNoDelayView = liveNoDelayView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.questionView = questionView;
        this.chatView = chatView;
        this.watchView.setPresenter(this);
        this.liveView.setPresenter(this);
        this.liveNoDelayView.setPresenter(this);
        this.chatView.setPresenter(this);
        this.questionView.setPresenter(this);
        this.documentView.setPresenter(this);
        initInteractive();
        /**
         * since 6.4.0
         */
        if (webinarInfo != null) {
            chatALLForbid = webinarInfo.chatAllForbid;
            chatOwnForbid = webinarInfo.chatOwnForbid;
            if (!webinarInfo.chatAllForbid) {
                //取消全员禁言
                canSpeak = !webinarInfo.chatOwnForbid;
                canSpeakQa = true;
            } else {
                //全员禁言
                canSpeak = false;
                canSpeakQa = !TextUtils.equals("1", webinarInfo.qa_status);
            }
        }
    }

    public void initInteractive() {
        if (webinarInfo == null) {
            watchView.showToast("webinarInfo null");
            return;
        }
        if (interactive == null) {
            interactive = new InterActive(watchView.getActivity(), new RoomCallback(), new ChatCallback(), new MessageEventCallback());
            interactive.init(webinarInfo, new RequestCallback() {
                @Override
                public void onSuccess() {
                    interactive.enterRoom();
                    /**
                     * since 6.3.1
                     * 互动特殊事件通知
                     */
                    interactive.setListener(new VHInteractiveListener() {
                        @Override
                        public void onEvent(int code, String msg) {
                            switch (code) {
                                case ERROR_LOGIN_MORE:
                                    //被其他人踢出 如果在上麦上走的时候 下麦
                                    watchView.showToast(msg);
                                    if (isPublic) {
                                        interactive.unpublished();
                                    }
                                    watchView.getActivity().finish();
                                default:
                                    break;
                            }
                        }
                    });
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    watchView.showToast("" + errorMsg);
                    watchView.getActivity().finish();
                }
            });
        }

        VhallSDK.getTimerInfo(new RequestDataCallbackV2<TimerInfoData>() {
            @Override
            public void onSuccess(TimerInfoData result) {
                MessageServer.TimerData timerData = new MessageServer.TimerData();
                timerData.is_all_show = result.is_all_show;
                timerData.remain_time = result.remain_time;
                timerData.is_timeout = result.is_timeout;
                timerData.duration = result.duration;
                liveNoDelayView.showTimeView(result.status.equals("4"));
                liveNoDelayView.showTimeView(timerData);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void start() {
        initWatch();
    }

    @Override
    public void onWatchBtnClick() {
        //直接看不支持点击
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
        if (interactive == null) {
            return;
        }
        interactive.sendMsg(text, "", new CallBack() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onError(int eventCode, String msg) {
                watchView.showToast(msg);
            }
        });
    }

    @Override
    public void sendCustom(JSONObject text) {
        if (!VhallSDK.isLogin()) {
            watchView.showToast(R.string.vhall_login_first);
            return;
        }

        if (!VhallSDK.isLogin()) {
            watchView.showToast(R.string.vhall_login_first);
            return;
        }

        if (webinarInfo != null && webinarInfo.chatforbid) {
            watchView.showToast("你被禁言了");
            return;
        }
        interactive.sendCustom(text, new RequestCallback() {
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
        if (!canSpeakQa) {
            watchView.showToast("你被禁言了");
            return;
        }
        interactive.sendQuestion(content, new RequestCallback() {
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

    }

    @Override
    public void onMobileSwitchRes(String dpi) {

    }


    @Override
    public int setScaleType() {

        return 1;
    }

    @Override
    public int changeOriention() {
        return watchView.changeOrientation();
    }

    private void unpublish() {
        if (interactive != null) {
            interactive.unpublish(new RequestCallback() {
                @Override
                public void onSuccess() {
                    isPublic = false;
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    watchView.showToast("下麦失败，errorMsg:" + errorMsg);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        if (interactive != null) {
            if (isPublic) {
                onDownMic(true);
            }
            interactive.leaveRoom();
            interactive.onDestroy();
        }
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
        return "";
    }

    @Override
    public int getScaleType() {

        return -1;
    }

    @Override
    public void setHeadTracker() {

    }

    @Override
    public boolean isHeadTracker() {
        return false;
    }

    @Override
    public void initWatch() {
        if (webinarInfo != null) {
            if (chatView != null)
                chatView.clearChatData();
            getChatHistory();
        }
    }

    @Override
    public void startWatch() {
        // 直接看
    }

    @Override
    public void stopWatch() {
        //不支持
    }

    @Override
    public void showDocFullScreen(int state) {
        if (null != watchView) {
            watchView.showDocFullScreen(state);
        }
    }

    @Override
    public void changeDocOrientation() {
        if (null != watchView) {
            watchView.changeDocOrientation();
        }
    }

    @Override
    public void triggerDocOrientation() {
        if (null != documentView) {
            documentView.triggerDocOrientation();
        }
    }

    @Override
    public void clickDocFullBack() {
        if (null != documentView) {
            documentView.clickDocFullBack();
        }
    }

    private RelativeLayout watchLayout;

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
        VhallSDK.submitSurveyInfo(webinarInfo.webinar_id, survey.surveyid, result, new RequestCallback() {
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
        if (!canSpeak) {
            ToastUtil.showToast("您已被禁言");
            return;
        }
        if (!isPublic) {
            interactive.onRaiseHand(params.watchId, isHand ? 0 : 1, new RequestCallback() {
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
        } else {
            onDownMic(true);
        }
    }

    @Override
    public void replyInvite(int type, RequestCallback callback) {
        interactive.replyInvitation(params.watchId, type, callback);
    }


    //TODO 投屏相关

    @Override
    public DMCControl dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service) {
        DMCControl dmcControl = new DMCControl(deviceDisplay, service, "", webinarInfo);
        return dmcControl;
    }

    @Override
    public void showDevices() {
        watchView.showDevices();
    }

    @Override
    public void dismissDevices() {
        watchView.dismissDevices();
    }

    String question_name = "问答";

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
                    canSpeak = false;
                    chatOwnForbid = true;
                    if (liveNoDelayView != null) {
                        liveNoDelayView.enterInteractive(false);
                    }
                    break;
                case MessageServer.EVENT_KICKOUT://踢出
                    watchView.showToast("您已被踢出");
                    watchView.getActivity().finish();
                    break;
                case MessageServer.EVENT_PERMIT_CHAT://解除禁言
                    watchView.showToast("您已被解除禁言");
                    chatOwnForbid = false;
                    canSpeak = !chatALLForbid;
                    break;
                case MessageServer.EVENT_CHAT_FORBID_ALL://全员禁言
                    //问答状态 根据全体禁言判断 如果开启禁言择不可以发送问答  如果关闭择根据 qa_status判断 1开启禁言 0关闭
                    if (messageInfo.status == 0) {
                        //取消全员禁言
                        watchView.showToast("解除全员禁言");
                        canSpeak = !chatOwnForbid;
                        chatALLForbid = false;
                        canSpeakQa = true;
                    } else {
                        //全员禁言
                        watchView.showToast("全员禁言");
                        chatALLForbid = true;
                        canSpeakQa = !TextUtils.equals("1", messageInfo.qa_status);
                        canSpeak = false;
                    }
                    break;
                case MessageServer.EVENT_OVER://直播结束
                    watchView.showToast("直播已结束");
                    liveView.liveFinished();
                    stopWatch();
                    break;
                case MessageServer.EVENT_START_LOTTERY://抽奖开始
                case MessageServer.EVENT_END_LOTTERY://抽奖结束
                    watchView.showLottery(messageInfo);
                    break;
                case MessageServer.EVENT_NOTICE:
                    watchView.showNotice(messageInfo.content);
                    break;
                case MessageServer.EVENT_SIGNIN: //签到消息
                    if (!TextUtils.isEmpty(messageInfo.id) && !TextUtils.isEmpty(messageInfo.sign_show_time)) {
                        watchView.showSignIn(messageInfo.id, messageInfo.signTitle, parseTime(messageInfo.sign_show_time, 30));
                    }
                    break;
                case MessageServer.EVENT_QUESTION_ANSWER_SET: // 修改问答昵称设置

                    if (!TextUtils.isEmpty(messageInfo.question_name)) {
                        question_name = messageInfo.question_name;
                    }
                    watchView.showQAndA(question_name);
                    break;
                case MessageServer.EVENT_QUESTION: // 问答开关
                    if (!TextUtils.isEmpty(messageInfo.question_name)) {
                        question_name = messageInfo.question_name;
                    }
                    watchView.showToast(question_name + "功能已" + (messageInfo.status == 0 ? "关闭" : "开启"));
                    if (messageInfo.status == 1) {
                        watchView.showQAndA(question_name);
                    } else {
                        watchView.dismissQAndA();
                    }
                    break;
                case MessageServer.EVENT_SURVEY://问卷
                    /**
                     * 获取msg内容
                     */
                    ChatServer.ChatInfo surveyData = new ChatServer.ChatInfo();
                    surveyData.event = MessageChatData.eventSurveyKey;
                    Map<String, String> params = new HashMap<>();
                    params.put(SurveyInternal.KEY_SURVEY_ID, messageInfo.id);
                    params.put(SurveyInternal.KEY_ROOMID, webinarInfo.vss_room_id);
                    params.put(SurveyInternal.KEY_WEBINAR_ID, webinarInfo.webinar_id);
                    params.put(SurveyInternal.KEY_APPID, VssSdk.getInstance().getAppId());
                    if (webinarInfo.getWebinarInfoData() != null) {
                        params.put(SurveyInternal.KEY_PAAS_ACCESS_TOKEN, webinarInfo.getWebinarInfoData().interact.paas_access_token);
                    }
                    params.put(SurveyInternal.KEY_USER_ID, webinarInfo.user_id);
                    params.put(SurveyInternal.KEY_TOKEN, TokenManger.getToken());
                    params.put(SurveyInternal.KEY_INTERACT_TOKEN, TokenManger.getInteractToken());
                    surveyData.url = (SurveyInternal.createSurveyUrl(params));
                    surveyData.id = (messageInfo.id);
                    surveyData.name = messageInfo.survey_name;
                    chatView.notifyDataChanged(ChatFragment.CHAT_EVENT_CHAT, surveyData);
                    break;
                case MessageServer.EVENT_SHOWDOC://文档开关指令 1 使用文档 0 关闭文档
                    Log.e(TAG, "onEvent:show_docType:watchType= " + messageInfo.watchType);
                    operationDocument(messageInfo.watchType);
                    break;
                case MessageServer.EVENT_SHOWH5DOC:
                    if (documentView != null) {
                        if (messageInfo.watchType == 1) {
                            documentView.showType(3);
                        } else {
                            documentView.showType(2);
                        }
                    }
                    break;
                case MessageServer.EVENT_PAINTH5DOC:
                    if (documentView != null) {
                        documentView.paintH5DocView(messageInfo.h5DocView);
                    }
                    break;
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
                    liveNoDelayView.enterInteractive(true);
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

                case MessageServer.EVENT_INTERACTIVE_DOWN_MIC:
                    if (interactive != null) {
                        liveNoDelayView.enterInteractive(false);
                    }
                    break;

                case MessageServer.EVENT_VRTC_SPEAKER_SWITCH:
                    //互动设置为主讲人
                    liveNoDelayView.updateMain(messageInfo.roomJoinId);
                    watchView.showToast(BaseUtil.getLimitString(messageInfo.roomJoinId) + "已被设为主讲人");
                    break;

                case MessageServer.EVENT_SWITCH_DEVICE:
                    /**
                     * 新增 收到消息 切换自己设备
                     */
                    if (messageInfo.device == CAMERA_AUDIO) { // 麦克风
                        switchAudioFrame(messageInfo.status);
                        liveNoDelayView.updateAudioFrame(messageInfo.status);
                    } else { //2摄像头
                        switchVideoFrame(messageInfo.status);
                        liveNoDelayView.updateVideoFrame(messageInfo.status);
                    }
                    break;

                case MessageServer.EVENT_VIDEO_ROUND_START:
                    watchView.showToast("主办方开启了视频轮巡功能，在主持人端将会看到您的视频画面，请保持视频设备一切正常");
                    break;
                case MessageServer.EVENT_VIDEO_ROUND_END:
                    //是轮巡调用下麦
                    releaseRound();
                    break;
                case MessageServer.EVENT_VIDEO_ROUND_USERS:
                    dealRound(messageInfo.uids, false);
                    break;

                case MessageServer.EVENT_TIMER_START:
                    liveNoDelayView.showTimeView(messageInfo.timerData);
                    break;
                case MessageServer.EVENT_TIMER_END:
                case MessageServer.EVENT_TIMER_RESET:
                    liveNoDelayView.showTimeView(null);
                    break;

                case MessageServer.EVENT_TIMER_RESUME:
                    liveNoDelayView.showTimeView(false);
                    watchView.showToast("计时器继续");
                    break;
                case MessageServer.EVENT_TIMER_PAUSE:
                    liveNoDelayView.showTimeView(true);
                    watchView.showToast("计时器暂停");
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
//            interactive.connectMsgServer();
        }

        @Override
        public void onMsgServerClosed() {

        }
    }


    @Override
    public void onResume() {
        if (webinarInfo != null && !TextUtils.isEmpty(webinarInfo.vss_room_id))
            getRoundUsers(false);
    }

    @Override
    public void onPause() {
        releaseRound();
    }

    @Override
    public boolean getIsRound() {
        return isRound;
    }

    //断网或者 推到后台回来 需要判断自己是否需要继续参加轮巡
    // isPublic 是否强制推流  如果断网重连 则需要在 onDidConnect 消息里面强制推流
    private void getRoundUsers(boolean isPublic) {
        if (webinarInfo != null && !TextUtils.isEmpty(webinarInfo.vss_room_id))
            VhallSDK.getRoundUsers(webinarInfo.vss_room_id, "0", new RequestDataCallback() {
                @Override
                public void onSuccess(Object o) {
                    RoundUserListData roundUserListData = (RoundUserListData) o;
                    List<String> uids = new ArrayList<>();
                    if (roundUserListData != null && !ListUtils.isEmpty(roundUserListData.list))
                        for (int i = 0; i < roundUserListData.list.size(); i++) {
                            uids.add(roundUserListData.list.get(i).account_id);
                        }
                    dealRound(uids, isPublic);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    ToastUtil.showToast(errorMsg);
                }
            });
    }


    // 是否参与轮巡
    private boolean isRound = false;

    /**
     * @param uids     包含自己判断是否需要轮巡 如果没有 已经参与轮巡的需要下麦
     * @param isPublic 是否强制推流  如果断网重连 则需要在 onDidConnect 消息里面强制推流
     */
    public void dealRound(List uids, boolean isPublic) {
        if (!ListUtils.isEmpty(uids) && uids.contains(webinarInfo.user_id)) {
            //已经参加的不需要进入
            if (!isRound || isPublic) {
                //断网重连之后
                setLocalView(Stream.VhallStreamType.VhallStreamTypeVideoPatrol);
                interactive.publish();
                isRound = true;
            }
        } else {
            releaseRound();
        }
    }

    private void releaseRound() {
        //没有自己停止轮巡
        isRound = false;
        if (interactive != null && interactive.getLocalStream() != null && interactive.getLocalStream().getStreamType() == 5) {
            interactive.unpublished();
        }
    }

    public void switchVideoFrame(int status) {
        if (status == CAMERA_DEVICE_OPEN) { //1打开
            interactive.getLocalStream().unmuteVideo(null);
        } else { // 0禁止
            interactive.getLocalStream().muteVideo(null);
        }
    }

    public void switchAudioFrame(int status) {
        if (status == CAMERA_DEVICE_OPEN) {
            interactive.getLocalStream().unmuteAudio(null);
        } else {
            interactive.getLocalStream().muteAudio(null);
        }
    }


    //无延迟 互动直播 使用
    @Override
    public void onSwitchCamera() {
        if (interactive != null && interactive.getLocalStream() != null)
            interactive.switchCamera();
    }

    //无延迟 互动直播 使用
    @Override
    public void onSwitchVideo(boolean isOpen) {
        interactive.switchDevice(CAMERA_VIDEO, isOpen ? CAMERA_DEVICE_OPEN : CAMERA_DEVICE_CLOSE, new RequestCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                watchView.showToast(errorMsg);
            }
        });
    }

    @Override
    public void onSwitchAudio(boolean isOpen) {
        interactive.switchDevice(CAMERA_AUDIO, isOpen ? CAMERA_DEVICE_OPEN : CAMERA_DEVICE_CLOSE, new RequestCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                watchView.showToast(errorMsg);
            }
        });
    }

    @Override
    public void onDownMic(boolean own) {
        //own true 代表自己主动下麦
        if (interactive != null && isPublic) {
            if (own) {
                unpublish();
            } else {
                interactive.unpublished();
                isPublic = false;
            }
        }
    }

    @Override
    public void onUpMic() {
        if (isRound) {
            watchView.showToast("已经参加了轮巡");
            return;
        }
        if (interactive != null) {
            setLocalView(Stream.VhallStreamType.VhallStreamTypeAudioAndVideo);
            interactive.publish();
            isPublic = true;
        }
    }

    @Override
    public boolean getIsPlaying() {
        return false;
    }

    /**
     * 设置本地流
     * 参数类型 VHStreamTypeParams
     */
    private void setLocalView(Stream.VhallStreamType streamType) {
        VHRenderView vhRenderView;
        vhRenderView = new VHRenderView(watchView.getActivity());
        vhRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        vhRenderView.init(null, null);
        interactive.setLocalView(vhRenderView, streamType, null);
    }

    @Override
    public void beautyOpen() {
        if (VHBeautifyKit.getInstance().isBeautifyEnable()) {
            ToastUtil.showToast("美颜关闭");
        } else {
            ToastUtil.showToast("美颜开启");
        }
        VHBeautifyKit.getInstance().setBeautifyEnable(!VHBeautifyKit.getInstance().isBeautifyEnable());
    }

    /**
     * 根据文档状态选择展示
     */
    private void operationDocument(int type) {
        if (type == 2) {
            documentView.showType(type);//关闭文档
            watchView.showToast("关闭文档");
        } else {
            watchView.showToast("打开文档");
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
                    chatView.notifyDataChanged(ChatFragment.CHAT_EVENT_CHAT, chatInfo);
                    liveView.addDanmu(chatInfo.msgData.text);
                    break;
                case ChatServer.eventCustomKey:
                    chatView.notifyDataChanged(ChatFragment.CHAT_EVENT_CHAT, chatInfo);
                    break;
                case ChatServer.eventOnlineKey:
                    chatView.notifyDataChanged(ChatFragment.CHAT_EVENT_CHAT, chatInfo);
                    if (chatInfo.onlineData != null) {
                        watchView.setOnlineNum(chatInfo.onlineData.concurrent_user, 0);
                    }
                    break;
                case ChatServer.eventOfflineKey:
                    chatView.notifyDataChanged(ChatFragment.CHAT_EVENT_CHAT, chatInfo);
                    break;
                case ChatServer.eventQuestion:
                    if (chatInfo.questionData != null && chatInfo.questionData.answer != null) {
                        if (chatInfo.questionData.answer.is_open == 0 && !chatInfo.questionData.join_id.equals(webinarInfo.join_id)) {
                            return;
                        }
                    }
                    questionView.notifyDataChanged(ChatFragment.CHAT_EVENT_QUESTION, chatInfo);
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
        if (interactive == null) {
            return;
        }
        interactive.acquireChatRecord(true, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                chatView.notifyDataChanged(ChatFragment.CHAT_EVENT_CHAT, list);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
                Log.e(TAG, "onFailed->" + errorcode + ":" + messaage);
            }
        });
    }

    class RoomCallback implements InterActive.RoomCallback {

        @Override
        public void onDidConnect() {//进入房间
            Log.e(TAG, "onDidConnect");
            if (interactive != null) {
                getRoundUsers(true);
            }
        }

        @Override
        public void onDidError() {//进入房间失败
            Log.e(TAG, "onDidError");
        }

        @Override
        public void onDidPublishStream() {// 上麦
            watchView.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    liveNoDelayView.updateStream(false, interactive.getLocalStream(), null);
                }
            });
            Log.e(TAG, "onDidPublishStream");
        }

        @Override
        public void onDidUnPublishStream() {//下麦
            watchView.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    liveNoDelayView.updateStream(true, interactive.getLocalStream(), null);
                }
            });
            Log.e(TAG, "onDidUnPublishStream");
        }

        @Override
        public void onDidSubscribeStream(Stream stream, final VHRenderView newRenderView) {
            Log.e(TAG, "onDidSubscribeStream");
            liveNoDelayView.updateStream(false, stream, newRenderView);
        }

        @Override
        public void onDidRoomStatus(Room room, Room.VHRoomStatus vhRoomStatus) {
            Log.e(TAG, "onDidRoomStatus  " + vhRoomStatus);
            switch (vhRoomStatus) {
                case VHRoomStatusDisconnected:
                    watchView.getActivity().finish();
                    break;
                case VHRoomStatusError:
                    watchView.showToast("互动房间链接失败");
                    watchView.getActivity().finish();
                    Log.e(TAG, "VHRoomStatusError");
                    break;
                case VHRoomStatusReady:
                    Log.e(TAG, "VHRoomStatusReady");
                    break;
                case VHRoomStatusConnected: // 重连进房间
                    Log.e(TAG, "VHRoomStatusConnected");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onDidRemoveStream(Room room, final Stream stream) {
            Log.e(TAG, "onDidRemoveStream");
            liveNoDelayView.updateStream(true, stream, null);
        }
    }
}

