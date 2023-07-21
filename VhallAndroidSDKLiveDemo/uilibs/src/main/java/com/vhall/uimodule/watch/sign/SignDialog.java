package com.vhall.uimodule.watch.sign;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.vhall.business.MessageServer;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.sign.SignServer;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.utils.CommonUtil;

/**
 * @author hkl
 * 公告列表
 */

public class SignDialog extends BaseBottomDialog implements View.OnClickListener {


    public WebinarInfo webinarInfo;
    private SignServer signServer;
    private MessageServer.MsgInfo msgInfo;
    private TextView tvTitle, tvTime;
    private MyCount myCount;

    public SignDialog(Context context, WebinarInfo webinarInfo, MessageServer.MsgInfo msgInfo) {
        super(context);
        this.webinarInfo = webinarInfo;
        this.msgInfo = msgInfo;
        signServer = new SignServer.Builder()
                .webinarInfo(webinarInfo)
                .build();


        if (myCount != null) {
            myCount.cancel();
            myCount = null;
        }
        long sign_show_time = Long.parseLong(msgInfo.sign_show_time);
        myCount = new MyCount(sign_show_time * 1000, 100);
        myCount.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_sign);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        findViewById(R.id.tv_sign).setOnClickListener(this);
        tvTitle = findViewById(R.id.tv_title);
        tvTime = findViewById(R.id.tv_time);
        String title = "主持人发起了签到";
        if (!TextUtils.isEmpty(msgInfo.signTitle)) {
            title = msgInfo.signTitle;
        }
        tvTitle.setText(title);
    }

    @Override
    public void dismiss() {
        super.dismiss();
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
            showToast("签到已结束");
            dismiss();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tvTime.setText(CommonUtil.converLongTimeToStr1(millisUntilFinished));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel || v.getId() == R.id.root) {
            dismiss();
        } else if (v.getId() == R.id.tv_sign) {
            signServer.performSignIn(msgInfo.id, new RequestCallback() {
                @Override
                public void onSuccess() {
                    showToast("签到成功");
                    dismiss();
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        }
    }
}