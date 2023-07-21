package com.vhall.uimodule.watch.card;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.MessageServer;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.card.CardMessageCallBack;
import com.vhall.business.module.card.CardServer;
import com.vhall.logmanager.VLog;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.utils.ListUtils;
import com.vhall.vhss.data.CardsInfoData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author hkl
 * 公告列表
 */

public class CardListDialog extends BaseBottomDialog implements View.OnClickListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private MyAdapter adapter = new MyAdapter();
    public int page = 0;
    public int limit = 10;
    public WebinarInfo webinarInfo;
    private CardServer cardServer;

    public CardListDialog(Context context, WebinarInfo webinarInfo, boolean showDialog) {
        super(context);
        this.webinarInfo = webinarInfo;
        cardServer = new CardServer.Builder()
                .webinarInfo(webinarInfo)
                .cardMessageCallBack(new CardMessageCallBack() {
                    @Override
                    public void cardSend(MessageServer.MsgInfo msgInfo) {
                        if(CardListDialog.this.isShowing())
                            onRefreshData(false);
                    }

                    @Override
                    public void cardUpdate(MessageServer.MsgInfo msgInfo) {
                        if(CardListDialog.this.isShowing())
                            onRefreshData(false);
                    }

                    @Override
                    public void cardDelete(MessageServer.MsgInfo msgInfo) {
                        VLog.d("CardListDialog",msgInfo.card_delete_ids);
                        if(CardListDialog.this.isShowing())
                            onRefreshData(false);
                    }
                })
                .build();
        setOnDismissListener(dialog -> {
            cardServer = null;
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
        findViewById(R.id.tv_top).setBackgroundResource(R.mipmap.icon_card_top);

        refreshLayout.setOnRefreshListener(() -> onRefreshData(false));

        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            page++;
            loadData(false);
        });

        adapter.setOnItemClickListener( new OnItemClickListener() {
                                            @Override
                                            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                                                CardDialog dialog = new CardDialog(getMContext(),adapter.getData().get(i));
                                                dialog.loadCardInfo();
                                                dialog.show();
                                                dismiss();
                                            }
                                        });
    }

    private void refreshData(CardsInfoData cardsInfoData) {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }

        if (page == 0) {
            if (null != recyclerView) {
                adapter.setList(cardsInfoData.list);
            }
        } else {
            adapter.addData(cardsInfoData.list);
        }
        if (!ListUtils.isEmpty(cardsInfoData.list)) {
            if (cardsInfoData.list.size() < limit) {
                adapter.getLoadMoreModule().loadMoreEnd();
            } else {
                adapter.getLoadMoreModule().loadMoreComplete();
            }
        } else {
            adapter.getLoadMoreModule().loadMoreEnd();
        }
    }

    public void onRefreshData(boolean showDialog) {
        page = 0;
        loadData(showDialog);
    }

    private void loadData(boolean showDialog) {
        cardServer.getCardList(page+1, limit, new RequestDataCallbackV2<CardsInfoData>() {
            @Override
            public void onSuccess(CardsInfoData cardsInfoData) {

                if (cardsInfoData == null) {
                    if (page == 0) {
                        showToast("没有更多数据");
                        dismiss();
                    }
                    return;
                }
                if (page == 0 && ListUtils.isEmpty(cardsInfoData.list)) {
                    showToast("没有更多数据");
                    dismiss();
                    return;
                }

                if (showDialog) {
                    show();
                }

                refreshData(cardsInfoData);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                showToast(errorMsg);

                if (page == 0) {
                    adapter.setList(null);
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

    class MyAdapter extends BaseQuickAdapter<CardsInfoData.CardInfo, BaseViewHolder> implements LoadMoreModule {

        public MyAdapter() {
            super(R.layout.item_notice);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, final CardsInfoData.CardInfo info) {
            TextView tv_title = helper.getView(R.id.tv_title);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
                Date date = sdf.parse(info.push_time);
                sdf = new SimpleDateFormat( "MM-dd HH:mm" );
                helper.setText(R.id.tv_time, sdf.format(date));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            tv_title.setText(info.title);
        }
    }
}