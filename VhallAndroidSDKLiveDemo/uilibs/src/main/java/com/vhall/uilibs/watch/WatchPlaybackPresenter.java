package com.vhall.uilibs.watch;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business_support.WatchLive;
import com.vhall.business_support.WatchPlayback;
import com.vhall.business.data.Survey;
import com.vhall.business_support.Watch_Support;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.playersdk.player.vhallplayer.VHallPlayer;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.util.emoji.InputUser;

import org.fourthline.cling.android.AndroidUpnpService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 观看回放的Presenter
 */
public class WatchPlaybackPresenter implements WatchContract.PlaybackPresenter, ChatContract.ChatPresenter {
    private static final String TAG = "PlaybackPresenter";
    private Param param;
    WatchContract.PlaybackView playbackView;
    WatchContract.DocumentView documentView;
    WatchContract.WatchView watchView;
    ChatContract.ChatView chatView;
    private WatchPlayback watchPlayback;

    int[] scaleTypeList = new int[]{WatchLive.FIT_DEFAULT, WatchLive.FIT_CENTER_INSIDE, WatchLive.FIT_X, WatchLive.FIT_Y, WatchLive.FIT_XY};
    int currentPos = 0;
    private int scaleType = WatchLive.FIT_DEFAULT;

    private int limit = 5;
    private int pos = 0;

    private long playerCurrentPosition = 0L; // 当前的进度
    private long playerDuration;
    private String playerDurationTimeStr = "00:00:00";

    private boolean loadingVideo = false;
    private boolean loadingComment = false;

