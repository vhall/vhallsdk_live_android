package com.vhall.uilibs.watch;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vhall.uilibs.R;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.util.emoji.EmojiUtils;
import com.vhall.vhss.data.ScrollInfoData;

import java.io.InputStream;
import java.util.HashMap;

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
public class VWatchLiveFragment extends Fragment implements WatchContract.LiveView, View.OnClickListener {

    private WatchContract.LivePresenter mPresenter;
    private TextView fragmentDownloadSpeed, tv_color, tv_image;
    private RelativeLayout mContainerLayout;
    private ProgressBar progressbar;
    private IDanmakuView mDanmakuView;
    private DanmakuContext mDanmuContext;
    private BaseDanmakuParser mParser;

    private ImageView btn_change_scaletype;

    public static VWatchLiveFragment newInstance() {
        return new VWatchLiveFragment();
    }

    @Override
    public void setPresenter(WatchContract.LivePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.v_watch_live_fragment, container, false);
        initView(root);
        reFreshView();
        return root;
    }

    private void initView(View root) {
        mContainerLayout = root.findViewById(R.id.rl_container);
        fragmentDownloadSpeed = root.findViewById(R.id.fragment_download_speed);
        tv_color = root.findViewById(R.id.tv_color);
        tv_image = root.findViewById(R.id.tv_image);
        progressbar = root.findViewById(R.id.progressbar);
        btn_change_scaletype = (ImageView) root.findViewById(R.id.btn_change_scaletype);
        btn_change_scaletype.setOnClickListener(this);
        tv_color.setOnClickListener(this);
        tv_image.setOnClickListener(this);
        root.findViewById(R.id.image_action_back).setOnClickListener(this);
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        mDanmakuView = root.findViewById(R.id.sv_danmaku);
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
//        if (mPresenter != null) {
//            mPresenter.start();
//        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mPresenter.start();
            }
        });
    }

    @Override
    public RelativeLayout getWatchLayout() {
        return mContainerLayout;
    }

    @Override
    public void setPlayPicture(boolean state) {

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

    int color = 0;

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.image_action_back) {
            getActivity().onBackPressed();
        } else if (i == R.id.btn_change_scaletype) {
            mPresenter.setScaleType();
        } else if (i == R.id.tv_color) {
            int setColor = Color.parseColor("#000000");
            if (color == 0) {
                color = 1;
                setColor = Color.parseColor("#999999");
            } else if (color == 1) {
                color = 2;
                setColor = Color.parseColor("#ffffff");
            } else if (color == 2) {
                color = 3;
                setColor = Color.parseColor("#F09A37");
            } else if (color == 3) {
                color = 0;
                setColor = Color.parseColor("#000000");
            }
            if (!mPresenter.setVideoBackgroundColor(setColor)) {
                ToastUtil.showToast("设置失败");
            } else {
                ToastUtil.showToast("设置成功");
            }
        } else if (i == R.id.tv_image) {
            @SuppressLint("ResourceType") InputStream is = getResources().openRawResource(R.drawable.splash_bg);
            Bitmap mBitmap = BitmapFactory.decodeStream(is);
            if (!mPresenter.setVideoBackgroundImage(mBitmap)) {
                ToastUtil.showToast("设置失败");
            } else {
                ToastUtil.showToast("设置成功");
            }
        }
    }

    /**
     * 切换分辨率
     *
     * @param map 0 : 无效不可用  1 ：有效可用
     */
    @Override
    public void showRadioButton(HashMap map) {

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
        Spannable spannable = EmojiUtils.getEmojiText(getContext(), danmu);
        danmaku.text = spannable;
        danmaku.padding = 5;
        danmaku.priority = 0;  // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = true;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.WHITE;
        danmaku.borderColor = Color.TRANSPARENT;
        mDanmakuView.addDanmaku(danmaku);
    }

    @Override
    public void reFreshView() {
        if (mPresenter != null) {
            setScaleButtonText(mPresenter.getScaleType());
        }
    }

    @Override
    public void liveFinished() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().finish();
        }
    }

    @Override
    public void setScrollInfo(ScrollInfoData scrollInfo) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

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
//        if (mPresenter != null) {
//            mPresenter.stopWatch();
//        }
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
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
        super.onDestroy();
    }
}
