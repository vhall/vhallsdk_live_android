package com.vhall.uilibs.interactive.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;


import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;
import com.vhall.uilibs.util.CommonUtil;
import com.vhall.uilibs.util.ListUtils;

import java.util.List;

/**
 * @author hkl
 */

public class ChooseTypeDialog extends BaseBottomDialog implements View.OnClickListener {

    private onItemClickLister onItemClickLister = new onItemClickLister() {
        @Override
        public void onItemClick(int option) {

        }
    };
    private List<String> stringList;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;

    public void setOnItemClickLister(onItemClickLister onItemClickLister) {
        if (onItemClickLister != null) {
            this.onItemClickLister = onItemClickLister;
        }
    }


    public ChooseTypeDialog(Context context, List<String> stringList) {
        super(context);
        this.stringList = stringList;
    }

    private int unClickAble = -1;

    public void setViewUnClickable() {
        tv2.setTextColor(ContextCompat.getColor(mContext, R.color.color_99));
        Drawable dra = mContext.getResources().getDrawable(R.drawable.svg_ic_director_no_limit);
        dra.setBounds(0, 0, dra.getMinimumWidth(), dra.getMinimumHeight());
        tv2.setCompoundDrawables(null, null, dra, null);
        tv2.setCompoundDrawablePadding(CommonUtil.dip2px(mContext, 10f));
        unClickAble = 2;
    }

    public void setViewClickable() {
        tv2.setTextColor(ContextCompat.getColor(mContext, R.color.color_22));
        tv2.setCompoundDrawables(null, null, null, null);
        unClickAble = -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_broadcast_type);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        tv1 = findViewById(R.id.tv_1);
        tv1.setOnClickListener(this);
        tv2 = findViewById(R.id.tv_2);
        tv2.setOnClickListener(this);
        tv3 = findViewById(R.id.tv_3);
        tv3.setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        if (ListUtils.isEmpty(stringList)) {
            return;
        }
        for (int i = 0; i < stringList.size(); i++) {
            if (i == 0) {
                TextView tv1 = findViewById(R.id.tv_1);
                tv1.setVisibility(View.VISIBLE);
                tv1.setText(stringList.get(i));
            } else if (i == 1) {
                TextView tv2 = findViewById(R.id.tv_2);
                tv2.setVisibility(View.VISIBLE);
                tv2.setText(stringList.get(i));
            } else if (i == 2) {
                TextView tv3 = findViewById(R.id.tv_3);
                tv3.setVisibility(View.VISIBLE);
                tv3.setText(stringList.get(i));
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_1) {
            onItemClickLister.onItemClick(1);
            dismiss();
        } else if (v.getId() == R.id.tv_2) {
            if (unClickAble != 2){
                onItemClickLister.onItemClick(2);
                dismiss();
            }
        } else if (v.getId() == R.id.tv_3) {
            onItemClickLister.onItemClick(3);
        } else {
            dismiss();
        }
    }

    @Override
    public void show() {
        super.show();
    }

    public interface onItemClickLister {
        void onItemClick(int option);
    }
}