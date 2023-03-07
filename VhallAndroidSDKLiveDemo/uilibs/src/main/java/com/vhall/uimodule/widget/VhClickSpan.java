package com.vhall.uimodule.widget;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class VhClickSpan extends ClickableSpan {

    private int colorId;
    private boolean isUnderline;

    public VhClickSpan(int colorId, boolean isUnderline) {
        this.colorId = colorId;
        this.isUnderline = isUnderline;
    }

    @Override
    public void onClick(View widget) {

    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(colorId);//颜色
        ds.setUnderlineText(isUnderline); //去掉下划线
    }

}