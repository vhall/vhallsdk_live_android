package com.vhall.uilibs.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.vhss.data.AgreementData;

import java.util.List;

/**
 * @author hkl
 */

public class AgreementDialog extends BaseBottomDialog implements View.OnClickListener {


    private AgreementData agreementData;
    private OnItemClickLister onItemClickLister;
    private TextView tvTitle, tvContent, tvAgree, tvHint;

    public AgreementDialog(Context context) {
        super(context);
    }

    public void setOnItemClickLister(OnItemClickLister onItemClickLister) {
        if (onItemClickLister != null) {
            this.onItemClickLister = onItemClickLister;
        }
    }

    public void setAgreementData(AgreementData agreementData) {
        this.agreementData = agreementData;
        if (agreementData == null) {
            return;
        }
        updateUI();
    }

    private void updateUI() {
        if (tvTitle == null) {
            return;
        }
        tvTitle.setText(agreementData.title);
        tvContent.setText(agreementData.content);
        //rule 协议规则 0:同意后进入 1:阅读后进入
        if (TextUtils.equals("0", agreementData.rule)) {
            findViewById(R.id.tv_refuse).setVisibility(View.VISIBLE);
            tvAgree.setText("同意并继续");
        } else {
            findViewById(R.id.tv_refuse).setVisibility(View.GONE);
            tvAgree.setText("同意并继续");
        }
        // statement_status 声明状态  0:关 1:开
        if (!ListUtils.isEmpty(agreementData.statement_info) && TextUtils.equals("1", agreementData.statement_status)) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(agreementData.statement_content);
            for (int i = 0; i < agreementData.statement_info.size(); i++) {
                AgreementData.StatementInfoBean statementInfoBean = agreementData.statement_info.get(i);
                if (statementInfoBean != null && !TextUtils.isEmpty(statementInfoBean.title) && agreementData.statement_content.contains(statementInfoBean.title)) {
                    VhClickSpan span = new VhClickSpan(mContext.getResources().getColor(R.color.color_3562FA), false) {
                        @Override
                        public void onClick(View widget) {
                            if (onItemClickLister != null && !ListUtils.isEmpty(agreementData.statement_info)) {
                                onItemClickLister.jumpWeb(statementInfoBean.link);
                            }
                        }
                    };
                    spannableStringBuilder.setSpan(span, agreementData.statement_content.indexOf(statementInfoBean.title), agreementData.statement_content.indexOf(statementInfoBean.title) + statementInfoBean.title.length(), SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            tvHint.setMovementMethod(LinkMovementMethod.getInstance());//不设置点击会失效
            tvHint.setText(spannableStringBuilder);
            tvHint.setVisibility(View.VISIBLE);
        } else {
            tvHint.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_agreement);
        tvTitle = findViewById(R.id.tv_title);
        tvContent = findViewById(R.id.tv_content);
        tvAgree = findViewById(R.id.tv_agree);
        tvHint = findViewById(R.id.tv_hint);
        tvAgree.setOnClickListener(this);
        findViewById(R.id.tv_refuse).setOnClickListener(this);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);
        updateUI();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_refuse) {
            dismiss();
            ToastUtil.showToast("很遗憾无法继续为您提供服务");
        }
        if (v.getId() == R.id.tv_cancel) {
            dismiss();

        }
        if (v.getId() == R.id.tv_agree) {
            if (onItemClickLister != null) {
                onItemClickLister.onItemClick();
            }
            dismiss();
        }
    }

    @Override
    public void show() {
        super.show();
    }

    public interface OnItemClickLister {
        void onItemClick();

        void jumpWeb(String url);
    }
}