    private Timer timer;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0: // 每秒更新SeekBar
                    if (getWatchPlayback().isPlaying()) {
                        playerCurrentPosition = getWatchPlayback().getCurrentPosition();
                        playbackView.setSeekbarCurrentPosition((int) playerCurrentPosition);
                        //                String playerCurrentPositionStr = VhallUtil.converLongTimeToStr(playerCurrentPosition);
                        //                //playbackView.setProgressLabel(playerCurrentPositionStr + "/" + playerDurationTimeStr);
                        //                playbackView.setProgressLabel(playerCurrentPositionStr, playerDurationTimeStr);
                    }
                    break;
                case 1: // 获取当前的播放进度
                    boolean isStart = (boolean) msg.obj;
                    if (isStart) {
                        startPlay();
                    } else
                        paushPlay();
                    getWatchPlayback().seekTo(playerCurrentPosition);
                    //playbackView.setPlayIcon(!isStart);
                    break;
            }
        }
    };

    public WatchPlaybackPresenter(WatchContract.PlaybackView playbackView, WatchContract.DocumentView documentView, ChatContract.ChatView chatView, WatchContract.WatchView watchView, Param param) {
        this.playbackView = playbackView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.chatView = chatView;
        this.param = param;
        this.playbackView.setPresenter(this);
        this.chatView.setPresenter(this);
        this.watchView.setPresenter(this);
    }

    @Override
    public void start() {
        playbackView.setScaleTypeText(scaleType);
        initWatch();
    }

    private void initCommentData(int pos) {
        if (loadingComment)
            return;
        loadingComment = true;
        watchPlayback.requestCommentHistory(param.watchId, limit, pos, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                loadingComment = false;
                chatView.notifyDataChanged(list);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
                loadingComment = false;
                watchView.showToast(messaage);
            }
        });
    }

    private void initWatch() {
        if (loadingVideo)
            return;
        loadingVideo = true;
        //游客ID及昵称 已登录用户可传空
        TelephonyManager telephonyMgr = (TelephonyManager) watchView.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String customeId = telephonyMgr.getDeviceId();
        String customNickname = Build.BRAND + "手机用户";
        VhallSDK.initWatch(param.watchId, customeId, customNickname, param.key, getWatchPlayback(), WebinarInfo.VIDEO, new RequestCallback() {
            @Override
            public void onSuccess() {
                loadingVideo = false;
                handlePosition();
                pos = 0;
                initCommentData(pos);
                watchView.showNotice(getWatchPlayback().getNotice()); //显示公告
            }

            @Override
            public void onError(int errorCode, String reason) {
                loadingVideo = false;
                watchView.showToast(reason);
            }
        });
    }

    @Override
    public void onFragmentStop() {
        stopPlay();
    }

    @Override
    public void onFragmentDestory() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        getWatchPlayback().destory();
    }

    @Override
    public void startPlay() {
        if (!getWatchPlayback().isAvaliable())
            return;
        playbackView.setPlayIcon(false);
        getWatchPlayback().start();
    }

    @Override
    public void paushPlay() {
        getWatchPlayback().pause();
        playbackView.setPlayIcon(true);
    }

    @Override
    public void stopPlay() {
        getWatchPlayback().stop();
        playbackView.setPlayIcon(true);
    }

    @Override
    public void onPlayClick() {
        if (getWatchPlayback().isPlaying()) {
            paushPlay();
        } else {
            if (getWatchPlayback().isAvaliable()) {
                if (playerCurrentPosition > 0) {
                    Message msg = handler.obtainMessage();
                    msg.what = 1;
                    msg.obj = true;
                    handler.sendMessage(msg);
                } else {
                    startPlay();
                }
            } else {
                initWatch();
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
    public void saveCurrentPosition(boolean isStart) {
        if (playerCurrentPosition > 0 && handler != null) {
            //handler.sendEmptyMessage(1);
            Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.obj = isStart;
            handler.sendMessage(msg);
        }
    }

    public WatchPlayback getWatchPlayback() {
        if (watchPlayback == null) {
            WatchPlayback.Builder builder = new WatchPlayback.Builder().context(watchView.getActivity()).containerLayout(playbackView.getContainer()).callback(new WatchCallback()).docCallback(new DocCallback());
            watchPlayback = builder.build();
        }
        return watchPlayback;
    }

    @Override
    public void signIn(String signId) {

    }


    @Override
    public void submitSurvey(Survey survey, String result) {

    }

    @Override
    public void dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service) {
        getWatchPlayback().dlnaPost(deviceDisplay, service, new Watch_Support.DLNACallback() {
            @Override
            public void onError(int errorCode) {
                watchView.showToast("投屏失败，errorCode:" + errorCode);
            }

            @Override
            public void onSuccess() {
                watchView.showToast("投屏成功!");
            }
        });
    }

    @Override
    public void showDevices() {
        watchView.showDevices();
    }

    @Override
    public void dismissDevices() {
        watchView.dismissDevices();
    }

    private class DocCallback implements WatchPlayback.DocumentEventCallback {

        @Override
        public void onEvent(String key, List<MessageServer.MsgInfo> msgInfos) {
            if (msgInfos != null && msgInfos.size() > 0) {
                documentView.paintPPT(key, msgInfos);
                documentView.paintBoard(key, msgInfos);
            }
        }

        @Override
        public void onEvent(MessageServer.MsgInfo msgInfo) {
            documentView.paintPPT(msgInfo);
            documentView.paintBoard(msgInfo);
        }
    }

    private class WatchCallback implements WatchPlayback.WatchEventCallback {
        @Override
        public void onStartFailed(String reason) {//开始播放失败
            Toast.makeText(watchView.getActivity(), reason, Toast.LENGTH_SHORT).show();
            playbackView.setPlayIcon(true);
        }

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {//播放过程中的状态信息
            switch (playbackState) {
                case VHallPlayer.STATE_IDLE:
                    Log.e(TAG, "STATE_IDLE");
                    break;
                case VHallPlayer.STATE_PREPARING:
                    Log.e(TAG, "STATE_PREPARING");
                    playbackView.showProgressbar(true);
                    break;
                case VHallPlayer.STATE_BUFFERING:
                    Log.e(TAG, "STATE_BUFFERING");
                    playbackView.showProgressbar(true);
                    break;
                case VHallPlayer.STATE_READY:
                    playbackView.showProgressbar(false);
                    playerDuration = getWatchPlayback().getDuration();
                    playerDurationTimeStr = VhallUtil.converLongTimeToStr(playerDuration);
                    playbackView.setSeekbarMax((int) playerDuration);
                    Log.e(TAG, "STATE_READY");
                    break;
                case VHallPlayer.STATE_ENDED:
                    playbackView.showProgressbar(false);
                    Log.e(TAG, "STATE_ENDED");
                    playerCurrentPosition = 0;
                    getWatchPlayback().seekTo(0);
                    playbackView.setSeekbarCurrentPosition(0);
                    getWatchPlayback().pause();
                    playbackView.setPlayIcon(true);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(Exception e) {//播放出错
            playbackView.showProgressbar(false);
            stopPlay();
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {//视频宽高改变
        }

    }

    //每秒获取一下进度
    private void handlePosition() {
        if (timer != null)
            return;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 1000, 1000);
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
                chatView.clearChatData();
                initCommentData(pos = 0);
            }

            @Override
            public void onError(int errorCode, String reason) {
                watchView.showToast(reason);
            }
        });
    }

    @Override
    public void sendCustom(String text) {

    }

    @Override
    public void sendQuestion(String content) {

    }

    @Override
    public void onLoginReturn() {

    }

    @Override
    public void onFreshData() {
        pos = pos + limit;
        initCommentData(pos);
    }

    @Override
    public void showSurvey(String surveyid) {

    }
}
