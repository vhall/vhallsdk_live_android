package com.vhall.uilibs.util;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.vhall.vhss.data.ScrollInfoData;

import java.util.Random;

/**
 * Created by hkl
 * 自动调整位置的跑马灯
 */
public class MarqueeView extends FrameLayout {
    private Context mContext;
    private ScrollInfoData scrollingInfo;

    private Handler mHandler = new Handler();


    public MarqueeView(Context context) {
        this(context, null);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        setClickable(false);
    }

    /**
     * @param scrollingInfo 跑马灯信息
     * @param height        随机显示时 控件的高度范围
     */
    public void setScrollingInfo(final ScrollInfoData scrollingInfo, final int height) {
        if (scrollingInfo == null || this.scrollingInfo != null) {
            return;
        }
        this.scrollingInfo = scrollingInfo;

        addScrollView(scrollingInfo, height);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(this, scrollingInfo.interval * 1000);
                if (isStop) {
                    return;
                }
                addScrollView(scrollingInfo, height);
            }
        }, scrollingInfo.interval * 1000);
    }

    private void addScrollView(ScrollInfoData scrollingInfo, int height) {
        if (scrollingInfo == null) {
            return;
        }
        this.scrollingInfo = scrollingInfo;
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (scrollingInfo.getSize() * 3.5));
        final ScrollingTextView scrollingTextView = new ScrollingTextView(mContext);
        switch (scrollingInfo.getPosition()) {
            case ScrollInfoData.POSITION_RANDOM:
                int min = 100;
                int max = height == 0 ? 500 : height;
                Random random = new Random();
                int num = random.nextInt(max) % (max - min + 1) + min;
                layoutParams.setMargins(10, num, 10, 10);
                break;
            case ScrollInfoData.POSITION_HIGHT:
                layoutParams.gravity = Gravity.TOP;
                break;
            case ScrollInfoData.POSITION_MIDDLE:
                layoutParams.gravity = Gravity.CENTER_VERTICAL;
                break;
            case ScrollInfoData.POSITION_LOW:
                layoutParams.gravity = Gravity.BOTTOM;
                break;
            default:
                break;
        }
        scrollingTextView.setLayoutParams(layoutParams);
        addView(scrollingTextView);
        scrollingTextView.setScrollingInfo(scrollingInfo);
        scrollingTextView.setOnMargueeListener(new ScrollingTextView.OnMargueeListener() {
            @Override
            public void onRollOver() {
                removeView(scrollingTextView);
            }
        });
    }

    private boolean isStop = false;

    public void stopScroll() {
        isStop = true;
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof ScrollingTextView) {
                ScrollingTextView childAt = (ScrollingTextView) getChildAt(i);
                childAt.stopScroll();
            }
        }
    }

    public void startScroll() {
        isStop = false;
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof ScrollingTextView) {
                ScrollingTextView childAt = (ScrollingTextView) getChildAt(i);
                childAt.startScroll();
            }
        }
    }

    public void onDestroy() {
        stopScroll();
        mHandler.removeMessages(0);
    }
}
