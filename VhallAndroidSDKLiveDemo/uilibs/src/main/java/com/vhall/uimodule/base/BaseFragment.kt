package com.vhall.uimodule.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.vhall.business.data.WebinarInfo

open class BaseFragment<VB : ViewBinding>(val inflateFunc: (LayoutInflater) -> VB) :
    Fragment(), IBase {
    private lateinit var _viewBinding: VB
    protected val mViewBinding
        get() = _viewBinding
    protected lateinit var mContext: Context
    protected lateinit var webinarInfo: WebinarInfo

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = inflateFunc(inflater)
        return _viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }
    open fun initView() {
    }


}