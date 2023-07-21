package com.vhall.uimodule.watch.lottery;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.LotteryPrizeInfo;
import com.vhall.business.data.LotteryPrizeListInfo;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.watch.WatchLiveActivity;
import com.vhall.uimodule.utils.KeyboardsUtils;
import com.vhall.vhss.data.LotteryWinningUserInfoData;

import java.util.List;

public class LotteryRewardDialog extends BaseBottomDialog implements View.OnClickListener {
    public WebinarInfo webinarInfo;
    private MessageServer.MsgInfo msgInfo;
    private RecyclerView recyclerView;
    private TextView tv_join;
    private LotteryRewardDialog.MyAdapter adapter = new LotteryRewardDialog.MyAdapter();
    List<LotteryPrizeListInfo> mDataList;
    private WatchLiveActivity activity;
    //领奖方式 1寄送奖品2私信兑奖3无需领奖
    int receive_award_way = 0;
    public LotteryRewardDialog(@NonNull Context mContext, WebinarInfo webinarInfo, MessageServer.MsgInfo msgInfo, FragmentActivity activity) {
        super(mContext);

        this.webinarInfo = webinarInfo;
        this.msgInfo = msgInfo;
        this.activity = (WatchLiveActivity) activity;

        loadData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_lottery_reward);
        recyclerView = findViewById(R.id.recycle_view);
        tv_join = findViewById(R.id.tv_join);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        tv_join.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getMContext()));
        recyclerView.setAdapter(adapter);

        refreshDataList();
    }

    private void refreshDataList() {
        if (null != mDataList && mDataList.size() > 0) {
            adapter.setList(mDataList);
        }
    }
    @Override
    public void show(){
        super.show();
        this.activity.setCurDialog(this);
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            dismiss();
        }
        else if (v.getId() == R.id.tv_join) {
            if(getWindow().getCurrentFocus()!=null)
                KeyboardsUtils.hintKeyBoards(getWindow().getCurrentFocus());

            for (LotteryPrizeListInfo info:  mDataList) {
                if(info.is_required == 1 && (info.field_value == null || info.field_value.length()==0)){
                    showToast("必填参数不能为空");
                    return;
                }
            }

            VhallSDK.submitLotteryInfo(this.msgInfo.lotteryInfo.lottery_id,mDataList, new RequestCallback() {
                @Override
                public void onSuccess() {
                    showToast("提交成功");
                    dismiss();
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast("提交失败:"+errorMsg);
                }
            });
        }
    }

    private void loadData() {
        VhallSDK.getPrizeInfo(this.webinarInfo.vss_room_id,this.msgInfo.lotteryInfo.lottery_id, new RequestDataCallback() {
            @Override
            public void onSuccess(Object o) {
                receive_award_way = ((LotteryPrizeInfo) o).receive_award_way;
                mDataList = ((LotteryPrizeInfo) o).list;
                if(receive_award_way == 1) { //1寄送奖品2私信兑奖3无需领奖
                    tv_join.setVisibility(View.VISIBLE);
                    recyclerView.setBackgroundColor(Color.TRANSPARENT);
                }else{
                    tv_join.setVisibility(View.GONE);
                    recyclerView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                }

                if (null != recyclerView) {
                    refreshDataList();
                }
                show();
                lotteryWinningUserInfo();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }

    private void lotteryWinningUserInfo() {
        VhallSDK.lotteryWinningUserInfo(webinarInfo.vss_room_id, new RequestDataCallbackV2<LotteryWinningUserInfoData>() {
            @Override
            public void onSuccess(LotteryWinningUserInfoData data) {
                for (LotteryPrizeListInfo info:  mDataList) {
                    if(info.field_key.equals("name")){
                        info.field_value = (data.lottery_user_name==null || data.lottery_user_name.equals("null"))?"":data.lottery_user_name;
                    }
                    else if(info.field_key.equals("phone")){
                        info.field_value = (data.lottery_user_phone==null || data.lottery_user_phone.equals("null"))?"":data.lottery_user_phone;
                    }
                    else if(info.field_key.equals("address")){
                        info.field_value = (data.lottery_user_address==null || data.lottery_user_address.equals("null"))?"":data.lottery_user_address;
                    }
                }
                adapter.setList(mDataList);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }


    class MyAdapter extends BaseQuickAdapter<LotteryPrizeListInfo, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_lottery_info);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, final LotteryPrizeListInfo info) {
            TextView tv_required = helper.getView(R.id.tv_idx);
            EditText et_content = helper.getView(R.id.ed_value);
            TextView tv_title = helper.getView(R.id.tv_title);
            ImageView iv_qrcode = helper.getView(R.id.iv_qrcode);

            //1寄送奖品2私信兑奖3无需领奖
            if(receive_award_way == 1){
                tv_title.setVisibility(View.GONE);
                iv_qrcode.setVisibility(View.GONE);
                et_content.setVisibility(View.VISIBLE);
                tv_required.setVisibility((info.is_required == 1)?View.VISIBLE:View.INVISIBLE);
                et_content.setHint(info.placeholder);
                et_content.setText(info.field_value);
                int stlen = 50;
                if(info.field_key.equals("name") || info.field_key.equals("phone"))
                    stlen = 20;
                et_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(stlen)});//设置限制长度，多了输入不了
                et_content.setInputType(info.field_key.equals("phone")?EditorInfo.TYPE_CLASS_PHONE:EditorInfo.TYPE_CLASS_TEXT);

                et_content.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                        if (arg1 == EditorInfo.IME_ACTION_SEND
                                || (arg2 != null && arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                            info.field_value = et_content.getText().toString();
                            return true;
                        }
                        return false;
                    }
                });
                et_content.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        info.field_value = et_content.getText().toString();
                    }
                });
            }
            else if(receive_award_way == 2){
                tv_required.setVisibility(View.GONE);
                et_content.setVisibility(View.GONE);

                if(info.placeholder.contains("https://")){//私信领奖图片
                    tv_title.setVisibility(View.GONE);
                    iv_qrcode.setVisibility(View.VISIBLE);
                    Glide.with(getContext()).load(info.placeholder).into(iv_qrcode);
                }else {//私信领奖文本
                    iv_qrcode.setVisibility(View.GONE);
                    tv_title.setVisibility(View.VISIBLE);
                    tv_title.setText(info.placeholder.length()>20?(info.placeholder.substring(0,20)+"..."):info.placeholder);
                }
            }
        }
    }
}