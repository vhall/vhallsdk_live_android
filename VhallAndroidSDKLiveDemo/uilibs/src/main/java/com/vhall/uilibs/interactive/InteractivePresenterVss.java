package com.vhall.uilibs.interactive;

import android.graphics.PixelFormat;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vhall.business.VhallSDK;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.InteractiveDataSource;
import com.vhall.business.data.source.WebinarInfoRepository;
import com.vhall.business.data.source.remote.WebinarInfoRemoteDataSource;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.util.handler.WeakHandler;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import vhall.com.vss.data.MessageData;
import vhall.com.vss.CallBack;
import vhall.com.vss.module.room.VssRoomManger;
import vhall.com.vss.module.room.callback.IVssMessageLister;
import vhall.com.vss.module.rtc.IVssRtcLister;
import vhall.com.vss.module.rtc.VssRtcManger;

/**
 * @author hkl
 */
public class InteractivePresenterVss implements InteractiveContract.InteractiveFraPresenter {
    private InteractiveContract.InteractiveActView interActView;
    private InteractiveContract.InteractiveFraView interFraView;

    private static final int CAMERA_VIDEO = 2;
    private static final int CAMERA_AUDIO = 1;
    private static final int CAMERA_DEVICE_OPEN = 1;
    private static final int CAMERA_DEVICE_CLOSE = 0;

    private static final String TAG = "InteractivePresenter";
    private VHRenderView vhRenderView;
    private Stream localStream;

    public InteractivePresenterVss(final InteractiveContract.InteractiveActView interActView, InteractiveContract.InteractiveFraView interFraView, Param param) {
        this.interActView = interActView;
        this.interActView.setPresenter(this);
        this.interFraView = interFraView;
        this.interFraView.setPresenter(this);
        VssRtcManger.getInstance(interActView.getContext()).setRtvLister(new MyVssLister());
        VssRoomManger.getInstance().setVssMessageLister(new MyCallBack(), IVssMessageLister.MESSAGE_SERVICE_TYPE_ROOM);
    }

    @Override
    public void start() {
        initInteractive();
    }

    @Override
    public void initInteractive() {
        try {
            JSONObject option = new JSONObject();
            option.put(Stream.kMinBitrateKbpsKey, 200);
            option.put(Stream.kCurrentBitrateKey, 400);
            option.put(Stream.kMaxBitrateKey, 600);
            option.put(Stream.kFrameResolutionTypeKey, Stream.VhallFrameResolutionValue.VhallFrameResolution480x360.getValue());
            option.put(Stream.kStreamOptionStreamType, Stream.VhallStreamType.VhallStreamTypeAudioAndVideo.getValue());
            option.put(Stream.kNumSpatialLayersKey, 2);
            VssRtcManger.getInstance(interActView.getContext())
                    .speak(option, "passsdk", new CallBack() {
                        @Override
                        public void onSuccess(Object result) {
                            setLocalView();
                        }

                        @Override
                        public void onError(int eventCode, String msg) {
                            Log.e(TAG, "speak onError" + msg);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "speak JSONException" + e.getMessage());
        }
    }

    @Override
    public void onDownMic() {
        interActView.finish();
    }

    @Override
    public void onSwitchCamera() {
        if (localStream != null) {
            localStream.switchCamera();
        }
    }

    @Override
    public void onSwitchVideo(boolean isOpen) {
        Log.e(TAG, "Video isOpen  " + isOpen);

        VssRtcManger.getInstance(interActView.getContext()).setDeviceStatus(String.valueOf(CAMERA_VIDEO), isOpen ? String.valueOf(CAMERA_DEVICE_OPEN) : String.valueOf(CAMERA_DEVICE_CLOSE), new CallBack() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onError(int eventCode, String msg) {
                interActView.showToast(msg);
            }
        });
    }

    @Override
    public void onSwitchAudio(boolean isOpen) {
        Log.e(TAG, "Audio isOpen  " + isOpen);
        VssRtcManger.getInstance(interActView.getContext()).setDeviceStatus(String.valueOf(CAMERA_AUDIO), isOpen ? String.valueOf(CAMERA_DEVICE_OPEN) : String.valueOf(CAMERA_DEVICE_CLOSE), new CallBack() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onError(int eventCode, String msg) {
                interActView.showToast(msg);
            }
        });
    }

