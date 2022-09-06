package com.vhall.uilibs.watch;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.vhall.business.MessageServer;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.Survey;
import com.vhall.business_support.dlna.DMCControl;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.player.vod.VodPlayerView;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.BaseView;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;
import com.vhall.vhss.data.LotteryCheckData;
import com.vhall.vhss.data.ScrollInfoData;
import com.vhall.vhss.data.SurveyInfoData;

import org.fourthline.cling.android.AndroidUpnpService;

import java.util.ArrayList;
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
        void showSignIn(String signId, String title, int startTime);

        //隐藏签到框
        void dismissSignIn();

        //显示问卷
        void showSurvey(String url, String title);

        void showSurvey(Survey survey);

        //隐藏问卷
        void dismissSurvey();

        //显示问答
        void showQAndA(String name);

        //隐藏问答
        void dismissQAndA();

        //横竖屏切换
        int changeOrientation();

        //显示toast
        void showToast(String toast);

        void showToast(int toast);

        //获取当前activity实例
        Activity getActivity();

        //显示抽奖
        void showLottery(MessageServer.MsgInfo data);

        void enterInteractive(); // 进入互动

        // 投屏使用
        void showDevices();

        // 投屏使用
        void dismissDevices();

        void refreshHand(int second);

        //显示被邀请上麦
        void showInvited();

        void setOnlineNum(int onlineNum, int onlineVirtual);

        void setPvNum(int pvNum, int pvVirtual);

        /**
         * 文档全屏
         */
        default void showDocFullScreen(int state) {
        }

        /**
         * 改变文档方向
         */
        default void changeDocOrientation() {
        }
    }

    interface DocumentView extends BaseView<BasePresenter> {
        //        void showDoc(String docUrl);
        void paintBoard(MessageServer.MsgInfo msgInfo);

        void paintBoard(String key, List<MessageServer.MsgInfo> msgInfos);

        void paintPPT(MessageServer.MsgInfo msgInfo);

        void paintPPT(String key, List<MessageServer.MsgInfo> msgInfos);

        void showType(int type);

        void paintH5DocView(View docView);

        default void clickDocFullBack() {
        }

        default void triggerDocOrientation() {
        }
    }

    interface DocumentViewVss extends BaseView<BasePresenter> {
        void refreshView(com.vhall.document.DocumentView view);

        void switchType(String type);
    }

    interface DetailView extends BaseView<BasePresenter> {
    }

    interface LotteryView extends BaseView<BasePresenter> {
        void setLotteryData(MessageServer.MsgInfo lotteryData);
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

        void setScrollInfo(ScrollInfoData scrollInfo); // 设置跑马灯

        void liveFinished();//直播结束

        default void updateSurveyList(ArrayList<SurveyInfoData> result) {

        }

        //更新抽奖
        default void updateLotteryList(ArrayList<LotteryCheckData> lotteryCheckData) {

        }

        //显示公告 红点
        default void showNoticeRed() {
        }

    }

    interface LiveNoDelayView extends BaseView<LivePresenter> {

        void updateMain(String mainId);

        //无延迟直播专用
        void updateStream(boolean del, Stream stream, VHRenderView newRenderView);//直播结束

        void updateVideoFrame(int status);

        void updateAudioFrame(int status);

        void enterInteractive(boolean isEnter);

        //显示计时器
        default void showTimeView(MessageServer.TimerData timerData) {
        }

        //显示计时器
        default void showTimeView(boolean pause) {
        }

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



        void setScrollInfo(final ScrollInfoData scrollInfo);
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

        default boolean setVideoBackgroundColor(int color) {
            return false;
        }

        default boolean setVideoBackgroundImage(Bitmap bitmap) {
            return false;
        }


        default void onResume() {

        }

        default void onPause() {

        }


        void onDestroy();

        void submitLotteryInfo(String id, String lottery_id, String nickname, String phone);

        String getCurrentPixel();

        int getScaleType();

        void setHeadTracker(); // 设置陀螺仪

        boolean isHeadTracker();  // 当前的陀螺仪

        //无延迟 互动直播 使用
        void onSwitchCamera(); // 切换摄像头

        //无延迟 互动直播 使用
        void onSwitchVideo(boolean isOpen); // 视频开关

        //无延迟 互动直播 使用
        void onSwitchAudio(boolean isOpen); // 音频开关

        void onDownMic(boolean own);

        void onUpMic();

        //视频直播使用 返回当前播放状态
        boolean getIsPlaying();
    }

    interface WatchPresenter extends BasePresenter {

        default void signIn(String signId) {

        }

        default void submitSurvey(String result) {

        }

        default void submitSurvey(Survey survey, String result) {

        }

        void onRaiseHand(); // 举手

        default void beautyOpen() {

        }

        void replyInvite(int type, RequestCallback callback);


        //返回是否已经参加轮训
        default boolean getIsRound() {
            return false;
        }

        default DMCControl dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service) {
            return null;
        }

        default void showDevices() {

        }

        default void showSurveyListDialog(List<SurveyInfoData> dataList, boolean show) {

        }

        default void showLotteryListDialog(List<LotteryCheckData> dataList, boolean show) {

        }

        default void showNoticeDialog() {

        }

        default void showSurvey(SurveyInfoData data) {

        }

        default void dismissDevices() {

        }

        /**
         * 文档全屏显示
         *
         * @param state
         * @see DocFragState
         */
        default void showDocFullScreen(int state) {
        }

        /**
         * 触发doc切换横竖屏方向
         */
        default void changeDocOrientation() {
        }

        /**
         * 改变文档方向：事件从activity到fragment
         */
        default void triggerDocOrientation() {
        }

        /**
         * 文档全屏回退
         */
        default void clickDocFullBack() {
        }
    }
}
