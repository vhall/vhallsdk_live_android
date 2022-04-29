package com.vhall.uilibs.interactive.base;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.vhall.uilibs.R;


/**
 * @author hkl
 * Date: 2020-04-30 15:13
 */
public abstract class BaseBottomDialog extends Dialog {
    protected Context mContext;
    public BaseBottomDialog(Context context) {
        super(context);
        mContext=context;
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
        win.setBackgroundDrawableResource(android.R.color.transparent);
        this.onWindowAttributesChanged(lp);
    }
}
