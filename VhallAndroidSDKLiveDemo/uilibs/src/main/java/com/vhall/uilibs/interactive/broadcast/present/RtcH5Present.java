package com.vhall.uilibs.interactive.broadcast.present;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.interactive.broadcast.config.RtcConfig;
import com.vhall.uilibs.interactive.dialog.OutDialog;
import com.vhall.uilibs.interactive.dialog.OutDialogBuilder;
import com.vhall.uilibs.interactive.dialog.OutTwoTitleDialog;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.UserManger;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.uilibs.util.emoji.InputView;
import com.vhall.uilibs.util.emoji.KeyBoardManager;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhss.CallBack;
import com.vhall.vhss.data.WebinarInfoData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.vhall.business.MessageServer.EVENT_CHAT_FORBID_ALL;
import static com.vhall.business.MessageServer.EVENT_DISABLE_CHAT;
import static com.vhall.business.MessageServer.EVENT_INTERACTIVE_DOWN_MIC;
import static com.vhall.business.MessageServer.EVENT_KICKOUT;
import static com.vhall.business.MessageServer.EVENT_KICKOUT_RESTORE;
import static com.vhall.business.MessageServer.EVENT_PERMIT_CHAT;
import static com.vhall.business.MessageServer.EVENT_VRTC_CONNECT_REFUSED;
import static com.vhall.business.MessageServer.EVENT_VRTC_CONNECT_SUCCESS;
import static com.vhall.business.MessageServer.EVENT_VRTC_SPEAKER_SWITCH;

public class RtcH5Present implements IBroadcastContract.IBroadcastPresent {
    private static final String TAG = "BroadcastPresent";

    private IBroadcastContract.IBroadcastView broadcastView;
    private WebinarInfo mWebinarInfo;
    private WebinarInfoData responseRoomInfo;
    private InputView inputView;
    private Activity activity;
    private static final int CAMERA_VIDEO = 2;
    private static final int CAMERA_AUDIO = 1;
    private static final int CAMERA_DEVICE_OPEN = 1;
    private static final int CAMERA_DEVICE_CLOSE = 0;
    private OutDialog showInvited;
    private boolean canSpeak = true;
    private IBroadcastContract.RtcFragmentView mRtcFragmentView;
    /**
     * 嘉宾有没有上麦
     */
    private boolean isPublic = false;

    public RtcH5Present(IBroadcastContract.IBroadcastView broadcastView) {
        this.broadcastView = broadcastView;
        activity = broadcastView.getActivity();
    }

    @Override
    public void setRtcFragmentView(IBroadcastContract.RtcFragmentView rtcFragmentView) {
        this.mRtcFragmentView = rtcFragmentView;
    }

    //聊天消息
    class ChatCallback implements ChatServer.Callback, MessageServer.MessageSupportMsgFilterOther {
        @Override
        public void onChatServerConnected() {

        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
            refreshUserList(chatInfo);
            if (broadcastView != null) {
                int pv = 1;
                int uv = 1;
                if (chatInfo.onlineData != null) {
                    pv = chatInfo.onlineData.attend_count;
                    uv = chatInfo.onlineData.concurrent_user;
                    broadcastView.showLookNum(pv, uv);
                }
            }
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    broadcastView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onChatServerClosed() {

        }

        @Override
        public boolean isFilterOther() {
            return false;
        }
    }

