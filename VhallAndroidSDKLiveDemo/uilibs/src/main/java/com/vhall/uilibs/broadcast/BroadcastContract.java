package com.vhall.uilibs.broadcast;

import android.app.Activity;
import android.widget.RelativeLayout;

import com.vhall.push.VHVideoCaptureView;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.BaseView;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.vhallrtc.client.VHRenderView;

/**
 * 发直播的View接口类
 */
public class BroadcastContract {

    interface BroadcastView extends BaseView<Presenter> {
        void showChatView(boolean emoji, InputUser user, int limit);

        //获取当前activity实例
        Activity getActivity();
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

        default void startBroadcastSuccess(boolean success) {

        }

        default void liveRehearsal(boolean success) {

        }

        void setSpeedText(String text);

        void setModeText(String mode);

        //是否结束直播弹窗
        void showEndLiveDialog();
    }

    interface Presenter extends BasePresenter {

        // rehearsal是否是彩排
        void onstartBtnClick(boolean rehearsal);

        void initBroadcast(boolean rehearsal);

        void startBroadcast();

        void stopBroadcast();

        void finishBroadcast();

        void changeFlash();

        void changeMode();

        void changeCamera();

        void changeAudio();

        void destroyBroadcast();

        void setVolumeAmplificateSize(float size);

        void setRenderView(VHRenderView vhRenderView);

        void onResume();

        void onPause();

    }

    interface IDirectorPresenter extends BasePresenter {
        void onstartBtnClick();

        void onDestroy();

        //获取当前直播状态：有没有开播
        boolean getWebinarStatus();

        void onResume();

        void onPause();

        void init();
    }

    interface DirectorView extends BaseView<IDirectorPresenter> {
        RelativeLayout getWatchLayout();

        Activity getActivity();

        void setDirectorError(String type);

        void setStartBtnImage(boolean start);
    }
}
