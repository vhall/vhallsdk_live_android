package com.vhall.uilibs.broadcast;

import com.vhall.push.VHVideoCaptureView;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.BaseView;
import com.vhall.uilibs.util.emoji.InputUser;

/**
 * 发直播的View接口类
 */
public class BroadcastContract {

    interface BroadcastView extends BaseView<Presenter> {
        void showChatView(boolean emoji, InputUser user, int limit);
    }

    interface View extends BaseView<Presenter> {

        VHVideoCaptureView getCameraView();

        //模块化中，在播放器类初始化 可以删除
//        void initCamera(int piexl_type);

        void setStartBtnImage(boolean start);

        void setFlashBtnImage(boolean open);

        void setAudioBtnImage(boolean open);

        void setFlashBtnEnable(boolean enable);

        void setCameraBtnEnable(boolean enable);

        void showMsg(String msg);

        void setSpeedText(String text);
        void setModeText(String mode);
        //是否结束直播弹窗
        void showEndLiveDialog();
    }

    interface Presenter extends BasePresenter {
        void onstartBtnClick();

        void initBroadcast();

        void startBroadcast();

        void stopBroadcast();

        void finishBroadcast();

        void changeFlash();

        void changeMode();

        void changeCamera();

        void changeAudio();

        void destroyBroadcast();

        void setVolumeAmplificateSize(float size);

    }
}
