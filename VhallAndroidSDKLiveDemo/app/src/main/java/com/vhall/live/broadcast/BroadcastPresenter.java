package com.vhall.live.broadcast;

import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RelativeLayout;

import com.vhall.business.Broadcast;
import com.vhall.business.ChatServer;
import com.vhall.business.VhallSDK;
import com.vhall.live.chat.ChatContract;
import com.vhall.live.data.Param;

import java.util.List;

/**
 * 发直播的Presenter
 */
public class BroadcastPresenter implements BroadcastContract.Presenter, ChatContract.ChatPresenter {
    private static final String TAG = "BroadcastPresenter";
    private Param param;
    private BroadcastContract.View mView;
    ChatContract.ChatView chatView;
    private Broadcast broadcast;
    private boolean isPublishing = false;
    private boolean isFinish = true;

    public BroadcastPresenter(Param params, BroadcastContract.View mView, ChatContract.ChatView chatView) {
        this.param = params;
        this.mView = mView;
        this.chatView = chatView;
        this.chatView.setPresenter(this);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        //初始化，必须
        mView.getCameraView().init(param.pixel_type, mView.getmActivity(),
                new RelativeLayout.LayoutParams(0, 0));
        //获取摄像头个数，如果是1个，禁止切换摄像头
        if (mView.getCameraView().getCameraCount() > 1)
            mView.setChangeCameraEnable(true);
        else
            mView.setChangeCameraEnable(false);

        getBroadcast().setAudioing(true);
        VhallSDK.getInstance().setLogEnable(false);
    }


    @Override
    public void onstartBtnClick() {
        if (isPublishing) {
            finishBroadcast();
        } else {
            if (getBroadcast().isAvaliable() && !isFinish) {
                startBroadcast();
            } else {
                initBroadcast();
            }
        }
    }

    @Override
    public void initBroadcast() {
        VhallSDK.getInstance().initBroadcast(param.id, param.token, "", getBroadcast(), new VhallSDK.RequestCallback() {
            @Override
            public void success() {
                isFinish = false;
                startBroadcast();
//                getChatHistory();
            }

            @Override
            public void failed(int errorCode, String reason) {
                mView.showMsg("initBroadcastFailed：" + reason);
            }
        });
    }

    @Override
    public void startBroadcast() {//发起直播
        getBroadcast().start();
    }

    @Override
    public void stopBroadcast() {//停止直播
        getBroadcast().stop();
    }

    @Override
    public void finishBroadcast() {
        VhallSDK.getInstance().finishBroadcast(param.id, param.token, getBroadcast(), new VhallSDK.RequestCallback() {
            @Override
            public void success() {
                Log.e(TAG, "finishSuccess");
                isFinish = true;
            }

            @Override
            public void failed(int errorCode, String reason) {
                Log.e(TAG, "finishFailed：" + reason);
            }
        });
    }

    @Override
    public void changeFlash() {
        getBroadcast().changeFlash();
    }

    @Override
    public void changeCamera() {
        int cameraId = getBroadcast().changeCamera();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mView.setChangeFlashEnable(true);
        } else {
            mView.setChangeFlashEnable(false);
        }
    }

    @Override
    public void changeAudio() {
        boolean isAudioRecord = getBroadcast().isAudioing();
        getBroadcast().setAudioing(!isAudioRecord);
    }


    @Override
    public void onPause() {
        if (isPublishing)
            stopBroadcast();
    }

    @Override
    public void onDestory() {
        getBroadcast().destory();
    }

    @Override
    public void onResume() {
        //异常中断（HOME/PHONE）返回，是否自动继续直播
//        if (!ispublishing && getBroadcast().isAvaliable())
//            getBroadcast().start();
    }

    private Broadcast getBroadcast() {
        if (broadcast == null) {
            Broadcast.Builder builder = new Broadcast.Builder()
                    .cameraView(mView.getCameraView())
                    .frameRate(param.frameRate)
                    .videoBitrate(param.videoBitrate)
                    .callback(new BroadcastEventCallback())
                    .chatCallback(new ChatCallback());
            broadcast = builder.build();
        }
        return broadcast;
    }

    @Override
    public void sendChat(String text) {
        if (TextUtils.isEmpty(text))
            return;
        getBroadcast().sendChat(text, new VhallSDK.RequestCallback() {
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
    }

    @Override
    public void onLoginReturn() {

    }


    private class BroadcastEventCallback implements Broadcast.BroadcastEventCallback {
        @Override
        public void onError(int errorCode, String reason) {
            mView.showMsg(reason);
        }

        @Override
        public void onStateChanged(int stateCode) {
            switch (stateCode) {
                case Broadcast.STATE_CONNECTED: /** 连接成功*/
                    mView.showMsg("连接成功!");
                    isPublishing = true;
                    mView.setStartBtnText("结束直播");
                    break;
                case Broadcast.STATE_NETWORK_OK: /** 网络通畅*/
                    mView.showMsg("网络通畅!");
                    break;
                case Broadcast.STATE_NETWORK_EXCEPTION: /** 网络异常*/
                    mView.showMsg("网络环境差!");
                    break;
                case Broadcast.STATE_STOP:
                    isPublishing = false;
                    mView.setStartBtnText("开始直播");
                    break;
            }
        }

        @Override
        public void uploadSpeed(String kbps) {
            mView.setSpeedText(kbps + "/kbps");
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
                    chatView.notifyDataChanged(chatInfo);
                    break;
                case ChatServer.eventOnlineKey:
                    chatView.notifyDataChanged(chatInfo);
                    break;
                case ChatServer.eventOfflineKey:
                    chatView.notifyDataChanged(chatInfo);
                    break;
                case ChatServer.eventQuestion:
                    break;
            }
        }

        @Override
        public void onChatServerClosed() {

        }
    }

    //TODO 是否去掉  发起直播是否需要聊天记录
    private void getChatHistory() {
        getBroadcast().acquireChatRecord(false, new ChatServer.ChatRecordCallback() {
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
