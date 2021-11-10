package com.vhall.uilibs.broadcast;

import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.vhall.business.Broadcast;
import com.vhall.business.ChatServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.push.VHLivePushConfig;
import com.vhall.push.VHLivePushFormat;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.vhallrtc.client.VHRenderView;

import org.json.JSONObject;

import java.util.List;

/**
 * 发直播的Presenter
 */
public class BroadcastPresenter implements BroadcastContract.Presenter, ChatContract.ChatPresenter {
    private static final String TAG = "BroadcastPresenter";
    private Param param;
    private WebinarInfo webinarInfo;
    private BroadcastContract.View mView;
    private BroadcastContract.BroadcastView mBraodcastView;
    ChatContract.ChatView chatView;
    private Broadcast broadcast;
    private boolean isPublishing = false;
    private boolean isFinish = false;
    private boolean isFlashOpen = false;
    private int mode = VHLivePushFormat.DRAW_MODE_ASPECTFILL;


    public BroadcastPresenter(Param params, WebinarInfo webinarInfo, BroadcastContract.BroadcastView mBraodcastView, BroadcastContract.View mView, ChatContract.ChatView chatView) {
        this.param = params;
        this.webinarInfo = webinarInfo;
        this.mView = mView;
        this.mBraodcastView = mBraodcastView;
        this.chatView = chatView;
        this.chatView.setPresenter(this);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        /**
         * （默认预览分辨率640*480）
         *  播放器配置更改了 分辨率设置必需保证进入画面前播放器已经初始化
         *  否则可能出现因采集画面大小和推流大小不一致造成观看端花屏问题
         *
         */
        getBroadcast();

    }

//    @Override
//    public void initCameraView() {
//        VHLivePushConfig config = new VHLivePushConfig(param.pixel_type);
//        config.screenOri = param.screenOri;
//        mView.getCameraView().init(config);
//    }


    @Override
    public void onstartBtnClick() {
        if (isPublishing) {
//            finishBroadcast();
            mView.showEndLiveDialog();
        } else {
            if(getBroadcast().isAvaliable() && !isFinish){
                getBroadcast().start();
            }else{
                initBroadcast();
            }
        }
    }

    @Override
    public void initBroadcast() {
        if (webinarInfo != null && !getBroadcast().isAvaliable()) {
            getBroadcast().setWebinarInfo(webinarInfo);
            getBroadcast().start();
            getBroadcast().acquireChatRecord(true, new ChatServer.ChatRecordCallback() {
                @Override
                public void onDataLoaded(List<ChatServer.ChatInfo> list) {

                }

                @Override
                public void onFailed(int errorcode, String messaage) {

                }
            });
        } else {
            VhallSDK.initBroadcast(param.broId, param.broToken,param.broName, getBroadcast(), new RequestCallback() {
                @Override
                public void onSuccess() {
                    isFinish = false;
                    getBroadcast().start();
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    mView.showMsg("initBroadcastFailed：" + errorMsg);
                }
            });
        }
    }

    @Override
    public void startBroadcast() {//发起直播
        getBroadcast().start();
    }

