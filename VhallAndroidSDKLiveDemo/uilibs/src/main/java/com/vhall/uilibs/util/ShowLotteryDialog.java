package com.vhall.uilibs.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.handler.WeakHandler;

import java.util.List;

/**
 * Created by qing on 2017/4/6.
 */
public class ShowLotteryDialog extends AlertDialog implements View.OnClickListener {

    private static final String TAG = "ShowLotteryDialog";

    public LinearLayout mLayoutStatusShow, mLayoutStatusSubmit, mNameView;
    public TextView mIsLottery, mTextStatusStart, mTextTitleStr;
    private Context mContext;
    private Button mBtnSkip, mBtnSubmit;
    private EditText submitNickname, submitPhone;
    MessageServer.MsgInfo  lotteryData;
    MessageServer.Lottery lottery;

    public ShowLotteryDialog(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public ShowLotteryDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
        initView();
    }

    public void setMessageInfo(MessageServer.MsgInfo lotteryData) {
        this.lotteryData = lotteryData;
        initData();
    }

    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MessageServer.EVENT_START_LOTTERY:
                    mTextStatusStart.setVisibility(View.VISIBLE);
                    mLayoutStatusShow.setVisibility(View.GONE);
                    mLayoutStatusSubmit.setVisibility(View.GONE);
                    mTextTitleStr.setText(R.string.vhall_lottery_title_start);
                    break;
                case MessageServer.EVENT_END_LOTTERY:
                    mNameView.removeAllViews();  //清除原有的数据
                    lottery = null;
                    mTextStatusStart.setVisibility(View.GONE);
                    mLayoutStatusSubmit.setVisibility(View.GONE);
                    mLayoutStatusShow.setVisibility(View.VISIBLE);
                    mTextTitleStr.setText(R.string.vhall_lottery_title_show);

                    if (lotteryData != null && lotteryData.lotteries != null && lotteryData.lotteries.size() > 0) {
                        List<MessageServer.Lottery> lottery_winners = lotteryData.lotteries;
                        for (int i = 0; i < lottery_winners.size(); i++) {
                            if (lottery_winners.get(i).isSelf) {
                                lottery = lottery_winners.get(i);
                            }
                            View view = View.inflate(mContext, R.layout.item_common_list, null);
                            TextView mName = (TextView) view.findViewById(R.id.tv_common_name);
                            ImageView mCirlceAvatar = view.findViewById(R.id.circle_common_avatar);
                            mName.setText(lottery_winners.get(i).nick_name);
                            mNameView.addView(view);
                        }
                        if (lottery != null) {
                            mBtnSkip.setVisibility(View.VISIBLE);
                            mIsLottery.setText(R.string.vhall_lottery_true);
                        } else {
                            mBtnSkip.setVisibility(View.GONE);
                            mIsLottery.setText(R.string.vhall_lottery_false);
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void initView() {
        if (mContext != null) {
            //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            View root = View.inflate(mContext, R.layout.alert_dialog_show_lottery, null);
            mTextStatusStart = (TextView) root.findViewById(R.id.tv_lottery_start);
            mLayoutStatusShow = (LinearLayout) root.findViewById(R.id.layout_lottery_status_show);
            mLayoutStatusSubmit = (LinearLayout) root.findViewById(R.id.layout_lottery_status_submit);
            mNameView = (LinearLayout) root.findViewById(R.id.layout_lottery_add_view);
            mTextTitleStr = (TextView) root.findViewById(R.id.tv_lottery_title);
            mIsLottery = (TextView) root.findViewById(R.id.tv_lottery_islottery);
            submitNickname = (EditText) root.findViewById(R.id.lottery_submit_nickname);
            submitPhone = (EditText) root.findViewById(R.id.lottery_submit_phone);
            mBtnSubmit = (Button) root.findViewById(R.id.btn_lottery_submit);
            mBtnSubmit.setOnClickListener(this);
            mBtnSkip = (Button) root.findViewById(R.id.btn_lottery_skip);
            mBtnSkip.setOnClickListener(this);
            root.findViewById(R.id.image_lottery_close).setOnClickListener(this);
            this.setView(root);
            this.setCanceledOnTouchOutside(false); //点击外部不消失
        }
    }

    private void initData() {
        handler.sendEmptyMessage(this.lotteryData.event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "oncreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.image_lottery_close) {
            this.dismiss();
        } else if (i == R.id.btn_lottery_skip) {
            mTextStatusStart.setVisibility(View.GONE);
            mLayoutStatusShow.setVisibility(View.GONE);
            mLayoutStatusSubmit.setVisibility(View.VISIBLE);
            mTextTitleStr.setText(R.string.vhall_lottery_title_submit);
        } else if (i == R.id.btn_lottery_submit) {
            String submitNicknameStr = submitNickname.getText().toString();
            String submitPhoneStr = submitPhone.getText().toString();
            if (TextUtils.isEmpty(submitNicknameStr) || TextUtils.isEmpty(submitPhoneStr)) {
                Toast.makeText(mContext, "信息不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            submit(submitNicknameStr, submitPhoneStr);
        }
    }

    public void submit(String nickname, String phone) {
        if (!TextUtils.isEmpty(lottery.id) && !TextUtils.isEmpty(lottery.lottery_id)) {
            VhallSDK.submitLotteryInfo(lottery.id, lottery.lottery_id, nickname, phone,"", new RequestCallback() {
                @Override
                public void onSuccess() {
                    dismiss();
                    Toast.makeText(mContext, "信息提交成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int errorCode, String reason) {
                    Toast.makeText(mContext, "信息提交失败", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }
}
