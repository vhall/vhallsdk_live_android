package com.vhall.uilibs.watch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.vhall.business.data.LotteryPrizeListInfo;
import com.vhall.uilibs.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hkl
 * Date: 2020/12/23 2:38 PM
 */
class LotteryCommitAdapter extends RecyclerView.Adapter<LotteryCommitAdapter.RecyclerHolder> {
    private Context mContext;
    private List<LotteryPrizeListInfo> dataList = new ArrayList<>();
    private RecyclerHolder recyclerHolder;

    public LotteryCommitAdapter(RecyclerView recyclerView) {
        this.mContext = recyclerView.getContext();
    }

    public void setData(List<LotteryPrizeListInfo> dataList) {
        if (null != dataList) {
            this.dataList.clear();
            this.dataList.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_lottery_commit, viewGroup, false);
        recyclerHolder = new RecyclerHolder(view);
        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerHolder viewHolder, int position) {
        final int p = position;
        LotteryPrizeListInfo lotteryPrizeListInfo = dataList.get(position);
        viewHolder.textView.setVisibility(lotteryPrizeListInfo.is_required == 1 ? View.VISIBLE : View.GONE);
        viewHolder.editText.setHint(lotteryPrizeListInfo.placeholder);
        viewHolder.editText.setText(lotteryPrizeListInfo.field_value);
        viewHolder.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                dataList.get(p).field_value = s.toString();
            }
        });
    }

    public List<LotteryPrizeListInfo> getDataList() {
        return dataList;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class RecyclerHolder extends RecyclerView.ViewHolder {
        TextView textView;
        EditText editText;

        private RecyclerHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv_required);
            editText = (EditText) itemView.findViewById(R.id.et_content);
        }
    }
}
