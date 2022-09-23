package com.vhall.uilibs.watch;

import static com.vhall.business.ErrorCode.ERROR_LOGIN_MORE;
import static com.vhall.business.Watch.EVENT_INIT_PLAYER_SUCCESS;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vhall.beautify.VHBeautifyKit;
import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchLive;
import com.vhall.business.common.Constants;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.Survey;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.SurveyDataSource;
import com.vhall.business.utils.SurveyInternal;
import com.vhall.business_interactive.InterActive;
import com.vhall.business_support.dlna.DMCControl;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.net.NetBroadcastReceiver;
import com.vhall.net.NetUtil;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.play.impl.VHVideoPlayerView;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.chat.ChatFragment;
import com.vhall.uilibs.chat.PushChatFragment;
import com.vhall.uilibs.interactive.RtcInternal;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.uilibs.widget.LotteryListDialog;
import com.vhall.uilibs.widget.NoticeListDialog;
import com.vhall.uilibs.widget.SurveyListDialog;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;
import com.vhall.vhss.TokenManger;
import com.vhall.vhss.data.LotteryCheckData;
import com.vhall.vhss.data.RoundUserListData;
import com.vhall.vhss.data.SurveyInfoData;

import org.fourthline.cling.android.AndroidUpnpService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vhall.com.vss2.VssSdk;

//TODO 投屏相关


/**
 * 观看直播的Presenter
 */
public class WatchLivePresenter implements WatchContract.LivePresenter, ChatContract.ChatPresenter {
    private static final String TAG = "vhallWatchLivePresenter";
    private Param params;
    private WebinarInfo webinarInfo;
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

    private VHVideoPlayerView mPlayView;
    private boolean isHand = false;
    private int isHandStatus = 1;

    CountDownTimer onHandDownTimer;
    private int durationSec = 30; // 举手上麦倒计时
    //是否可以聊天
    private boolean canSpeak = true;
    //全体禁言
    private boolean chatALLForbid = true;
    //私人禁言
    private boolean chatOwnForbid = true;
    //是否可以 发问答
    private boolean canSpeakQa = true;


