package com.vhall.uimodule.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.vhall.uimodule.R;


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

    TextView tvTitle;

    public void init() {
        if (builder.layout != 0) {
            setContentView(builder.layout);
        } else
            setContentView(R.layout.dialog_out_login_exit);
        TextView tv1 = findViewById(R.id.tv_1);
        TextView tv2 = findViewById(R.id.tv_2);
        tvTitle = findViewById(R.id.tv_dialog_title);

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
                if (builder.dismiss1)
                    dismiss();
                if (builder.ClickLister1 != null) {
                    builder.ClickLister1.click();
                }
            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder.dismiss2)
                    dismiss();
                if (builder.ClickLister2 != null) {
                    builder.ClickLister2.click();
                }
            }
        });
        Window win = this.getWindow();
        win.setBackgroundDrawableResource(android.R.color.transparent);
    }


    public void setTitleText(String title) {
        if (tvTitle != null && !TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
    }

    public interface ClickLister {
        void click();
    }
}
