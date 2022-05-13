package com.vhall.uilibs.watch;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchPlayback;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.Survey;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business_support.dlna.DMCControl;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.chat.PushChatFragment;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.util.emoji.InputUser;

import org.fourthline.cling.android.AndroidUpnpService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.vhall.business.ErrorCode.ERROR_LOGIN_MORE;
import static com.vhall.business.MessageServer.EVENT_SHOWBOARD;
import static com.vhall.business.MessageServer.EVENT_SHOWDOC;
import static com.vhall.business.WatchPlayback.SHOW_DOC_KEY;
import static com.vhall.ops.VHOPS.ERROR_CONNECT;
import static com.vhall.ops.VHOPS.ERROR_DOC_INFO;
import static com.vhall.ops.VHOPS.ERROR_SEND;
import static com.vhall.ops.VHOPS.KEY_OPERATE;
import static com.vhall.ops.VHOPS.TYPE_ACTIVE;
import static com.vhall.ops.VHOPS.TYPE_SWITCHOFF;
import static com.vhall.ops.VHOPS.TYPE_SWITCHON;

//TODO  投屏相关

/**
 * 观看回放的Presenter
 */
public class WatchPlaybackPresenter implements WatchContract.PlaybackPresenter, ChatContract.ChatPresenter {
    private static final String TAG = "PlaybackPresenter";
    private Param param;
    private WebinarInfo webinarInfo;
    WatchContract.PlaybackView playbackView;
    WatchContract.DocumentView documentView;
    WatchContract.WatchView watchView;
    ChatContract.ChatView chatView;
    private WatchPlayback watchPlayback;

    //FIT_XY = 0;FIT = 1;FILL= 2;
    int[] scaleTypeList = new int[]{0, 1, 2};
    int currentPos = 0;
    private int scaleType = 0;//FIT_XY


    String[] speedStrs = new String[]{"0.25", "0.50", "1.00", "1.25", "1.50", "2.00"};
    int currentSpeed = 2;

    private int limit = 20;
    private int pos = 1;

    private long playerCurrentPosition = 0L; // 当前的进度
    private long playerDuration;
    private String playerDurationTimeStr = "00:00:00";

    private boolean loadingVideo = false;
    private boolean loadingComment = false;

    private Timer timer;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (getWatchPlayback().isPlaying()) {
                        playerCurrentPosition = getWatchPlayback().getCurrentPosition();
                        playbackView.setSeekbarCurrentPosition((int) playerCurrentPosition);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public WatchPlaybackPresenter(WatchContract.PlaybackView playbackView, WatchContract.DocumentView documentView, ChatContract.ChatView chatView, WatchContract.WatchView watchView, Param param, WebinarInfo webinarInfo) {
        this.playbackView = playbackView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.chatView = chatView;
        this.param = param;
        this.webinarInfo = webinarInfo;
        this.playbackView.setPresenter(this);
        this.chatView.setPresenter(this);
        this.watchView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    private void initCommentData(int pos) {
        if (loadingComment) {
            return;
        }
        loadingComment = true;
        watchPlayback.requestCommentHistory(param.watchId, limit, pos, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                chatView.clearChatData();
                loadingComment = false;
                List<MessageChatData> list1 = new ArrayList<>();
                for (ChatServer.ChatInfo chatInfo : list) {
                    list1.add(MessageChatData.getChatData(chatInfo));
                }
                chatView.notifyDataChangedChat(PushChatFragment.CHAT_EVENT_CHAT, list1);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
                loadingComment = false;
                watchView.showToast(messaage);
            }
        });
    }

    private void initWatch() {
        if (webinarInfo != null) {
            getWatchPlayback().setWebinarInfo(webinarInfo);
            handlePosition();
            pos = 1;
            initCommentData(pos);
            watchView.showNotice(getWatchPlayback().getNotice()); //显示公告
            playbackView.setQuality(getWatchPlayback().getQualities());
            operationDocument();
        }
    }

