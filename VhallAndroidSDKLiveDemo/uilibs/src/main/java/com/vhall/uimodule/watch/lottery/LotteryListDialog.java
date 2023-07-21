package com.vhall.uimodule.watch.lottery;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.LotteryWinnerData;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.watch.WatchLiveActivity;

import java.util.List;

public class LotteryListDialog extends BaseBottomDialog implements View.OnClickListener {
    public WebinarInfo webinarInfo;
    public String lotteryID;
    private RecyclerView recyclerView;
    private LotteryListDialog.MyAdapter adapter = new LotteryListDialog.MyAdapter();
    List<MessageServer.Lottery> mDataList;
    private WatchLiveActivity activity;
    public LotteryListDialog(Context context, WebinarInfo webinarInfo,String lotteryID, FragmentActivity activity) {
        super(context);

        this.webinarInfo = webinarInfo;
        this.lotteryID = lotteryID;
        this.activity = (WatchLiveActivity) activity;
        loadData();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_lottery_list);
        recyclerView = findViewById(R.id.recycle_view);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getMContext()));
        recyclerView.setAdapter(adapter);

        refreshDataList();
    }
    @Override
    public void show(){
        super.show();
        this.activity.setCurDialog(this);
    }
    private void refreshDataList() {
        if (null != mDataList && mDataList.size() > 0) {
//            if (mDataList.size() > 5) {
//                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800);
//                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
//                recyclerView.setLayoutParams(params);
//            }
            adapter.setList(mDataList);
        }
    }

    private void loadData() {
        VhallSDK.getLotteryWinner(this.webinarInfo.vss_room_id, this.lotteryID, new RequestDataCallback() {
            @Override
            public void onSuccess(Object o) {
                mDataList = ((LotteryWinnerData) o).list;
                if (null != recyclerView) {
                    refreshDataList();
                }
                show();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                showToast(errorMsg);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel || v.getId() == R.id.root) {
            dismiss();
        }
    }

    class MyAdapter extends BaseQuickAdapter<MessageServer.Lottery, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_lottery);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, final MessageServer.Lottery info) {
            ImageView iv_head = helper.getView(R.id.iv_head);
            TextView tv_idx = helper.getView(R.id.tv_idx);
            TextView tv_name = helper.getView(R.id.tv_name);
            TextView tv_award = helper.getView(R.id.tv_award);

            tv_idx.setText(String.format("%02d", (helper.getAdapterPosition()+1)));
            tv_name.setText(info.nick_name);
            tv_award.setText("获得 \""+info.lottery_award_name+"\"");
            tv_idx.setTextColor(Color.parseColor(info.isSelf? "#FF0000":"#000000"));
            tv_name.setTextColor(Color.parseColor(info.isSelf? "#FF0000":"#000000"));
            tv_award.setTextColor(Color.parseColor(info.isSelf? "#FF0000":"#000000"));
            RequestOptions requestOptions  = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.mipmap.icon_avatar);
            Glide.with(getContext()).load(info.lottery_user_avatar).apply(requestOptions).into(iv_head);
        }
    }
}