package com.vhall.uilibs.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.uilibs.R;

/**
 * Created by huanan on 2017/3/14.
 */
public class SignInDialog extends AlertDialog {

    private static final String TAG = "SignInDialog";

    private Context mContext;
    private ImageView iv_close;
    private TextView tv_content;
    private Button btn_signin;


    private String signInId;
    private int countDownTime = 0;
    private OnSignInClickListener onSignInClickListener;


    private MyCount myCount;


    public void setSignInId(String signInId) {
        this.signInId = signInId;
    }

    public void setCountDownTime(int countDownTime) {
        this.countDownTime = countDownTime;
    }

    public void setOnSignInClickListener(OnSignInClickListener onSignInClickListener) {
        this.onSignInClickListener = onSignInClickListener;
    }

    public SignInDialog(Context context) {
        super(context);
        initView(context);
    }

    public SignInDialog(Context context, int theme) {
        super(context, theme);
        initView(context);
    }

    public SignInDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        View root = View.inflate(mContext, R.layout.alert_dialog_show_signin, null);
        iv_close = (ImageView) root.findViewById(R.id.image_signin_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInDialog.this.dismiss();
            }
        });
        tv_content = (TextView) root.findViewById(R.id.tv_signin_content);
        btn_signin = (Button) root.findViewById(R.id.btn_signin);
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSignInClickListener != null) {
                    onSignInClickListener.signIn(signInId);
                }
            }
        });
        this.setView(root);
        this.setCanceledOnTouchOutside(false); //点击外部不消失

        this.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (myCount != null) {
                    myCount.cancel();
                    myCount = null;
                }
                myCount = new MyCount(countDownTime * 1000, 100);
                myCount.start();
            }
        });
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (myCount != null) {
                    myCount.cancel();
                    myCount = null;
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "oncreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            Toast.makeText(mContext, "签到已结束", Toast.LENGTH_SHORT).show();
            SignInDialog.this.dismiss();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tv_content.setText("您有" + millisUntilFinished / 1000 + "秒的时间进行签到");
        }
    }

    public interface OnSignInClickListener {
        void signIn(String signId);
    }


}