    @Override
    public void onFragmentDestory() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        getWatchPlayback().destroy();
    }

    @Override
    public void startPlay() {
        if (!getWatchPlayback().isAvaliable()) {
            return;
        }
        playbackView.setPlayIcon(false);
        getWatchPlayback().start();

    }

    @Override
    public void onPlayClick() {
        if (getWatchPlayback().isPlaying()) {
            onStop();
        } else {
            if (!getWatchPlayback().isAvaliable()) {
                initWatch();
            } else if (webinarInfo.status == WebinarInfo.VIDEO || webinarInfo.status == WebinarInfo.MEDIA) {
                if (getWatchPlayback().getPlayerState() == Constants.State.END) {
                    getWatchPlayback().seekTo(0);
                }
                startPlay();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        playbackView.setProgressLabel(VhallUtil.converLongTimeToStr(progress), playerDurationTimeStr);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        playerCurrentPosition = seekBar.getProgress();
        if (!getWatchPlayback().isPlaying()) {
            startPlay();
        }
        getWatchPlayback().seekTo(playerCurrentPosition);
    }

    @Override
    public int changeScaleType() {
        scaleType = scaleTypeList[(++currentPos) % scaleTypeList.length];
        getWatchPlayback().setScaleType(scaleType);
        playbackView.setScaleTypeText(scaleType);
        return scaleType;
    }

    @Override
    public int changeScreenOri() {
        return watchView.changeOrientation();
    }

    @Override
    public void onResume() {
        if (!getWatchPlayback().isAvaliable()) {
            playbackView.setScaleTypeText(scaleType);
            initWatch();
        } else {
            getWatchPlayback().onResume();

            if (getWatchPlayback().isAvaliable()) {
                playbackView.setPlayIcon(false);
            } else {
                playbackView.setPlayIcon(true);
            }
        }
    }


    @Override
    public void onPause() {
        /** onPause只需要根据Activity的生命周期调用即可,暂停可以使用stop方法*/
        getWatchPlayback().onPause();
        playbackView.setPlayIcon(true);
    }

    @Override
    public void onStop() {
        getWatchPlayback().stop();
        playbackView.setPlayIcon(true);
    }

    @Override
    public void onSwitchPixel(String pix) {
        getWatchPlayback().setDefinition(pix);
    }

    //设置观看倍速
    @Override
    public void setSpeed() {
        String speed = speedStrs[(++currentSpeed) % speedStrs.length];
        if (getWatchPlayback().setSpeed(Float.parseFloat(speed)) == 0) {
            speed = speedStrs[(--currentSpeed) % speedStrs.length];
        }
        playbackView.setPlaySpeedText(speed);
    }

    public WatchPlayback getWatchPlayback() {
        if (watchPlayback == null) {
            WatchPlayback.Builder builder = new WatchPlayback.Builder()
                    .context(watchView.getActivity())
                    .vodPlayView(playbackView.getVideoView())
//                    .surfaceView(playbackView.getVideoView())
                    .callback(new WatchCallback())
                    .chatCallback(new ChatCallback())
                    .docCallback(new DocCallback());
            watchPlayback = builder.build();
        }
        return watchPlayback;
    }

    @Override
    public void signIn(String signId) {

    }


    @Override
    public void submitSurvey(String result) {

    }

    @Override
    public void submitSurvey(Survey survey, String result) {

    }

    @Override
    public void onRaiseHand() {

    }

    @Override
    public void replyInvite(int type) {

    }

    //TODO 投屏相关
    @Override
    public DMCControl dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service) {
        DMCControl dmcControl = new DMCControl(deviceDisplay, service, getWatchPlayback().getOriginalUrl(), webinarInfo);
        return dmcControl;
    }

    @Override
    public void showDevices() {
        watchView.showDevices();
        getWatchPlayback().onPause();
    }

    @Override
    public void dismissDevices() {
        watchView.dismissDevices();
    }

    /**
     * 根据文档状态选择展示
     */
    private void operationDocument() {
        if (getWatchPlayback().isUseDoc()) {
            documentView.showType(0);//关闭文档
            return;
        }

        if (getWatchPlayback().isUseBoard()) {
            documentView.showType(1);
            return;
        }
        documentView.showType(2);

    }

    private class DocCallback implements WatchPlayback.DocumentEventCallback {

        @Override
        public void onEvent(String key, List<MessageServer.MsgInfo> msgInfos) {
            if (SHOW_DOC_KEY.equals(key)) {
                if (msgInfos.size() > 0) {
                    getWatchPlayback().setIsUseDoc(msgInfos.get(msgInfos.size() - 1).watchType);
                    operationDocument();
                }
            } else {
                if (msgInfos != null && msgInfos.size() > 0) {
                    getWatchPlayback().setIsUseBoard(msgInfos.get(msgInfos.size() - 1).showType);
                    operationDocument();
                    documentView.paintBoard(key, msgInfos);
                    documentView.paintPPT(key, msgInfos);
                }
            }
        }

        @Override
        public void onEvent(MessageServer.MsgInfo msgInfo) {
            if (msgInfo.event == EVENT_SHOWDOC) {
                getWatchPlayback().setIsUseDoc(msgInfo.watchType);
                operationDocument();
            } else {
                if (msgInfo.event == EVENT_SHOWBOARD) {
                    getWatchPlayback().setIsUseBoard(msgInfo.showType);
                }
                if (getWatchPlayback().isUseBoard()) {
                    documentView.paintBoard(msgInfo);
                }
                documentView.paintPPT(msgInfo);
            }
        }

        @Override
        public void onEvent(String event, String type, View docView) {
            if (documentView != null) {
                if (event.equals(KEY_OPERATE)) {
                    if (type.equals(TYPE_ACTIVE)) {
                        documentView.paintH5DocView(docView);
                    } else if (type.equals(TYPE_SWITCHOFF)) {
                        documentView.showType(2);
                    } else if (type.equals(TYPE_SWITCHON)) {
                        documentView.showType(3);
                    }
                }
            }
        }

        @Override
        public void onError(int errorCode, int innerError, String errorMsg) {
            switch (errorCode) {
                case ERROR_CONNECT://文档服务链接错误
                case ERROR_SEND://文档信息发送错误，演示端生效
                    break;
                case ERROR_DOC_INFO://文档加载错误
                    try {
                        JSONObject obj = new JSONObject(errorMsg);
                        String msg = obj.optString("msg");
                        String cid = obj.optString("cid");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class WatchCallback implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case IDLE:
                    Log.e(TAG, "STATE_IDLE");
                    break;
                case START:
                    playbackView.showProgressbar(false);
                    playbackView.setPlayIcon(false);
                    playerDuration = getWatchPlayback().getDuration();
                    playerDurationTimeStr = VhallUtil.converLongTimeToStr(playerDuration);
                    playbackView.setSeekbarMax((int) playerDuration);
                    break;
                case BUFFER:
                    Log.e(TAG, "STATE_BUFFERING");
                    playbackView.showProgressbar(true);
                    break;
                case STOP:
                    playbackView.showProgressbar(false);
                    Log.e(TAG, "STATE_STOP");
                    playbackView.setPlayIcon(true);
                    break;
                case END:
                    Log.e(TAG, "STATE_END" + (int) playerDuration);
                    playerCurrentPosition = getWatchPlayback().getCurrentPosition();
                    playbackView.setSeekbarCurrentPosition((int) playerCurrentPosition);
                    playbackView.showProgressbar(false);
                    playerCurrentPosition = 0;
                    playbackView.setPlayIcon(true);
                    playbackView.setSeekbarCurrentPosition((int) playerDuration);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case Constants.Event.EVENT_DPI_LIST:
                    //支持的分辨率 msg
                    try {
                        JSONArray array = new JSONArray(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.Event.EVENT_DPI_CHANGED:
                    playbackView.setQualityChecked(msg);
                    break;
                case ERROR_LOGIN_MORE://被其他人踢出
                    watchView.showToast(msg);
                    watchView.getActivity().finish();
                    break;
            }
        }

        @Override
        public void onError(int errorCode, int innerErrorCode, String msg) {
            Log.e(TAG, "errorCode:" + errorCode + "  errorMsg:" + msg);
            switch (errorCode) {
                case Constants.ErrorCode.ERROR_INIT:
                    break;
                case Constants.ErrorCode.ERROR_INIT_FIRST:
                    break;
                default:
                    break;
            }
            playbackView.showProgressbar(false);
            playbackView.setPlayIcon(true);
            watchView.showToast("播放出错：" + msg);
        }
    }

    //每秒获取一下进度
    private void handlePosition() {
        if (timer != null) {
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 1000);
    }

    @Override
    public void showChatView(boolean emoji, InputUser user, int limit) {
        watchView.showChatView(emoji, user, limit);
    }

    @Override
    public void sendChat(final String text) {
        if (!VhallSDK.isLogin()) {
            Toast.makeText(watchView.getActivity(), R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
            return;
        }
        getWatchPlayback().sendComment(text, new RequestCallback() {
            @Override
            public void onSuccess() {
//                initCommentData(pos = 1);
            }

            @Override
            public void onError(int errorCode, String reason) {
                watchView.showToast(reason);
            }
        });
    }

    private class ChatCallback implements ChatServer.Callback {
        @Override
        public void onChatServerConnected() {

        }

        @Override
        public void onConnectFailed() {
        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    if (chatInfo.msgData!=null&&!TextUtils.isEmpty(chatInfo.msgData.target_id)){
                        //根据target_id 不为空标记当前是不是问答私聊 是的话直接过滤
                        return;
                    }
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onChatServerClosed() {

        }
    }

    @Override
    public void sendCustom(JSONObject text) {

    }

    @Override
    public void sendQuestion(String content) {

    }

    @Override
    public void onLoginReturn() {

    }

    @Override
    public void showSurvey(String url, String title) {

    }

    @Override
    public void showSurvey(String surveyid) {

    }
}
