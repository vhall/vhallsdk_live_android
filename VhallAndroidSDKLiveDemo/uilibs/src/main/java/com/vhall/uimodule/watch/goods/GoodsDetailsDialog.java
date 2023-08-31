package com.vhall.uimodule.watch.goods;

import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.card.CardServer;
import com.vhall.business.module.goods.GoodsServer;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.utils.DensityUtils;
import com.vhall.uimodule.utils.ToastUtils;
import com.vhall.uimodule.utils.WeakHandler;
import com.vhall.uimodule.widget.CornerTransform;
import com.vhall.uimodule.widget.OutDialogBuilder;
import com.vhall.vhss.data.CardsInfoData;
import com.vhall.vhss.data.GoodsInfoData;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GoodsDetailsDialog extends BaseBottomDialog implements View.OnClickListener {
    TextView tv_page;
    ViewPager vp_viewPager;
    TextView price;
    TextView price1;
    TextView tv_name;
    TextView tv_introduce;
    TextView tv_shop;
    MyPagerAdapter adapter;
    private WebinarInfo webinarInfo;
    public GoodsInfoData.GoodsInfo goodsInfo;
    private View.OnClickListener onClickListener;

    public GoodsDetailsDialog(@NonNull Context context, WebinarInfo webinarInfo, GoodsInfoData.GoodsInfo goodsInfo, View.OnClickListener l) {
        super(context);
        this.goodsInfo = goodsInfo;
        this.webinarInfo = webinarInfo;
        this.onClickListener = l;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_goods_details);
        findViewById(R.id.tv_cancel).setOnClickListener(this);

        tv_page = findViewById(R.id.tv_page);
        price = findViewById(R.id.tv_price);
        price1 = findViewById(R.id.tv_price1);
        tv_name = findViewById(R.id.tv_name);
        tv_shop = findViewById(R.id.tv_shop);
        tv_introduce = findViewById(R.id.tv_introduce);
        vp_viewPager = findViewById(R.id.vp_images);
        List<ImageView> imageList = new ArrayList<>();
        adapter = new MyPagerAdapter(imageList);
        vp_viewPager.setAdapter(adapter);
        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // 页面滑动时的回调方法
                // 可以根据需要来处理滑动事件
                tv_page.setText((position+1)+"/"+goodsInfo.images.size());
            }

            @Override
            public void onPageSelected(int position) {
                // 页面选中时的回调方法
                // 可以根据需要来处理选中事件
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 页面滑动状态变化时的回调方法
                // 可以根据需要来处理滑动状态变化事件
            }
        };
        vp_viewPager.addOnPageChangeListener(onPageChangeListener);

        findViewById(R.id.tv_buy).setOnClickListener(onClickListener);
        tv_shop.setOnClickListener(this);

        loadGoodsInfo();
        updateUI(this.goodsInfo);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            dismiss();
        }
        else if (v.getId() == R.id.tv_buy) {//按钮
        }
        else if (v.getId() == R.id.tv_shop) {//按钮
            if(!TextUtils.isEmpty(goodsInfo.shop_url) && goodsInfo.shop_url.contains("http")) {
                //通过浏览器打开URL
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(goodsInfo.shop_url));
                getContext().startActivity(intent);
            } else {
                Toast.makeText(getContext(), "请填写正确的店铺url地址", Toast.LENGTH_SHORT).show();
            }
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
        tv_page.setText("1/"+goodsInfo.images.size());
        tv_name.setText(goodsInfo.name);
        tv_introduce.setText(goodsInfo.description);
        tv_shop.setVisibility(goodsInfo.shop_show?View.VISIBLE:View.GONE);

        List<ImageView> newImageList = generateImageList(goodsInfo.images);
        adapter.updateImageList(newImageList);

        boolean is_have_discount_price = !TextUtils.isEmpty(goodsInfo.discount_price);
        findViewById(R.id.tv_label).setVisibility(is_have_discount_price?View.VISIBLE:View.GONE);
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
                price1.setTextColor(ContextCompat.getColor(getMContext(), R.color.black_25));
            }else{
                price1.setTextColor(ContextCompat.getColor(getMContext(), R.color.color_FB3A32));
            }
            price1.setText(builder1);
        }
    }

    // 根据URL生成新的imageList
    private List<ImageView> generateImageList(List<GoodsInfoData.ImageInfo> images) {
        List<ImageView> imageList = new ArrayList<>();
        for (GoodsInfoData.ImageInfo image : images) {
            ImageView imageView = new ImageView(getMContext());
            imageView.setScaleType(image.scale_type);

            CornerTransform cornerTransform = new CornerTransform(getMContext(), DensityUtils.dpToPx(16f));
            cornerTransform.setNeedCorner(true, true, false, false);
            RequestOptions options = new RequestOptions().transform(cornerTransform);
            Glide.with(getMContext()).asBitmap()
                    .load(image.img_url)
                    .apply(options)
                    .into(imageView);
            imageList.add(imageView);
        }
        return imageList;
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {// 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }

    public class MyPagerAdapter extends PagerAdapter {

        private List<ImageView> imageList;

        public MyPagerAdapter(List<ImageView> imageList) {
            this.imageList = imageList;
        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = imageList.get(position);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(imageList.get(position));
        }

        public void updateImageList(List<ImageView> newImageList) {
            imageList.clear();
            notifyDataSetChanged();
            imageList.addAll(newImageList);
            notifyDataSetChanged();
        }
    }
}



