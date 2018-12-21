package com.vhall.uilibs.interactive;


import android.content.Context;
import android.support.annotation.AnyRes;

import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.BaseView;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

/**
 * 观看页的接口类
 */
public class InteractiveContract {

    interface InteractiveActView extends BaseView<InteractiveActPresenter> {

        Context getContext();

        void setSpeakerphoneOn(boolean on);

        void finish();

        void showToast(String toast);
    }

    interface InteractiveActPresenter extends BasePresenter {

    }

    interface InteractiveFraView extends BaseView<InteractiveFraPresenter> {

        void addLocalView( VHRenderView view);

        void updateVideoFrame(int status);

        void updateAudioFrame(int status);

        void addStream(VHRenderView view);

        void removeStream(Stream stream);
    }

    interface InteractiveFraPresenter extends InteractiveActPresenter {

        void initInteractive(); // 初始化互动模块

        void onDownMic(); // 下麦

        void onSwitchCamera(); // 切换摄像头

        void onSwitchVideo(boolean isOpen); // 视频开关

        void onSwitchAudio(boolean isOpen); // 音频开关

        void onDestory();

    }
}
