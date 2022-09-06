package com.vhall.uilibs.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vhall.business.MessageServer;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.CommonUtil;
import com.vhall.uilibs.util.DensityUtils;
import com.vhall.uilibs.util.ToastUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author hkl
 * 计时器
 */

public class TimerView extends ConstraintLayout implements View.OnClickListener {


    private OnItemClickLister onItemClickLister;
    private TextView tvTitle, tv1, tv2, tv3, tv4, tvMiddle;
    private Context mContext;
    private ImageView tvCancel;
    private Timer timer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean pause = false;
    private TimerTask timerTask;

    public TimerView(Context context) {
        super(context);
        init(context);
    }

    public TimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.layout_timer, this);
        tvTitle = findViewById(R.id.tv_title);
        tvCancel = findViewById(R.id.tv_cancel);
        tv1 = findViewById(R.id.tv_1);
        tv2 = findViewById(R.id.tv_2);
        tv3 = findViewById(R.id.tv_3);
        tv4 = findViewById(R.id.tv_4);
        tvMiddle = findViewById(R.id.tv_middle);
        tvCancel.setOnClickListener(this);
    }

    public void setOnItemClickLister(OnItemClickLister onItemClickLister) {
        if (onItemClickLister != null) {
            this.onItemClickLister = onItemClickLister;
        }
    }

    private int duration = 0;
    private int time = 0;
    int tvTime;

    public void setData(MessageServer.TimerData timerData) {
        if (timer != null) {
            timer.cancel();
        }
        if (timerData != null) {
            if ("1".equals(timerData.is_all_show)) {
                if (timerData.remain_time > 0) {
                    duration = timerData.remain_time - 1;
                    time = 0;
                    dealDuration(duration);
                    setTextColor(false);
                    tvTitle.setText(String.format("%s 倒计时进行中……", CommonUtil.converTimeToStr(Long.parseLong(timerData.duration))));
                } else {
                    duration = 0;
                    time = -timerData.remain_time;
                    dealDuration(time);
                    setTextColor(true);
                }
                setVisibility(VISIBLE);
                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!pause) {
                                    if (duration <= 0) {
                                        if ("1".equals(timerData.is_timeout)) {
                                            tvTime = time++;
                                            setTextColor(true);
                                        } else {
                                            if (onItemClickLister != null) {
                                                onItemClickLister.dismiss();
                                            }
                                            setVisibility(GONE);
                                            timer.cancel();
                                        }
                                    } else {
                                        tvTime = duration--;
                                    }
                                    dealDuration(tvTime);
                                } else {
                                    tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_FC9600));
                                    tvTitle.setText("已暂停");
                                }
                            }
                        });
                    }
                };
                timer.schedule(timerTask, 1000, 1000);
            }
        }
    }

    private void setTextColor(boolean timeOut) {
        if (timeOut) {
            tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_FB3A32));
            tv1.setTextColor(ContextCompat.getColor(mContext, R.color.color_FB3A32));
            tv2.setTextColor(ContextCompat.getColor(mContext, R.color.color_FB3A32));
            tv3.setTextColor(ContextCompat.getColor(mContext, R.color.color_FB3A32));
            tv4.setTextColor(ContextCompat.getColor(mContext, R.color.color_FB3A32));
            tvMiddle.setTextColor(ContextCompat.getColor(mContext, R.color.color_FB3A32));
            tvTitle.setText("已超时，从00:00开始正向计时");
        } else {
            tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_0FBB5A));
            tv1.setTextColor(ContextCompat.getColor(mContext, R.color.color_22));
            tv2.setTextColor(ContextCompat.getColor(mContext, R.color.color_22));
            tv3.setTextColor(ContextCompat.getColor(mContext, R.color.color_22));
            tv4.setTextColor(ContextCompat.getColor(mContext, R.color.color_22));
            tvMiddle.setTextColor(ContextCompat.getColor(mContext, R.color.color_22));
        }
    }

    public void setPause(boolean pause) {
        this.pause = pause;
        if (pause) {
            tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.color_FC9600));
            tvTitle.setText("已暂停");
        }
    }

    public void dismiss() {
        if (timer != null) {
            timer.cancel();
            timer=null;
            pause = false;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            setVisibility(GONE);
            if (onItemClickLister != null) {
                onItemClickLister.cancel();
            }
        }
    }

    public interface OnItemClickLister {
        //自己手动关闭
        void cancel();

        //计时器结束
        void dismiss();
    }

    private void dealDuration(int duration) {

        if (duration > 3599) {
            dismiss();
            setVisibility(GONE);
            if (onItemClickLister != null) {
                onItemClickLister.dismiss();
            }
        }
        int mi = 60;
        int minute = (duration) / mi;
        int second = (duration - minute * mi);

        tv1.setText(String.valueOf(minute / 10));
        tv2.setText(String.valueOf(minute % 10));
        tv3.setText(String.valueOf(second / 10));
        tv4.setText(String.valueOf(second % 10));

    }
}

