package com.vhall.uilibs.interactive.broadcast.present;


import android.app.Activity;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhss.CallBack;

/**
 * @author hkl
 * Date: 2019-08-26 17:48
 */
public interface IBroadcastContract {
    interface IBroadcastView extends IHostBaseView<IBroadcastPresent> {
        Activity getActivity();

        void setMic(boolean isMic);

        void updateVideoFrame(boolean isCheck);

        void updateAudioFrame(boolean isChecks);

        void refreshUserList();

        void refreshStream(String userId, int voice, int camera);

        void updateMain(String mainId);

        void notifyDataChangedChat(MessageChatData data);

        void showLookNum(int pv, int uv);

        void forbidBroadcast();
        void userNoSpeaker(String userId);

        void notifyRoleName(String type,String name);
    }

    interface RtcFragmentView extends IHostBaseView<IBroadcastPresent>{
        Stream getLocalStream();

        void noSpeak();

        void sendMsg(String msg, String type, final CallBack callBack);

        boolean isPublish();

        void initLocalStream();
    }

    interface IBroadcastPresent{
        void init(WebinarInfo webinarInfo);

        void initInputView();


        void showInputView();

        void hintInputView();

        void setLocalStream(Stream localStream);

        void changeCamera();

        boolean canSpeak();

        void showBeauty(boolean showBeauty);

        void onSwitchVideo(boolean isOpen); // 视频开关

        void onSwitchAudio(boolean isOpen); // 音频开关

        void updateHostRoleName(String role_name);

        void onDestroyed();//销毁

        MessageServer.Callback getMessageCallback();

        ChatServer.Callback getChatCallback();

        void setRtcFragmentView(IBroadcastContract.RtcFragmentView rtcFragmentView);
    }


}
