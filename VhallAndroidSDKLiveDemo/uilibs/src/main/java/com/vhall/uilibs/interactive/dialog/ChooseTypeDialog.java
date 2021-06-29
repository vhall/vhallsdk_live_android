package com.vhall.uilibs.interactive.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;
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

    public void setOnItemClickLister(onItemClickLister onItemClickLister) {
        if(onItemClickLister != null){
            this.onItemClickLister = onItemClickLister;
        }
    }


    public ChooseTypeDialog(Context context, List<String> stringList) {
        super(context);
        this.stringList = stringList;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_broadcast_type);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.tv_1).setOnClickListener(this);
        findViewById(R.id.tv_2).setOnClickListener(this);
        findViewById(R.id.tv_3).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        if (ListUtils.isEmpty(stringList)){
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
        if(v.getId() == R.id.tv_1){
            onItemClickLister.onItemClick(1);
        }else if(v.getId() == R.id.tv_2){
            onItemClickLister.onItemClick(2);
        }else if(v.getId() == R.id.tv_3){
            onItemClickLister.onItemClick(3);
        }else{
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