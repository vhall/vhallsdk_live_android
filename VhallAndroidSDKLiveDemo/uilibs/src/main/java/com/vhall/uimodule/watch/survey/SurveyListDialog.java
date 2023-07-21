package com.vhall.uimodule.watch.survey;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.MessageServer;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.survey.SurveyMessageCallBack;
import com.vhall.business.module.survey.SurveyServer;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.utils.ListUtils;
import com.vhall.uimodule.widget.OnNoDoubleClickListener;
import com.vhall.vhss.data.SurveyInfoData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author hkl
 * 问卷列表
 */

public class SurveyListDialog extends BaseBottomDialog implements View.OnClickListener {


    private RecyclerView recyclerView;
    private MyAdapter adapter = new MyAdapter();
    private SurveyServer surveyServer;
    List<SurveyInfoData> mDataList;

    public SurveyListDialog(Context context, WebinarInfo webinarInfo) {
        super(context);
        surveyServer = new SurveyServer.Builder()
                .webinarInfo(webinarInfo)
                .surveyMessageCallBack(new SurveyMessageCallBack() {
                    @Override
                    public void questionAnswerSend(MessageServer.MsgInfo msgInfo) {
                        if (isShowing()) {
                            loadData();
                        }
                    }
                })
                .build();
        loadData();
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getMContext()));
        recyclerView.setAdapter(adapter);

        refreshDataList();
    }

    private void refreshDataList() {
        if (null != mDataList && mDataList.size() > 0) {
            if (mDataList.size() > 5) {
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800);
                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                recyclerView.setLayoutParams(params);
            }
            adapter.setList(mDataList);
        }
    }

    private void loadData() {
        surveyServer.getHistorySurveyList(new RequestDataCallback() {
            @Override
            public void onSuccess(Object o) {
                List<SurveyInfoData> dataList = (List<SurveyInfoData>) o;
                if (ListUtils.isEmpty(dataList)) {
                    showToast("没有更多数据");
                    dismiss();
                    return;
                }
                mDataList = dataList;
                if (null != recyclerView) {
                    refreshDataList();
                }
                show();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                showToast(errorMsg);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel || v.getId() == R.id.root) {
            dismiss();
        }
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
            try {
                SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
                Date date = sdf.parse(info.created_at);
                sdf = new SimpleDateFormat( "MM-dd HH:mm" );
                helper.setText(R.id.tv_time, sdf.format(date));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            tv_title.setText(info.title);
            if (TextUtils.equals("0", info.is_answered)) {
                tv_status.setText("填写");
                tv_status.setTextColor(ContextCompat.getColor(getMContext(), R.color.color_3562FA));
                tv_title.setTextColor(ContextCompat.getColor(getMContext(), R.color.color_1a));
                tv_status.setClickable(true);
            } else {
                tv_status.setText("已填");
                tv_status.setTextColor(ContextCompat.getColor(getMContext(), R.color.color_66));
                tv_title.setTextColor(ContextCompat.getColor(getMContext(), R.color.color_66));
                tv_status.setClickable(false);
            }
            tv_status.setOnClickListener(new OnNoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    if (TextUtils.equals("0", info.is_answered)) {
                        new SurveyWebView(getMContext(), surveyServer.getSurveyUrl(info.question_id)).show();
                        dismiss();
                    }
                }
            });
        }
    }
}