    /**
     * 设置本地流
     */
    private void setLocalView() {
        vhRenderView = new VHRenderView(interActView.getContext());
        vhRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        vhRenderView.init(null, null);
        localStream = VssRtcManger.getLocalStream();
        vhRenderView.setStream(localStream);
        interFraView.addLocalView(vhRenderView);
    }

    class MyCallBack implements IVssMessageLister {
        @Override
        public void onMessage(MessageData msg) {
            if (msg == null || TextUtils.isEmpty(msg.getType())) {
                return;
            }
            switch (msg.getType()) {
                case "vrtc_frames_forbid":
                    interFraView.updateVideoFrame(CAMERA_DEVICE_CLOSE);
                    switchVideoFrame(CAMERA_DEVICE_CLOSE);
                    break;
                case "vrtc_frames_display":
                    interFraView.updateVideoFrame(CAMERA_DEVICE_OPEN);
                    switchVideoFrame(CAMERA_DEVICE_OPEN);
                    break;
                case "vrtc_mute":
                case "vrtc_mute_all":
                    interFraView.updateAudioFrame(CAMERA_DEVICE_CLOSE);
                    switchAudioFrame(CAMERA_DEVICE_CLOSE);
                    break;
                case "vrtc_mute_cancel":
                case "vrtc_mute_all_cancel":
                    interFraView.updateAudioFrame(CAMERA_DEVICE_OPEN);
                    switchAudioFrame(CAMERA_DEVICE_OPEN);
                    break;
                case "vrtc_disconnect_success":
                    onDownMic();
                    break;
                case "room_kickout":
                case "live_over":
                    onDownMic();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(int code, String msg) {

        }

    }

    private void switchVideoFrame(int status) {
        if (localStream == null) {
            return;
        }
        if (status == CAMERA_DEVICE_OPEN) {
            //1打开
            localStream.unmuteVideo(null);
        } else { // 0禁止
            localStream.muteVideo(null);
        }
    }

    private void switchAudioFrame(int status) {
        if (localStream == null) {
            return;
        }
        if (status == CAMERA_DEVICE_OPEN) {
            localStream.unmuteAudio(null);
        } else {
            localStream.muteAudio(null);
        }
    }

    class MyVssLister implements IVssRtcLister {

        /**
         * 有流加入并订阅成功
         *
         * @param stream
         */
        @Override
        public void addStream(final Stream stream) {
            if (stream == null) {
                return;
            }
            new WeakHandler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    boolean b = stream.hasVideo();
                    VHRenderView newRenderView = new VHRenderView(interActView.getContext());
                    newRenderView.init(null, null);
                    newRenderView.getHolder().setFormat(PixelFormat.TRANSPARENT);
                    newRenderView.setZOrderMediaOverlay(true);
                    newRenderView.setStream(stream);
                    interFraView.addStream(newRenderView);
                }
            });
        }

        /**
         * 有流退出或去掉订阅某路流后回调
         *
         * @param stream
         */
        @Override
        public void removeStream(final Stream stream) {
            if (stream == null) {
                return;
            }
            new WeakHandler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    interFraView.removeStream(stream);
                }
            });
        }

        /**
         * 更新流状态，有流的音视频状态发生改变
         *
         * @param stream
         */
        @Override
        public void updateStream(final Stream stream) {
            if (stream == null) {
                return;
            }
            Log.e("updateStream   id   ", stream.userId + "");
            new WeakHandler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    interFraView.updateStream(stream);
                }
            });
        }

        /**
         * 互动房间重连
         *
         * @param i  总重连次数
         * @param i1 当前重连次数
         */
        @Override
        public void onReconnect(int i, int i1) {

        }

        /**
         * 房间连接状态变化
         *
         * @param room
         * @param status
         */
        @Override
        public void roomStatusMessageChange(Room room, int status) {

        }

        /**
         * 房间内人数更新
         *
         * @param jsonObject
         */
        @Override
        public void onRefreshMembers(JSONObject jsonObject) {

        }

        /**
         * 房间内成员状态变化
         */
        @Override
        public void onRefreshMemberState() {

        }

        /**
         * 有流加入并重新开始混流操作
         * 可用于作为开启旁路直播判定
         *
         * @param jsonObject
         */
        @Override
        public void onStreamMixed(JSONObject jsonObject) {

        }

    }

    @Override
    public void onDestory() {
        VssRtcManger.leaveRoom();
    }
}
