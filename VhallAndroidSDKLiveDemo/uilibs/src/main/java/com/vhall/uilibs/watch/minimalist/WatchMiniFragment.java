package com.vhall.uilibs.watch.minimalist;

import static com.vhall.business.ErrorCode.ERROR_LOGIN_MORE;
import static com.vhall.business.Watch.EVENT_INIT_PLAYER_SUCCESS;
import static com.vhall.uilibs.util.ToastUtil.showToast;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.WatchLive;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.player.VHPlayerListener;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;

public class WatchMiniFragment extends Fragment {

    private RelativeLayout rl_container;
    private WatchLive watchLive;
    private Param params;
    private MessageServer.Callback messageEventCallback;
    private ChatServer.Callback chatCallBack;
    private WebinarInfo webinarInfoData;
    protected Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    public void initWebinarInfo(WebinarInfo webinarInfoData, Param params, MessageServer.Callback messageEventCallback, ChatServer.Callback chatCallBack) {
        this.webinarInfoData = webinarInfoData;
        this.params = params;
        this.messageEventCallback = messageEventCallback;
        this.chatCallBack = chatCallBack;
    }

    public static WatchMiniFragment newInstance() {
        return new WatchMiniFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_watch_mini, container, false);
        rl_container = inflate.findViewById(R.id.rl_container);
        initWatch();
        return inflate;
    }


    public WatchLive getWatchLive() {
        return watchLive;
    }

    private void initWatch() {
        WatchLive.Builder builder = new WatchLive.Builder()
                .context(getContext())
                .containerLayout(rl_container)
                .bufferDelay(params.bufferSecond)
                .callback(new WatchCallback())
                .messageCallback(messageEventCallback)
                .connectTimeoutMils(10000)
                .chatCallback(chatCallBack);
        watchLive = builder.build();
        watchLive.setWebinarInfo(webinarInfoData);
        watchLive.acquireChatRecord(true, chatRecordCallback);
    }

    public void setRootViewVisibility(int visibility){
        rl_container.setVisibility(visibility);
    }

    public boolean isWatching = false;

    private class WatchCallback implements VHPlayerListener {
        @Override
        public void onStateChanged(com.vhall.player.Constants.State state) {
            switch (state) {
                case START:
                    isWatching = true;
                    break;
                case STOP:
                    isWatching = false;
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case com.vhall.player.Constants.Event.EVENT_DOWNLOAD_SPEED:
                    break;
                case com.vhall.player.Constants.Event.EVENT_DPI_CHANGED:

                    break;
                case com.vhall.player.Constants.Event.EVENT_DPI_LIST:
                    break;
                case com.vhall.player.Constants.Event.EVENT_VIDEO_SIZE_CHANGED:
                    break;
                case com.vhall.player.Constants.Event.EVENT_STREAM_START://发起端开始推流

                    break;
                case com.vhall.player.Constants.Event.EVENT_STREAM_STOP://发起端停止推流
                    break;
                case ERROR_LOGIN_MORE://被其他人踢出
                    showToast(msg);
                    mActivity.finish();
                    break;
                case EVENT_INIT_PLAYER_SUCCESS:
                    showToast("自动播放");
                    //自动播放
                    watchLive.start();
                    break;
                default:
                    break;

            }
        }

        @Override
        public void onError(int errorCode, int innerCode, String msg) {
            showToast(msg);
        }
    }

    public void sendMgs(String content) {
        if (watchLive != null) {
            watchLive.sendChat(content, new RequestCallback() {
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

    @Override
    public void onResume() {
        super.onResume();
        if (watchLive != null) {
            watchLive.start();
        }
    }

    private ChatServer.ChatRecordCallback chatRecordCallback;

    public void getChatHistory(ChatServer.ChatRecordCallback chatRecordCallback) {
        this.chatRecordCallback = chatRecordCallback;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (watchLive != null) {
            watchLive.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (watchLive != null) {
            watchLive.destroy();
        }
    }
}