    //自定义消息
    class MessageCallback implements MessageServer.Callback, MessageServer.MessageSupportMsgFilterOther {
        @Override
        public void onEvent(MessageServer.MsgInfo msg) {

            if (msg == null) {
                return;
            }
            refreshUserList(msg);

            switch (msg.event) {
                case MessageServer.EVENT_RESTART: {
                    //直播开始
                    if (UserManger.isHost(mWebinarInfo.getWebinarInfoData().getJoin_info().getRole_name())) {
                        RtcConfig.getInterActive().setEnableBeautify(true);
                    } else {
                        broadcastView.showToast("直播开始");
                    }
                }
                break;

                case MessageServer.EVENT_OVER: {
                    //直播已结束
                    if (!UserManger.isHost(responseRoomInfo.getJoin_info().getRole_name())) {
                        broadcastView.showToast("直播已结束");
                        activity.finish();
                    } else {
                        broadcastView.forbidBroadcast();
                    }
                }
                break;

                case MessageServer.EVENT_INTERACTIVE_ALLOW_HAND: {
                    broadcastView.showToast(msg.status == 1 ? "允许用户上麦" : "不允许用户上麦");
                }
                break;
                case MessageServer.EVENT_CONNECT_INVITE_REFUSED: {
                    if (UserManger.isHost(responseRoomInfo.getJoin_info().getRole_name())) {
                        broadcastView.showToast(String.format("%s拒绝了您的邀请", msg.nick_name));
                    }
                }
                break;
                //互动举手消息
                case MessageServer.EVENT_INTERACTIVE_HAND: {
                    if (msg.status == 1) {
                        try {
                            String name = msg.nick_name;
                            name = BaseUtil.getLimitString(name);
                            userId = msg.user_id;
                            if (TextUtils.isEmpty(userId)) {
                                userId = msg.roomJoinId;
                            }
                            if (UserManger.isHost(responseRoomInfo.getJoin_info().getRole_name()) && !TextUtils.equals(responseRoomInfo.getJoin_info().getThird_party_user_id(), userId)) {
                                showInvitedView(name, userId);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (msg.status == 0) {
                        cancelRtcDialog();
                    }
                }
                break;


                case MessageServer.EVENT_KICKOUT: {
                    broadcastView.showToast("您已被踢出");
                    activity.finish();
                }
                break;


                case EVENT_VRTC_CONNECT_REFUSED: {
                    userId = msg.user_id;
                    if (TextUtils.equals(responseRoomInfo.getJoin_info().getThird_party_user_id(), userId)) {
                        broadcastView.showToast("主持人拒绝了您的上麦申请");
                        broadcastView.setMic(false);
                    }
                }
                break;

                case MessageServer.EVENT_DISABLE_CHAT: {
                    userId = msg.targetId;
                    if (UserManger.isHost(responseRoomInfo.getJoin_info().getRole_name())) {
                        break;
                    }
                    if (TextUtils.equals(responseRoomInfo.getJoin_info().getThird_party_user_id(), userId)) {
                        if (isPublic) {
                            mRtcFragmentView.noSpeak();
                        }
                        broadcastView.showToast("您已被禁言");
                        canSpeak = false;
                    }

                }
                break;

                case MessageServer.EVENT_PERMIT_CHAT: {
                    userId = msg.targetId;
                    if (UserManger.isHost(responseRoomInfo.getJoin_info().getRole_name())) {
                        break;
                    }
                    if (TextUtils.equals(responseRoomInfo.getJoin_info().getThird_party_user_id(), userId)) {
                        broadcastView.showToast("您被取消禁言");
                        canSpeak = true;
                    }
                }
                break;

                case MessageServer.EVENT_INVITED_MIC: {
                    //被邀请上麦
                    if (showInvited == null) {
                        showInvited = new OutDialogBuilder()
                                .title("主持人邀请您上麦，是否同意？")
                                .tv1("拒绝")
                                .tv2("同意")
                                .onCancel(new OutDialog.ClickLister() {
                                    @Override
                                    public void click() {
                                        RtcConfig.getInterActive().rejectInvite(new SimpleRequestCallback());
                                    }
                                })
                                .onConfirm(new OutDialog.ClickLister() {
                                    @Override
                                    public void click() {
                                        RtcConfig.getInterActive().agreeInvite(new RequestCallback() {
                                            @Override
                                            public void onSuccess() {
                                                RtcConfig.getInterActive().publish();
                                            }

                                            @Override
                                            public void onError(int eventCode, String msg) {
                                                broadcastView.showToast(msg);
                                            }
                                        });
                                    }
                                })
                                .build(activity);
                    }
                    showInvited.show();
                }
                break;
                //切换设备
                case MessageServer.EVENT_SWITCH_DEVICE:
                    userId = msg.targetId;
                    if (msg.device == CAMERA_AUDIO) { // 麦克风
                        broadcastView.refreshStream(userId, msg.status, -1);
                    } else {
                        broadcastView.refreshStream(userId, -1, msg.status);
                    }
                    if (TextUtils.equals(responseRoomInfo.getJoin_info().getThird_party_user_id(), userId)) {
                        //切换自己本身设备的摄像头和麦克风
                        //更新UI
                        if (msg.device == CAMERA_AUDIO) { // 麦克风
                            switchAudioFrame(msg.status);
                        } else {
                            switchVideoFrame(msg.status);
                        }
                    }
                    break;
                case MessageServer.EVENT_INTERACTIVE_ALLOW_MIC:
                    //接收上麦消息
                    RtcConfig.getInterActive().publish();
                    break;
                case MessageServer.EVENT_VRTC_CONNECT_SUCCESS:
                    //用户上麦成功
                    if (UserManger.isHost(responseRoomInfo.getJoin_info().getRole_name())) {
                        break;
                    }
                    try {
                        userId = msg.roomJoinId;
                        if (!TextUtils.isEmpty(userId) && TextUtils.equals(responseRoomInfo.getJoin_info().getThird_party_user_id(), userId)) {
                            isPublic = true;
                            broadcastView.setMic(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MessageServer.EVENT_INTERACTIVE_DOWN_MIC:
                    //接收下麦消息
                    try {
                        userId = msg.user_id;
                        String name = msg.nick_name;
                        String roomJoinId = msg.roomJoinId;
                        if (!TextUtils.isEmpty(userId)){
                            broadcastView.userNoSpeaker(userId);
                        }
                        if (!TextUtils.isEmpty(userId) && TextUtils.equals(responseRoomInfo.getJoin_info().getThird_party_user_id(), userId)) {
                            //主播下麦用户/嘉宾
                            RtcConfig.getInterActive().unpublished();
                            if (!UserManger.isHost(responseRoomInfo.getJoin_info().getRole_name())) {
                                broadcastView.setMic(false);
                            }
                            isPublic = false;
                            if (!userId.equals(roomJoinId)) {
                                broadcastView.showToast("您已被主持人下麦");
                            }
                        } else {
                            broadcastView.showToast(name + "已下麦");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case EVENT_VRTC_SPEAKER_SWITCH:
                    //互动设置为主讲人
                    broadcastView.updateMain(msg.roomJoinId);
//                    if (UserManger.isHost(responseRoomInfo.getJoin_info().getRole_name())) {
//                        mInterActive.setMainSpeaker(responseRoomInfo.interact.room_id, msg.roomJoinId,null);
//                    }
                    if (TextUtils.equals(msg.roomJoinId, responseRoomInfo.getJoin_info().getThird_party_user_id())) {
                        broadcastView.showToast("您已被设为主讲人");
                        return;
                    }
                    broadcastView.showToast(BaseUtil.getLimitString(msg.roomJoinId) + "已被设为主讲人");
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

        @Override
        public boolean isFilterOther() {
            //是否过滤他人消息
            return false;
        }
    }

    private void cancelRtcDialog() {
        if (mRtcDialogMap.containsKey(userId)) {
            Dialog dialog = mRtcDialogMap.remove(userId);
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    //刷新用户列表
    private void refreshUserList(MessageServer.MsgInfo msg) {
        if (broadcastView != null && sRequireRefreshEvents.contains(msg.event)) {
            broadcastView.refreshUserList();
        }
    }

    public void refreshUserList(ChatServer.ChatInfo chatInfo) {
        if (broadcastView != null && sRequireRefreshActions.contains(chatInfo.event)) {
            broadcastView.refreshUserList();
        }
    }


    //自定义消息监听
    private MessageCallback mMessageCallback = new MessageCallback();

    //聊天消息监听
    private ChatCallback mChatCallback = new ChatCallback();

    @Override
    public MessageCallback getMessageCallback() {
        return mMessageCallback;
    }

    @Override
    public ChatServer.Callback getChatCallback() {
        return mChatCallback;
    }

    //    @Override
    public void init(WebinarInfo webinarInfo) {
        this.mWebinarInfo = webinarInfo;
        if (mWebinarInfo != null) {
            this.responseRoomInfo = webinarInfo.getWebinarInfoData();
            canSpeak = 0 == mWebinarInfo.getWebinarInfoData().getJoin_info().is_gag;
        }
    }

    @Override
    public void initInputView() {
        if (inputView == null) {
            inputView = new InputView(activity, KeyBoardManager.getKeyboardHeight(activity), KeyBoardManager.getKeyboardHeightLandspace(activity));
            inputView.add2Window(activity);
            inputView.setOnSendClickListener(new InputView.SendMsgClickListener() {
                @Override
                public void onSendClick(String msg, InputUser user) {
                    if (!canSpeak && !UserManger.isHost(mWebinarInfo.getWebinarInfoData().getJoin_info().role_name)) {
                        if (broadcastView != null) {
                            broadcastView.showToast("您已被禁言");
                            return;
                        }
                    }
                    if (TextUtils.isEmpty(msg)) {
                        broadcastView.showToast("请输入内容");
                        return;
                    }
                    sendMessage(msg);
                }
            });
            inputView.setOnHeightReceivedListener(new InputView.KeyboardHeightListener() {
                @Override
                public void onHeightReceived(int screenOri, int height) {
                    if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        KeyBoardManager.setKeyboardHeight(activity, height);
                    } else {
                        KeyBoardManager.setKeyboardHeightLandspace(activity, height);
                    }
                }
            });
        }
    }

    private void sendMessage(String msg) {
        if (mRtcFragmentView != null) {
            mRtcFragmentView.sendMsg(msg, "", new SimpleCallback());
        }
    }


    @Override
    public void showInputView() {
        if (inputView != null) {
            inputView.show(false, null);
        }
    }

    @Override
    public void hintInputView() {
        inputView.dismiss();
    }

    @Override
    public void setLocalStream(Stream localStream) {
//        mRtcFragmentView.getLocalStream().setEnableBeautify(true);
        RtcConfig.getInterActive().setEnableBeautify(true);
    }

    @Override
    public void onSwitchVideo(final boolean isOpen) {
        if (mRtcFragmentView.getLocalStream() == null) {
            return;
        }
        /**
         * 请求 接口
         */
        RtcConfig.getInterActive().switchDevice(mWebinarInfo.getWebinarInfoData().join_info.third_party_user_id, "2", isOpen ? String.valueOf(CAMERA_DEVICE_OPEN) : String.valueOf(CAMERA_DEVICE_CLOSE), new SimpleRequestCallback());
        /**
         * 根据消息更改 这个不用了
         */
        //        switchVideoFrame(isOpen ? CAMERA_DEVICE_OPEN : CAMERA_DEVICE_CLOSE);
    }

    @Override
    public void onSwitchAudio(final boolean isOpen) {
        if (mRtcFragmentView.getLocalStream() == null) {
            return;
        }
        /**
         * 请求 接口
         */
        RtcConfig.getInterActive().switchDevice(mWebinarInfo.getWebinarInfoData().join_info.third_party_user_id, "1", isOpen ? String.valueOf(CAMERA_DEVICE_OPEN) : String.valueOf(CAMERA_DEVICE_CLOSE), new SimpleRequestCallback());

        /**
         * 根据消息更改 这个不用了
         */
        //        switchAudioFrame(isOpen ? CAMERA_DEVICE_OPEN : CAMERA_DEVICE_CLOSE);
    }

    @Override
    public void onDestroyed() {
        if (inputView != null) {
            inputView.destroyed();
        }
    }

    private void switchVideoFrame(int status) {
        if (mRtcFragmentView.getLocalStream() == null) {
            return;
        }
        if (status == CAMERA_DEVICE_OPEN) {
            //1打开
            broadcastView.updateVideoFrame(true);
            mRtcFragmentView.getLocalStream().unmuteVideo(null);
        } else { // 0禁止
            mRtcFragmentView.getLocalStream().muteVideo(null);
            broadcastView.updateVideoFrame(false);
        }
    }

    private void switchAudioFrame(int status) {
        if (mRtcFragmentView.getLocalStream() == null) {
            return;
        }
        if (status == CAMERA_DEVICE_OPEN) {
            mRtcFragmentView.getLocalStream().unmuteAudio(null);
            //开启自己麦克风
            broadcastView.updateAudioFrame(true);
        } else {
            mRtcFragmentView.getLocalStream().muteAudio(null);
            broadcastView.updateAudioFrame(false);
        }
    }

    String userId = "";


    //记录要求刷新页面的action
    static Set<Integer> sRequireRefreshEvents = new HashSet<>();


    //记录要求刷新页面的action
    static Set<String> sRequireRefreshActions = new HashSet<>();

    static {
        sRequireRefreshEvents.add(EVENT_DISABLE_CHAT);
        sRequireRefreshEvents.add(EVENT_PERMIT_CHAT);
        sRequireRefreshEvents.add(EVENT_CHAT_FORBID_ALL);
        sRequireRefreshEvents.add(EVENT_VRTC_SPEAKER_SWITCH);
        sRequireRefreshEvents.add(EVENT_VRTC_CONNECT_SUCCESS);
        sRequireRefreshEvents.add(EVENT_KICKOUT);
        sRequireRefreshEvents.add(EVENT_KICKOUT_RESTORE);
        sRequireRefreshEvents.add(EVENT_INTERACTIVE_DOWN_MIC);

        //上下线消息
        sRequireRefreshActions.add(ChatServer.eventOnlineKey);
        sRequireRefreshActions.add(ChatServer.eventOfflineKey);
    }

    private Map<String, OutTwoTitleDialog> mRtcDialogMap = new HashMap<>();

    private void showInvitedView(String name, final String thisId) {
        OutTwoTitleDialog showApply = new OutDialogBuilder()
                .title(name)
                .title1("申请上麦，是否同意？")
                .tv1("拒绝")
                .tv2("同意")
                .onCancel(new OutDialog.ClickLister() {
                    @Override
                    public void click() {
                        rejectOrAgreeApply(true, thisId);
                    }
                })
                .onConfirm(new OutDialog.ClickLister() {
                    @Override
                    public void click() {
                        rejectOrAgreeApply(false, thisId);
                    }
                })
                .buildTwo(activity);
        showApply.show();
        mRtcDialogMap.put(thisId, showApply);
    }

    private void rejectOrAgreeApply(boolean reject, String userId) {
        if (reject) {
            RtcConfig.getInterActive().rejectApply(userId, new SimpleRequestCallback());
        } else {
            RtcConfig.getInterActive().agreeApply(userId, new SimpleRequestCallback());
        }
        cancelRtcDialog();
    }


    private class SimpleRequestCallback implements RequestCallback {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(int errorCode, String errorMsg) {
            broadcastView.showToast(errorMsg);
        }
    }


    private class SimpleCallback implements CallBack {
        @Override
        public void onSuccess(Object result) {

        }

        @Override
        public void onError(int eventCode, String msg) {
            broadcastView.showToast(msg);
        }
    }

    @Override
    public void changeCamera() {
        RtcConfig.getInterActive().switchCamera();
    }

    @Override
    public boolean canSpeak() {
        return canSpeak;
    }

    @Override
    public void showBeauty(boolean showBeauty) {
        if (mRtcFragmentView.getLocalStream() != null) {
            if (showBeauty) {
                RtcConfig.getInterActive().setEnableBeautify(true);
                RtcConfig.getInterActive().setBeautifyLevel(3);
            } else {
                RtcConfig.getInterActive().setEnableBeautify(false);
                RtcConfig.getInterActive().setBeautifyLevel(0);
            }
        }
    }
}
