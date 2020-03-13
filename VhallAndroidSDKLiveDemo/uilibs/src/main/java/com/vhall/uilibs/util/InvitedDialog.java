package com.vhall.uilibs.util;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vhall.uilibs.R;

/**
 * Created by zwp on 2019/1/2
 */
public class InvitedDialog extends AlertDialog implements View.OnClickListener {

    private Context mContext;
    private ImageView imgClose;
    private TextView tvAgree;
    private TextView tvDisagree;
    private MyCount count;
    private int countTime = 30;

    private RefuseInviteListener refuseInviteListener;

    public void setRefuseInviteListener(RefuseInviteListener refuseInviteListener) {
        this.refuseInviteListener = refuseInviteListener;
    }

    public InvitedDialog(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public InvitedDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        initView();
    }


    private void initView() {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.alert_dialog_show_invitedmic, null);
        imgClose = rootView.findViewById(R.id.image_invited_close);
        tvAgree = rootView.findViewById(R.id.tv_invited_agree);
        tvDisagree = rootView.findViewById(R.id.tv_invited_disagree);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (refuseInviteListener!=null){
                    refuseInviteListener.refuseInvite();
                }
            }
        });
        setView(rootView);
        setCanceledOnTouchOutside(false);

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                tvAgree.setText(mContext.getString(R.string.vhall_agree) + "(" + countTime + "s)");
                if (count == null) {
                    count = new MyCount(countTime * 1000, 1000);
                }
                count.start();
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (count != null) {
                    count.cancel();
                }
            }
        });
    }

    public void setPositiveOnClickListener(View.OnClickListener onClickListener) {
        tvAgree.setOnClickListener(onClickListener);
    }

    public void setNegativeOnClickListener(View.OnClickListener onClickListener){
        tvDisagree.setOnClickListener(onClickListener);
    }

    public void setCountTime(int countTime) {
        this.countTime = countTime;
        if (count != null) {
            count.cancel();
            count = null;
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }


    class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tvAgree.setText(mContext.getString(R.string.vhall_agree) + "(" + millisUntilFinished / 1000 + "s)");
        }

        @Override
        public void onFinish() {
            if (isShowing()) {
                dismiss();
            }
            if (refuseInviteListener!=null){
                refuseInviteListener.refuseInvite();
            }
        }
    }

   public interface RefuseInviteListener {
        void refuseInvite();
   }
}
