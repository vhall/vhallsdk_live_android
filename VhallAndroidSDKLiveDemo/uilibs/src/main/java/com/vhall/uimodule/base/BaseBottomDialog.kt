package com.vhall.uimodule.base

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import com.vhall.uimodule.R
import android.view.Gravity

/**
 * @author hkl
 * Date: 2020-04-30 15:13
 */
abstract class BaseBottomDialog @JvmOverloads constructor( var mContext: Context) : IBase,Dialog(
    mContext
) {
    protected fun init() {
        val win = this.window
        win!!.decorView.setPadding(0, 0, 0, 0)
        val lp = win.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.windowAnimations = R.style.DialogBottomInAndOutStyle
        lp.gravity = Gravity.BOTTOM or Gravity.END
        win.attributes = lp
        win.setBackgroundDrawableResource(android.R.color.transparent)
        onWindowAttributesChanged(lp)
    }

    init {
        init()
    }
}