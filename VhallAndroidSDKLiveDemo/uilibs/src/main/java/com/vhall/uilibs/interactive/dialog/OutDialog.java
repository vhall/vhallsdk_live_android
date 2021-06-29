package com.vhall.uilibs.interactive.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.vhall.uilibs.R;

/**
 * @author hkl
 */
public class OutDialog extends Dialog {


    private Context mContext;
    private OutDialogBuilder builder;

    public OutDialog(Context context, OutDialogBuilder builder) {
        super(context);
        mContext = context;
        this.builder = builder;
        setCanceledOnTouchOutside(false);
        init();
    }


    public void init() {
        setContentView(R.layout.dialog_out_login_exit);
        TextView tv1 = findViewById(R.id.tv_1);
        TextView tv2 = findViewById(R.id.tv_2);
        TextView tvTitle = findViewById(R.id.tv_title);

        if (!TextUtils.isEmpty(builder.title)) {
            tvTitle.setText(builder.title);
        }
        if (!TextUtils.isEmpty(builder.tv1)) {
            tv1.setText(builder.tv1);
        }
        if (!TextUtils.isEmpty(builder.tv2)) {
            tv2.setText(builder.tv2);
        }

        if (builder.color1 != 0) {
            tv1.setTextColor(ContextCompat.getColor(mContext, builder.color1));
        }
        if (builder.color2 != 0) {
            tv2.setTextColor(ContextCompat.getColor(mContext, builder.color2));
        }
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (builder.mCancelListener != null) {
                    builder.mCancelListener.click();
                }
            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (builder.mConfirmListener != null) {
                    builder.mConfirmListener.click();
                }
            }
        });
        Window win = this.getWindow();
        win.setBackgroundDrawableResource(android.R.color.transparent);
    }


    public interface ClickLister {
        void click();
    }
}
