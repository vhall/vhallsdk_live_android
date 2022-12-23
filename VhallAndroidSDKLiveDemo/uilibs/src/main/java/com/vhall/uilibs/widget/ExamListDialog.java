package com.vhall.uilibs.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.MessageServer;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.exam.ExamMessageCallBack;
import com.vhall.business.module.exam.ExamServer;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;
import com.vhall.uilibs.util.CommonUtil;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.vhss.data.ExamListData;

import java.text.DecimalFormat;

/**
 * @author hkl
 * 考试列表
 */

public class ExamListDialog extends BaseBottomDialog implements View.OnClickListener {


    private RecyclerView recyclerView;
    private MyAdapter adapter = new MyAdapter();
    public int page = 0;
    public int limit = 10;
    public WebinarInfo webinarInfo;
    private ExamServer examServer;

    public ExamListDialog(Context context, WebinarInfo webinarInfo) {
        super(context);
        this.webinarInfo = webinarInfo;
        examServer = new ExamServer.Builder()
                .webinarInfo(webinarInfo)
                .context(context)
                .examMessageCallBack(new ExamMessageCallBack() {
                    @Override
                    public void examPush(MessageServer.MsgInfo msgInfo) {
                        loadData();
                    }

                    @Override
                    public void examEnd(MessageServer.MsgInfo msgInfo) {
                        loadData();
                    }

                    @Override
                    public void examAutoEnd(MessageServer.MsgInfo msgInfo) {
                        loadData();
                    }

                    @Override
                    public void examSendRank(MessageServer.MsgInfo msgInfo) {
                        loadData();
                    }

                    @Override
                    public void examAutoSendRank(MessageServer.MsgInfo msgInfo) {
                        loadData();
                    }
                })
                .build();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_exam_list);
        recyclerView = findViewById(R.id.recycle_view);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
        findViewById(R.id.tv_cancel).setOnLongClickListener(v -> false
        );
        loadData();
        adapter.setOnItemClickListener((adapter, view, position) -> {
            ExamListData.ListBean listBean = (ExamListData.ListBean) adapter.getItem(position);
            ExamWebView examWebView = new ExamWebView(mContext, examServer.getExamUrl(listBean.paper_id));
            examWebView.show();
            dismiss();
        });
    }

    private void loadData() {
        examServer.examGetPushedPaperList(new RequestDataCallbackV2<ExamListData>() {
            @Override
            public void onSuccess(ExamListData data) {
                if (!ListUtils.isEmpty(data.list)) {
                    adapter.setNewData(data.list);
                } else {
                    ToastUtil.showToast("没有数据");
                    dismiss();
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                ToastUtil.showToast("errorMsg");
                dismiss();
            }
        });
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
        if (examServer != null)
            loadData();
    }

    class MyAdapter extends BaseQuickAdapter<ExamListData.ListBean, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_exam);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, final ExamListData.ListBean info) {
            TextView tv_get_score = helper.getView(R.id.tv_get_score);
            helper.setText(R.id.tv_title, info.title);
            helper.setText(R.id.tv_push_time, "推送时间：" + info.push_time);
            // 1 限时 0不限时
            if (TextUtils.equals(info.limit_time_switch, "1"))
                helper.setText(R.id.tv_q_time, "限时：" + CommonUtil.examConverTimeToStr(info.limit_time));
            else
                helper.setText(R.id.tv_q_time, "限时：无");
            helper.setText(R.id.tv_all_score, info.total_score);
            helper.setText(R.id.tv_q_num, "题数：" + info.question_num);

            helper.getView(R.id.view).setVisibility(View.GONE);
            //答题是否结束 0.否 1.是
            if (TextUtils.equals(info.is_end, "1") && TextUtils.equals(info.status, "0")) {
                tv_get_score.setBackgroundResource(R.color.transparent);
                tv_get_score.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                helper.setText(R.id.tv_get_score, "已结束");
                helper.getView(R.id.view).setVisibility(View.VISIBLE);
            } else if (TextUtils.equals(info.status, "1")) {
                //是否作答 0.否 1.是
                tv_get_score.setBackgroundResource(R.color.transparent);
                tv_get_score.setTextColor(ContextCompat.getColor(mContext, R.color.color_FB3A32));
                DecimalFormat format = new DecimalFormat("#.0");
                String numberStr = format.format(info.right_rate);
                if (info.score > 0) {
                    if (info.total_score.equals(String.valueOf(info.score))) {
                        helper.setText(R.id.tv_get_score, "满分");
                    } else
                        helper.setText(R.id.tv_get_score, info.score + "分");

                } else {
                    if (info.right_rate == 0)
                        helper.setText(R.id.tv_get_score, "0分");
                    else
                        helper.setText(R.id.tv_get_score, numberStr + "%");
                }
            } else if (TextUtils.equals(info.status, "0")) {
                //是否作答 0.否 1.是
                tv_get_score.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                tv_get_score.setBackgroundResource(R.drawable.bg_cicle_red);
                helper.setText(R.id.tv_get_score, "答题");
            }
        }
    }

}