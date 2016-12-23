package com.vhall.live.broadcast;

import android.app.Activity;

import com.vhall.live.BasePresenter;
import com.vhall.live.BaseView;
import com.vinny.vinnylive.CameraFilterView;

/**
 * 发直播的View接口类
 */
public class BroadcastContract {
    interface View extends BaseView<Presenter> {
        CameraFilterView getCameraView();

        Activity getmActivity();

        void setStartBtnText(String text);

        void setChangeFlashEnable(boolean enable);

        void setChangeCameraEnable(boolean enable);

        void showMsg(String msg);

        void setSpeedText(String text);

        void showSeekbar(boolean show);

        void setSeekbarPro(int progress);
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

        void switchFilter(boolean close);

        void onPause();

        void onDestory();

        void onResume();
    }
}
