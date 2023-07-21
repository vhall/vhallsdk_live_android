package com.vhall.uimodule.watch.card;

import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.card.CardServer;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.utils.CommonUtil;
import com.vhall.uimodule.utils.DensityUtils;
import com.vhall.uimodule.utils.WeakHandler;
import com.vhall.uimodule.watch.WatchLiveActivity;
import com.vhall.uimodule.watch.lottery.LotteryListDialog;
import com.vhall.uimodule.watch.lottery.LotteryRewardDialog;
import com.vhall.uimodule.widget.OutDialogBuilder;
import com.vhall.vhss.data.CardsInfoData;

import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import vhall.com.vss2.VssSdk;

public class CardDialog extends BaseBottomDialog implements View.OnClickListener {
    ImageView mImageView;
    TextView tv_mark;

    TextView tv_timeclose;
    TextView tv_btn;
    private CardsInfoData.CardInfo cardInfo;
    private ScheduledThreadPoolExecutor timeService;
    int remaindTime= 0;

    private volatile static CardDialog curCardDialog = null;

    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            remaindTime--;
            tv_timeclose.setText(remaindTime +"s 关闭");
            if(remaindTime ==0)
                try{
//                    if(!activity.isFinishing() && isShowing())
                    {
                        dismiss();
                    }
                }catch (Exception e)
                {

                }


            return false;
        }
    });

    //开播倒计时显示
    private void handleTimePosition() {
        if (timeService != null) {
            return;
        }
        timeService = new ScheduledThreadPoolExecutor(1);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };
        timeService.scheduleAtFixedRate(timerTask, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public CardDialog(@NonNull Context context, CardsInfoData.CardInfo cardInfo) {
        super(context);
        this.cardInfo = cardInfo;

        if(curCardDialog!=null && curCardDialog.isShowing())
            curCardDialog.dismiss();
        curCardDialog = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_card);
        findViewById(R.id.tv_cancel).setOnClickListener(this);

        tv_timeclose = (TextView)findViewById(R.id.tv_timeclose);
        mImageView = (ImageView)findViewById(R.id.iv_card);
        tv_mark = (TextView)findViewById(R.id.tv_desc);
        tv_btn = (TextView)findViewById(R.id.tv_btn);
        tv_btn.setOnClickListener(this);
        mImageView.setOnClickListener(this);
        tv_mark.setOnClickListener(this);

        updateUI(this.cardInfo);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            dismiss();
        }
        else if (v.getId() == R.id.tv_btn || v.getId() == R.id.tv_desc || v.getId() == R.id.iv_card) {//按钮
            if(cardInfo.href_enable){
                if(!TextUtils.isEmpty(cardInfo.href) && cardInfo.href.contains("http")) {
                    //通过浏览器打开URL
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(cardInfo.href));
                    getMContext().startActivity(intent);
                    CardServer.cardClicked(cardInfo, null);
                    dismiss();
                } else {
                    showToast("请填写正确的url地址");
                }
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
        if (timeService != null) {
            timeService.shutdown();
            timeService = null;
        }
    }

    public void updateUI(CardsInfoData.CardInfo cardInfo) {
        this.cardInfo = cardInfo;

        tv_timeclose.setVisibility(cardInfo.timer_enable?View.VISIBLE:View.GONE);
        tv_timeclose.setText(cardInfo.timer_interval +"s 关闭");
        if(cardInfo.timer_enable){
            remaindTime = cardInfo.timer_interval;
            handleTimePosition();
        }

        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mImageView.getLayoutParams();
        int width  = DensityUtils.getScreenWidth(getMContext())- DensityUtils.dpToPxInt(getMContext(),90);
        int height = (int)width;
        switch (cardInfo.img_rate){
            case 0: height = (int) (width*1.333f);break;//竖版
            case 1: height = (int) (width*0.75f);break;//横版
            default:break;
        }
        params.height = height;
        mImageView.setScaleType(cardInfo.scaleType);
        mImageView.setLayoutParams(params);
        //圆角
        RequestOptions options = new RequestOptions().transform(new RoundedCorners(DensityUtils.dpToPxInt(8)));
        Glide.with(getContext()).load(cardInfo.img_url).apply(options).into(mImageView);
        mImageView.setOutlineProvider(new RoundViewOutlineProvider(DensityUtils.dpToPxInt(8)));
        mImageView.setClipToOutline(true);
        
        tv_mark.setText(cardInfo.remark);
        tv_btn.setVisibility((cardInfo.href_enable && !TextUtils.isEmpty(cardInfo.href_btn_label))?View.VISIBLE:View.GONE);
        tv_btn.setText(TextUtils.isEmpty(cardInfo.href_btn_label) ?"立即查看":cardInfo.href_btn_label);
        findViewById(R.id.tv_cancel).setVisibility(cardInfo.timer_enable?View.GONE:View.VISIBLE);
    }

    /*
       获取卡片信息，防止卡片信息被修改
     */
    public void loadCardInfo() {
        CardServer.getCardInfo(cardInfo,new RequestDataCallbackV2<CardsInfoData.CardInfo>() {
            @Override
            public void onSuccess(CardsInfoData.CardInfo data) {
                show();
                updateUI(data);
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
    public class RoundViewOutlineProvider extends ViewOutlineProvider {
        private final float radius;
        public RoundViewOutlineProvider(float radius) {
            this.radius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            int leftMargin = 0;
            int topMargin = 0;
            Rect selfRect = new Rect(leftMargin, topMargin, view.getWidth(), view.getHeight());
            outline.setRoundRect(selfRect, radius);
        }
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

}



