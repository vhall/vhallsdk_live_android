package com.vhall.uimodule.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;
import com.vhall.uimodule.utils.CommonUtil;
import com.vhall.uimodule.utils.DensityUtils;
import com.vhall.uimodule.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hkl
 */

public class ScrollChooseTypeDialog extends BaseBottomDialog implements View.OnClickListener {

    private onItemClickLister onItemClickLister;
    private List<String> stringList = new ArrayList<>();
    private int showNum = 5;
    private RecyclerView recyclerView;
    private int layout = R.layout.dialog_scroll_type;
    private int choosePosition = -1;
    private MyAdapter adapter = new MyAdapter();

    public void setOnItemClickLister(ScrollChooseTypeDialog.onItemClickLister onItemClickLister) {
        this.onItemClickLister = onItemClickLister;
    }


    public ScrollChooseTypeDialog(Context context, List<String> stringList) {
        super(context);
        this.stringList = stringList;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(layout);
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
            layoutParams.height = DensityUtils.dpToPxInt(getMContext(), 275);
            recyclerView.setLayoutParams(layoutParams);
        }
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getMContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setNewData(stringList);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (onItemClickLister != null) {
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
                String a="1";
                float b= Float.parseFloat(a);
            }
        } else if (id == R.id.root || id == R.id.tv_cancel) {
            dismiss();
        }
    }


    class MyAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_dialog_scroll_list);
        }

        @Override
        protected void convert(BaseViewHolder helper, String info) {
            TextView view = helper.getView(R.id.tv);
            view.setText(CommonUtil.changeDefinition(info));
            view.setTextColor(ContextCompat.getColor(getMContext(), R.color.color_22));
        }
    }


    public interface onItemClickLister {
        void onItemClick(int option, String content);
    }
}