package com.vhall.uimodule.module.notice;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.MessageServer;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.notice.NoticeMessageCallBack;
import com.vhall.business.module.notice.NoticeServer;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.utils.ListUtils;
import com.vhall.vhss.data.NoticeListInfoData;

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
    public WebinarInfo webinarInfo;
    private NoticeServer noticeServer;

    public NoticeListDialog(Context context, WebinarInfo webinarInfo, boolean showDialog) {
        super(context);
        this.webinarInfo = webinarInfo;
        noticeServer = new NoticeServer.Builder()
                .webinarInfo(webinarInfo)
                .noticeMessageCallBack(new NoticeMessageCallBack() {
                    @Override
                    public void noticeSend(MessageServer.MsgInfo msgInfo) {
                        onRefreshData(false);
                    }
                })
                .build();
        setOnDismissListener(dialog -> {
            noticeServer = null;
        });
        onRefreshData(true);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getMContext()));
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(() -> onRefreshData(false));

        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            page++;
            loadData(false);
        });
    }

    private void refreshData(NoticeListInfoData noticeListInfoData) {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }

        if (page == 0) {
            if (null != recyclerView) {
                adapter.setNewData(noticeListInfoData.list);
            }
        } else {
            adapter.addData(noticeListInfoData.list);
            adapter.notifyItemChanged(page * limit - 1);
            if (!ListUtils.isEmpty(noticeListInfoData.list)) {
                if (noticeListInfoData.list.size() < limit) {
                    adapter.getLoadMoreModule().loadMoreEnd();
                } else {
                    adapter.getLoadMoreModule().loadMoreComplete();
                }
            } else {
                adapter.getLoadMoreModule().loadMoreEnd();
            }
        }
    }

    public void onRefreshData(boolean showDialog) {
        page = 0;
        loadData(showDialog);
    }

    private void loadData(boolean showDialog) {
        noticeServer.getNoticeList(page, limit, new RequestDataCallbackV2<NoticeListInfoData>() {
            @Override
            public void onSuccess(NoticeListInfoData noticeListInfoData) {

                if (noticeListInfoData == null) {
                    if (page == 0) {
                        showToast("没有更多数据");
                        dismiss();
                    }
                    return;
                }
                if (page == 0 && ListUtils.isEmpty(noticeListInfoData.list)) {
                    showToast("没有更多数据");
                    dismiss();
                    return;
                }

                if (showDialog) {
                    show();
                }

                refreshData(noticeListInfoData);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                showToast(errorCode);

                if (page == 0) {
                    adapter.setNewData(null);
                    showToast("没有更多数据");
                    dismiss();
                } else {
                    adapter.getLoadMoreModule().loadMoreComplete();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel || v.getId() == R.id.root) {
            dismiss();
        }
    }

    class MyAdapter extends BaseQuickAdapter<NoticeListInfoData.ListBean, BaseViewHolder> implements LoadMoreModule {

        public MyAdapter() {
            super(R.layout.item_notice);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, final NoticeListInfoData.ListBean info) {
            TextView tv_title = helper.getView(R.id.tv_title);
            helper.setText(R.id.tv_time, info.created_at);
            tv_title.setText(info.content.content);
        }
    }
}