package com.vhall.uimodule.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.vhall.uimodule.R;
import com.vhall.uimodule.utils.ToastUtils;


/**
 * @author hkl
 */
public class EditDialog extends Dialog {

    private Context mContext;
    private EditText etContent;
    private ClickLister mClickLister;

    public EditDialog(Context context, ClickLister clickLister) {
        super(context);
        mContext = context;
        mClickLister = clickLister;
        setCanceledOnTouchOutside(false);
        init();
    }

    public void init() {
        setContentView(R.layout.dialog_with_edit);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        TextView tvConfirm = findViewById(R.id.tv_confirm);
        etContent = findViewById(R.id.et_content);

        tvCancel.setOnClickListener(v -> {
            if (null != mClickLister) {
                mClickLister.onClickCancel();
            }
            dismiss();
        });
        tvConfirm.setOnClickListener(v -> {
            if (null != mClickLister) {
                String conent = "";
                try {
                    conent = etContent.getText().toString().trim();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (TextUtils.isEmpty(conent)) {
                    ToastUtils.Companion.showToast("输入内容不能为空");
                } else {
                    mClickLister.onClickConfirm(conent);
                    dismiss();
                }
            }
        });
        Window win = this.getWindow();
        win.setBackgroundDrawableResource(android.R.color.transparent);
    }

    public void setEditHint(String hint) {
        if (etContent != null && !TextUtils.isEmpty(hint)) {
            etContent.setHint(hint);
        }
    }

    public void showAsPasswordType() {
        if (etContent != null) {
            etContent.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public interface ClickLister {
        void onClickCancel();

        void onClickConfirm(String content);
    }
}
