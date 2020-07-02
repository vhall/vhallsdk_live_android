package com.vhall.uilibs.watch;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.ErrorCode;
import com.vhall.business.VhallCallback;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.Survey;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.UserInfoRepository;
import com.vhall.business.data.source.WebinarInfoRepository;
import com.vhall.business.data.source.local.UserInfoLocalDataSource;
import com.vhall.business.data.source.remote.UserInfoRemoteDataSource;
import com.vhall.business.data.WebinarInfoRemoteDataSource;
import com.vhall.business_support.dlna.DMCControl;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.ops.VHOPS;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.chat.ChatFragment;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.uilibs.util.handler.WeakHandler;
import com.vhall.vod.VHVodPlayer;

import org.fourthline.cling.android.AndroidUpnpService;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import vhall.com.vss.CallBack;
import vhall.com.vss.VssSdk;
import vhall.com.vss.data.ResponseRoomInfo;
import vhall.com.vss.module.room.VssRoomManger;

import static com.vhall.ops.VHOPS.KEY_OPERATE;
import static com.vhall.ops.VHOPS.TYPE_ACTIVE;
import static com.vhall.ops.VHOPS.TYPE_SWITCHOFF;
import static com.vhall.ops.VHOPS.TYPE_SWITCHON;


/**
 * 观看回放的Presenter
 *
 * @author hkl
 */
public class WatchPlaybackPresenterVss implements WatchContract.PlaybackPresenter, ChatContract.ChatPresenter {
    private static final String TAG = "PlaybackPresenter";
    private Param param;
    WatchContract.PlaybackView playbackView;
    WatchContract.DocumentViewVss documentView;
    WatchContract.WatchView watchView;
    ChatContract.ChatView chatView;
    private Context context;
    private WebinarInfo webinarInfo;

    //FIT_XY = 0;FIT = 1;FILL= 2;
    int[] scaleTypeList = new int[]{0, 1, 2};
    int currentPos = 0;
    private int scaleType = 0;//FIT_XY

    String[] speedStrs = new String[]{"0.25", "0.50", "1.00", "1.25", "1.50", "2.00"};
    int currentSpeed = 2;

    private int limit = 5;
    private int pos = 0;

    private long playerCurrentPosition = 0L;
    // 当前的进度
    private long playerDuration;
    private String playerDurationTimeStr = "00:00:00";

    private boolean loadingComment = false;
    private VHVodPlayer mPlayer;
    private VHOPS mDocument;

