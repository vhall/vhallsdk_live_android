package com.vhall.uimodule.widget

import android.content.Context
import android.os.Bundle
import android.view.View
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseBottomDialog
import com.vhall.uimodule.databinding.DialogStartBroBinding

/**
 * @author hkl
 *Date: 2022/12/7 17:49
 */
class StartBroDialog(context: Context) : BaseBottomDialog(context) {
    private lateinit var binding: DialogStartBroBinding
    private var mCusListener: CustomListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = View.inflate(context, R.layout.dialog_start_bro, null)
        binding = DialogStartBroBinding.bind(root)
        setContentView(binding.root)
        binding.tvJoin.setOnClickListener {
            mCusListener?.onWatchClick()
        }

        setCancelable(true)
        setCanceledOnTouchOutside(false)
    }

    fun setCusListener(custom: CustomListener) {
        mCusListener = custom
    }

    interface CustomListener {
        fun onWatchClick()
    }
}