package com.vhall.uilibs.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.vhall.uilibs.R;
import com.vhall.uilibs.util.CommonUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author hkl
 * Date: 2022/4/15 11:30 上午
 */
public class DirectorErrorView extends ConstraintLayout {
    Context context;
    public static final String TIME_START = "time_start";
    public static final String  TIME_END = "time_end";
    public static final String  NO_STREAM = "no_stream";

    public DirectorErrorView(@NonNull Context context) {
        super(context);
        this.context=context;
        initView();
    }

    public DirectorErrorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        initView();
    }

    public DirectorErrorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        initView();
    }

    public TextView tvErrorTitle;
    private Timer timer;
    private long time;
    private Handler handler=new Handler(Looper.getMainLooper());

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_director_error, this);
        tvErrorTitle=findViewById(R.id.tv_error_title);
    }

    public void call(String method,String  arg) {
        if (timer!=null){
            timer.cancel();
            timer = null;
        }
        switch (arg) {
            case TIME_START:
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                time += 1000;
                                tvErrorTitle.setText(String.format("云导播推流异常%s", CommonUtil.converLongTimeToStr(time)));
                            }
                        });
                    }
                }, 1000, 1000);
                setVisibility(VISIBLE);
                break;
            case TIME_END:
                setVisibility(GONE);
                break;

        }
    }

    public void release(){
      if (timer!=null){
          timer.cancel();
      }
    }
}
