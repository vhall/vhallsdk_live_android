package com.vhall.uilibs.watch;

import android.app.Activity;
import android.widget.SeekBar;

import com.vhall.business.MessageServer;
import com.vhall.business.data.Survey;
import com.vhall.business.widget.ContainerLayout;

import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.BaseView;
import com.vhall.uilibs.util.emoji.InputUser;

//import org.fourthline.cling.android.AndroidUpnpService;
//import com.vhall.business_support.dlna.DeviceDisplay;
import java.util.HashMap;
import java.util.List;

/**
 * 观看页的接口类
 */
public class WatchContract {

    interface WatchView extends BaseView<WatchPresenter> {

        //显示聊天view
        void showChatView(boolean emoji, InputUser user, int limit);

        //显示公告
        void showNotice(String content);

        //隐藏公告
        void dismissNotice();

        //显示签到框
        void showSignIn(String signId, int startTime);

        //隐藏签到框
        void dismissSignIn();

        //显示问卷
        void showSurvey(Survey survey);

        //隐藏问卷
        void dismissSurvey();

        //横竖屏切换
        int changeOrientation();

        //显示toast
        void showToast(String toast);

        void showToast(int toast);

        //获取当前activity实例
        Activity getActivity();

        //显示抽奖
        void showLottery(final MessageServer.MsgInfo messageInfo);


        // 投屏使用
        // void showDevices();
        // 投屏使用
        // void dismissDevices();

    }

    interface DocumentView extends BaseView<BasePresenter> {
        //        void showDoc(String docUrl);
        void paintBoard(MessageServer.MsgInfo msgInfo);

        void paintBoard(String key, List<MessageServer.MsgInfo> msgInfos);

        void paintPPT(MessageServer.MsgInfo msgInfo);

        void paintPPT(String key, List<MessageServer.MsgInfo> msgInfos);
    }

    interface DetailView extends BaseView<BasePresenter> {
    }

    interface LiveView extends BaseView<LivePresenter> {

        ContainerLayout getWatchLayout();

        void setPlayPicture(boolean state);

        void setDownSpeed(String text);

        void showLoading(boolean isShow);

        void showRadioButton(HashMap map);

        void setScaleButtonText(int type);

        void addDanmu(String danmu);

        void reFreshView();
    }


    interface PlaybackView extends BaseView<PlaybackPresenter> {

        void setPlayIcon(boolean isStop);

        void setProgressLabel(String currentTime, String max);

        void setSeekbarMax(int max);

        void setSeekbarCurrentPosition(int position);

        void showProgressbar(boolean show);

        ContainerLayout getContainer();

        void setScaleTypeText(int type);

    }

    interface PlaybackPresenter extends WatchPresenter {
        void onFragmentDestory();

        void onPlayClick();

        void startPlay();

        //void paushPlay();

        //void stopPlay();

        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

        void onStopTrackingTouch(SeekBar seekBar);

        int changeScaleType();

        int changeScreenOri();

        void onPause();
        void onResume();
        void onStop();
    }

    interface LivePresenter extends WatchPresenter {

        void initWatch();

        void startWatch();

        void stopWatch();

        void onWatchBtnClick();

        void onSwitchPixel(int pixel);// 切换分辨率

        void onMobileSwitchRes(int res);// 切换分辨率

        int setScaleType();

        int changeOriention();

        void onDestory();

        void submitLotteryInfo(String id, String lottery_id, String nickname, String phone);

        int getCurrentPixel();

        int getScaleType();

        void setHeadTracker(); // 设置陀螺仪

        boolean isHeadTracker();  // 当前的陀螺仪
    }

    interface WatchPresenter extends BasePresenter {

        void signIn(String signId);

        void submitSurvey(Survey survey, String result);

        // void dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service);

        // void showDevices();

        // void dismissDevices();
    }
}
