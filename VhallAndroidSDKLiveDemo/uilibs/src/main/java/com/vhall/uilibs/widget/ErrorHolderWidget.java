package com.vhall.uilibs.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vhall.uilibs.R;

public class ErrorHolderWidget extends FrameLayout {
    private TextView tv_error_title,tv_error_btn;
    public ErrorHolderWidget(@NonNull Context context) {
        super(context);
        init();
    }

    public ErrorHolderWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ErrorHolderWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.layout_broadcast_error,this);

        this.tv_error_title = findViewById(R.id.tv_error_title);
        this.tv_error_btn = findViewById(R.id.tv_error_btn);
    }

}
