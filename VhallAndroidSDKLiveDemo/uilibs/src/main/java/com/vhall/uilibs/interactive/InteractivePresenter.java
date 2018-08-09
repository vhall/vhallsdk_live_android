package com.vhall.uilibs.interactive;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.vhall.business.InterActive;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.uilibs.Param;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

import org.json.JSONException;

public class InteractivePresenter implements InteractiveContract.InteractiveFraPresenter {
    private InteractiveContract.InteractiveActView interActView;
    private InteractiveContract.InteractiveFraView interFraView;

    public static final int CAMERA_VIDEO = 2; //摄像头
    public static final int CAMERA_AUDIO = 1; //麦克风
    public static final int CAMERA_DEVICE_OPEN = 1;
    public static final int CAMERA_DEVICE_CLOSE = 0;

    private static final String TAG = "InteractivePresenter";
    private InterActive interactive;
    private VHRenderView vhRenderView;
    private Param mParam;

    public InteractivePresenter(InteractiveContract.InteractiveActView interActView, InteractiveContract.InteractiveFraView interFraView, Param param) {
        this.interActView = interActView;
        this.interActView.setPresenter(this);
        this.interFraView = interFraView;
        this.interFraView.setPresenter(this);
        this.mParam = param;
    }

    @Override
    public void start() {
        initInteractive();
    }

    @Override
    public void initInteractive() {
        VhallSDK.initInteractive(mParam.watchId, "", "", "", getInteractive(), new RequestCallback() {
            @Override
            public void onSuccess() {
                getInteractive().setDefinition(mParam.interactive_definition);
                setLocalView();
            }

            @Override
            public void onError(int errorCode, String msg) {
                Toast.makeText(interActView.getContext(), "" + msg, Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onDownMic() {
        getInteractive().unpublish(new RequestCallback() {
            @Override
            public void onSuccess() {
                interActView.finish();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void onSwitchCamera() {
        getInteractive().switchCamera();
    }

    @Override
    public void onSwitchVideo(boolean isOpen) {
        getInteractive().switchDevice(CAMERA_VIDEO, isOpen ? CAMERA_DEVICE_OPEN : CAMERA_DEVICE_CLOSE, new RequestCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Toast.makeText(interActView.getContext(), "onError " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSwitchAudio(boolean isOpen) {
        getInteractive().switchDevice(CAMERA_AUDIO, isOpen ? CAMERA_DEVICE_OPEN : CAMERA_DEVICE_CLOSE, new RequestCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Toast.makeText(interActView.getContext(), "onError " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLocalView() {
        vhRenderView = new VHRenderView(interActView.getContext());
        vhRenderView.setScalingMode(VHRenderView.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
        vhRenderView.init(getInteractive().getEglBase().getEglBaseContext(), null);
        getInteractive().setLocalView(vhRenderView, Stream.VhallStreamType.VhallStreamTypeAudioAndVideo);
        interFraView.addLocalView(vhRenderView);
        getInteractive().enterRoom();
    }

    public InterActive getInteractive() {
        if (interactive == null) {
            InterActive.Builder builder = new InterActive.Builder()
                    .context(interActView.getContext())
                    .messageCallback(new MessageEventCallback())
                    .roomCallback(new RoomCallback());
            interactive = builder.build();
        }
        return interactive;
    }

    /**
     * 观看过程消息监听
     */
    private class MessageEventCallback implements MessageServer.Callback {
        @Override
        public void onEvent(MessageServer.MsgInfo messageInfo) {
            Log.e(TAG, "messageInfo " + messageInfo.event);
            switch (messageInfo.event) {
                case MessageServer.EVENT_DISABLE_CHAT://禁言
                    break;
                case MessageServer.EVENT_KICKOUT://踢出
                    break;
                case MessageServer.EVENT_OVER://直播结束
                    if (interactive != null) {
                        interactive.unpublish();
                        interActView.finish();
                    }
                    break;
                case MessageServer.EVENT_PERMIT_CHAT://解除禁言
                    break;
                case MessageServer.EVENT_SWITCH_DEVICE:
                    if (messageInfo.device == CAMERA_AUDIO) { // 麦克风
                        switchAudioFrame(messageInfo.status);
                        interFraView.updateAudioFrame(messageInfo.status);
                    } else { //2摄像头
                        switchVideoFrame(messageInfo.status);
                        interFraView.updateVideoFrame(messageInfo.status);
                    }
                    break;
                case MessageServer.EVENT_INTERACTIVE_DOWN_MIC:
                    if (interactive != null) {
                        interactive.unpublish();
                        interActView.finish();
                    }
                    break;
            }
        }

        @Override
        public void onMsgServerConnected() {

        }

        @Override
        public void onConnectFailed() {
        }

        @Override
        public void onMsgServerClosed() {

        }
    }

    public void switchVideoFrame(int status) {
        try {
            if (status == CAMERA_DEVICE_OPEN) { //1打开
                interactive.getLocalStream().unmuteVideo();
            } else { // 0禁止
                interactive.getLocalStream().muteVideo();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void switchAudioFrame(int status) {
        try {
            if (status == CAMERA_DEVICE_OPEN) {
                interactive.getLocalStream().unmuteAudio();
            } else {
                interactive.getLocalStream().muteAudio();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class RoomCallback implements InterActive.RoomCallback {

        @Override
        public void onDidConnect() {//进入房间
            if (interactive != null) {
                interactive.publish();
            }
        }

        @Override
        public void onDidError() {//进入房间失败

        }

        @Override
        public void onDidPublishStream() {// 上麦
            Log.e(TAG, "onDidPublishStream");
            interActView.setSpeakerphoneOn(true);
        }

        @Override
        public void onDidUnPublishStream() {//下麦
            Log.e(TAG, "onDidUnPublishStream");
        }

        @Override
        public void onDidSubscribeStream(Stream stream, final VHRenderView newRenderView) {
            Log.e(TAG, "onDidSubscribeStream");
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    interActView.setSpeakerphoneOn(true);
                    newRenderView.getHolder().setFormat(PixelFormat.TRANSPARENT);
                    newRenderView.setZOrderMediaOverlay(true);
                    newRenderView.setScalingMode(VHRenderView.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                    interFraView.addStream(newRenderView);
                }
            });
        }

        @Override
        public void onDidRoomStatus(Room room, Room.VHRoomStatus vhRoomStatus) {
            switch (vhRoomStatus) {
                case VHRoomStatusDisconnected:// 异常退出
                    interActView.finish();
                    break;
                case VHRoomStatusError:
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
        public void onDidRemoveStream(Room room, Stream stream) {
            Log.e(TAG, "onDidRemoveStream");
            interFraView.removeStream(stream);
        }
    }


    @Override
    public void onDestory() {
        onDownMic();
        getInteractive().leaveRoom();
        getInteractive().onDestory();
        interactive = null;
    }

}
