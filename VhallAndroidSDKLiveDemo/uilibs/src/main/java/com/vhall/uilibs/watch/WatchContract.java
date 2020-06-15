package com.vhall.uilibs.watch;

import android.app.Activity;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.vhall.business.MessageServer;
import com.vhall.business.data.Survey;
import com.vhall.business_support.dlna.DMCControl;
import com.vhall.business_support.dlna.DMCControlListener;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.document.DocumentView;
import com.vhall.player.vod.VodPlayerView;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.BaseView;
import com.vhall.uilibs.util.MessageLotteryData;
import com.vhall.uilibs.util.emoji.InputUser;

import org.fourthline.cling.android.AndroidUpnpService;

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
        void showSurvey(String url, String title);

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
        void showLottery(MessageLotteryData data);

        void enterInteractive(); // 进入互动
        // 投屏使用
         void showDevices();
        // 投屏使用
         void dismissDevices();

        void refreshHand(int second);

        //显示被邀请上麦
        void showInvited();

        void setOnlineNum(int onlineNum);

    }

    interface DocumentView extends BaseView<BasePresenter> {
        //        void showDoc(String docUrl);
        void paintBoard(MessageServer.MsgInfo msgInfo);

        void paintBoard(String key, List<MessageServer.MsgInfo> msgInfos);

        void paintPPT(MessageServer.MsgInfo msgInfo);

        void paintPPT(String key, List<MessageServer.MsgInfo> msgInfos);

        void showType(int type);

    }

    interface DocumentViewVss extends BaseView<BasePresenter> {
        void refreshView(com.vhall.document.DocumentView view);

        void switchType(String type);
    }

    interface DetailView extends BaseView<BasePresenter> {
    }

    interface LiveView extends BaseView<LivePresenter> {

        RelativeLayout getWatchLayout();

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

        //TODO VodPlayView 更换为SurfaceView 原因说明
//        SurfaceView getVideoView();

        VodPlayerView getVideoView();


        void setScaleTypeText(int type);

        void setQuality(List<String> qualities);

        void setQualityChecked(String dpi);

        void setPlaySpeedText(String text);
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

        void onSwitchPixel(String pix);// 切换分辨率

        void setSpeed();
    }

    interface LivePresenter extends WatchPresenter {

        void initWatch();

        void startWatch();

        void stopWatch();

        void onWatchBtnClick();

        void onSwitchPixel(String dpi);// 切换分辨率

        void onMobileSwitchRes(String dpi);// 切换分辨率

        int setScaleType();

        int changeOriention();

        void onDestory();

        void submitLotteryInfo(String id, String lottery_id, String nickname, String phone);

        String getCurrentPixel();

        int getScaleType();

        void setHeadTracker(); // 设置陀螺仪

        boolean isHeadTracker();  // 当前的陀螺仪
    }

    interface WatchPresenter extends BasePresenter {

        void signIn(String signId);

        void submitSurvey(String result);

        void submitSurvey(Survey survey,String result);

        void onRaiseHand(); // 举手

        void replyInvite(int type);

         DMCControl dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service);

         void showDevices();

         void dismissDevices();
    }
}
