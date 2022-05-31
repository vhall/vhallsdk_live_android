package com.vhall.uilibs.broadcast;

import android.text.TextUtils;
import android.util.Log;

import com.vhall.business.ChatServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business_interactive.InterActive;
import com.vhall.push.VHLivePushFormat;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;
import com.vhall.vhss.CallBack;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 发直播的Presenter
 */
public class BroadcastNoDelayPresenter implements BroadcastContract.Presenter, ChatContract.ChatPresenter {
    private static final String TAG = "BroadcastPresenter";
    private Param param;
    private WebinarInfo webinarInfo;
    private BroadcastContract.View mView;
    private BroadcastContract.BroadcastView mBraodcastView;
    ChatContract.ChatView chatView;
    private boolean isPublishing = false;
    private boolean isFinish = false;
    private boolean isFlashOpen = false;
    private int mode = VHLivePushFormat.DRAW_MODE_ASPECTFILL;

    private InterActive interactive;
    private Stream localStream;

    public BroadcastNoDelayPresenter(Param params, WebinarInfo webinarInfo, BroadcastContract.BroadcastView mBraodcastView, BroadcastContract.View mView, ChatContract.ChatView chatView) {
        this.param = params;
        this.webinarInfo = webinarInfo;
        this.mView = mView;
        this.mBraodcastView = mBraodcastView;
        this.chatView = chatView;
        this.chatView.setPresenter(this);
        mView.setPresenter(this);
    }

    private VHRenderView renderView;

    public void setRenderView(VHRenderView renderView) {
        this.renderView = renderView;

    }

    @Override
    public void start() {
        initBroadcast();
    }


    private boolean isStart = false;

    @Override
    public void onstartBtnClick() {
        if (!isStart) {
            //直接开始
            startBroadcast();
        } else {
            mView.showEndLiveDialog();
        }
    }

    /**
     * 1、后台一段时间摄像头会被释放
     * 2、重新获取摄像头需要初始化本地stream
     * 3、重新调用推流publish
     */
    private void rePush() {
        if (interactive != null && renderView != null) {
            if (localStream != null) {
                localStream.removeAllRenderView();
            }
            interactive.setLocalView(renderView, Stream.VhallStreamType.VhallStreamTypeAudioAndVideo, null);
            localStream = interactive.getLocalStream();
            if (isStart) {
                interactive.unpublished();
                interactive.publish();
            }
        }
    }

    @Override
    public void onResume() {
        if (interactive != null && renderView != null) {
            if (localStream != null) {
                localStream.removeAllRenderView();
            }
            interactive.setLocalView(renderView, Stream.VhallStreamType.VhallStreamTypeAudioAndVideo, null);
            localStream = interactive.getLocalStream();
            if (isMute) {
                localStream.muteAudio(null);
            }else {
                localStream.unmuteAudio(null);
            }
            if (isStart) {
                interactive.publish();
            }
        }
    }


    @Override
    public void onPause() {
        if (interactive != null && renderView != null && localStream != null) {
            if (isStart) {
                interactive.unpublish(null);
            }
        }
    }

    @Override
    public void initBroadcast() {
        if (interactive == null) {
            interactive = new InterActive(mBraodcastView.getActivity(), new RoomCallback(), new ChatCallback(), null);
            interactive.init(webinarInfo, new RequestCallback() {
                @Override
                public void onSuccess() {
                    interactive.setDefinition(5);
                    interactive.setLocalView(renderView, Stream.VhallStreamType.VhallStreamTypeAudioAndVideo, null);
                    localStream = interactive.getLocalStream();
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    chatView.showToast("" + errorMsg);
                    isStart = false;
                }
            });
        }
    }

    @Override
    public void startBroadcast() {//发起直播
        interactive.enterRoom();
    }

    @Override
    public void stopBroadcast() {//停止直播
        //无延迟不支持
    }

