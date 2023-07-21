package com.vhall.uimodule.watch.watchlive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.widget.ItemClickLister;

/**
 * @author hkl
 */

public class ApplyHandUpDialog extends BaseBottomDialog implements View.OnClickListener {

    private TextView tvHand;
    private boolean isHanding = false;
    private MyCount myCount;
    public static final int clickTypeHand = 1;
    public static final int clickTypeCancelHand = 2;

    public ApplyHandUpDialog(Context context) {
        super(context);
    }

    private ItemClickLister myItemClickLister;

    public void setOnItemClickLister(ItemClickLister myItemClickLister) {
        this.myItemClickLister = myItemClickLister;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_apply_hand_up);
        tvHand = findViewById(R.id.tv_hand);
        tvHand.setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_hand) {
            if (myItemClickLister != null)
                //20230131 为与saas同步，该处屏蔽取消申请上麦的逻辑
                if (!isHanding) {
                    myItemClickLister.onItemClick(isHanding ? clickTypeCancelHand : clickTypeHand);
                }
        } else if (id == R.id.root || id == R.id.tv_cancel) {
            dismiss();
        }
    }

    public void handUp() {
        isHanding = true;
        if (myCount != null) {
            myCount.cancel();
            myCount = null;
        }
        myCount = new MyCount(30 * 1000, 1000);
        myCount.start();
    }

    public void cancelHandUp() {
        isHanding = false;
        tvHand.setText("申请上麦");
        if (myCount != null) {
            myCount.cancel();
            myCount = null;
        }
    }

    class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            cancelHandUp();
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onTick(long millisUntilFinished) {
            tvHand.setText(String.format("等待中...（%ds）", millisUntilFinished / 1000));
        }
    }

}