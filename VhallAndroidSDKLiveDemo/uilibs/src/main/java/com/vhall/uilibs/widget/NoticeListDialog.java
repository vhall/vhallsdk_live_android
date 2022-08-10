package com.vhall.uilibs.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.RtcInternal;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;
import com.vhall.uilibs.interactive.base.OnNoDoubleClickListener;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.vhss.data.NoticeListInfoData;
import com.vhall.vhss.data.SurveyInfoData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hkl
 * 公告列表
 */

public class NoticeListDialog extends BaseBottomDialog implements View.OnClickListener {


    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private MyAdapter adapter = new MyAdapter();
    public int page = 0;
    public int limit = 10;
    public String roomId;

    public NoticeListDialog(Context context, String roomId) {
        super(context);
        this.roomId = roomId;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_notice);
        recyclerView = findViewById(R.id.recycle_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);

        loadData();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshData();
            }
        });

        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                page++;
                loadData();
            }
        }, recyclerView);
    }

    public void onRefreshData() {
        page = 0;
        loadData();
    }

    private void loadData() {
        VhallSDK.getNoticeList(roomId, page, limit, new RequestDataCallbackV2<NoticeListInfoData>() {
            @Override
            public void onSuccess(NoticeListInfoData noticeListInfoData) {
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                if (noticeListInfoData == null) {
                    return;
                }
                if (page == 0) {
                    adapter.setNewData(noticeListInfoData.list);
                    adapter.disableLoadMoreIfNotFullPage(recyclerView);
                } else {
                    adapter.addData(noticeListInfoData.list);
                    adapter.notifyItemChanged(page * limit - 1);
                    if (!ListUtils.isEmpty(noticeListInfoData.list)) {
                        if (noticeListInfoData.list.size() < limit) {
                            adapter.loadMoreEnd();
                        } else {
                            adapter.loadMoreComplete();
                        }
                    } else {
                        adapter.loadMoreEnd();
                    }

                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.e("vhall_", "5" );
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                ToastUtil.showToast(errorCode);

                if (page == 0) {
                    adapter.setNewData(null);
                    adapter.disableLoadMoreIfNotFullPage(recyclerView);
                } else {
                    adapter.loadMoreComplete();
                }
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
    }

    class MyAdapter extends BaseQuickAdapter<NoticeListInfoData.ListBean, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_notice);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, final NoticeListInfoData.ListBean info) {
            TextView tv_title = helper.getView(R.id.tv_title);
            helper.setText(R.id.tv_time, RtcInternal.dateToString2(info.created_at));
            tv_title.setText(info.content.content);
            if (helper.getAdapterPosition() == (adapter.getData().size() - 1)) {
                helper.getView(R.id.iv_line).setVisibility(View.GONE);
            } else {
                helper.getView(R.id.iv_line).setVisibility(View.VISIBLE);
            }
        }
    }

}