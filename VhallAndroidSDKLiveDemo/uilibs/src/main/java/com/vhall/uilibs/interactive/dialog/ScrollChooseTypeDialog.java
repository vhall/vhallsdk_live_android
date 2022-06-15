package com.vhall.uilibs.interactive.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;
import com.vhall.uilibs.util.DensityUtils;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hkl
 */

public class ScrollChooseTypeDialog extends BaseBottomDialog implements View.OnClickListener {

    private onItemClickLister onItemClickLister;
    private List<String> stringList = new ArrayList<>();
    private List<Boolean> booleans = null;
    private int showNum = 5;
    private RecyclerView recyclerView;
    private boolean canCheck = false;
    private int choosePosition = -1;

    public void setOnItemClickLister(ScrollChooseTypeDialog.onItemClickLister onItemClickLister) {
        this.onItemClickLister = onItemClickLister;
        canCheck = true;
    }


    public ScrollChooseTypeDialog(Context context, List<String> stringList, List<Boolean> booleans) {
        super(context);
        this.stringList = stringList;
        if (booleans != null) {
            this.booleans = booleans;
        }
        canCheck = true;
    }


    public ScrollChooseTypeDialog(Context context, int num) {
        super(context);
        if (num >= 1) {
            for (int i = 1; i <= num; i++) {
                stringList.add("1V" + i);
            }
        }
    }

    public ScrollChooseTypeDialog(Context context, List<String> stringList, String showNum) {
        super(context);
        this.stringList = stringList;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_choose_camera_stand);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        TextView tvSure = findViewById(R.id.tv_sure);
        tvSure.setOnClickListener(this);
        if (ListUtils.isEmpty(stringList)) {
            return;
        }
        recyclerView = findViewById(R.id.recycle_view);
        if (stringList.size() > showNum) {
            ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
            layoutParams.height = DensityUtils.dpToPxInt(275);
            recyclerView.setLayoutParams(layoutParams);
        }
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        MyAdapter adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
        adapter.bindToRecyclerView(recyclerView);
        adapter.disableLoadMoreIfNotFullPage();
        adapter.setNewData(stringList);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!ListUtils.isEmpty(booleans) && booleans.get(position) && canCheck) {
                    choosePosition = position;
                    adapter.setNewData(stringList);
                    tvSure.setTextColor(ContextCompat.getColor(mContext,R.color.color_FB3A32));
                }
                if (!canCheck && onItemClickLister != null) {
                    onItemClickLister.onItemClick(position, stringList.get(position));
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_sure) {
            if (choosePosition != -1) {
                if (onItemClickLister != null) {
                    onItemClickLister.onItemClick(choosePosition, stringList.get(choosePosition));
                }
                dismiss();
            } else {
                ToastUtil.showToast(mContext, "至少选择一个机位");
            }
        } else if (id == R.id.root || id == R.id.tv_cancel) {
            dismiss();
        }
    }

    @Override
    public void show() {
        super.show();
    }

    class MyAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_dialog_scroll_list);
        }

        @Override
        protected void convert(BaseViewHolder helper, String info) {
            TextView view = helper.getView(R.id.tv);
            view.setText(info);
            if (!ListUtils.isEmpty(booleans) && !booleans.get(helper.getLayoutPosition())) {
                view.setTextColor(ContextCompat.getColor(mContext, R.color.color_99));
            } else {
                view.setTextColor(ContextCompat.getColor(mContext, R.color.color_22));
            }
            if (canCheck && helper.getLayoutPosition() == choosePosition) {
                view.setTextColor(ContextCompat.getColor(mContext, R.color.color_FB3A32));
            }
        }
    }


    public interface onItemClickLister {
        void onItemClick(int option, String content);
    }
}