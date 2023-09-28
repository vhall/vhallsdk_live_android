package com.vhall.uimodule.watch.coupon;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.goods.GoodsServer;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.utils.WeakHandler;
import com.vhall.vhss.data.CouponInfoData;

/**
 * @author wxx
 * 优惠劵列表
 */
public class CouponListDialog extends BaseBottomDialog implements View.OnClickListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private MyAdapter adapter = new MyAdapter();
    private MyAdapter adapterUnAvailable = new MyAdapter();
    private String goodsId;
    private Integer goodsNum;
    private int check = -1;
    public WebinarInfo webinarInfo;
    private boolean isAvailable = true;
    CouponInfoData.CouponItem selectedCoupon;
    OnCouponSelectedListener couponListener;

    private TextView tvAvailable;
    private TextView tvAvailableBg;
    private TextView tvUnAvailable;
    private TextView tvUnAvailableBg;

    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            selectTicket(msg.what);
            return false;
        }
    });


    public CouponListDialog(Context context, WebinarInfo webinarInfo,String goodsId,Integer goodsNum) {
        super(context);
        this.webinarInfo = webinarInfo;
        this.goodsId = goodsId;
        this.goodsNum = goodsNum;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_coupon);
        recyclerView = findViewById(R.id.recycle_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        tvAvailable= findViewById(R.id.tv_use_coupon);
        tvAvailableBg= findViewById(R.id.tv_use_coupon_bg);
        tvUnAvailable= findViewById(R.id.tv_nouse_coupon);
        tvUnAvailableBg= findViewById(R.id.tv_unuse_coupon_bg);
        tvUnAvailableBg.setVisibility(View.INVISIBLE);

        tvAvailable.setOnClickListener(this);
        tvUnAvailable.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getMContext()));
        recyclerView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(() -> loadData(isAvailable));
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                selectTicket(i);
            }
        });
        refreshLayout.setRefreshing(true);
        loadData(isAvailable);
    }

    public void setOnCouponSelectedListener(OnCouponSelectedListener listener) {
        couponListener = listener;
    }

    private void selectTicket(int checked_pos){
        if(isAvailable) {
            check = checked_pos;

            for (int i = 0; i < adapter.getData().size(); i++) {
                CouponInfoData.CouponItem item = adapter.getData().get(i);
                if (i == check) {
                    item.is_selected = !item.is_selected;
                    selectedCoupon = item.is_selected?item:null;
                    if(couponListener != null)
                        couponListener.onCouponSelectedClick(selectedCoupon);
                }
                else
                    item.is_selected = false;
            }

            adapter.notifyDataSetChanged();
        }
        else {
            adapterUnAvailable.notifyDataSetChanged();
        }
    }

    private void updateTabBtn(boolean available){
        tvAvailable.setTextColor(ContextCompat.getColor(getMContext(), available?R.color.black_85:R.color.black_65));
        tvAvailableBg.setVisibility(available?View.VISIBLE:View.INVISIBLE);
        tvUnAvailable.setTextColor(ContextCompat.getColor(getMContext(), available?R.color.black_65:R.color.black_85));
        tvUnAvailableBg.setVisibility(available?View.INVISIBLE:View.VISIBLE);

        recyclerView.setAdapter(available?adapter:adapterUnAvailable);

        if(isAvailable){
            findViewById(R.id.im_empty).setVisibility((adapter.getData().size()>0)?View.INVISIBLE:View.VISIBLE);
            findViewById(R.id.tv_empty).setVisibility((adapter.getData().size()>0)?View.INVISIBLE:View.VISIBLE);
        }
        else {
            findViewById(R.id.im_empty).setVisibility((adapterUnAvailable.getData().size() > 0) ? View.INVISIBLE : View.VISIBLE);
            findViewById(R.id.tv_empty).setVisibility((adapterUnAvailable.getData().size() > 0) ? View.INVISIBLE : View.VISIBLE);
        }
    }

    private void refreshData(CouponInfoData couponInfoData,CouponInfoData unAvailableCouponInfoData) {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
        if(couponInfoData!=null) {
            adapter.setList(couponInfoData.coupon_items);
            tvAvailable.setText("可用优惠券（"+couponInfoData.coupon_items.size()+"）");

            if(!TextUtils.isEmpty(this.goodsId) && couponInfoData.coupon_items.size()>0) {
                selectedCoupon = couponInfoData.coupon_items.get(0);
                selectedCoupon.is_selected = true;
                if(couponListener != null)
                    couponListener.onCouponSelectedClick(selectedCoupon);
            }
            else {
                selectedCoupon = null;
            }
        }
        if(unAvailableCouponInfoData!=null) {
            adapterUnAvailable.setList(unAvailableCouponInfoData.coupon_items);
            tvUnAvailable.setText("不可用优惠券（"+unAvailableCouponInfoData.coupon_items.size()+"）");
        }

        updateTabBtn(isAvailable);
    }

    private void loadData(boolean available) {
        GoodsServer.getCouponAvailableList(webinarInfo.webinar_id,goodsId,goodsNum, new RequestDataCallbackV2<CouponInfoData>() {
            @Override
            public void onSuccess(CouponInfoData couponInfoData) {
                refreshData(couponInfoData,null);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                showToast(errorMsg);
            }
        });
        GoodsServer.getCouponUnavailableList(webinarInfo.webinar_id,goodsId,goodsNum, new RequestDataCallbackV2<CouponInfoData>() {
            @Override
            public void onSuccess(CouponInfoData couponInfoData) {
                refreshData(null,couponInfoData);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                showToast(errorMsg);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            dismiss();
        } else if (v.getId() == R.id.tv_use_coupon) {
            isAvailable = true;
            updateTabBtn(isAvailable);
        } else if (v.getId() == R.id.tv_nouse_coupon) {
            isAvailable = false;
            updateTabBtn(isAvailable);
        }
    }

    @Override
    public void show() {
        super.show();
    }

    class MyAdapter extends BaseQuickAdapter<CouponInfoData.CouponItem, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_coupon);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, final CouponInfoData.CouponItem item) {
            RadioButton rb = helper.getView(R.id.rb_select);
            TextView tv_deduction_amount = helper.getView(R.id.tv_coupon_deduction_amount);

            helper.setVisible(R.id.cl_ticket_bg,!item.unavailable||(item.unavailable && item.unavailable_type == 0));
            helper.setVisible(R.id.cl_ticket_bg_grey,item.unavailable && item.unavailable_type != 0);
            helper.setText(R.id.tv_coupon_deduction_amount, item.deduction_amount);
            switch (item.coupon_type){
                case 0:helper.setText(R.id.tv_coupon_threshold_amount, "满"+String.format("%.2f",Double.parseDouble(item.threshold_amount))+"可用");break;
                case 1:helper.setText(R.id.tv_coupon_threshold_amount, "无门槛");break;
            }
            helper.setText(R.id.tv_ex,item.use_desc);
            helper.setText(R.id.tv_name, item.coupon_name);
            helper.setTextColorRes(R.id.tv_name,item.unavailable?R.color.color_26:R.color.color_FB2626);
            switch (item.applicable_product_type){
                case 0:helper.setText(R.id.tv_sub, "全部商品可用");break;
                case 1:helper.setText(R.id.tv_sub, "指定商品可用");break;
                case 2:helper.setText(R.id.tv_sub, "指定商品不可用");break;
            }
            switch (item.validity_type){
                case 0:helper.setText(R.id.tv_time, item.validity_start_time+"-"+item.validity_end_time);break;
                case 1:helper.setText(R.id.tv_time, "自领取起"+item.validity_day+"天有效");break;
            }

            if(item.unavailable){
                rb.setVisibility(View.GONE);
                helper.setVisible(R.id.im_ticket_flag, false);
                helper.setVisible(R.id.im_ticket_flag_grey, false);
                switch (item.unavailable_type){
                    case 0:
                        helper.setVisible(R.id.im_ticket_flag, true);
                        break;
                    case 1:
                        helper.setVisible(R.id.im_ticket_flag_grey, true);
                        helper.setBackgroundResource(R.id.im_ticket_flag_grey,R.drawable.icon_coupon_expire);
                        break;
                    case 2:
                        helper.setVisible(R.id.im_ticket_flag_grey, true);
                        helper.setBackgroundResource(R.id.im_ticket_flag_grey,R.drawable.icon_coupon_received);
                        break;
                }
            }
            else{
                rb.setVisibility(View.VISIBLE);
                rb.setChecked(item.is_selected);
            }

            rb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.sendEmptyMessage(helper.getAdapterPosition());
                }
            });

            helper.getView(R.id.tv_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View cl_view = helper.getView(R.id.cl_ex);
                    helper.getView(R.id.v_bg_circle).setBackgroundResource((cl_view.getVisibility() == View.GONE)?R.drawable.shape_white_circle1:R.drawable.shape_white_circle);
                    cl_view.setVisibility((cl_view.getVisibility() == View.GONE)? View.VISIBLE:View.GONE);
                }
            });
        }
    }

}