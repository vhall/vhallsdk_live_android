package com.vhall.uimodule.watch.lottery;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.lottery.LotteryServer;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.watch.WatchLiveActivity;

public class LotteryDialog  extends BaseBottomDialog implements View.OnClickListener {
    public WebinarInfo webinarInfo;
    ImageView mImageView;
    TextView tv_mark;
    TextView tv_btn;
    private MessageServer.MsgInfo msgInfo;
    private WatchLiveActivity activity;
    public LotteryDialog(@NonNull Context context, WebinarInfo webinarInfo, MessageServer.MsgInfo msgInfo, WatchLiveActivity activity) {
        super(context);
        this.webinarInfo = webinarInfo;
        this.msgInfo = msgInfo;
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_lottery);
        findViewById(R.id.tv_cancel).setOnClickListener(this);

        mImageView = (ImageView)findViewById(R.id.iv);
        tv_mark = (TextView)findViewById(R.id.tv_mark);
        tv_btn = (TextView)findViewById(R.id.tv_btn);

        tv_btn.setOnClickListener(this);

        lotteryMsg(this.msgInfo);
        this.activity.setCurDialog(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            dismiss();
        }
        else if (v.getId() == R.id.tv_btn) {//按钮
            if(this.msgInfo.lotteryInfo.lottery_status == 0 && this.msgInfo.lotteryInfo.lottery_type.equals("8")){//开始抽奖
                LotteryServer.joinCodeLottery(this.webinarInfo.vss_room_id, this.msgInfo.lotteryInfo.lottery_id, this.msgInfo.lotteryInfo.command, new RequestCallback() {
                    @Override
                    public void onSuccess() {
                        showToast("已发送口令");
                        dismiss();
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        showToast("发送口令失败"+errorMsg);
                    }
                });
            }
            else {
                if(this.msgInfo.winnerLottery && this.msgInfo.lotteryInfo.need_take_award == 1){//需要领奖
                    BaseBottomDialog dialog = new LotteryRewardDialog(getMContext(),webinarInfo,this.msgInfo,activity);
                    dialog.show();
                }else {//查看结果
                    BaseBottomDialog dialog = new LotteryListDialog(getMContext(), webinarInfo,this.msgInfo.lotteryInfo.lottery_id,activity);
                    dialog.show();
                }
                dismiss();
            }

        }
    }
    @Override
    public void show(){
        super.show();
        this.activity.setCurDialog(this);
    }

    public void lotteryMsg(MessageServer.MsgInfo msgInfo) {
        this.msgInfo = msgInfo;
        if(this.msgInfo.event == MessageServer.EVENT_START_LOTTERY){
            if(msgInfo.lotteryInfo.icon.contains("https:"))
                Glide.with(getContext()).load(msgInfo.lotteryInfo.icon).into(mImageView);
            else
                Glide.with(getContext()).load("https:"+msgInfo.lotteryInfo.icon).into(mImageView);

            Boolean isShowBtn = (this.msgInfo.lotteryInfo.lottery_type.equals("8"));
            tv_btn.setVisibility(isShowBtn?View.VISIBLE:View.GONE);
            tv_btn.setText("立即发送口令");
        }
        else if(this.msgInfo.event == MessageServer.EVENT_END_LOTTERY){
            tv_btn.setVisibility(View.VISIBLE);

            tv_btn.setText((this.msgInfo.winnerLottery && this.msgInfo.lotteryInfo.need_take_award == 1)?"立即领奖":"查看中奖名单");

            int idx = this.msgInfo.winnerLottery?R.mipmap.icon_lottery_winer:R.mipmap.icon_lottery_no;
            RequestOptions requestOptions  = RequestOptions.noTransformation().placeholder(idx);
            Glide.with(getContext()).load("").apply(requestOptions).into(mImageView);
        }
    }
}


//        RequestOptions requestOptions  = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.mipmap.icon_avatar);
//        Glide.with(getContext()).load("https:"+this.msgInfo.lotteryInfo.icon).apply(requestOptions).into(mImageView);
