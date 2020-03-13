package com.vhall.uilibs.broadcast;

import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;

import com.vhall.business.Broadcast;
import com.vhall.business.MessageServer;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.utils.LogManager;
import com.vhall.ims.VHIM;
import com.vhall.lss.push.VHLivePusher;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.push.VHAudioCapture;
import com.vhall.push.VHLivePushConfig;
import com.vhall.push.VHLivePushFormat;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.util.emoji.InputUser;

import org.json.JSONObject;

import vhall.com.vss.data.MessageData;
import vhall.com.vss.data.ResponseRoomInfo;
import vhall.com.vss.data.VssMessageChatData;
import vhall.com.vss.CallBack;
import vhall.com.vss.module.room.VssRoomManger;
import vhall.com.vss.module.room.callback.IVssMessageLister;

/**
 * 发直播的Presenter
 *
 * @author hkl
 */
public class BroadcastPresenterVss implements BroadcastContract.Presenter, ChatContract.ChatPresenter {
    private static final String TAG = "BroadcastPresenter";
    private Param param;
    private BroadcastContract.View mView;
    private BroadcastContract.BroadcastView mBroadcastView;
    ChatContract.ChatView chatView;
    private boolean isPublishing = false;
    private boolean isFlashOpen = false;
    private VHLivePusher pusher;
    private String paasToken;
    private VHAudioCapture mAudioCapture;

    private int mode = VHLivePushFormat.DRAW_MODE_ASPECTFILL;

    public BroadcastPresenterVss(Param params, BroadcastContract.BroadcastView mBraodcastView, final BroadcastContract.View mView, ChatContract.ChatView chatView) {
        this.param = params;
        this.mView = mView;
        this.mBroadcastView = mBraodcastView;
        this.chatView = chatView;
        this.chatView.setPresenter(this);
        mView.setPresenter(this);
        VssRoomManger.getInstance().enterRoom(param.vssToken, param.vssRoomId, new CallBack<ResponseRoomInfo>() {
            @Override
            public void onSuccess(ResponseRoomInfo result) {
                paasToken = result.getPaas_access_token();
                VssRoomManger.getInstance().setVssMessageLister(new MyLister(), IVssMessageLister.MESSAGE_SERVICE_TYPE_ALL);
            }

            @Override
            public void onError(int eventCode, String msg) {
                mView.showMsg("initBroadcastFailed：" + msg);

            }
        });
    }

    @Override
    public void start() {
        getPush();
    }

    @Override
    public void onstartBtnClick() {
        startBroadcast();
    }

    @Override
    public void initBroadcast() {
    }

    @Override
    public void startBroadcast() {//发起直播
        if (getPush().getState() == Constants.State.START) {
            getPush().pause();
            VssRoomManger.getInstance().roomEndLive(new CallBack<String>() {
                @Override
                public void onSuccess(String result) {

                }

                @Override
                public void onError(int eventCode, String msg) {
                    mView.showMsg("roomEndLive：" + msg);
                }
            });
        } else {
            if (getPush().resumeAble()) {
                getPush().resume();
            } else {
                getPush().start(param.vssRoomId, paasToken);
            }
            VssRoomManger.getInstance().roomStartLive(new CallBack<String>() {
                @Override
                public void onSuccess(String result) {

                }

                @Override
                public void onError(int eventCode, String msg) {
                    mView.showMsg("roomStartLive：" + msg);
                }
            });
        }
    }

    @Override
    public void stopBroadcast() {//停止直播
        if (isPublishing) {
            getPush().stop();
        }
    }

    @Override
    public void finishBroadcast() {
    }

    /**
     * 切换闪光灯
     *
     * @return boolean
     */
    private boolean changeFlash(boolean isOpen) {
        if (mView.getCameraView() != null) {
            return mView.getCameraView().changeFlash(isOpen);
        }
        return false;
    }

    @Override
    public void changeFlash() {
        isFlashOpen = changeFlash(!isFlashOpen);
        mView.setFlashBtnImage(isFlashOpen);
    }

    @Override
    public void changeMode() {
        if (mView.getCameraView() == null) {
            return;
        }
        if (mode == VHLivePushFormat.DRAW_MODE_ASPECTFILL) {
            mode = VHLivePushFormat.DRAW_MODE_ASPECTFIT;
            mView.setModeText("FIT");
        } else if (mode == VHLivePushFormat.DRAW_MODE_ASPECTFIT) {
            mode = VHLivePushFormat.DRAW_MODE_NONE;
            mView.setModeText("NONE");
        } else if (mode == VHLivePushFormat.DRAW_MODE_NONE) {
            mode = VHLivePushFormat.DRAW_MODE_ASPECTFILL;
            mView.setModeText("FILL");
        }
        mView.getCameraView().setCameraDrawMode(mode);
    }

