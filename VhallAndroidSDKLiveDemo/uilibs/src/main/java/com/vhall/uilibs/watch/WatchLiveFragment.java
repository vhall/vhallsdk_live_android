package com.vhall.uilibs.watch;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.vhall.business.WatchLive;
import com.vhall.business.utils.LogManager;
import com.vhall.business.widget.ContainerLayout;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.emoji.EmojiUtils;

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

    private ImageView clickOrientation, clickStart, mVrButton;
    private RadioButton radioButtonShowDEFAULT, radioButtonShowSD, radioButtonShowHD, radioButtonShowUHD;

    private RadioGroup radioChoose;
    private TextView fragmentDownloadSpeed;
    private ContainerLayout mContainerLayout;
    private ImageView btn_change_scaletype;
    private ImageView btnChangePlayStatus;
    ImageView btn_danmaku;
    ProgressBar progressbar;

    private IDanmakuView mDanmakuView;
    private DanmakuContext mDanmuContext;
    private BaseDanmakuParser mParser;
    private Activity context;

    private ImageView iv_dlna;

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
        iv_dlna = (ImageView) root.findViewById(R.id.iv_dlna);
        iv_dlna.setOnClickListener(this);
        clickStart = (ImageView) root.findViewById(R.id.click_rtmp_watch);
        clickStart.setOnClickListener(this);
        clickOrientation = (ImageView) root.findViewById(R.id.click_rtmp_orientation);
        clickOrientation.setOnClickListener(this);
        radioChoose = (RadioGroup) root.findViewById(R.id.radio_choose);
        radioChoose.setOnCheckedChangeListener(checkListener);
        radioButtonShowDEFAULT = (RadioButton) root.findViewById(R.id.radio_btn_default);
        radioButtonShowSD = (RadioButton) root.findViewById(R.id.radio_btn_sd);
        radioButtonShowHD = (RadioButton) root.findViewById(R.id.radio_btn_hd);
        radioButtonShowUHD = (RadioButton) root.findViewById(R.id.radio_btn_uhd);
        mContainerLayout = (ContainerLayout) root.findViewById(R.id.rl_container);
        fragmentDownloadSpeed = (TextView) root.findViewById(R.id.fragment_download_speed);
        mVrButton = (ImageView) root.findViewById(R.id.btn_headtracker);
        mVrButton.setOnClickListener(this);
        btn_danmaku = (ImageView) root.findViewById(R.id.btn_danmaku);
        btn_danmaku.setImageResource(R.drawable.vhall_icon_danmaku_close);
        btn_danmaku.setOnClickListener(this);
        btnChangePlayStatus = (ImageView) root.findViewById(R.id.btn_change_audio);
        btnChangePlayStatus.setOnClickListener(this);
        btn_change_scaletype = (ImageView) root.findViewById(R.id.btn_change_scaletype);
        btn_change_scaletype.setOnClickListener(this);
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
    public ContainerLayout getWatchLayout() {
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
        if (isShow)
            progressbar.setVisibility(View.VISIBLE);
        else
            progressbar.setVisibility(View.GONE);
    }


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
        } else if (i == R.id.btn_change_audio) {
            if (mPresenter.getCurrentPixel() == WatchLive.DPI_AUDIO) {
                mPresenter.onSwitchPixel(WatchLive.DPI_DEFAULT);
                btnChangePlayStatus.setImageResource(R.drawable.audio_close);
            } else {
                mPresenter.onSwitchPixel(WatchLive.DPI_AUDIO);
                btnChangePlayStatus.setImageResource(R.drawable.audio_open);
            }
        } else if (i == R.id.btn_danmaku) {
            if (mDanmakuView == null || !mDanmakuView.isPrepared())
                return;
            if (mDanmakuView.isShown()) {
                mDanmakuView.hide();
                btn_danmaku.setImageResource(R.drawable.vhall_icon_danmaku_close);
            } else {
                mDanmakuView.show();
                btn_danmaku.setImageResource(R.drawable.vhall_icon_danmaku_open);
            }

        } else if (i == R.id.iv_dlna) {
            mPresenter.showDevices();
        }
    }

    /**
     * 切换分辨率
     *
     * @param map 0 : 无效不可用  1 ：有效可用
     */
    @Override
    public void showRadioButton(HashMap map) {
        if (map == null)
            return;
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            Integer value = (Integer) entry.getValue();
            switch (key) {
                case "A":
                    if (value == 1)
                        btnChangePlayStatus.setVisibility(View.VISIBLE);
                    else
                        btnChangePlayStatus.setVisibility(View.GONE);
                    break;
                case "SD":
                    if (value == 1)
                        radioButtonShowSD.setVisibility(View.VISIBLE);
                    else
                        radioButtonShowSD.setVisibility(View.GONE);
                    break;
                case "HD":
                    if (value == 1)
                        radioButtonShowHD.setVisibility(View.VISIBLE);
                    else
                        radioButtonShowHD.setVisibility(View.GONE);
                    break;
                case "UHD":
                    if (value == 1)
                        radioButtonShowUHD.setVisibility(View.VISIBLE);
                    else
                        radioButtonShowUHD.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public void setScaleButtonText(int type) {
        switch (type) {
            case WatchLive.FIT_DEFAULT:
                btn_change_scaletype.setBackground(getResources().getDrawable(R.drawable.fit_default));
                break;
            case WatchLive.FIT_CENTER_INSIDE:
                btn_change_scaletype.setBackground(getResources().getDrawable(R.drawable.fit_center));
                break;
            case WatchLive.FIT_X:
                btn_change_scaletype.setBackground(getResources().getDrawable(R.drawable.fit_x));
                break;
            case WatchLive.FIT_Y:
                btn_change_scaletype.setBackground(getResources().getDrawable(R.drawable.fit_y));
                break;
            case WatchLive.FIT_XY:
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
            if (mPresenter.getCurrentPixel() == WatchLive.DPI_DEFAULT) {
                btnChangePlayStatus.setBackground(getResources().getDrawable(R.drawable.audio_close));
            } else if (mPresenter.getCurrentPixel() == WatchLive.DPI_AUDIO) {
                btnChangePlayStatus.setBackground(getResources().getDrawable(R.drawable.audio_open));
            }
            setScaleButtonText(mPresenter.getScaleType());
            if (mPresenter.isHeadTracker()) {
                mVrButton.setImageDrawable(getResources().getDrawable(R.drawable.vhall_icon_headtracker_checked));
            } else {
                mVrButton.setImageDrawable(getResources().getDrawable(R.drawable.vhall_icon_headtracker));
            }
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
                mPresenter.onSwitchPixel(WatchLive.DPI_DEFAULT);
            } else if (i == R.id.radio_btn_sd) {
                mPresenter.onSwitchPixel(WatchLive.DPI_SD);
            } else if (i == R.id.radio_btn_hd) {
                mPresenter.onSwitchPixel(WatchLive.DPI_HD);
            } else if (i == R.id.radio_btn_uhd) {
                mPresenter.onSwitchPixel(WatchLive.DPI_UHD);
            } else {
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stopWatch();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestory();
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

}
