package com.vhall.uimodule.watch.goods;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uimodule.R;
import com.vhall.uimodule.utils.DensityUtils;
import com.vhall.uimodule.utils.ToastUtils;
import com.vhall.uimodule.watch.card.CardDialog;
import com.vhall.vhss.data.GoodsInfoData;

import java.util.UUID;

/**
 * @author hkl
 * Date: 2022/7/21 18:01
 */
public class GoodsAdapter extends BaseQuickAdapter<GoodsInfoData.GoodsInfo, BaseViewHolder> {
    private Context mContext;
    WebinarInfo webinarInfo;
    GoodsFragment goodsFragment;

    public GoodsAdapter(Context context, WebinarInfo webinarInfo,GoodsFragment goodsFragment) {
        super(R.layout.item_goods);
        mContext = context;
        this.webinarInfo = webinarInfo;
        this.goodsFragment = goodsFragment;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, GoodsInfoData.GoodsInfo goodsInfo) {
        ImageView mImageView = viewHolder.getView(R.id.iv_head);
        TextView buyBtn = viewHolder.getView(R.id.tv_buy);
        TextView price = viewHolder.getView(R.id.tv_price);
        TextView price1 = viewHolder.getView(R.id.tv_price1);
        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (goodsInfo.buy_type){
                    case 2:{//外链购买
                        if(!TextUtils.isEmpty(goodsInfo.url) && goodsInfo.url.contains("http")) {
                            //通过浏览器打开URL
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(goodsInfo.url));
                            getContext().startActivity(intent);
                        } else {
                            ToastUtils.Companion.showToast("请填写正确的url地址");
                        }
                    }
                        break;
                    case 3:
                        Toast.makeText(getContext(), "三方商品id: "+goodsInfo.third_goods_id, Toast.LENGTH_SHORT).show();
                        break;
                    case 1:{
                        // 显示选择支付方式的 Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("选择支付方式");
                        String[] options = {"平台购买：URL支付", "平台购买：本地支付"};
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // URL 支付  如果有跳转购买地址，也可以使用嵌入地址打开下单页面进行下单。解决使用原生方式下单
                                        if (!TextUtils.isEmpty(goodsInfo.goods_detail_url)) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            //携带自生成的 ext_order_no 作为参数方便回跳作为查询参数,查询订单信息
                                            String ext_order_no = UUID.randomUUID().toString();
                                            String url = goodsInfo.goods_detail_url + "&ext_order_no=" + ext_order_no;
                                            intent.setData(Uri.parse(url));
                                            getContext().startActivity(intent);
                                        } else {
                                            Toast.makeText(getContext(), "商品详情地址为空", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case 1: // 本地支付
                                        goodsFragment.showGoodsOrderDialog(goodsInfo);
                                        break;
                                }
                            }
                        });

                        builder.setNegativeButton("取消", null);
                        builder.show();

                    }
                        break;
                    default:
                        break;
                }
            }
        });


        viewHolder.setText(R.id.tv_idx, String.format("%02d", viewHolder.getAdapterPosition()+1));
        viewHolder.setText(R.id.tv_name, goodsInfo.name);
        viewHolder.setText(R.id.tv_description, goodsInfo.description);

        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        RequestOptions options = new RequestOptions().transform(new RoundedCorners(DensityUtils.dpToPxInt(4)));
        Glide.with(getContext()).load(goodsInfo.cover_img).apply(options).into(mImageView);
        mImageView.setOutlineProvider(new CardDialog.RoundViewOutlineProvider(DensityUtils.dpToPxInt(4)));
        mImageView.setClipToOutline(true);

        boolean is_have_discount_price = !TextUtils.isEmpty(goodsInfo.discount_price);
        viewHolder.getView(R.id.tv_label).setVisibility(is_have_discount_price?View.VISIBLE:View.GONE);
        price.setVisibility(is_have_discount_price?View.VISIBLE:View.GONE);

        if(is_have_discount_price){
            SpannableStringBuilder builder = new SpannableStringBuilder();
            String text = "￥"+goodsInfo.discount_price;
            RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.7f);
            builder.append(text);
            builder.setSpan(sizeSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            RelativeSizeSpan sizeSpan2 = new RelativeSizeSpan(1.0f);
            if(text.length() > 2){
                builder.setSpan(sizeSpan2,  1, text.length()-2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else{
                builder.setSpan(sizeSpan2,  1, text.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            RelativeSizeSpan sizeSpan3 = new RelativeSizeSpan(0.7f);
            builder.setSpan(sizeSpan3, text.length()-2, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            price.setText(builder);
        }

        price1.setVisibility(TextUtils.isEmpty(goodsInfo.price)?View.GONE:View.VISIBLE);
        if(!TextUtils.isEmpty(goodsInfo.price)) {
            SpannableStringBuilder builder1 = new SpannableStringBuilder();
            String text1 = "￥" + goodsInfo.price;
            RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.7f);
            builder1.append(text1);
            builder1.setSpan(sizeSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            RelativeSizeSpan sizeSpan2 = new RelativeSizeSpan(1.0f);
            if(text1.length() > 2){
                builder1.setSpan(sizeSpan2, 1, text1.length() - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else{
                builder1.setSpan(sizeSpan2, 1, text1.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            RelativeSizeSpan sizeSpan3 = new RelativeSizeSpan(0.7f);
            builder1.setSpan(sizeSpan3, text1.length() - 2, text1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (is_have_discount_price) {
                StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
                builder1.setSpan(strikethroughSpan, 1, text1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                price1.setTextColor(ContextCompat.getColor(mContext, R.color.black_25));
            }else{
                price1.setTextColor(ContextCompat.getColor(mContext, R.color.color_FB3A32));
            }
            price1.setText(builder1);
        }
    }
} 