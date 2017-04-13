package com.vhall.uilibs.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import master.flame.danmaku.ui.widget.DanmakuView;

/**
 * Created by huanan on 2017/4/6.
 */
public class DanmuView extends DanmakuView {
    public DanmuView(Context context) {
        super(context);
    }

    public DanmuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DanmuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
