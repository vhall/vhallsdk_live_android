package com.vhall.uilibs.watch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.MessageServer;
import com.vhall.uilibs.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hkl
 * Date: 2020/12/23 2:38 PM
 */
class LotteryWinnerAdapter extends RecyclerView.Adapter<LotteryWinnerAdapter.RecyclerHolder> {
    private Context mContext;
    private List<MessageServer.Lottery> dataList = new ArrayList<>();

    public LotteryWinnerAdapter(RecyclerView recyclerView) {
        this.mContext = recyclerView.getContext();
    }

    public void setData(List<MessageServer.Lottery> dataList) {
        if (null != dataList) {
            this.dataList.clear();
            this.dataList.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_lottery_list, viewGroup, false);
        return new RecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder viewHolder, int position) {
        viewHolder.tvName.setText(dataList.get(position).nick_name);
        RequestOptions requestOptions = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.drawable.icon_default_avatar);
        Glide.with(mContext).load(dataList.get(position).lottery_user_avatar).apply(requestOptions).into(viewHolder.ivAvatar);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class RecyclerHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivAvatar;

        private RecyclerHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            ivAvatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
        }
    }
}