    private Timer timer;
    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0: // 每秒更新SeekBar
                    if (getPlay().getState() != null && getPlay().isPlaying()) {
                        playerCurrentPosition = getPlay().getPosition();
                        playbackView.setSeekbarCurrentPosition((int) playerCurrentPosition);
                        if (mDocument != null) {
                            mDocument.setTime(playerCurrentPosition);
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    public WatchPlaybackPresenterVss(WatchContract.PlaybackView playbackView, final WatchContract.DocumentViewVss documentView, ChatContract.ChatView chatView, final WatchContract.WatchView watchView, Param param,WebinarInfo webinarInfo) {
        this(playbackView,documentView,chatView,watchView,param);
        this.webinarInfo = webinarInfo;
    }

    public WatchPlaybackPresenterVss(WatchContract.PlaybackView playbackView, final WatchContract.DocumentViewVss documentView, ChatContract.ChatView chatView, final WatchContract.WatchView watchView, Param param) {
        this.playbackView = playbackView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.chatView = chatView;
        this.param = param;
        this.playbackView.setPresenter(this);
        this.chatView.setPresenter(this);
        this.watchView.setPresenter(this);
        context = watchView.getActivity();
    }

    private VHOPS.EventListener opsListener = new VHOPS.EventListener() {
        @Override
        public void onEvent(String event, String type, String cid) {
            if (event.equals(KEY_OPERATE)) {
                if (type.equals(TYPE_ACTIVE)) {
                    documentView.refreshView(mDocument.getActiveView());
                } else if (type.equals(TYPE_SWITCHOFF) || type.equals(TYPE_SWITCHON)) {
                    //文档演示 开关
                    documentView.switchType(type);
                }
            }
        }

        @Override
        public void onError(int i, int i1, String s) {

        }
    };

    @Override
    public void start() {
        playbackView.setScaleTypeText(scaleType);
        initWatch();
        if (!TextUtils.isEmpty(param.noticeContent)) {
            watchView.showNotice(param.noticeContent);
        }
    }

    private void requestCommentHistory(String webinar_id, int limit, int pos, final ChatServer.ChatRecordCallback callback) {
        if (limit <= 0 || pos < 0) {
            callback.onFailed(ErrorCode.ERROR_PARAM, ErrorCode.ERROR_PARAM_STR);
            return;
        }
        WebinarInfoRepository repository = WebinarInfoRepository.getInstance(WebinarInfoRemoteDataSource.getInstance());
        repository.getCommentHistory(param.join_id, webinar_id, String.valueOf(limit), String.valueOf(pos), new ChatServer.ChatRecordCallback() {
                    @Override
                    public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                        for (ChatServer.ChatInfo info : list) {
                            Log.e(TAG, "onDataLoaded: " + info.msgData.text);
                        }
                        callback.onDataLoaded(list);
                    }

                    @Override
                    public void onFailed(int errorcode, String messaage) {
                        callback.onFailed(errorcode, messaage);
                    }
                }
        );
    }

    private void initCommentData(int pos) {
        if (loadingComment) {
            return;
        }
        loadingComment = true;
        requestCommentHistory(param.watchId, limit, pos, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                chatView.clearChatData();
                loadingComment = false;
                List<MessageChatData> list1 = new ArrayList<>();
                for (ChatServer.ChatInfo chatInfo : list) {
                    list1.add(MessageChatData.getChatData(chatInfo));
                }
                chatView.notifyDataChangedChat(ChatFragment.CHAT_EVENT_CHAT, list1);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
                loadingComment = false;
                watchView.showToast(messaage);
            }
        });

    }

    private VHVodPlayer getPlay() {
        if (mPlayer == null) {
            mPlayer = new VHVodPlayer(watchView.getActivity());
            mPlayer.setDisplay(playbackView.getVideoView());
            mPlayer.setListener(new MyPlayer());
        }
        return mPlayer;
    }

    private void initWatch() {
        VssRoomManger.getInstance().enterRoom(param.vssToken, param.vssRoomId, new CallBack<ResponseRoomInfo>() {
            @Override
            public void onSuccess(ResponseRoomInfo result) {
                getPlay().init(result.getRecord_id(), result.getPaas_access_token());
                mDocument = new VHOPS(context, result.getRecord_id(), null);
                mDocument.setListener(opsListener);
                mDocument.join();
                pos = 0;
                initCommentData(pos);
                handlePosition();
            }

            @Override
            public void onError(int eventCode, String msg) {
                watchView.showToast(msg);
            }
        });
    }

    @Override
    public void onFragmentDestory() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        getPlay().release();
        if (mDocument != null) {
            mDocument.leave();
        }
    }

    @Override
    public void startPlay() {
        playbackView.setPlayIcon(false);
        if (getPlay().getState() == Constants.State.STOP) {
            getPlay().resume();
            handlePosition();
        } else if (getPlay().getState() == Constants.State.END) {
            getPlay().seekto(0);
            mDocument.seekTo(0);
        } else {
            getPlay().setWaterMark(webinarInfo.watermark.imgUrl,webinarInfo.watermark.imgPosition,webinarInfo.watermark.imgAlpha);
            getPlay().start();
        }
    }

    @Override
    public void onPlayClick() {
        if (getPlay().isPlaying()) {
            onStop();
        } else if(webinarInfo.status == WebinarInfo.VIDEO || webinarInfo.status == WebinarInfo.MEDIA) {
            if (getPlay().getState() == Constants.State.END) {
                getPlay().seekto(0);
            }
            startPlay();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        playbackView.setProgressLabel(VhallUtil.converLongTimeToStr(progress), playerDurationTimeStr);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        playerCurrentPosition = seekBar.getProgress();
        if (!getPlay().isPlaying()) {
            startPlay();
        }
        getPlay().seekto(playerCurrentPosition);
        mDocument.seekTo(playerCurrentPosition);
    }

    @Override
    public int changeScaleType() {
        scaleType = scaleTypeList[(++currentPos) % scaleTypeList.length];
        getPlay().setDrawMode(scaleType);
        playbackView.setScaleTypeText(scaleType);
        return scaleType;
    }

    @Override
    public int changeScreenOri() {
        return watchView.changeOrientation();
    }

    @Override
    public void onResume() {
        getPlay().resume();
        handlePosition();
        if (getPlay() == null) {
            playbackView.setPlayIcon(false);
        } else {
            playbackView.setPlayIcon(true);
        }
    }


    @Override
    public void onPause() {
        /** onPause只需要根据Activity的生命周期调用即可,暂停可以使用stop方法*/
        if (timer != null) {
            timer.cancel();
        }
        getPlay().stop();
        playbackView.setPlayIcon(true);
    }

    @Override
    public void onStop() {
        if (timer != null) {
            timer.cancel();
        }
        getPlay().stop();
        playbackView.setPlayIcon(true);
    }

    @Override
    public void onSwitchPixel(String pix) {
        getPlay().setDPI(pix);
    }

    @Override
    public void setSpeed() {
        String speed = speedStrs[(++currentSpeed) % speedStrs.length];
        if (getPlay().setSpeed(Float.parseFloat(speed)) == 0) {
            speed = speedStrs[(--currentSpeed) % speedStrs.length];
        }
        playbackView.setPlaySpeedText(speed);
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

    @Override
    public DMCControl dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service) {
        DMCControl dmcControl = new DMCControl(deviceDisplay, service, mPlayer.getOriginalUrl(),webinarInfo);
        return dmcControl;
    }

    @Override
    public void showDevices() {
        watchView.showDevices();
        getPlay().pause();
    }

    @Override
    public void dismissDevices() {
        watchView.dismissDevices();
    }


    //每秒获取一下进度
    private void handlePosition() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 150, 150);
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
        sendComment(text, new RequestCallback() {
            @Override
            public void onSuccess() {
                initCommentData(pos = 0);
            }

            @Override
            public void onError(int errorCode, String reason) {
                watchView.showToast(reason);
            }
        });
    }

    private void sendComment(String content, final RequestCallback callback) {
        if (TextUtils.isEmpty(param.webinar_id)) {
            VhallCallback.ErrorCallback(callback, ErrorCode.ERROR_PARAM, "房间ID不能为空！");
            return;
        }
        if (param.filters != null) {
            for (int i = 0; i < param.filters.size(); i++) {
                if (content.contains(param.filters.get(i))) {
                    VhallCallback.ErrorCallback(callback, ErrorCode.ERROR_KEY_FILTERS, "消息中含有禁用关键字");
                    return;
                }
            }
        }

        if (TextUtils.isEmpty(content) || content.length() > 200) {
            VhallCallback.ErrorCallback(callback, ErrorCode.ERROR_PARAM, "聊天内容长度在0-200之间");
            return;
        }
        UserInfoRepository userRepository = UserInfoRepository.getInstance(UserInfoRemoteDataSource.getInstance(), UserInfoLocalDataSource.getInstance());
        userRepository.sendComment(param.webinar_id, content, VhallSDK.user.user_id, callback);
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

    class MyPlayer implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case IDLE:
                    Log.e(TAG, "STATE_IDLE");
                    break;
                case START:
                    playbackView.showProgressbar(false);
                    playbackView.setPlayIcon(false);
                    playerDuration = getPlay().getDuration();
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
//                    getWatchPlayback().stop();
                    playbackView.setPlayIcon(true);
                    break;
                case END:
                    playbackView.showProgressbar(false);
                    Log.e(TAG, "STATE_ENDED");
                    playerCurrentPosition = 0;
//                    getWatchPlayback().stop();
                    playbackView.setPlayIcon(true);
                    break;
                default:
                    break;
            }

        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case Constants.Event.EVENT_INIT_SUCCESS:
                    mDocument.setCue_point(mPlayer.getCurePoint());
//                    mPlayer.start();
                    break;
                case Constants.Event.EVENT_DPI_LIST:

                    break;
                case Constants.Event.EVENT_DPI_CHANGED:
                    playbackView.setQualityChecked(msg);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(int errorCode, int i1, String msg) {
            switch (errorCode) {
                case Constants.ErrorCode.ERROR_INIT:
                    watchView.showToast("初始化失败");
                    break;
                case Constants.ErrorCode.ERROR_INIT_FIRST:
                    watchView.showToast("请先初始化");
                    break;
                default:
                    watchView.showToast("播放出错：" + msg);
                    break;
            }
            playbackView.showProgressbar(false);
            playbackView.setPlayIcon(true);
        }

    }
}
