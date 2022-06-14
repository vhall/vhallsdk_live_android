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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.LotteryPrizeListInfo;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.RtcInternal;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;
import com.vhall.uilibs.interactive.base.OnNoDoubleClickListener;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.watch.LotteryCommitAdapter;
import com.vhall.vhss.data.LotteryCheckData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * @author hkl
 * 抽奖列表
 */

public class LotteryListDialog extends BaseBottomDialog implements View.OnClickListener {


    private List<LotteryCheckData> dataList;
    private RecyclerView recyclerView;
    private MyAdapter adapter = new MyAdapter();
    // 中奖者列表   提交中奖信息列表
    private RecyclerView commitRecyclerView;
    private TextView tvCommit, tv_top;
    private LinearLayout ll_lottery_commit;

    private OnItemClickLister onItemClickLister;

    private LotteryCommitAdapter lotteryCommitAdapter;
    private String webinar_id;
    private String lotteryId;


    public void setOnItemClickLister(OnItemClickLister onItemClickLister) {
        if (onItemClickLister != null) {
            this.onItemClickLister = onItemClickLister;
        }
    }

    public interface OnItemClickLister {
        void update();
    }

    public LotteryListDialog(Context context, List<LotteryCheckData> dataList, String webinar_id) {
        super(context);
        this.dataList = dataList;
        this.webinar_id = webinar_id;
    }

    public void setDataList(List<LotteryCheckData> dataList) {
        this.dataList = dataList;
        if (dataList != null && adapter != null && dataList.size() > 1) {
            adapter.setNewData(dataList);
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
                tv_top.setVisibility(View.VISIBLE);
                ll_lottery_commit.setVisibility(View.GONE);
            }
        } else {
            if (!ListUtils.isEmpty(dataList) && dataList.size() == 1) {
                lotteryId =dataList.get(0).id;
            }
            getPrizeInfo();
        }

    }

    private void getPrizeInfo() {
        VhallSDK.getPrizeInfo(webinar_id, new RequestDataCallback() {
            @Override
            public void onSuccess(Object data) {
                List<LotteryPrizeListInfo> lotteryPrizeListInfos = (List<LotteryPrizeListInfo>) data;
                if (lotteryPrizeListInfos != null && lotteryPrizeListInfos.size() > 0) {
                    lotteryCommitAdapter.setData(lotteryPrizeListInfos);
                    recyclerView.setVisibility(View.INVISIBLE);
                    ll_lottery_commit.setVisibility(View.VISIBLE);
                    tv_top.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getContext(), "返回数据缺少必要参数", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_lottery);
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);

        tv_top = findViewById(R.id.tv_top);
        commitRecyclerView = findViewById(R.id.commit_recycle_view);
        tvCommit = findViewById(R.id.tv_lottery_commit);
        ll_lottery_commit = findViewById(R.id.ll_lottery_commit);

        commitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lotteryCommitAdapter = new LotteryCommitAdapter(commitRecyclerView);
        commitRecyclerView.setAdapter(lotteryCommitAdapter);

        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        tvCommit.setOnClickListener(this);

        if (dataList != null && dataList.size() > 1) {
            adapter.setNewData(dataList);
            recyclerView.setVisibility(View.VISIBLE);
            tv_top.setVisibility(View.VISIBLE);
            ll_lottery_commit.setVisibility(View.GONE);
        } else {
            getPrizeInfo();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            dismiss();
        }
        if (v.getId() == R.id.tv_lottery_commit) {
            //提交中奖信息

            String name = "", phone = "";
            for (int i = 0; i < lotteryCommitAdapter.getDataList().size(); i++) {
                LotteryPrizeListInfo lotteryPrizeListInfo = lotteryCommitAdapter.getDataList().get(i);
                if (lotteryPrizeListInfo.is_required == 1) {
                    if (TextUtils.isEmpty(lotteryPrizeListInfo.field_value)) {
                        Toast.makeText(getContext(), "请填写" + lotteryPrizeListInfo.field, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if ("name".equals(lotteryPrizeListInfo.field_key)) {
                    name = lotteryPrizeListInfo.field_value;
                }
                if ("phone".equals(lotteryPrizeListInfo.field_key)) {
                    phone = lotteryPrizeListInfo.field_value;
                }
            }

            VhallSDK.submitLotteryInfo(VhallSDK.getUserId(), lotteryId, name, phone, markToString(lotteryCommitAdapter.getDataList()), new RequestCallback() {
                @Override
                public void onSuccess() {
                    ToastUtil.showToast("提交成功");
                    if (onItemClickLister != null) {
                        onItemClickLister.update();
                    }
                    dismiss();
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    String msg = errorMsg;
                    if (TextUtils.isEmpty(msg)) {
                        msg = "提交失败，请重试!";
                    }
                    ToastUtil.showToast(msg);
                }
            });
        }
    }


    private String markToString(List<LotteryPrizeListInfo> data) {
        String mark = "";
        if (data != null && data.size() > 0) {
            JSONArray array = new JSONArray();
            for (LotteryPrizeListInfo datum : data) {
                JSONObject object = new JSONObject();
                try {
                    object.put("field", datum.field);
                    object.put("is_required", datum.is_required);
                    object.put("is_system", datum.is_system);
                    object.put("field", datum.field);
                    object.put("rank", datum.rank);
                    object.put("field_value", datum.field_value);
                    object.put("field_key", datum.field_key);
                    object.put("placeholder", datum.placeholder);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(object);
            }
            mark = array.toString();
        }
        return mark;
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    class MyAdapter extends BaseQuickAdapter<LotteryCheckData, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_lottery);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, final LotteryCheckData info) {
            TextView tv_status = helper.getView(R.id.tv_status);
            TextView tv_title = helper.getView(R.id.tv_title);
            helper.setText(R.id.tv_time, info.created_at);
            String award_name = "礼品";
            if (info.award_snapshoot != null && !TextUtils.isEmpty(info.award_snapshoot.award_name)) {
                award_name = info.award_snapshoot.award_name;
            }
            tv_title.setText(award_name);
            //是否已领奖 0-否 1-是
            if (0 == info.take_award) {
                tv_status.setText("领取");
                tv_status.setTextColor(ContextCompat.getColor(mContext, R.color.color_3562FA));
            } else {
                tv_status.setText("已领取");
                tv_status.setTextColor(ContextCompat.getColor(mContext, R.color.color_FC9600));
            }
            //是否需要领奖 0-否 1-是
            if (0 == info.need_take_award) {
                tv_status.setText("已中奖");
                tv_status.setTextColor(ContextCompat.getColor(mContext, R.color.color_FC9600));
            }
            if (dataList == null || dataList.size() == 0 || helper.getAdapterPosition() == (dataList.size() - 1)) {
                helper.getView(R.id.iv_line).setVisibility(View.GONE);
            } else {
                helper.getView(R.id.iv_line).setVisibility(View.VISIBLE);
            }
            tv_status.setOnClickListener(new OnNoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    if (0 == info.take_award && 1 == info.need_take_award) {
                        lotteryId = info.id;
                        getPrizeInfo();
                    }
                }
            });


        }
    }

}