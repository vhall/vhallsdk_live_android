package com.vhall.uilibs.interactive.broadcast;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business_interactive.InterActive;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.OnNoDoubleClickListener;
import com.vhall.uilibs.interactive.broadcast.config.RtcConfig;
import com.vhall.uilibs.interactive.doc.EnumDoc;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.vhss.data.DocListInfoData;
import com.vhall.vhss.data.WebinarInfoData;
import java.text.DecimalFormat;
import static android.view.View.VISIBLE;

/**
 * @author hkl
 * 文档选择界面
 */
public class DocChooseActivity extends FragmentActivity {

    public final static String KEY_WEBINAR_INFO_DATA = "webinarInfoData";

    public static void startActivityForResult(Activity context, WebinarInfoData webinarInfoData, int code) {
        Intent intent = new Intent(context, DocChooseActivity.class);
        intent.putExtra(KEY_WEBINAR_INFO_DATA, webinarInfoData);
        context.startActivityForResult(intent, code);
    }

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private DocAdapter adapter;
    private TextView tvEmpty;
    private int page = 1;
    private String chooseId = "";
    private WebinarInfoData mWebinarInfoData;

    private InterActive mInterActive = RtcConfig.getInterActive();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_doc);
        mWebinarInfoData = (WebinarInfoData) getIntent().getSerializableExtra(KEY_WEBINAR_INFO_DATA);
        if (mWebinarInfoData == null) {
            baseShowToast("error info");
            finish();
        }
        int orientation = mWebinarInfoData.getWebinar_show_type();
        if (1 == orientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        initView();
    }


    protected void initView() {
        recyclerView = findViewById(R.id.recycle_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        tvEmpty = findViewById(R.id.tv_empty);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(DocChooseActivity.this);
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
//        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
//            @Override
//            public void onLoadMoreRequested() {
//                page++;
//                getListInfo();
//            }
//        }, recyclerView);

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.tv_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(chooseId)) {
                    baseShowToast("请选择一个文档");
                } else {
                    Intent result = new Intent();
                    result.putExtra("docId", chooseId);
                    setResult(RESULT_OK, result);
                    finish();
                }
            }
        });
    }

    private void getListInfo() {
        mInterActive.getDocList(page, 10, "", new RequestDataCallbackV2<DocListInfoData>() {
            @Override
            public void onSuccess(DocListInfoData result) {
                hideLoadProgress();
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
                hideLoadProgress();
                adapter.setNewData(null);
                tvEmpty.setVisibility(VISIBLE);
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadProgress();
        getListInfo();
    }

    private void showLoadProgress() {

    }

    public void hideLoadProgress() {

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

}
