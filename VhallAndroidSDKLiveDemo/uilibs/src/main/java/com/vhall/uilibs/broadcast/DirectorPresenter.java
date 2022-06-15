package com.vhall.uilibs.broadcast;

import android.text.TextUtils;
import android.util.Log;
import android.widget.RelativeLayout;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchLive;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.player.VHPlayerListener;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.util.emoji.InputUser;

import org.json.JSONObject;

/**
 * 发直播的Presenter
 */
public class DirectorPresenter implements ChatContract.ChatPresenter, BroadcastContract.IDirectorPresenter {
    private static final String TAG = "BroadcastPresenter";
    private Param params;
    private WebinarInfo webinarInfo;
    ChatContract.ChatView chatView;
    BroadcastContract.DirectorView directorView;
    private BroadcastContract.BroadcastView mBraodcastView;
    private WatchLive watchLive;

    public DirectorPresenter(Param params, WebinarInfo webinarInfo, BroadcastContract.BroadcastView mBraodcastView, BroadcastContract.DirectorView directorView, ChatContract.ChatView chatView) {
        this.params = params;
        this.webinarInfo = webinarInfo;
        this.chatView = chatView;
        this.directorView = directorView;
        this.mBraodcastView = mBraodcastView;
        this.chatView.setPresenter(this);
        this.directorView.setPresenter(this);
        directorView.setDirectorError(webinarInfo.director_stream);
    }


    @Override
    public void init() {
        getWatchLive().setWebinarInfo(webinarInfo);
    }

    @Override
    public void start() {
        getWatchLive().start();
    }

    private RelativeLayout watchLayout;

    public WatchLive getWatchLive() {
        if (watchLive == null) {
            watchLayout = directorView.getWatchLayout();
            WatchLive.Builder builder = new WatchLive.Builder()
                    .context(directorView.getActivity().getApplicationContext())
                    .containerLayout(watchLayout)
                    .bufferDelay(params.bufferSecond)
                    .callback(new WatchCallback())
                    .messageCallback(new MessageEventCallback())
                    .connectTimeoutMils(10000)
                    .chatCallback(new ChatCallback());
            watchLive = builder.build();
        }
        return watchLive;
    }

    @Override
    public void showChatView(boolean emoji, InputUser user, int limit) {
        mBraodcastView.showChatView(emoji, user, limit);
    }


    @Override
    public void sendChat(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        getWatchLive().sendChat(text, new RequestCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                chatView.showToast(errorMsg);
            }
        });

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

    private boolean starWebinar = false;

    @Override
    public void onResume() {
        VhallSDK.getDirectorStreamStatus(params.broId, new RequestDataCallback() {
            @Override
            public void onSuccess(Object o) {
                String director_stream_status = (String) o;
                directorView.setDirectorError(director_stream_status);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                chatView.showToast(errorMsg);
            }
        });
    }

    @Override
    public void onPause() {
        getWatchLive().stop();
    }


    @Override
    public boolean getWebinarStatus() {
        return starWebinar;
    }

    //点击开始按钮
    @Override
    public void onstartBtnClick() {

        VhallSDK.startBroadcast(params.broId, new RequestCallback() {
            @Override
            public void onSuccess() {
                starWebinar = true;
                directorView.setStartBtnImage(true);
                chatView.showToast("直播开始");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                starWebinar = false;
                chatView.showToast(errorMsg);
                directorView.setStartBtnImage(false);
            }
        });
    }


    @Override
    public void onDestroy() {
        //如果活动开启 离开要结束直播
        if (starWebinar) {
            VhallSDK.finishBroadcast(params.broId, "", null, null);
        }
        //先调用结束直播在调用销毁
        getWatchLive().destroy();
    }

    /**
     * 观看过程中事件监听
     */
    private class WatchCallback implements VHPlayerListener {
        @Override
        public void onStateChanged(com.vhall.player.Constants.State state) {
            switch (state) {
                case START:

                    break;
                case BUFFER:

                    break;
                case STOP:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {

        }

        @Override
        public void onError(int errorCode, int innerCode, String msg) {
            chatView.showToast(msg);
        }
    }

    /**
     * 观看过程消息监听
     */
    private class MessageEventCallback implements MessageServer.Callback {
        @Override
        public void onEvent(MessageServer.MsgInfo messageInfo) {
            switch (messageInfo.event) {
                case MessageServer.EVENT_DIRECTOR_STREAM://云导播活动流状态
                    directorView.setDirectorError(messageInfo.director_stream_status);
                default:
                    break;
            }
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

    private class ChatCallback implements ChatServer.Callback {
        @Override
        public void onChatServerConnected() {

        }

        @Override
        public void onConnectFailed() {
//            getBroadcast().connectChatServer();
        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    break;
                case ChatServer.eventCustomKey:
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    break;
                case ChatServer.eventOnlineKey:
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    break;
                case ChatServer.eventOfflineKey:
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

}
