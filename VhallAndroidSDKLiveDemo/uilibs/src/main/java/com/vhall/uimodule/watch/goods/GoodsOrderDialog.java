package com.vhall.uimodule.watch.goods;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.alipay.sdk.app.PayTask;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.MessageServer;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.goods.GoodsMessageCallBack;
import com.vhall.business.module.goods.GoodsServer;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.utils.DensityUtils;
import com.vhall.uimodule.utils.WeakHandler;
import com.vhall.uimodule.watch.WatchLiveActivity;
import com.vhall.uimodule.watch.card.CardDialog;
import com.vhall.uimodule.watch.coupon.CouponListDialog;
import com.vhall.uimodule.watch.coupon.OnCouponSelectedListener;
import com.vhall.uimodule.widget.AmountView;
import com.vhall.uimodule.widget.OutDialogBuilder;

import com.vhall.vhss.data.CouponInfoData;
import com.vhall.vhss.data.GoodsInfoData;
import com.vhall.vhss.data.GoodsOrderSetting;
import com.vhall.vhss.data.OrderInfoData;
import com.vhall.vhss.data.WXPayParamData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelbiz.SubscribeMessage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.modelpay.PayReq;

import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;

import org.json.JSONObject;

public class GoodsOrderDialog extends BaseBottomDialog implements View.OnClickListener {
    public GoodsInfoData.GoodsInfo goodsInfo;
    private WebinarInfo webinarInfo;
    private volatile static GoodsOrderDialog curCardDialog = null;
    private AmountView mAmountView;
    private TextView tvCoupon;
    private int mAmount = 1;
//    private IWXAPI api;
    private String mPayType;//0 wx  1 ali
    private GoodsServer goodsServer;
    private CouponInfoData.CouponItem selectedCoupon;
    private Timer getCouponTimer;
    public GoodsOrderDialog(@NonNull Context context, WebinarInfo webinarInfo, GoodsInfoData.GoodsInfo goodsInfo) {
        super(context);
        this.goodsInfo = goodsInfo;
        this.webinarInfo = webinarInfo;

        if(curCardDialog!=null && curCardDialog.isShowing())
            curCardDialog.dismiss();
        curCardDialog = this;

        goodsServer = new GoodsServer.Builder()
                .context(context)
                .webinarInfo(webinarInfo)
                .goodsMessageCallBack(new GoodsMessageCallBack() {
                    @Override
                    public void orderStatusChange(OrderInfoData orderInfo) {
                        Toast.makeText(getContext(), orderInfo.order_status, Toast.LENGTH_SHORT).show();
                        if(orderInfo.order_status.equals("SUCCESS")) {
                            (new OutDialogBuilder()).layout(R.layout.dialog_goods_payok).build(getMContext()).show();
                            dismiss();
                        }
                        else {
                            (new OutDialogBuilder()).layout(R.layout.dialog_goods_payerror).build(getMContext()).show();
                        }

                    }
                    @Override
                    public void pushGoodsCard(MessageServer.MsgInfo msgInfo, int push_status) {}
                    @Override
                    public void addGoodsInfo(GoodsInfoData.GoodsInfo goodsInfo, String goods_list_cdn_url) {}
                    @Override
                    public void deleteGoods(List<String> del_goods_ids, String goods_list_cdn_url) {}
                    @Override
                    public void updateGoodsInfo(GoodsInfoData.GoodsInfo goodsInfo, String goods_list_cdn_url) {}
                    @Override
                    public void updateGoodsList(String goods_list_cdn_url) {}
                    //商品售罄
                    @Override
                    public void goodsSaleOut(GoodsInfoData.GoodsInfo goodsInfo,String goods_list_cdn_url){}
                    //商品在售中
                    @Override
                    public void goodsSaleIng(GoodsInfoData.GoodsInfo goodsInfo,String goods_list_cdn_url){}
                    //商品价格隐藏
                    @Override
                    public void goodsPriceCovered(GoodsInfoData.GoodsInfo goodsInfo,String goods_list_cdn_url){}
                    //显示商品价格
                    @Override
                    public void goodsPriceShow(GoodsInfoData.GoodsInfo goodsInfo,String goods_list_cdn_url){}
                })
            .build();


        //用于同步购买商品
        goodsServer.setGoodsBuyNotice(goodsInfo.goods_id, "buy", goodsInfo.business_uid.toString(), new RequestDataCallbackV2<String>() {
            @Override
            public void onSuccess(String data) {

            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_goods_order);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        mAmountView = (AmountView) findViewById(R.id.amount_view);
        tvCoupon = findViewById(R.id.tv_coupon);
        mAmountView.setGoods_storage(100);
        mAmountView.setOnAmountChangeListener(new AmountView.OnAmountChangeListener() {
            @Override
            public void onAmountChange(View view, int amount) {
                mAmount = amount;
                updateUI(goodsInfo);
                limitGetCoupon();
            }
        });

        tvCoupon.setOnClickListener(this);

        findViewById(R.id.tv_buy).setOnClickListener(this);
        loadGoodsSetting();
        loadGoodsInfo();
        updateUI(this.goodsInfo);
        getCoupon();
    }

    void limitGetCoupon(){
        if(getCouponTimer != null) {
            getCouponTimer.cancel();//销毁
        }
        getCouponTimer = new Timer();
        getCouponTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 要执行的操作
                getCoupon();

                if(getCouponTimer != null) {
                    getCouponTimer.cancel();//销毁
                }
            }
        }, 500); // 延时3s 执行TimeTask的run方法
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    break;
                }
                default:
                    break;
            }
        };
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            dismiss();
        }
        else if (v.getId() == R.id.tv_buy) {//按钮
            switch (goodsInfo.buy_type){
                case 1: {//平台支付
                    EditText et_name =  findViewById(R.id.et_name);
                    EditText et_phone =  findViewById(R.id.et_phone);
                    EditText et_mark =  findViewById(R.id.et_mark);
                    mPayType = (((RadioGroup)findViewById(R.id.rg_pay_type)).getCheckedRadioButtonId() == R.id.rb_wx)?GoodsServer.PAY_TYPE_WEIXIN:GoodsServer.PAY_TYPE_ALIPAY;

                    String price = (TextUtils.isEmpty(goodsInfo.discount_price))? goodsInfo.price : goodsInfo.discount_price;
                    double pay_Amount = Double.parseDouble(price)*mAmount;

                    List<String> coupon_user_ids = null ;
                    if(selectedCoupon != null){
                        pay_Amount = pay_Amount - Double.parseDouble(selectedCoupon.deduction_amount);
                        coupon_user_ids = new ArrayList<String>();
                        coupon_user_ids.add(selectedCoupon.coupon_user_id);
                    }
                    getActivity().showLoading(null, "创建订单中");



                    //此方法获取原生支付参数。本地集成微信和支付宝SDK进行支付
                    goodsServer.goodsCreateOrderByNative(goodsInfo.goods_id,
                            mAmount,
                            goodsInfo.buy_type,
                            pay_Amount,
                            mPayType,
                            et_name.getText().toString(),
                            et_phone.getText().toString(),
                            et_mark.getText().toString(),
                            "andorid",
                            coupon_user_ids,
                            new RequestDataCallbackV2<OrderInfoData>() {
                                @Override
                                public void onSuccess(OrderInfoData data) {
                                    getActivity().finishLoading();
                                    try {
                                        if(mPayType == "WEIXIN"){ // 微信支付
                                            JSONObject jsonObject = new JSONObject(data.order_url);
                                            WXPayParamData payParam = new WXPayParamData( jsonObject );
                                            IWXAPI api = WXAPIFactory.createWXAPI(getContext(), payParam.appId, true);
                                            // 检查是否注册成功
                                            if (!api.registerApp(payParam.appId)) {
                                                // 注册失败
                                                Toast.makeText(getContext(), "发送支付请求失败", Toast.LENGTH_SHORT).show();
                                            }
                                            PayReq request = new PayReq();
                                            request.appId = payParam.appId;
                                            request.partnerId = payParam.partnerId;
                                            request.prepayId= payParam.prepayId;
                                            request.packageValue = payParam.packAge;
                                            request.nonceStr=payParam.nonceStr;
                                            request.timeStamp= payParam.timeStamp;
                                            request.sign= payParam.paySign;

                                            // 在调用 sendReq 前检查
                                            if (!api.isWXAppInstalled()) {
                                                // 微信未安装
                                                Toast.makeText(getContext(), "发送支付请求失败", Toast.LENGTH_SHORT).show();
                                            }

                                            boolean ret = api.sendReq(request);
                                            if(!ret){
                                                Toast.makeText(getContext(), "发送支付请求失败", Toast.LENGTH_SHORT).show();
                                            }
                                        }else if(mPayType == "ALIPAY"){
                                            final String orderInfo = data.order_url;

                                            final Runnable payRunnable = new Runnable() {

                                                @Override
                                                public void run() {
                                                    Activity activity= getActivity();
                                                    PayTask alipay = new PayTask(activity);
                                                    Map<String, String> result = alipay.payV2(orderInfo, true);
                                                    Log.i("msp", result.toString());

                                                    Message msg = new Message();
                                                    msg.what = 1;
                                                    msg.obj = result;
                                                    mHandler.sendMessage(msg);
                                                }
                                            };

                                            // 必须异步调用
                                            Thread payThread = new Thread(payRunnable);
                                            payThread.start();
                                        }
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                @Override
                                public void onError(int errorCode, String errorMsg) {
                                    getActivity().finishLoading();
                                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                                }
                            });


                    // 此方法是获取微吼H5支付连接，通过 URL Scheme 方式进行唤起微信进行支付
//                    goodsServer.createOrder(goodsInfo.goods_id,
//                            mAmount,
//                            goodsInfo.buy_type,
//                            pay_Amount,
//                            mPayType,
//                            et_name.getText().toString(),
//                            et_phone.getText().toString(),
//                            et_mark.getText().toString(),
//                            "ios",
//                            coupon_user_ids,
//                            new RequestDataCallbackV2<OrderInfoData>() {
//                                @Override
//                                public void onSuccess(OrderInfoData data) {
//                                    getActivity().finishLoading();
//                                    //唤起支付页面
//                                    goodsServer.payOrder(getContext(),data, new RequestDataCallbackV2<String>() {
//                                        @Override
//                                        public void onSuccess(String data) {
//
//                                        }
//
//                                        @Override
//                                        public void onError(int errorCode, String errorMsg) {
//                                            Toast.makeText(getContext(), "未安装应用", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                }
//                                @Override
//                                public void onError(int errorCode, String errorMsg) {
//                                    getActivity().finishLoading();
//                                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
//                                }
//                            });

                }
                    break;
                case 3: {//自定义支付
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vhallsdk://?skuid="+goodsInfo.third_goods_id+"&webinarid="+webinarInfo.webinar_id));
                    getContext().startActivity(intent);
                    dismiss();
                }
                    break;
                case 2://外链
                    break;
                default:
                    break;
            }
        } else if (v.getId() == R.id.tv_coupon) {//
            CouponListDialog couponListDialog = new CouponListDialog(getContext(), webinarInfo,goodsInfo.goods_id,mAmount);
            couponListDialog.setOnCouponSelectedListener(new OnCouponSelectedListener() {
                @Override
                public void onCouponSelectedClick(CouponInfoData.CouponItem couponItem) {
                    selectedCoupon = couponItem;
                    updateUI(goodsInfo);
                }
            });
            couponListDialog.show();
        }
    }
    @Override
    public void show(){
        super.show();
    }
    @Override
    public void dismiss(){
        super.dismiss();

    }

    public void updateUI(GoodsInfoData.GoodsInfo goodsInfo) {
        this.goodsInfo = goodsInfo;
        if(goodsInfo.buy_type != 1 && goodsInfo.buy_type != 3) {//外链购买
            dismiss();
            return;
        }
        ImageView iv_head =  findViewById(R.id.iv_head);
        TextView tv_name =  findViewById(R.id.tv_name);
        TextView tv_desc =  findViewById(R.id.tv_description);
        TextView tv_price =  findViewById(R.id.tv_price);
        TextView tv_price_totle =  findViewById(R.id.tv_price_totle);
        TextView tv_price_coupon =  findViewById(R.id.tv_price_coupon);

        findViewById(R.id.tv_input_info).setVisibility(goodsInfo.buy_type == 3? View.GONE:View.VISIBLE);

        //圆角
        iv_head.setScaleType(goodsInfo.cover_img_scale_type);
        RequestOptions options = new RequestOptions().transform(new RoundedCorners(DensityUtils.dpToPxInt(8)));
        Glide.with(getContext()).load(goodsInfo.cover_img).apply(options).into(iv_head);
        iv_head.setOutlineProvider(new CardDialog.RoundViewOutlineProvider(DensityUtils.dpToPxInt(8)));
        iv_head.setClipToOutline(true);

        tv_name.setText(goodsInfo.name);
        tv_desc.setText(goodsInfo.description);

        String price = (TextUtils.isEmpty(goodsInfo.discount_price))? goodsInfo.price : goodsInfo.discount_price;
        if(!TextUtils.isEmpty(price)){
            SpannableStringBuilder builder = new SpannableStringBuilder();
            double pay_Amount = Double.parseDouble(price)*mAmount;
            if(selectedCoupon != null)
                pay_Amount = pay_Amount - Double.parseDouble(selectedCoupon.deduction_amount);

            String text = "￥"+price;
            RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.7f);
            builder.append(text);
            builder.setSpan(sizeSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            RelativeSizeSpan sizeSpan2 = new RelativeSizeSpan(1.0f);
            builder.setSpan(sizeSpan2,  1, text.length()-2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            RelativeSizeSpan sizeSpan3 = new RelativeSizeSpan(0.7f);
            builder.setSpan(sizeSpan3, text.length()-2, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv_price.setText(builder);

            SpannableStringBuilder builder1 = new SpannableStringBuilder();
            String text1 = "￥"+String.format("%.2f", pay_Amount);
            RelativeSizeSpan sp = new RelativeSizeSpan(0.7f);
            builder1.append(text1);
            builder1.setSpan(sp, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            RelativeSizeSpan sp2 = new RelativeSizeSpan(1.0f);
            builder1.setSpan(sp2,  1, text1.length()-2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            RelativeSizeSpan sp3 = new RelativeSizeSpan(0.7f);
            builder1.setSpan(sp3, text1.length()-2, text1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv_price_totle.setText(builder1);
        }
        else {
            tv_price.setText("");
            tv_price_totle.setText("");
        }

        if(selectedCoupon != null){
            tvCoupon.setText("减 ￥" + String.format("%.2f",Double.parseDouble(selectedCoupon.deduction_amount)) +" 〉");
            tvCoupon.setTextColor(ContextCompat.getColor(getMContext(), R.color.color_FB2626));
            tv_price_coupon.setText("共减 ￥"+String.format("%.2f",Double.parseDouble(selectedCoupon.deduction_amount)));
            tv_price_coupon.setVisibility(View.VISIBLE);
            findViewById(R.id.tv_coupon_label).setVisibility(View.VISIBLE);
        }
        else {
            tvCoupon.setText("无可用优惠券 〉");
            tvCoupon.setTextColor(ContextCompat.getColor(getMContext(), R.color.black_45));
            tv_price_coupon.setVisibility(View.GONE);
            findViewById(R.id.tv_coupon_label).setVisibility(View.GONE);
        }
    }


    public void loadGoodsSetting() {
        if(goodsInfo.buy_type != 1)
            return;

        goodsServer.getGoodsOrderSetting(new RequestDataCallbackV2<GoodsOrderSetting>() {
            @Override
            public void onSuccess(GoodsOrderSetting data) {
                findViewById(R.id.et_name).setVisibility(data.enable_username? View.VISIBLE : View.GONE);
                findViewById(R.id.et_phone).setVisibility(data.enable_phone? View.VISIBLE : View.GONE);
                findViewById(R.id.et_mark).setVisibility(data.enable_remark? View.VISIBLE : View.GONE);

                findViewById(R.id.tv_xing).setVisibility((data.enable_username||data.enable_phone)? View.VISIBLE : View.GONE);
                findViewById(R.id.tv_user_name).setVisibility((data.enable_username||data.enable_phone)? View.VISIBLE : View.GONE);
                findViewById(R.id.tv_mark).setVisibility(data.enable_remark? View.VISIBLE : View.GONE);

                findViewById(R.id.tv_wx).setVisibility(data.enable_weixin? View.VISIBLE : View.GONE);
                findViewById(R.id.rb_wx).setVisibility(data.enable_weixin? View.VISIBLE : View.GONE);
                findViewById(R.id.im_wx).setVisibility(data.enable_weixin? View.VISIBLE : View.GONE);

                findViewById(R.id.tv_zfb).setVisibility(data.enable_alipay? View.VISIBLE : View.GONE);
                findViewById(R.id.im_zfb).setVisibility(data.enable_alipay? View.VISIBLE : View.GONE);
                findViewById(R.id.rb_ali).setVisibility(data.enable_alipay? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if(errorCode == 513600){
                    (new OutDialogBuilder()).layout(R.layout.dialog_card_delete).build(getMContext()).show();
                }
                else
                    showToast(errorMsg);
                dismiss();
            }
        });
    }

    public void getCoupon() {
        if(goodsInfo.buy_type != 1)
            return;

        GoodsServer.getCouponAvailableList(webinarInfo.webinar_id, goodsInfo.goods_id, mAmount, new RequestDataCallbackV2<CouponInfoData>() {
            @Override
            public void onSuccess(CouponInfoData data) {
                selectedCoupon = null;
                if(data.coupon_items.size()>0)
                    selectedCoupon = data.coupon_items.get(0);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(goodsInfo);
                    }
                });
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }

    private WatchLiveActivity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof WatchLiveActivity || context instanceof WatchLiveActivity) {
                return (WatchLiveActivity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    /*
   获取商品信息，防止商品信息被修改
 */
    public void loadGoodsInfo() {
        GoodsServer.getGoodsInfo(goodsInfo.goods_id,new RequestDataCallbackV2<GoodsInfoData.GoodsInfo>() {
            @Override
            public void onSuccess(GoodsInfoData.GoodsInfo data) {
                if(data.status == 0) {
                    (new OutDialogBuilder()).layout(R.layout.dialog_card_delete).build(getMContext()).show();
                    dismiss();
                } else {
                    show();
                    updateUI(data);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if(errorCode == 513600){
                    (new OutDialogBuilder()).layout(R.layout.dialog_card_delete).build(getMContext()).show();
                }
                else
                    showToast(errorMsg);
                dismiss();
            }
        });
    }
}



