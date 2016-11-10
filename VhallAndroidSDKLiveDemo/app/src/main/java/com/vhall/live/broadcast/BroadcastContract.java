package com.vhall.live.broadcast;

import android.app.Activity;

import com.vhall.business.VhallCameraView;
import com.vhall.live.BasePresenter;
import com.vhall.live.BaseView;

/**
 * 发直播的View接口类
 */
public class BroadcastContract {
    interface View extends BaseView<Presenter> {
        VhallCameraView getCameraView();

        Activity getmActivity();

        void setStartBtnText(String text);

        void setChangeFlashEnable(boolean enable);

        void setChangeCameraEnable(boolean enable);

        void showMsg(String msg);

        void setSpeedText(String text);
    }

    interface Presenter extends BasePresenter {
        void onstartBtnClick();

        void initBroadcast();

        void startBroadcast();

        void stopBroadcast();

        void finishBroadcast();

        void changeFlash();

        void changeCamera();

        void changeAudio();

        void onPause();

        void onDestory();

        void onResume();
    }
}
