package com.vhall.uilibs.watch;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vhall.business.utils.LogManager;
import com.vhall.player.Constants;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.MarqueeView;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.util.emoji.EmojiUtils;
import com.vhall.uilibs.widget.ExamListDialog;
import com.vhall.vhss.data.LotteryCheckData;
import com.vhall.vhss.data.ScrollInfoData;
import com.vhall.vhss.data.SurveyInfoData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

/**
 * 观看直播的Fragment
 */
public class WatchLiveFragment extends Fragment implements WatchContract.LiveView, View.OnClickListener {

    private WatchContract.LivePresenter mPresenter;

    private ImageView clickOrientation, clickStart, mVrButton, btn_mute,iv_exam;
    private RadioButton radioButtonShowDEFAULT, radioButtonShowSD, radioButtonShowHD, radioButtonShowUHD, radioButtonShowA;

    private RadioGroup radioChoose;
    private TextView fragmentDownloadSpeed;
    private RelativeLayout mContainerLayout;
    private ImageView btn_change_scaletype;
    //    private ImageView btnChangePlayStatus;
    ImageView btn_danmaku;
    ProgressBar progressbar;

    private IDanmakuView mDanmakuView;
    private DanmakuContext mDanmuContext;
    private MarqueeView marquee_view;
    private BaseDanmakuParser mParser;
    private Activity context;

    private ImageView iv_dlna;
    private FrameLayout fl_survey;
    private View surveyRedPoint;
    private FrameLayout fl_lottery;
    private View lotteryRedPoint;
    private FrameLayout fl_notice;
    private View noticeRedPoint;

    public static WatchLiveFragment newInstance() {
        return new WatchLiveFragment();
    }

    @Override
    public void setPresenter(WatchContract.LivePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.watch_live_fragment, container, false);
        initView(root);
        reFreshView();
        return root;
    }

    private void initView(View root) {
        root.findViewById(R.id.btn_mute).setOnClickListener(this);
        btn_mute = root.findViewById(R.id.btn_mute);
        iv_dlna = (ImageView) root.findViewById(R.id.iv_dlna);
        iv_dlna.setOnClickListener(this);
        clickStart = (ImageView) root.findViewById(R.id.click_rtmp_watch);
        clickStart.setOnClickListener(this);
        clickOrientation = (ImageView) root.findViewById(R.id.click_rtmp_orientation);
        fl_survey = root.findViewById(R.id.fl_survey);
        fl_lottery = root.findViewById(R.id.fl_lottery);
        fl_notice = root.findViewById(R.id.fl_notice);
        surveyRedPoint = root.findViewById(R.id.survey_red_point);
        lotteryRedPoint = root.findViewById(R.id.lottery_red_point);
        noticeRedPoint = root.findViewById(R.id.notice_red_point);
        clickOrientation.setOnClickListener(this);
        fl_survey.setOnClickListener(this);
        fl_lottery.setOnClickListener(this);
        fl_notice.setOnClickListener(this);
        radioChoose = (RadioGroup) root.findViewById(R.id.radio_choose);
        radioChoose.setOnCheckedChangeListener(checkListener);
        radioButtonShowDEFAULT = (RadioButton) root.findViewById(R.id.radio_btn_default);
        radioButtonShowA = (RadioButton) root.findViewById(R.id.radio_btn_a);
        radioButtonShowSD = (RadioButton) root.findViewById(R.id.radio_btn_sd);
        radioButtonShowHD = (RadioButton) root.findViewById(R.id.radio_btn_hd);
        radioButtonShowUHD = (RadioButton) root.findViewById(R.id.radio_btn_uhd);
        mContainerLayout = (RelativeLayout) root.findViewById(R.id.rl_container);
        fragmentDownloadSpeed = (TextView) root.findViewById(R.id.fragment_download_speed);
        mVrButton = (ImageView) root.findViewById(R.id.btn_headtracker);
        mVrButton.setOnClickListener(this);
        btn_danmaku = (ImageView) root.findViewById(R.id.btn_danmaku);
        btn_danmaku.setImageResource(R.drawable.vhall_icon_danmaku_close);
        btn_danmaku.setOnClickListener(this);
//        btnChangePlayStatus = (ImageView) root.findViewById(R.id.btn_change_audio);
//        btnChangePlayStatus.setOnClickListener(this);
        btn_change_scaletype = (ImageView) root.findViewById(R.id.btn_change_scaletype);
        btn_change_scaletype.setOnClickListener(this);
        iv_exam = (ImageView) root.findViewById(R.id.iv_exam);
        iv_exam.setOnClickListener(this);
        progressbar = (ProgressBar) root.findViewById(R.id.progressbar);
        root.findViewById(R.id.image_action_back).setOnClickListener(this);
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        mDanmakuView = (IDanmakuView) root.findViewById(R.id.sv_danmaku);
        marquee_view = root.findViewById(R.id.marquee_view);
        mDanmakuView.hide();
        mDanmuContext = DanmakuContext.create();
        mDanmuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false).setScrollSpeedFactor(2.2f).setScaleTextSize(1.2f)