    /**
     * 切换摄像头
     *
     * @return camerId
     */
    private int changeCamera1() {
        int id = -1;
        if (Camera.getNumberOfCameras() <= 1) {
            LogManager.e(TAG, "device has only one camera...");
            return -1;
        }
        if (mView.getCameraView() != null) {
            id = mView.getCameraView().switchCamera();
        }
        return id;
    }

    @Override
    public void changeCamera() {
        int cameraId = changeCamera1();
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
        boolean isMute = mAudioCapture.isEnable();
        mAudioCapture.setEnable(!isMute);
        mView.setAudioBtnImage(!isMute);
    }

    @Override
    public void destroyBroadcast() {
        VssRoomManger.getInstance().roomEndLive(new CallBack<String>() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(int eventCode, String msg) {
                mView.showMsg("roomStartLive：" + msg);
            }
        });
        if (pusher != null) {
            pusher.release();
        }
        VssRoomManger.leaveRoom();
    }

    @Override
    public void setVolumeAmplificateSize(float size) {
        getPush().setVolumeAmplificateSize(size);
    }

    private VHLivePusher getPush() {
        if (pusher == null) {
            mAudioCapture = new VHAudioCapture();
            VHLivePushConfig config = new VHLivePushConfig(param.pixel_type);
            config.screenOri = param.screenOri;
            //可不设置
            config.videoFrameRate = param.videoFrameRate;
            config.videoBitrate = param.videoBitrate;
            pusher = new VHLivePusher(mView.getCameraView(), mAudioCapture, config);
            pusher.setListener(new VHPlayerListener() {
                @Override
                public void onStateChanged(Constants.State state) {
                    switch (state) {
                        case START:
                            mView.showMsg("连接成功!");
                            isPublishing = true;
                            mView.setStartBtnImage(false);
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
            });
        }

        return pusher;
    }

    @Override
    public void showChatView(boolean emoji, InputUser user, int limit) {
        mBroadcastView.showChatView(emoji, user, limit);
    }

    private int request = 0;
    private int response = 0;

    @Override
    public void sendChat(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        request++;
        Log.e(TAG, "请求：" + request);
        VssRoomManger.getInstance().sendMsg(text, "", new CallBack() {
            @Override
            public void onSuccess(Object result) {
                response++;
                Log.e(TAG, "响应成功：" + response);
            }

            @Override
            public void onError(int eventCode, String msg) {
                response++;
                Log.e(TAG, "响应失败：" + msg + "count:" + response);
            }
        });
    }

    @Override
    public void sendCustom(JSONObject text) {
        VssRoomManger.getInstance().sendMsg(text.toString(), VHIM.TYPE_CUSTOM, new CallBack() {
            @Override
            public void onSuccess(Object result) {
                response++;
                Log.e(TAG, "响应成功：" + response);
            }

            @Override
            public void onError(int eventCode, String msg) {
                response++;
                Log.e(TAG, "响应失败：" + msg + "count:" + response);
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

    class MyLister implements IVssMessageLister {
        @Override
        public void onMessage(MessageData msg) {
            //解析
            if (msg == null || TextUtils.isEmpty(msg.getType())) {
                return;
            }
            switch (msg.getType()) {
                case "Join":
                    JSONObject joinObject = (JSONObject) msg.getT();
                    MessageChatData join = new MessageChatData();
                    join.event = MessageChatData.eventOnlineKey;
                    join.setTime(joinObject.optString("time"));
                    join.setNickname(joinObject.optString("name"));
                    chatView.notifyDataChangedChat(join);
                    break;
                case "Leave":
                    JSONObject leaveObject = (JSONObject) msg.getT();
                    MessageChatData leave = new MessageChatData();
                    leave.setTime(leaveObject.optString("time"));
                    leave.setNickname(leaveObject.optString("name"));
                    leave.event = MessageChatData.eventOfflineKey;
                    chatView.notifyDataChangedChat(leave);
                    break;
                case "service_im":
                case "service_custom":
                    //聊天消息
                    VssMessageChatData messageChatData = (VssMessageChatData) msg.getT();
                    MessageChatData data = new MessageChatData();
                    if (messageChatData != null) {
                        data.setType(messageChatData.getType());
                        data.setAvatar(messageChatData.getAvatar());
                        data.setNickname(messageChatData.getNickname());
                        data.setMy(messageChatData.isMy());
                        data.setUserId(messageChatData.getUserId());
                        data.setTime(messageChatData.getTime());
                        data.event = messageChatData.event;
                        data.setRoom_id(messageChatData.getRoom_id());
                        data.setText_content(messageChatData.getText_content());
                        data.setImage_urls(messageChatData.getImage_urls());
                        data.setImage_url(messageChatData.getImage_url());
                        chatView.notifyDataChangedChat(data);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(int code, String msg) {
            Log.e(TAG, msg);
        }
    }
}
