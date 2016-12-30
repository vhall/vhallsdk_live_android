package com.vhall.live.watch;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.VhallPPT;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchLive;
import com.vhall.business.WatchPlayback;
import com.vhall.live.VhallApplication;
import com.vhall.live.chat.ChatContract;
import com.vhall.live.data.Param;
import com.vhall.live.utils.VhallUtil;
import com.vhall.playersdk.player.vhallplayer.VHallPlayer;

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
    private int limit = 5;
    private int pos = 0;


    private WatchPlayback watchPlayback;
    private VhallPPT ppt;

    int[] scaleTypeList = new int[]{WatchLive.FIT_DEFAULT, WatchLive.FIT_CENTER_INSIDE, WatchLive.FIT_X, WatchLive.FIT_Y, WatchLive.FIT_XY};
    int currentPos = 0;
    private int scaleType = WatchLive.FIT_DEFAULT;

    private long playerCurrentPosition = 0L;
    private long playerDuration;
    private String playerDurationTimeStr = "00:00:00";

    private Timer timer;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (getWatchPlayback().isPlaying()) {
                playerCurrentPosition = getWatchPlayback().getCurrentPosition();
                playbackView.setSeekbarCurrentPosition((int) playerCurrentPosition);
                String playerCurrentPositionStr = VhallUtil.converLongTimeToStr(playerCurrentPosition);
                playbackView.setProgressLabel(playerCurrentPositionStr + "/" + playerDurationTimeStr);
                if (ppt != null) {
                    String url = ppt.getPPT(playerCurrentPosition / 1000);
                    documentView.showDoc(url);
                }
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
    }

    @Override
    public void start() {
        playbackView.setScaleTypeText(VhallUtil.getFixType(scaleType));
        initWatch();
    }

    private void initCommentData(int pos) {
        watchPlayback.requestCommentHistory(param.id, limit, pos, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                chatView.notifyDataChanged(list);
                chatView.clearInputContent();
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
                Toast.makeText(VhallApplication.getApp(), messaage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initWatch() {
        VhallSDK.getInstance().initWatch(param.id, "test", "test@vhall.com", VhallApplication.user_vhall_id, param.k, getWatchPlayback(), new VhallSDK.RequestCallback() {
            @Override
            public void onSuccess() {
                handlePosition();
                setPPT();
                pos = 0;
                initCommentData(pos);
            }

            @Override
            public void onError(int errorCode, String reason) {
                Toast.makeText(VhallApplication.getApp(), reason, Toast.LENGTH_SHORT).show();
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
                startPlay();
            } else {
                initWatch();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        playbackView.setProgressLabel(VhallUtil.converLongTimeToStr(progress) + "/" + playerDurationTimeStr);
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
        playbackView.setScaleTypeText(VhallUtil.getFixType(scaleType));
        return scaleType;
    }

    @Override
    public int changeScreenOri() {
        int ori = playbackView.changeScreenOri();
        if (ori == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            watchView.setShowDetail(true);
        } else {
            watchView.setShowDetail(false);
        }
        return playbackView.getmActivity().getRequestedOrientation();
    }

    public WatchPlayback getWatchPlayback() {
        if (watchPlayback == null) {
            WatchPlayback.Builder builder = new WatchPlayback.Builder().context(playbackView.getmActivity()).containerLayout(playbackView.getContainer()).callback(new WatchPlayback.WatchEventCallback() {
                @Override
                public void onStartFailed(String reason) {//开始播放失败
                    Toast.makeText(playbackView.getmActivity(), reason, Toast.LENGTH_SHORT).show();
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
                            stopPlay();
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onError(Exception e) {//播放出错
                    stopPlay();
                }

                @Override
                public void onVideoSizeChanged(int width, int height) {//视频宽高改变
                }
            });
            watchPlayback = builder.build();
        }
        return watchPlayback;
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

    //如果没有PPT业务，可忽略
    private void setPPT() {
        if (ppt == null)
            ppt = new VhallPPT();
        getWatchPlayback().setVhallPPT(ppt);
    }

    @Override
    public void sendChat(final String text, final String user_id) {
        getWatchPlayback().sendComment(text, user_id, new VhallSDK.RequestCallback() {
            @Override
            public void onSuccess() {
                chatView.clearChatData();
                initCommentData(pos = 0);
                chatView.clearInputContent();
            }

            @Override
            public void onError(int errorCode, String reason) {
                Toast.makeText(VhallApplication.getApp(), reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void sendQuestion(String content, String vhall_id) {

    }

    @Override
    public void onLoginReturn() {

    }

    @Override
    public void onFreshData() {
        pos = pos + limit;
        initCommentData(pos);
    }
}
