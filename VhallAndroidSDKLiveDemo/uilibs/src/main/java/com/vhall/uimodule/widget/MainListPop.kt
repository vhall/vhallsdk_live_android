package com.vhall.uimodule.widget

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import com.vhall.uimodule.R
import com.vhall.uimodule.databinding.PopHintBinding
import com.vhall.uimodule.utils.CommonUtil
import com.vhall.uimodule.utils.DensityUtils

/**
 * @author hkl
 * Date: 2019-11-20 16:19
 */
class MainListPop @JvmOverloads constructor(private val context: Context,val title: String, val hint: String) :
    PopupWindow(DensityUtils.dpToPxInt(context,200), ViewGroup.LayoutParams.WRAP_CONTENT) {
    var activity: Activity? = null
    override fun dismiss() {
        super.dismiss()
        val lp = activity!!.window.attributes
        lp.alpha = 1f
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        activity!!.window.attributes = lp
    }

    fun show(v: View, x: Int, y: Int) {
        showAsDropDown(v, x, y)
        activity = context as Activity
        /**
         * 点击popupWindow让背景变暗
         */
        val lp = activity?.window?.attributes
        lp?.alpha = 0.5f
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        activity?.window?.attributes = lp
        binding.tvHint.text=hint
        binding.tvTitle.text=title
    }

    private var binding: PopHintBinding

    init {
        val root = View.inflate(context, R.layout.pop_hint, null)
        binding = PopHintBinding.bind(root)
        contentView = root
        setBackgroundDrawable(null)
        isOutsideTouchable = true
        isFocusable = true

    }
}