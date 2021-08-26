package com.vhall.uilibs.interactive.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business_interactive.InterActive;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;
import com.vhall.uilibs.interactive.base.OnNoDoubleClickListener;
import com.vhall.uilibs.interactive.broadcast.DocChooseActivity;
import com.vhall.uilibs.interactive.broadcast.config.RtcConfig;
import com.vhall.uilibs.interactive.doc.EnumDoc;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.vhss.data.DocListInfoData;
import com.vhall.vhss.data.WebinarInfoData;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @author hkl
 */

public class ChooseDocDialog extends Dialog {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private DocAdapter adapter;
    private TextView tvEmpty;
    private int page = 1;
    private String chooseId = "";
    private ChooseIdClickLister chooseIdClickLister;
    private Activity activity;

    private InterActive mInterActive = RtcConfig.getInterActive();


    public ChooseDocDialog(Activity context,ChooseIdClickLister chooseIdClickLister) {
        super(context,R.style.full_dialog);
        activity=context;
        this.chooseIdClickLister=chooseIdClickLister;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.activity_choose_doc);
        initView();
        getListInfo();
        init();
    }
    protected void init() {
        Window win = this.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.windowAnimations = R.style.DialogBottomInAndOutStyle;
        lp.gravity = Gravity.BOTTOM | Gravity.END;
        win.setAttributes(lp);
        win.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.onWindowAttributesChanged(lp);
    }

    public int getDisplayWidth(Activity aAty) {
        DisplayMetrics dm = new DisplayMetrics();
        //将当前窗口的一些信息放在DisplayMetrics类中，
        aAty.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public int getDisplayHeight(Activity aAty) {
        if (aAty == null) return 600;
        DisplayMetrics dm = new DisplayMetrics();
        aAty.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }


    protected void initView() {
        recyclerView = findViewById(R.id.recycle_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        tvEmpty = findViewById(R.id.tv_empty);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        adapter = new DocAdapter();
        recyclerView.setAdapter(adapter);
        adapter.bindToRecyclerView(recyclerView);
        adapter.disableLoadMoreIfNotFullPage();
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                getListInfo();
            }
        });

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        findViewById(R.id.tv_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(chooseId)) {
                    baseShowToast("请选择一个文档");
                } else {
                    if (chooseIdClickLister!=null){
                        chooseIdClickLister.onChooseIdClick(chooseId);
                    }
                    dismiss();
                }
            }
        });
    }

    private void getListInfo() {
        mInterActive.getDocList(page, 10, "", new RequestDataCallbackV2<DocListInfoData>() {
            @Override
            public void onSuccess(DocListInfoData result) {
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                if (page == 1) {
                    if (result != null && !ListUtils.isEmpty(result.list)) {
                        recyclerView.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);

                        adapter.setNewData(result.list);
                        adapter.disableLoadMoreIfNotFullPage(recyclerView);
                        if (result.list.size() < 10) {
                            adapter.loadMoreEnd();
                        }
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (result != null && !ListUtils.isEmpty(result.list)) {
                        adapter.addData(result.list);
                        if (result.list.size() < 10) {
                            adapter.loadMoreEnd();
                            return;
                        }
                        adapter.loadMoreComplete();
                    } else {
                        adapter.loadMoreEnd();
                    }
                }
            }

            @Override
            public void onError(int eventCode, String msg) {
                adapter.setNewData(null);
                tvEmpty.setVisibility(View.VISIBLE);
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void baseShowToast(String text) {
        ToastUtil.showToast(text);
    }


    class DocAdapter extends BaseQuickAdapter<DocListInfoData.DetailBean, BaseViewHolder> {

        public DocAdapter() {
            super(R.layout.item_doc_list);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, final DocListInfoData.DetailBean info) {
            helper.setText(R.id.tv_title, info.file_name);
            DecimalFormat df = new DecimalFormat("######0.00");
            double size = ((double) info.size) / 1024 / 1024;
            helper.setText(R.id.tv_time, info.created_at + " " + df.format(size) + "MB");
            CheckBox checkBox = helper.getView(R.id.checkbox);
            checkBox.setOnCheckedChangeListener(null);
            if (TextUtils.equals(chooseId, info.document_id)) {
                checkBox.setChecked(true);
                helper.getView(R.id.cl_root).setBackgroundColor(ContextCompat.getColor(mContext, R.color.color_EFF6FE));
            } else {
                checkBox.setChecked(false);
                helper.getView(R.id.cl_root).setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        chooseId = info.document_id;
                        adapter.notifyDataSetChanged();
                    } else {
                        chooseId = "";
                    }
                }
            });
            helper.getView(R.id.cl_root).setOnClickListener(new OnNoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    if (TextUtils.equals(chooseId, info.document_id)) {
                        chooseId = "";
                    } else {
                        chooseId = info.document_id;
                    }
                    adapter.notifyDataSetChanged();
                }
            });
            EnumDoc doc = EnumDoc.parseDoc(info.ext.toLowerCase());
            helper.setBackgroundColor(R.id.iv_icon,doc==null?R.mipmap.icon_jpg:doc.icon);
        }
    }

    public interface ChooseIdClickLister {
        void onChooseIdClick(String docId);
    }
}