    @Override
    public void finishBroadcast() {
        String broId;
        String broToken;
        if (webinarInfo != null) {
            broId = webinarInfo.webinar_id;
            broToken = webinarInfo.broadcastToken;
        } else {
            broId = param.broId;
            broToken = param.broToken;
        }
        if (interactive != null)
            interactive.broadCastRoom(2, 0, null);
        VhallSDK.finishBroadcast(broId, broToken, null, new RequestCallback() {
            @Override
            public void onSuccess() {
                isFinish = true;
            }

            @Override
            public void onError(int errorCode, String reason) {
                Log.e(TAG, "finishFailed：" + reason);
            }
        });
    }

    @Override
    public void changeFlash() {

    }

    boolean isMute = false;

    @Override
    public void changeMode() {
    }

    @Override
    public void changeCamera() {
        if (localStream != null)
            localStream.switchCamera();

    }

    @Override
    public void changeAudio() {
        if (localStream != null) {
            isMute = !isMute;
            if (isMute) {
                localStream.muteAudio(null);
            }else {
                localStream.unmuteAudio(null);
            }
            mView.setAudioBtnImage(!isMute);
        }
    }

    @Override
    public void destroyBroadcast() {
        finishBroadcast();
        interactive.onDestroy();
    }

    @Override
    public void setVolumeAmplificateSize(float size) {

    }


    @Override
    public void showChatView(boolean emoji, InputUser user, int limit) {
        mBraodcastView.showChatView(emoji, user, limit);
    }

    int request = 0;
    int response = 0;

    @Override
    public void sendChat(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        request++;
        Log.e(TAG, "请求：" + request);

        interactive.sendMsg(text, "", new CallBack() {
            @Override
            public void onSuccess(Object result) {
                response++;
                Log.e(TAG, "响应成功：" + response);
            }

            @Override
            public void onError(int eventCode, String msg) {
                chatView.showToast(msg);
                response++;
                Log.e(TAG, "响应失败：" + msg + "count:" + response);
            }
        });
    }

    @Override
    public void sendCustom(JSONObject text) {
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

    class RoomCallback implements InterActive.RoomCallback {

        @Override
        public void onDidConnect() {//进入房间
            Log.e("vhall_", "onDidConnect");
            if (interactive != null) {
                interactive.publish();
            }
            mBraodcastView.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //开始直播
                    JSONObject config = new JSONObject();
                    try {
                        config.put("profile", Room.getProfile(Room.BROADCAST_VIDEO_PROFILE_1080P_1));
                        config.put("adaptiveLayoutMode", 2);
                        config.put("precast_pic_exist", false);
                        interactive.broadCastRoom(1, config, new CallBack() {
                            @Override
                            public void onSuccess(Object result) {
                                mView.setStartBtnImage(false);
                                chatView.showToast("开始成功");
                                isStart = true;
                            }

                            @Override
                            public void onError(int eventCode, String msg) {
                                chatView.showToast("" + msg);
                                isStart = false;
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        chatView.showToast("" + e.getMessage());
                        isStart = false;
                    }
                }
            });
        }

        @Override
        public void onDidError() {//进入房间失败
            Log.e(TAG, "onDidError");
        }

        @Override
        public void onDidPublishStream() {// 上麦
            Log.e("vhall_", "onDidPublishStream");
        }

        @Override
        public void onDidUnPublishStream() {//下麦
            Log.e(TAG, "onDidUnPublishStream");
        }

        @Override
        public void onDidSubscribeStream(Stream stream, final VHRenderView newRenderView) {
            Log.e(TAG, "onDidSubscribeStream");
//            liveNoDelayView.updateStream(false, stream, newRenderView);
        }

        @Override
        public void onDidRoomStatus(Room room, Room.VHRoomStatus vhRoomStatus) {
            Log.e(TAG, "onDidRoomStatus  " + vhRoomStatus);
            switch (vhRoomStatus) {
                case VHRoomStatusDisconnected:
                    break;
                case VHRoomStatusError:
                    mBraodcastView.getActivity().finish();
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
        }
    }
}
