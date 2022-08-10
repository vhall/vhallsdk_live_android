package com.vhall.uilibs.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.utils.SurveyInternal;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.RtcInternal;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;
import com.vhall.uilibs.interactive.base.OnNoDoubleClickListener;
import com.vhall.vhss.TokenManger;
import com.vhall.vhss.data.SurveyInfoData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vhall.com.vss2.VssSdk;

/**
 * @author hkl
 * 问卷列表
 */

public class SurveyListDialog extends BaseBottomDialog implements View.OnClickListener {


    private List<SurveyInfoData> dataList;
    private RecyclerView recyclerView;
    private MyAdapter adapter = new MyAdapter();

    private OnItemClickLister onItemClickLister;


    public void setOnItemClickLister(OnItemClickLister onItemClickLister) {
        if (onItemClickLister != null) {
            this.onItemClickLister = onItemClickLister;
        }
    }

    public interface OnItemClickLister {
        void jump(SurveyInfoData info);
    }

    public SurveyListDialog(Context context, List<SurveyInfoData> dataList) {
        super(context);
        this.dataList = dataList;
    }

    public void setDataList(List<SurveyInfoData> dataList) {
        this.dataList = dataList;
        if (dataList != null && adapter != null) {
            adapter.setNewData(dataList);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_survey);
        recyclerView = findViewById(R.id.recycle_view);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        if (dataList != null && dataList.size() > 5) {
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800);
            params.bottomToBottom= ConstraintLayout.LayoutParams.PARENT_ID;
            recyclerView.setLayoutParams(params);
        }
        if (dataList != null) {
            adapter.setNewData(dataList);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            dismiss();
        }
    }

    @Override
    public void show() {
        super.show();
    }

    class MyAdapter extends BaseQuickAdapter<SurveyInfoData, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_survey);
        }

        /**
         * <p>
         * public String title;//问题 标题
         * public String questionId;////问题id
         * public String isAnswered;//是否已参与 1参加 0没有
         * public String createdAt;//问题 时间
         */
        @Override
        protected void convert(@NonNull BaseViewHolder helper, final SurveyInfoData info) {
            TextView tv_status = helper.getView(R.id.tv_status);
            TextView tv_title = helper.getView(R.id.tv_title);
            helper.setText(R.id.tv_time, RtcInternal.dateToString2(info.created_at));
            tv_title.setText(info.title);
            if (TextUtils.equals("0", info.is_answered)) {
                tv_status.setText("填写");
                tv_status.setTextColor(ContextCompat.getColor(mContext, R.color.color_3562FA));
                tv_title.setTextColor(ContextCompat.getColor(mContext, R.color.color_1a));
                tv_status.setClickable(true);
            } else {
                tv_status.setText("已填");
                tv_status.setTextColor(ContextCompat.getColor(mContext, R.color.color_66));
                tv_title.setTextColor(ContextCompat.getColor(mContext, R.color.color_66));
                tv_status.setClickable(false);
            }
            if (dataList == null || dataList.size() == 0 || helper.getAdapterPosition() == (dataList.size() - 1)) {
                helper.getView(R.id.iv_line).setVisibility(View.GONE);
            } else {
                helper.getView(R.id.iv_line).setVisibility(View.VISIBLE);
            }
            tv_status.setOnClickListener(new OnNoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    if (TextUtils.equals("0", info.is_answered) && onItemClickLister != null) {
                        onItemClickLister.jump(info);
                        dismiss();
                    }
                }
            });


        }
    }

}