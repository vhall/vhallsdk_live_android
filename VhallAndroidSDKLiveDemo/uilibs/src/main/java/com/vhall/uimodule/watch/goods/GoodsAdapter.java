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
                    case 1:
                    default://平台购买
                        goodsFragment.showGoodsOrderDialog(goodsInfo);
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
            builder.setSpan(sizeSpan2,  1, text.length()-2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            builder1.setSpan(sizeSpan2, 1, text1.length() - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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