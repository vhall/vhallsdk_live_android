package com.vhall.uimodule.module.gift;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.vhss.data.GiftListData;

/**
 * @author hkl
 * 礼物列表
 */

public class GiftListDialog extends BaseBottomDialog implements View.OnClickListener {
    private RecyclerView recyclerView;
    private MyAdapter adapter = new MyAdapter();
    public String roomId;
    private int check = -1;

    public GiftListDialog(Context context, String roomId) {
        super(context);
        this.roomId = roomId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_gift);
        recyclerView = findViewById(R.id.recycle_view);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        recyclerView.setLayoutManager(new GridLayoutManager(getMContext(), 4));
        recyclerView.setAdapter(adapter);
        loadData();

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                check = i;
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void loadData() {
        VhallSDK.getGiftList(roomId, new RequestDataCallbackV2<GiftListData>() {
            @Override
            public void onSuccess(GiftListData giftListData) {
                adapter.setNewData(giftListData.list);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                showToast(errorCode);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel || v.getId() == R.id.root) {
            dismiss();
        }
    }

    @Override
    public void show() {
        super.show();
    }

    class MyAdapter extends BaseQuickAdapter<GiftListData.ListBean, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_gift);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, final GiftListData.ListBean info) {
            helper.setText(R.id.tv_name, info.name);

            ImageView view = helper.getView(R.id.image_view);

            Glide.with(getMContext()).load(info.image_url).apply(new RequestOptions()).into(view);

            if (helper.getAdapterPosition() == check) {
                helper.getView(R.id.line).setVisibility(View.VISIBLE);
                helper.getView(R.id.tv_send).setVisibility(View.VISIBLE);
                helper.getView(R.id.tv_name).setVisibility(View.GONE);
            } else {
                helper.getView(R.id.line).setVisibility(View.GONE);
                helper.getView(R.id.tv_send).setVisibility(View.GONE);
                helper.getView(R.id.tv_name).setVisibility(View.VISIBLE);
            }
            helper.getView(R.id.tv_send).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (info.price.equals("0"))
                        VhallSDK.sendGift(roomId, info.id, "WEIXIN", "H5_PAY", "", new RequestCallback() {
                            @Override
                            public void onSuccess() {
                                showToast("赠送成功" + info.name);
                                dismiss();
                            }

                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                showToast(errorCode);
                            }
                        });
                    else
                        showToast("自行接入付费系统");
                }
            });
        }
    }

}