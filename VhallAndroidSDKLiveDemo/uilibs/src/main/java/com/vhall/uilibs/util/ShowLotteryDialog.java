package com.vhall.uilibs.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.uilibs.R;

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
    MessageServer.MsgInfo messageInfo;
    MessageServer.Lottery lottery = null;

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

    public void setMessageInfo(MessageServer.MsgInfo messageInfo) {
        this.messageInfo = messageInfo;
        initData();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
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
                    if (messageInfo != null && messageInfo.lotteries != null && messageInfo.lotteries.size() > 0) {
                        for (int i = 0; i < messageInfo.lotteries.size(); i++) {
                            if (messageInfo.lotteries.get(i).isSelf) {
                                lottery = messageInfo.lotteries.get(i);
                            }
                            View view = View.inflate(mContext, R.layout.item_common_list, null);
                            TextView mName = (TextView) view.findViewById(R.id.tv_common_name);
                            CircleImageView mCirlceAvatar = (CircleImageView) view.findViewById(R.id.circle_common_avatar);
                            mName.setText(messageInfo.lotteries.get(i).nick_name);
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
            }
            super.handleMessage(msg);
        }
    };

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
        handler.sendEmptyMessage(this.messageInfo.event);
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
            submit(submitNicknameStr, submitPhoneStr);
        }
    }

    public void submit(String nickname, String phone) {
        if (!TextUtils.isEmpty(lottery.id) && !TextUtils.isEmpty(lottery.lottery_id)) {
            VhallSDK.getInstance().submitLotteryInfo(lottery.id, lottery.lottery_id, nickname, phone, new VhallSDK.RequestCallback() {
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