    @Override
    public void stopBroadcast() {//停止直播
        if (isPublishing) {
            getBroadcast().stop();
        }
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
        VhallSDK.finishBroadcast(broId, broToken, getBroadcast(), new RequestCallback() {
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
        isFlashOpen = getBroadcast().changeFlash(!isFlashOpen);
        mView.setFlashBtnImage(isFlashOpen);
    }

    @Override
    public void changeMode() {
        if (mode == VHLivePushFormat.DRAW_MODE_ASPECTFILL) {
            getBroadcast().changeMode(VHLivePushFormat.DRAW_MODE_ASPECTFIT);
            mode = VHLivePushFormat.DRAW_MODE_ASPECTFIT;
            mView.setModeText("FIT");
        } else if (mode == VHLivePushFormat.DRAW_MODE_ASPECTFIT) {
            getBroadcast().changeMode(VHLivePushFormat.DRAW_MODE_NONE);
            mode = VHLivePushFormat.DRAW_MODE_NONE;
            mView.setModeText("NONE");
        } else if (mode == VHLivePushFormat.DRAW_MODE_NONE) {
            getBroadcast().changeMode(VHLivePushFormat.DRAW_MODE_ASPECTFILL);
            mode = VHLivePushFormat.DRAW_MODE_ASPECTFILL;
            mView.setModeText("FILL");
        }
    }

    @Override
    public void changeCamera() {
        int cameraId = getBroadcast().changeCamera();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mView.setFlashBtnEnable(true);
        } else {
            mView.setFlashBtnEnable(false);
        }
    }

    @Override
    public void changeAudio() {
        boolean isMute = getBroadcast().isMute();
        getBroadcast().setMute(!isMute);
        mView.setAudioBtnImage(!isMute);
    }

    @Override
    public void destroyBroadcast() {
        finishBroadcast();
        getBroadcast().destroy();
    }

    @Override
    public void setVolumeAmplificateSize(float size) {
        getBroadcast().setVolumeAmplificateSize(size);
    }

    @Override
    public void setRenderView(VHRenderView vhRenderView) {
        //无延迟使用
    }

    @Override
    public void onResume() {
        //无延迟使用
    }


    @Override
    public void onPause() {
        //无延迟使用
    }

    private Broadcast getBroadcast() {
        if (broadcast == null) {
            VHLivePushConfig config = new VHLivePushConfig(param.pixel_type);
            Log.e("onCreate", "param.screenOri    " + param.screenOri);
            config.screenOri = param.screenOri;//横竖屏设置 重要
            //可不设置
            config.videoFrameRate = param.videoFrameRate;//帧率
            config.videoBitrate = param.videoBitrate;//码率
            //2 音频直播
            if (webinarInfo.layout == 2||webinarInfo.layout == 4) {
                config.streamType = VHLivePushFormat.STREAM_TYPE_A;
                mView.getCameraView().setVisibility(View.GONE);
            } else {
                config.streamType = VHLivePushFormat.STREAM_TYPE_AV;
            }
            Broadcast.Builder builder = new Broadcast.Builder()
                    .cameraView(mView.getCameraView())
                    .config(config)
                    .callback(listener)
                    .callback(new BroadcastEventCallback())
                    .chatCallback(new ChatCallback());
            //狄拍
//            LiveParam.PushParam param = new LiveParam.PushParam();
//            param.video_width = 1280;
//            param.video_height = 720;
//            Broadcast.Builder builder = new Broadcast.Builder()
//                    .stream(true)
//                    .param(param)
//                    .callback(new BroadcastEventCallback())
//                    .chatCallback(new ChatCallback());
            broadcast = builder.build();
        }

        return broadcast;
    }
    private VHPlayerListener listener = new VHPlayerListener() {
        @Override
        public void onStateChanged(Constants.State state) {

        }

        @Override
        public void onEvent(int event, String msg) {

        }

        @Override
        public void onError(int errorCode, int innerErrorCode, String msg) {
            chatView.showToast(msg);
        }
    };
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
        getBroadcast().sendChat(String.valueOf(text), new RequestCallback() {
            @Override
            public void onSuccess() {
                response++;
                Log.e(TAG, "响应成功：" + response);
            }

            @Override
            public void onError(int errorCode, String reason) {
                chatView.showToast(reason);
                response++;
                Log.e(TAG, "响应失败：" + reason + "count:" + response);
            }
        });
    }

    @Override
    public void sendCustom(JSONObject text) {
        getBroadcast().sendCustom(text, new RequestCallback() {
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


    private class BroadcastEventCallback implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case START:
                    mView.showMsg("连接成功!");
                    isPublishing = true;
                    mView.setStartBtnImage(false);

                    //start push data for 狄拍
                    /**
                     * push音频数据
                     *
                     * @param data      音频数据（aac编码的数据）注意要先传音频头
                     * @param size      音频数据大小
                     * @param type      数据类型 0代表视频头 1代表音频头 2代表音频数据 3代表I帧 4代表p帧 5代表b帧
                     * @param timestamp 音频时间戳 单位MS
                     * @return 0是成功，非0是失败
                     */
//                    getBroadcast().PushAACDataTs();
                    /**
                     * push视频数据
                     *
                     * @param data      视屏数据(h264编码的数据) 注意要先传视频头
                     * @param size      视频数据的大小
                     * @param type      数据类型 0代表视频头 1代表音频头 2代表音频数据 3代表I帧 4代表p帧 5代表b帧
                     * @param timestamp 视频时间戳 单位MS
                     * @return 0是成功，非0是失败
                     */
//                    getBroadcast().PushH264DataTs();
                    break;
                case STOP:
                    isPublishing = false;
                    mView.setStartBtnImage(true);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int eventCode, String eventMsg) {
            switch (eventCode) {
                case Constants.Event.EVENT_UPLOAD_SPEED:
                    mView.setSpeedText(eventMsg + "/kbps");
                    break;
                case Constants.Event.EVENT_NETWORK_UNOBS:
                    mView.showMsg("网络通畅!");
                    break;
                case Constants.Event.EVENT_NETWORK_OBS:
                    mView.showMsg("网络环境差!");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(int errorCode, int i1, String reason) {
            if (errorCode == Broadcast.ERROR_CONNECTE) {
                Log.e(TAG, "broadcast error, reason:" + reason);
            }
            mView.showMsg(reason);
        }
    }


    private class ChatCallback implements ChatServer.Callback {
        @Override
        public void onChatServerConnected() {

        }

        @Override
        public void onConnectFailed() {
            getBroadcast().connectChatServer();
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
