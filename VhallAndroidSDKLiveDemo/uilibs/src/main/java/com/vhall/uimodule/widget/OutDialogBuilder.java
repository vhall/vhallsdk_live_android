package com.vhall.uimodule.widget;

import android.content.Context;

/**
 * @author hkl
 * Date: 2020-06-11 14:38
 */
public class OutDialogBuilder {
    public String title;
    public String title1;
    public String tv1;
    public String tv2;
    public OutDialog.ClickLister ClickLister1;
    public OutDialog.ClickLister ClickLister2;
    public int color1;
    public int color2;
    public int layout = 0;
    public boolean dismiss2 = true;
    public boolean dismiss1 = true;

    public OutDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public OutDialogBuilder layout(int layout) {
        this.layout = layout;
        return this;
    }

    public OutDialogBuilder dismiss2(boolean dismiss2) {
        this.dismiss2 = dismiss2;
        return this;
    }

    public OutDialogBuilder dismiss1(boolean dismiss1) {
        this.dismiss1 = dismiss1;
        return this;
    }

    public OutDialogBuilder title1(String title1) {
        this.title1 = title1;
        return this;
    }

    public OutDialogBuilder tv1(String tv1) {
        this.tv1 = tv1;
        return this;
    }

    public OutDialogBuilder tv2(String tv2) {
        this.tv2 = tv2;
        return this;
    }

    public OutDialogBuilder clickLister1(OutDialog.ClickLister ClickLister1) {
        this.ClickLister1 = ClickLister1;
        return this;
    }

    public OutDialogBuilder clickLister2(OutDialog.ClickLister ClickLister2) {
        this.ClickLister2 = ClickLister2;
        return this;
    }

    public OutDialogBuilder color1(int color1) {
        this.color1 = color1;
        return this;
    }

    public OutDialogBuilder color2(int color2) {
        this.color2 = color2;
        return this;
    }

    public OutDialog build(Context context) {
        return new OutDialog(context, this);
    }

}