//                .setCacheStuffer(new SimpleTextCacheStuffer(), null)
                .setCacheStuffer(new SpannedCacheStuffer(), null) // 图文混排使用SpannedCacheStuffer
//        .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);
        if (mDanmakuView != null) {
            mParser = new BaseDanmakuParser() {
                @Override
                protected IDanmakus parse() {
                    return new Danmakus();
                }
            };
            mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
                }

                @Override
                public void prepared() {
                    mDanmakuView.start();
                }
            });
            mDanmakuView.prepare(mParser, mDanmuContext);
            mDanmakuView.showFPS(false);
            mDanmakuView.enableDanmakuDrawingCache(true);
        }
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    @Override
    public RelativeLayout getWatchLayout() {
        return mContainerLayout;
    }

    @Override
    public void setPlayPicture(boolean state) {
        if (state) {
            clickStart.setBackgroundResource(R.drawable.vhall_icon_live_pause);
        } else {
            clickStart.setBackgroundResource(R.drawable.vhall_icon_live_play);
        }
    }

    @Override
    public void setDownSpeed(String text) {
        fragmentDownloadSpeed.setText(text);
    }

    @Override
    public void showLoading(boolean isShow) {
        if (isShow) {
            progressbar.setVisibility(View.VISIBLE);
        } else {
            progressbar.setVisibility(View.GONE);
        }
    }

    //是否 开启续播 只有播放中 推到后台才续播
    boolean restart = false;

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.click_rtmp_watch) {
            mPresenter.onWatchBtnClick();
        } else if (i == R.id.click_rtmp_orientation) {
            mPresenter.changeOriention();
        } else if (i == R.id.btn_change_scaletype) {
            mPresenter.setScaleType();
        } else if (i == R.id.btn_headtracker) {
            mPresenter.setHeadTracker();
            LogManager.innerLog("HeadTracker", " HeadTracker == " + mPresenter.isHeadTracker());
        } else if (i == R.id.image_action_back) {
            getActivity().onBackPressed();
        } /*else if (i == R.id.btn_change_audio) {
            if (mPresenter.getCurrentPixel().equals(Constants.Rate.DPI_AUDIO)) {
                btnChangePlayStatus.setImageResource(R.drawable.audio_close);
                mPresenter.onMobileSwitchRes(Constants.Rate.DPI_SAME);
            } else {
                btnChangePlayStatus.setImageResource(R.drawable.audio_open);
                mPresenter.onMobileSwitchRes(Constants.Rate.DPI_AUDIO);
            }
        }*/ else if (i == R.id.btn_danmaku) {
            if (mDanmakuView == null || !mDanmakuView.isPrepared()) {
                return;
            }
            if (mDanmakuView.isShown()) {
                mDanmakuView.hide();
                btn_danmaku.setImageResource(R.drawable.vhall_icon_danmaku_close);
            } else {
                mDanmakuView.show();
                btn_danmaku.setImageResource(R.drawable.vhall_icon_danmaku_open);
            }

        } else if (i == R.id.iv_dlna) {
            mPresenter.showDevices();
        } else if (i == R.id.fl_survey) {
            judgeSurveyRed();
            if (surveyStatus == 0) {
                ToastUtil.showToast("已提交成功感谢参与");
            } else if (surveyStatus == 1) {
                mPresenter.showSurvey(surveyInfoData);
            } else {
                mPresenter.showSurveyListDialog(resultSurvey, true);
            }
        } else if (i == R.id.fl_lottery) {
            mPresenter.showLotteryListDialog(lotteryCheckData, true);
        }else if (i == R.id.fl_notice) {
            noticeRedPoint.setVisibility(View.GONE);
            mPresenter.showNoticeDialog();
        }else if (i == R.id.iv_exam) {
            mPresenter.showExamDialog();
        }
    }

    // 静音 默认关闭
    private boolean mute = false;

    /**
     * 切换分辨率
     *
     * @param map 0 : 无效不可用  1 ：有效可用
     */
    @Override
    public void showRadioButton(HashMap map) {
        if (map == null) {
            return;
        }
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            Integer value = (Integer) entry.getValue();
            switch (key) {
                case "A":
                case "a":
                    if (value == 1) {
                        radioButtonShowA.setVisibility(View.VISIBLE);
                    } else {
                        radioButtonShowA.setVisibility(View.GONE);
                    }
                    break;
                case "SD":
                case "360p":
                    if (value == 1) {
                        radioButtonShowSD.setVisibility(View.VISIBLE);
                    } else {
                        radioButtonShowSD.setVisibility(View.GONE);
                    }
                    break;
                case "HD":
                case "480p":
                    if (value == 1) {
                        radioButtonShowHD.setVisibility(View.VISIBLE);
                    } else {
                        radioButtonShowHD.setVisibility(View.GONE);
                    }
                    break;
                case "UHD":
                case "720p":
                    if (value == 1) {
                        radioButtonShowUHD.setVisibility(View.VISIBLE);
                    } else {
                        radioButtonShowUHD.setVisibility(View.GONE);
                    }
                    break;
                case "same":
                    if (value == 1) {
                        radioButtonShowDEFAULT.setVisibility(View.VISIBLE);
                    } else {
                        radioButtonShowDEFAULT.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void setScaleButtonText(int type) {
        switch (type) {
            case 0://FIT_DEFAULT
                btn_change_scaletype.setBackground(getResources().getDrawable(R.drawable.fit_default));
                break;
            case 1://FIT_CENTER_INSIDE
                btn_change_scaletype.setBackground(getResources().getDrawable(R.drawable.fit_center));
                break;
            case 2://FIT_X
                btn_change_scaletype.setBackground(getResources().getDrawable(R.drawable.fit_x));
                break;
            case 3://FIT_Y
                btn_change_scaletype.setBackground(getResources().getDrawable(R.drawable.fit_y));
                break;
            case 4://FIT_XY
                btn_change_scaletype.setBackground(getResources().getDrawable(R.drawable.fit_xy));
                break;
        }
    }

    @Override
    public void addDanmu(String danmu) {
        BaseDanmaku danmaku = mDanmuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        Spannable spannable = EmojiUtils.getEmojiText(context, danmu);
        danmaku.text = spannable;
        danmaku.padding = 5;
        danmaku.priority = 0;  // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = true;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.WHITE;
//        danmaku.textShadowColor = Color.WHITE;
        // danmaku.underlineColor = Color.GREEN;
        danmaku.borderColor = Color.TRANSPARENT;
        mDanmakuView.addDanmaku(danmaku);
    }

    @Override
    public void reFreshView() {
        if (mPresenter != null) {
//            if (mPresenter.getCurrentPixel() == Constants.Rate.DPI_SAME) {
//                btnChangePlayStatus.setBackground(getResources().getDrawable(R.drawable.audio_close));
//            } else if (mPresenter.getCurrentPixel() == Constants.Rate.DPI_AUDIO) {
//                btnChangePlayStatus.setBackground(getResources().getDrawable(R.drawable.audio_open));
//            }
            setScaleButtonText(mPresenter.getScaleType());
            if (mPresenter.isHeadTracker()) {
                mVrButton.setImageDrawable(getResources().getDrawable(R.drawable.vhall_icon_headtracker_checked));
            } else {
                mVrButton.setImageDrawable(getResources().getDrawable(R.drawable.vhall_icon_headtracker));
            }
        }
    }

    @Override
    public void liveFinished() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().finish();
        }
    }

    private ArrayList<SurveyInfoData> resultSurvey = new ArrayList<>();
    private ArrayList<LotteryCheckData> lotteryCheckData = new ArrayList<>();

    @Override
    public void showNoticeRed() {
        noticeRedPoint.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateLotteryList(ArrayList<LotteryCheckData> result) {
        judgeLotteryRed(result);
        lotteryCheckData.clear();
        if (!ListUtils.isEmpty(result)) {
            lotteryCheckData.addAll(result);
            mPresenter.showLotteryListDialog(result, false);
        }
    }

    @Override
    public void updateSurveyList(ArrayList<SurveyInfoData> result) {
        resultSurvey.clear();
        if (!ListUtils.isEmpty(result)) {
            resultSurvey.addAll(result);
            mPresenter.showSurveyListDialog(result, false);
        }
        judgeSurveyRed();
    }


    // 1 只有一个没有填写 2+多个没有填写 0 全部填写
    private int surveyStatus;
    private SurveyInfoData surveyInfoData;//如果只有一个没有填写记录信息

    /**
     * 判读显示不显示红点
     * 判断按钮的跳转
     * 问卷
     */
    private void judgeSurveyRed() {
        surveyStatus = 0;
        if (ListUtils.isEmpty(resultSurvey)) {
            surveyRedPoint.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < resultSurvey.size(); i++) {
                if (TextUtils.equals("0", resultSurvey.get(i).is_answered)) {
                    surveyStatus++;
                    surveyInfoData = resultSurvey.get(i);
                }
            }
            if (surveyStatus > 0) {
                surveyRedPoint.setVisibility(View.VISIBLE);
            } else {
                surveyRedPoint.setVisibility(View.GONE);
            }
        }
    }

    // 1 只有一个没有填写 2+多个没有填写 0 全部填写
    private int lotteryStatus;
    //如果只有一个没有填写记录信息

    /**
     * 判读显示不显示红点
     * 判断按钮的跳转
     * 抽奖
     * <p>
     * public int take_award;//是否已领奖 0-否 1-是
     * public int need_take_award;//是否需要领奖 0-否 1-是
     */
    private void judgeLotteryRed(ArrayList<LotteryCheckData> result) {
        lotteryStatus = 0;
        if (ListUtils.isEmpty(result)) {
            lotteryRedPoint.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < result.size(); i++) {
                if (1 == result.get(i).need_take_award && 0 == result.get(i).take_award) {
                    lotteryStatus++;
                }
            }
            if (lotteryStatus > 0) {
                lotteryRedPoint.setVisibility(View.VISIBLE);
            } else {
                lotteryRedPoint.setVisibility(View.GONE);
            }
        }
    }


    /**
     * 判读显示不显示红点
     * 公告
     * 每次进入直播间公告不为空 则显示红点，点击消失 弹窗关闭之后 收到公告则恢复红点显示
     * <p>
     * public int take_award;//是否已领奖 0-否 1-是
     * public int need_take_award;//是否需要领奖 0-否 1-是
     */
    private void judgeNoticeRed(ArrayList<LotteryCheckData> result) {
        lotteryStatus = 0;
        if (ListUtils.isEmpty(result)) {
            lotteryRedPoint.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < result.size(); i++) {
                if (1 == result.get(i).need_take_award && 0 == result.get(i).take_award) {
                    lotteryStatus++;
                }
            }
            if (lotteryStatus > 0) {
                lotteryRedPoint.setVisibility(View.VISIBLE);
            } else {
                lotteryRedPoint.setVisibility(View.GONE);
            }
        }
    }


    private ScrollInfoData scrollInfoData;

    @Override
    public void setScrollInfo(final ScrollInfoData scrollInfo) {
        if (scrollInfoData != null || scrollInfo == null) {
            //设置一次就好了
            return;
        }
        scrollInfoData = scrollInfo;
        if (marquee_view != null) {
            marquee_view.setVisibility(View.VISIBLE);
            //height        随机显示时 控件显示的的高度范围  默认 100 -500 可以自己根据需求改
            marquee_view.setScrollingInfo(scrollInfoData, 450);
        }
    }


    private void addDanmaKuShowTextAndImage(boolean islive) {
        BaseDanmaku danmaku = mDanmuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
        drawable.setBounds(0, 0, 100, 100);
        SpannableStringBuilder spannable = createSpannable(drawable);
        danmaku.text = spannable;
        danmaku.padding = 5;
        danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
        danmaku.isLive = islive;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
        danmaku.underlineColor = Color.GREEN;
        mDanmakuView.addDanmaku(danmaku);
    }

    private SpannableStringBuilder createSpannable(Drawable drawable) {
        String text = "bitmap";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        ImageSpan span = new ImageSpan(drawable);//ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("图文混排");
        spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public RadioGroup.OnCheckedChangeListener checkListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if (i == R.id.radio_btn_default) {
                mPresenter.onMobileSwitchRes(Constants.Rate.DPI_SAME);
                //mPresenter.onSwitchPixel(WatchLive.DPI_DEFAULT);
            } else if (i == R.id.radio_btn_sd) {
                mPresenter.onMobileSwitchRes(Constants.Rate.DPI_SD);
//                mPresenter.onSwitchPixel(WatchLive.DPI_SD);
            } else if (i == R.id.radio_btn_hd) {
                mPresenter.onMobileSwitchRes(Constants.Rate.DPI_HD);
//                mPresenter.onSwitchPixel(WatchLive.DPI_HD);
            } else if (i == R.id.radio_btn_uhd) {
                mPresenter.onMobileSwitchRes(Constants.Rate.DPI_XHD);
//                mPresenter.onSwitchPixel(WatchLive.DPI_UHD);
            } else if (i == R.id.radio_btn_a) {
                mPresenter.onMobileSwitchRes(Constants.Rate.DPI_AUDIO);
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
        if (marquee_view != null && marquee_view.getVisibility() == View.VISIBLE) {
            marquee_view.stopScroll();
        }
        if (mPresenter != null && mPresenter.getIsPlaying()) {
            mPresenter.stopWatch();
            restart = true;
            mPresenter.onPause();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
        if (marquee_view != null && marquee_view.getVisibility() == View.VISIBLE) {
            marquee_view.startScroll();
        }
        if (mPresenter != null && restart) {
            mPresenter.startWatch();
            restart = false;
            mPresenter.onResume();
        }
    }

    @Override
    public void onDestroy() {
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
        if (marquee_view != null) {
            marquee_view.onDestroy();
        }
        super.onDestroy();
    }
}