    public WatchLivePresenter(WatchContract.LiveView liveView, WatchContract.DocumentView documentView, ChatContract.ChatView chatView, ChatContract.ChatView questionView, WatchContract.WatchView watchView, Param param, WebinarInfo webinarInfo) {
        this.params = param;
        this.webinarInfo = webinarInfo;
        this.liveView = liveView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.questionView = questionView;
        this.chatView = chatView;
        this.watchView.setPresenter(this);
        this.liveView.setPresenter(this);
        this.chatView.setPresenter(this);
        this.questionView.setPresenter(this);
        this.documentView.setPresenter(this);

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

            if (webinarInfo.notice != null && !TextUtils.isEmpty(webinarInfo.notice.content)) {
                hasNotice = true;
            }
        }

    }


    @Override
    public void start() {
        getWatchLive().setVRHeadTracker(true);
        initWatch();
        getWatchLive().setScaleType(Constants.DrawMode.kVHallDrawModeAspectFit.getValue());

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
        if (!canSpeak) {
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
        //禁言与自定义消息无关 根据自己的需求 判断
        if (!canSpeak) {
            watchView.showToast("你被禁言了");
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
        if (!canSpeakQa) {
            watchView.showToast("你被禁言了");
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
        liveView.setScaleButtonText(scaleType);
        return scaleType;
    }

    @Override
    public int changeOriention() {
        return watchView.changeOrientation();
    }

    @Override
    public void onDestroy() {
        releaseRound();
        getWatchLive().destroy();
        if (netUtil != null)
            netUtil.release();
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
            // operationDocument();
            liveView.showRadioButton(getWatchLive().getDefinitionAvailable());
            chatView.clearChatData();
//            getChatHistory();
            getAnswerList();
            getSurveyList();
            updateLotteryList();
            monitorNetWork();
        }
    }

    /**
     * 6.3.5
     */
    private void getSurveyList() {
        VhallSDK.getHistorySurveyList(webinarInfo.switch_id, webinarInfo.vss_room_id, webinarInfo.webinar_id, new RequestDataCallback() {
            @Override
            public void onSuccess(Object o) {
                liveView.updateSurveyList((ArrayList<SurveyInfoData>) o);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                ToastUtil.showToast(errorMsg);
            }
        });
    }

    private NoticeListDialog noticeListDialog;

    private boolean hasNotice = false;

    @Override
    public void showNoticeDialog() {
        if (hasNotice) {
            noticeListDialog = new NoticeListDialog(chatView.getContext(), webinarInfo.vss_room_id);
            noticeListDialog.show();
        } else {
            ToastUtil.showToast("当前主持人没有发布公告");
        }
    }

    private LotteryListDialog lotteryListDialog;

    //show 不需不需要展示弹窗 false 只更新数据
    @Override
    public void showLotteryListDialog(List<LotteryCheckData> dataList, boolean show) {
        if (chatView == null || webinarInfo == null) {
            ToastUtil.showToast("error data");
            return;
        }
        if (ListUtils.isEmpty(dataList)) {
            ToastUtil.showToast("当前没有中奖");
            return;
        }
        if (dataList.size() == 1) {
            // public int take_award;//是否已领奖 0-否 1-是
            //  public int need_take_award;//是否需要领奖 0-否 1-是
            LotteryCheckData lotteryCheckData = dataList.get(0);
            if (lotteryCheckData.need_take_award == 0) {
                if (show)
                    ToastUtil.showToast("当前奖品不需要领奖");
                return;
            }
            if (lotteryCheckData.take_award == 1) {
                if (show)
                    ToastUtil.showToast("当前奖品已经领过奖");
                return;
            }
        }
        if (lotteryListDialog == null) {
            lotteryListDialog = new LotteryListDialog(chatView.getContext(), dataList, webinarInfo.webinar_id);
            lotteryListDialog.setOnItemClickLister(new LotteryListDialog.OnItemClickLister() {
                @Override
                public void update() {
                    updateLotteryList();
                }
            });
        } else {
            lotteryListDialog.setDataList(dataList);
        }
        if (show) {
            lotteryListDialog.show();
            //更新数据
            updateLotteryList();
        }
    }

    /**
     * 6.4.1
     */
    private void updateLotteryList() {
        VhallSDK.getHistoryLotteryList("2", new RequestDataCallback() {
            @Override
            public void onSuccess(Object result) {
                liveView.updateLotteryList((ArrayList<LotteryCheckData>) result);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void showSurveyListDialog(List<SurveyInfoData> dataList, boolean show) {
        if (chatView == null || webinarInfo == null || ListUtils.isEmpty(dataList)) {
            ToastUtil.showToast("error data");
            return;
        }
        if (surveyListDialog == null) {
            surveyListDialog = new SurveyListDialog(chatView.getContext(), dataList);
            surveyListDialog.setOnItemClickLister(new SurveyListDialog.OnItemClickLister() {
                @Override
                public void jump(SurveyInfoData info) {
                    watchView.showSurvey(SurveyInternal.createSurveyUrl(webinarInfo, info), "");
                }
            });
        } else {
            surveyListDialog.setDataList(dataList);
        }
        if (show)
            surveyListDialog.show();
    }

    private void getAnswerList() {
        VhallSDK.getAnswerList(params.watchId, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                questionView.notifyDataChanged(PushChatFragment.CHAT_EVENT_QUESTION, list);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
//                Toast.makeText(watchView.getActivity(), messaage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void startWatch() {
        if (webinarInfo == null) {
            return;
        }
        getWatchLive().start();
        if (webinarInfo.getWebinarInfoData() != null && webinarInfo.getWebinarInfoData().scrollInfoData != null) {
            liveView.setScrollInfo(webinarInfo.getWebinarInfoData().scrollInfoData);
        }
    }

    @Override
    public void stopWatch() {
        if (isWatching) {
            getWatchLive().stop();
            isWatching = false;
            liveView.setPlayPicture(isWatching);
        }
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

    public WatchLive getWatchLive() {
        if (watchLive == null) {
            watchLayout = liveView.getWatchLayout();
            WatchLive.Builder builder = new WatchLive.Builder()
                    .context(watchView.getActivity().getApplicationContext())
                    .containerLayout(watchLayout)
                    .bufferDelay(params.bufferSecond)
                    .callback(new WatchCallback())
                    .messageCallback(new MessageEventCallback())
                    .connectTimeoutMils(10000)
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

    /**
     * todo 水印和视频画面之间插入图层
     *
     * @param viewGroup
     */
    private void insertPic(final ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        }
        viewGroup.post(new Runnable() {
            @Override
            public void run() {
                View view = new View(viewGroup.getContext());
                view.setBackgroundColor(Color.RED);
                view.setTag("pic");
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                Object o = viewGroup.getChildAt(0);
                if (o instanceof ViewGroup) {
                    int count = ((ViewGroup) o).getChildCount();
                    if (count == 1) {
                        ((ViewGroup) o).addView(view);
                    } else if (count == 2) {
                        ((ViewGroup) o).addView(view, 1);
                    }
                }
            }
        });
    }

    private void removePic(final ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        }
        Object vGroup = viewGroup.getChildAt(0);
        if (vGroup instanceof ViewGroup) {
            int count = ((ViewGroup) vGroup).getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                View view = ((ViewGroup) vGroup).getChildAt(i);
                Object o = view.getTag();
                if (o == null) {
                    continue;
                }
                if (TextUtils.equals("pic", o.toString())) {
                    ((ViewGroup) vGroup).removeView(view);
                }
            }
        }

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
        /*if (!VhallSDK.isLogin()) {
            watchView.showToast("请先登录！");
            return;
        }
        JSONObject obj = null;
        try {
            obj = new JSONObject(result);
            String qId = obj.optString("question_id");
            VhallSDK.submitSurveyInfo(getWatchLive(), qId, result, new RequestCallback() {
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
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
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
    public void beautyOpen() {
        if (VHBeautifyKit.getInstance().isBeautifyEnable()) {
            ToastUtil.showToast("美颜关闭");
        } else {
            ToastUtil.showToast("美颜开启");
        }
        VHBeautifyKit.getInstance().setBeautifyEnable(!VHBeautifyKit.getInstance().isBeautifyEnable());
    }

    @Override
    public void onRaiseHand() {
        if (!canSpeak) {
            ToastUtil.showToast("您已被禁言");
            return;
        }
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
    public void replyInvite(int type, RequestCallback callback) {
        getWatchLive().replyInvitation(params.watchId, type, callback);
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

    SurveyListDialog surveyListDialog;

    @Override
    public void showSurvey(SurveyInfoData info) {
        if (info == null || webinarInfo == null) {
            ToastUtil.showToast("error data");
            return;
        }
        watchView.showSurvey(SurveyInternal.createSurveyUrl(webinarInfo, info), "");
    }

    @Override
    public void dismissDevices() {
        watchView.dismissDevices();
    }

    /**
     * 观看过程中事件监听
     */
    private boolean setBg = false;

    private class WatchCallback implements VHPlayerListener {
        @Override
        public void onStateChanged(com.vhall.player.Constants.State state) {
            switch (state) {
                case START:
                    isWatching = true;
                    liveView.showLoading(false);
                    liveView.setPlayPicture(isWatching);
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
                        liveView.showRadioButton(getWatchLive().getDefinitionAvailable());
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
                    break;
                case EVENT_INIT_PLAYER_SUCCESS://
//                    if (!setBg) {
//                        //只设置一次 设置背景 没有需求的可以不加
//                        @SuppressLint("ResourceType") InputStream is = watchView.getActivity().getResources().openRawResource(R.drawable.splash_bg);
//                        Bitmap mBitmap = BitmapFactory.decodeStream(is);
//                        if (!watchLive.setVideoBackgroundImage(mBitmap)) {
//                            ToastUtil.showToast("设置失败");
//                        } else {
//                            ToastUtil.showToast("设置成功");
//                        }
//                        setBg = true;
//                    }
                    startWatch();
                    break;
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

    String question_name = "问答";//默认显示 可以自己按照需求设置

    /**
     * 观看过程消息监听
     */
    private class MessageEventCallback implements MessageServer.Callback {
        @Override
        public void onEvent(MessageServer.MsgInfo messageInfo) {
            Log.e("vhall_", "messageInfo " + messageInfo.event +" msg_id  "+ messageInfo.msg_id + ((null != messageInfo.responseImMessageInfo) ? messageInfo.responseImMessageInfo.getData() : "nulllll"));
            switch (messageInfo.event) {
                case MessageServer.EVENT_KICKOUT://踢出
                    watchView.showToast("您已被踢出");
                    watchView.getActivity().finish();
                    break;

                case MessageServer.EVENT_DISABLE_CHAT://禁言
                    watchView.showToast("您已被禁言");
                    canSpeak = false;
                    chatOwnForbid = true;
                    break;
                case MessageServer.EVENT_PERMIT_CHAT://解除禁言
                    watchView.showToast("您已被解除禁言");
                    chatOwnForbid = false;
                    canSpeak = !chatALLForbid;
                    break;
                case MessageServer.EVENT_CHAT_FORBID_ALL://全员禁言
                    //问答状态 根据全体禁言判断 如果开启禁言则根据 qa_status判断 1开启 0关闭  如果关闭全体禁言 则直接开启问答
                    //聊天 如果开启 则不可以聊 如果关闭则根据当前的 个人禁言情况
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
                        canSpeak = false;
                        canSpeakQa = !TextUtils.equals("1", messageInfo.qa_status);
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
                case MessageServer.EVENT_START_LOTTERY://抽奖开始
                    //watchView.showToast(messageInfo.lotteryInfo.title);
                    watchView.showLottery(messageInfo);
                    break;
                case MessageServer.EVENT_END_LOTTERY://抽奖结束
                    // watchView.showToast(messageInfo.lotteryInfo.title);
                    watchView.showLottery(messageInfo);
                    updateLotteryList();
                    break;
                case MessageServer.EVENT_NOTICE:
                    hasNotice = true;
                    watchView.showNotice(messageInfo.content);
                    if (noticeListDialog != null && noticeListDialog.isShowing()) {
                        noticeListDialog.onRefreshData();
                    } else {
                        liveView.showNoticeRed();
                    }
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
                    getSurveyList();
                    break;

                //有人提交问卷  messageInfo.user_id 提交人的id
                case MessageServer.EVENT_SURVEY_PUSH:
                    if (TextUtils.equals(messageInfo.user_id, webinarInfo.user_id))
                        getSurveyList();
                    break;
                case MessageServer.EVENT_SHOWDOC://文档开关指令 1 使用文档 0 关闭文档
                    Log.e(TAG, "onEvent:show_docType:watchType= " + messageInfo.watchType);
                    getWatchLive().setIsUseDoc(messageInfo.watchType);
                    operationDocument();
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
                case MessageServer.EVENT_CLEARBOARD:
                case MessageServer.EVENT_DELETEBOARD:
                case MessageServer.EVENT_INITBOARD:
                case MessageServer.EVENT_PAINTBOARD:
                    if (getWatchLive().isUseDoc()) {
                        documentView.paintBoard(messageInfo);
                    }
                    break;
                case MessageServer.EVENT_SHOWBOARD:
                    getWatchLive().setIsUseBoard(messageInfo.showType);
                    if (getWatchLive().isUseDoc()) {
                        documentView.paintBoard(messageInfo);
                    }
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
                    watchView.showToast("已开启直播");
                    break;
                case MessageServer.EVENT_INTERACTIVE_HAND:
                    Log.e(TAG, " status " + messageInfo.status);
                    /** 互动举手消息 status = 1  允许上麦  */
                    break;
                case MessageServer.EVENT_INTERACTIVE_ALLOW_MIC:
                    //主持人 同意上麦
                    if (isRound) {
                        watchView.showToast("已经参加了轮巡");
                        return;
                    }
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
                    if (isRound) {
                        watchView.showToast("已经参加了轮巡");
                        return;
                    }
                    watchView.showInvited();
                    break;

                case MessageServer.EVENT_EDIT_WEBINAR_ROLE_NAME:
                    //角色修改消息 根据自己的逻辑更新对应的UI
                    watchView.showToast("更新了角色   " + messageInfo.edit_role_type + " 的名字为---" + messageInfo.edit_role_name);
                    break;
                case MessageServer.EVENT_VRTC_BIG_SCREEN_SET:
                    watchView.showToast("流消息 互动流设置混流大画面 " + messageInfo.user_id);
                    break;

                case MessageServer.EVENT_VIDEO_ROUND_START:
                    //轮巡开启
                    watchView.showToast("主办方开启了视频轮巡功能，在主持人端将会看到您的视频画面，请保持视频设备一切正常");
                    break;
                case MessageServer.EVENT_VIDEO_ROUND_END:
                    //轮巡结束
                    releaseRound();
                    break;
                case MessageServer.EVENT_VIDEO_ROUND_USERS:
                    //轮巡用户 再次之前必需要有麦克风、摄像头权限 uids次轮参与用户id
                    dealRound(messageInfo.uids, false);
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

    private NetUtil netUtil;

    @Override
    public boolean getIsRound() {
        return isRound;
    }

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
                    if (errorCode == 513343) {
                        //视频轮巡未开启 根据自己需求是否提示
                        return;
                    }
                    ToastUtil.showToast(errorMsg);
                }
            });
    }

    private void monitorNetWork() {
        netUtil = new NetUtil(watchView.getActivity(), new NetBroadcastReceiver.NetChangeListener() {
            @Override
            public void onChangeListener(int status) {
                if (RtcInternal.isNetworkConnected(watchView.getActivity())) {
                    //互动没有初始化需要在这里获取，已经初始化了走互动自己的重连
                    if (interactive == null)
                        getRoundUsers(false);
                } else {
                    ToastUtil.showToast("当前网络异常");
                }
            }
        });
    }

    private InterActive interactive;
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
                if (interactive == null) {
                    interactive = new InterActive(watchView.getActivity(), new RoomCallback(), null);
                    interactive.init(true, webinarInfo, new RequestCallback() {
                        @Override
                        public void onSuccess() {
                            setLocalView();
                            interactive.enterRoom();
                            isRound = true;
                        }

                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            watchView.showToast(errorMsg);
                        }
                    });
                } else {
                    //此时互动没有被释放可以接着使用、适用于断网重联
                    setLocalView();
                    interactive.publish();
                    isRound = true;
                }
            }
            //轮训开启美颜
//            VHBeautifyKit.getInstance().setBeautifyEnable(true);
        } else {
            releaseRound();
        }
    }

    private void releaseRound() {
        //没有自己停止轮巡，必需离开互动房间
        isRound = false;
        if (interactive != null) {
            interactive.unpublished();
            interactive.leaveRoom();
            interactive.onDestroy();
            interactive = null;
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

    /**
     * 设置本地流
     */
    private void setLocalView() {
        if (interactive != null) {
            interactive.setLocalView(null, Stream.VhallStreamType.VhallStreamTypeVideoPatrol, null);
        }
    }

    class RoomCallback implements InterActive.RoomCallback {
        @Override
        public void onDidConnect() {
            Log.e(TAG, "onDidConnect");
            //进入房间
            //断网重联也会触发 需要记录上麦状态
            if (interactive != null) {
                getRoundUsers(true);
            }
        }

        @Override
        public void onDidError() {//进入房间失败

        }

        @Override
        public void onDidPublishStream() {// 上麦
            Log.e(TAG, "onDidPublishStream");
        }

        @Override
        public void onDidUnPublishStream() {//下麦
            Log.e(TAG, "onDidUnPublishStream");
        }

        @Override
        public void onDidSubscribeStream(Stream stream, final VHRenderView newRenderView) {

        }

        @Override
        public void onDidRoomStatus(Room room, Room.VHRoomStatus vhRoomStatus) {
            Log.e(TAG, "onDidRoomStatus  " + vhRoomStatus);
            switch (vhRoomStatus) {
                case VHRoomStatusDisconnected:// 异常退出
                    break;
                case VHRoomStatusError:
                    Log.e(TAG, "VHRoomStatusError");
                    if (interactive != null) {
                        interactive.leaveRoom();
                        interactive.onDestroy();
                        interactive = null;
                        isRound = false;
                    }
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

    //当前消息的标示，如果进来就拉历史新消息，取最后一个
    //如果进来不拉取历史消息，则取时时消息最早的一个，如果不穿默认根据页数拉取,page 从1开始
    private String msgId = "";

    @Override
    public void acquireChatRecord(int page, final ChatServer.ChatRecordCallback callback) {
        if (webinarInfo == null) {
            ToastUtil.showToast("error data");
            return;
        }
        /**
         * 获取当前房间聊天列表
         *
         * @param page        获取条目节点，默认为1
         * @param limit       获取条目数量，最大100
         * @param msg_id      获取条目数量，聊天记录 锚点消息id,此参数存在时anchor_path 参数必须存在
         * @param anchor_path 锚点方向，up 向上查找，down 向下查找,此参数存在时 msg_id 参数必须存在
         * @param is_role     0：不筛选主办方 1：筛选主办方 默认是0
         */
        getWatchLive().acquireChatRecord(page, 10, msgId, "down", "0", new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                if (!ListUtils.isEmpty(list)) {
                    msgId = list.get(list.size() - 1).msg_id;
                }
                Collections.reverse(list);
                if (callback != null) {
                    callback.onDataLoaded(list);
                }
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
                if (callback != null) {
                    callback.onFailed(errorcode, messaage);
                }
            }
        });
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
                        if (TextUtils.equals(chatInfo.msgData.target_id, webinarInfo.user_id)) {
                            //自己的私聊展示
                            chatInfo.msgData.text = "私聊消息---" + chatInfo.msgData.text;
                        } else {
                            return;
                        }
                    } else {
                        //为空代表没有拉去历史聊天记录 锚点id设置为 进入房间的第一天数据 私聊消息除外
                        if (TextUtils.isEmpty(msgId)) {
                            msgId = chatInfo.msg_id;
                        }
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
        getWatchLive().acquireChatRecord(true, new ChatServer.ChatRecordCallback() {
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

    /*
    //核心模块中已经实现VR渲染器，可直接使用
    //狄拍自定义渲染
    public class VRPlayView extends GL_Preview_YUV implements IVHVideoPlayer {
        AtomicBoolean mIsReady = new AtomicBoolean(false);
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

        @Override
        public void play(byte[] bytes, int i, int i1) {

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
    }*/
}

