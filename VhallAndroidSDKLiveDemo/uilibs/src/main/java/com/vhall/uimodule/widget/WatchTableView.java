package com.vhall.uimodule.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vhall.uimodule.base.IBase;


/**
 * @author hkl
 * Date: 2022/12/8 13:56
 */
public class WatchTableView extends FrameLayout implements IBase {
    public WatchTableView(@NonNull Context context) {
        super(context);
    }

    public WatchTableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WatchTableView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}