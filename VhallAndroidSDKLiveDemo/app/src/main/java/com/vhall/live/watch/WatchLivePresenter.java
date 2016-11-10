package com.vhall.live.watch;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchLive;
import com.vhall.live.VhallApplication;
import com.vhall.live.chat.ChatContract;
import com.vhall.live.data.Param;
import com.vhall.live.utils.VhallUtil;

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

    int[] scaleTypes = new int[]{WatchLive.FIT_DEFAULT, WatchLive.FIT_CENTER_INSIDE, WatchLive.FIT_X, WatchLive.FIT_Y, WatchLive.FIT_XY};
    int currentPos = 0;
    private int scaleType = WatchLive.FIT_DEFAULT;
    private MessageServer.MsgInfo messageServer;

    public WatchLivePresenter(WatchContract.LiveView liveView, WatchContract.DocumentView documentView, ChatContract.ChatView chatView, ChatContract.ChatView questionView, WatchContract.WatchView watchView, Param param) {
        this.params = param;
        this.liveView = liveView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.questionView = questionView;
        this.chatView = chatView;
        this.liveView.setPresenter(this);
        this.chatView.setPresenter(this);
        this.questionView.setPresenter(this);
        this.messageServer = new MessageServer.MsgInfo();
    }

    @Override
    public void start() {
        VhallSDK.getInstance().setLogEnable(true);
        liveView.setScaleButtonText(VhallUtil.getFixType(scaleType));
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
    public void sendChat(String text) {
        if (TextUtils.isEmpty(text))
            return;
        getWatchLive().sendChat(text, new VhallSDK.RequestCallback() {
            @Override
            public void success() {
                chatView.clearInputContent();
            }

            @Override
            public void failed(int errorCode, String reason) {
                chatView.showToast(reason);
            }
        });
    }

    @Override
    public void sendQuestion(String content, String vhall_id) {
        if (TextUtils.isEmpty(content))
            return;
        getWatchLive().sendQuestion(content, vhall_id, new VhallSDK.RequestCallback() {
            @Override
            public void success() {
                questionView.clearInputContent();
            }

            @Override
            public void failed(int errorCode, String reason) {
                questionView.showToast(reason);
            }
        });
    }

    @Override
    public void onLoginReturn() {
        initWatch();
    }

    @Override
    public void onSwitchPixel(int level) {
        if (getWatchLive().getDefinition() == level) {
            liveView.showToast("已经切过了!!!");
            return;
        }
        if (liveView.getmActivity().isFinishing()) {
            return;
        }
        if (isWatching) {
            stopWatch();
        }
        getWatchLive().setDefinition(level);
        /** 停止观看 不能立即重连 要延迟一秒重连*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isWatching && !liveView.getmActivity().isFinishing()) {
                    startWatch();
                }
            }
        }, 500);
    }


    @Override
    public int setScaleType() {
        scaleType = scaleTypes[(++currentPos) % scaleTypes.length];
        getWatchLive().setScaleType(scaleType);
        liveView.setScaleButtonText(VhallUtil.getFixType(scaleType));
        return scaleType;
    }

    @Override
    public int changeOriention() {
        int ori = liveView.changeOrientation();
        if (ori == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            watchView.setShowDetail(true);
        } else {
            watchView.setShowDetail(false);
        }
        return ori;
    }

    @Override
    public void onDestory() {
        getWatchLive().destory();
    }

    @Override
    public void submitLotteryInfo(String nickname, String phone) {
        if (!TextUtils.isEmpty(messageServer.id) && !TextUtils.isEmpty(messageServer.lottery_id)) {
            VhallSDK.getInstance().submitLotteryInfo(messageServer.id, messageServer.lottery_id, nickname, phone, new VhallSDK.RequestCallback() {
                @Override
                public void success() {
                    liveView.showToast("信息提交成功");
                }

                @Override
                public void failed(int errorCode, String reason) {

                }
            });
        }
    }

    @Override
    public void initWatch() {
        VhallSDK.getInstance().initWatch(params.id,  "test", "test@vhall.com", VhallApplication.user_vhall_id, params.k, getWatchLive(), new VhallSDK.RequestCallback() {
            @Override
            public void success() {
                liveView.showRadioButton(getWatchLive().getDefinitionAvailable());
                getChatHistory();
            }

            @Override
            public void failed(int errorCode, String msg) {
                Toast.makeText(VhallApplication.getApp(), msg, Toast.LENGTH_SHORT).show();
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
                    .context(liveView.getmActivity())
                    .containerLayout(liveView.getWatchLayout())
                    .bufferDelay(params.bufferSecond)
                    .callback(new WatchCallback())
                    .messageCallback(new MessageEventCallback())
                    .chatCallback(new ChatCallback());
            watchLive = builder.build();
        }
        return watchLive;
    }

    /**
     * 观看过程中事件监听
     */
    private class WatchCallback implements WatchLive.WatchEventCallback {
        @Override
        public void onError(int errorCode, String errorMsg) {
            switch (errorCode) {
                case WatchLive.ERROR_CONNECT:
                    isWatching = false;
                    liveView.showLoading(false);
                    liveView.setPlayPicture(isWatching);
                    liveView.showToast(errorMsg);
                    break;
                default:
                    liveView.showToast(errorMsg);
            }
        }

        @Override
        public void onStateChanged(int stateCode) {
            switch (stateCode) {
                case WatchLive.STATE_CONNECTED:
                    isWatching = true;
                    liveView.setPlayPicture(isWatching);
                    break;
                case WatchLive.STATE_BUFFER_START:
                    if (isWatching)
                        liveView.showLoading(true);
                    break;
                case WatchLive.STATE_BUFFER_STOP:
                    liveView.showLoading(false);
                    break;
                case WatchLive.STATE_STOP:
                    isWatching = false;
                    liveView.setPlayPicture(isWatching);
                    break;
            }
        }

        @Override
        public void uploadSpeed(String kbps) {
            liveView.setDownSpeed("速率" + kbps + "/kbps");
        }

    }

    /**
     * 观看过程消息监听
     */
    private class MessageEventCallback implements MessageServer.Callback {
        @Override
        public void onEvent(MessageServer.MsgInfo messageInfo) {
            switch (messageInfo.event) {
                case MessageServer.EVENT_PPT_CHANGED://PPT翻页消息
                    documentView.showDoc(messageInfo.pptUrl);
                    break;
                case MessageServer.EVENT_DISABLE_CHAT://禁言
                    break;
                case MessageServer.EVENT_KICKOUT://踢出
                    break;
                case MessageServer.EVENT_OVER://直播结束
                    liveView.showToast("直播已结束");
                    stopWatch();
                    break;
                case MessageServer.EVENT_PERMIT_CHAT://解除禁言
                    break;
                case MessageServer.EVENT_START_LOTTERY://抽奖开始
                    liveView.showDialogStatus(1, false, null);
                    break;
                case MessageServer.EVENT_END_LOTTERY://抽奖结束
                    String[] nameList = null;
                    if (messageInfo != null && !TextUtils.isEmpty(messageInfo.id)) {
                        messageServer.id = messageInfo.id;
                        messageServer.lottery_id = messageInfo.lottery_id;
                    }
                    if (messageInfo.lists != null && messageInfo.lists.size() > 0) {
                        nameList = new String[messageInfo.lists.size()];
                        for (int i = 0; i < messageInfo.lists.size(); i++) {
                            nameList[i] = messageInfo.lists.get(i).nick_name;
                        }
                        liveView.showDialogStatus(2, messageInfo.isLottery, nameList);
                    }
                    break;
            }

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
            Log.e(TAG, " ChatServerConnected");
        }

        @Override
        public void onConnectFailed() {
            Log.e(TAG, " onConnectFailed -----");
//            getWatchLive().connectChatServer();
        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
            Log.e(TAG, " onChatMessageReceived -----");
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
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
            Log.e(TAG, " onChatServerClosed -----");
        }
    }

    private void getChatHistory() {
        getWatchLive().acquireChatRecord(true, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                Log.e(TAG, "list->" + list.size());
                chatView.notifyDataChanged(list);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
                Log.e(TAG, "onFailed->" + errorcode + ":" + messaage);
            }
        });
    }

}

