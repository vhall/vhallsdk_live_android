package com.vhall.uimodule.watch.doc

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.vhall.business.MessageServer
import com.vhall.business.data.WebinarInfo
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseFragment
import com.vhall.uimodule.base.IBase.*
import com.vhall.uimodule.databinding.FragmentDocBinding
import com.vhall.uimodule.watch.WatchLiveActivity


class DocFragment : BaseFragment<FragmentDocBinding>(FragmentDocBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo) =
            DocFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(INFO_KEY, info)
                }
            }
    }

    //是否全屏
    private var isFull = false

    override fun initView() {
        arguments?.let {
            webinarInfo = it.getSerializable(INFO_KEY) as WebinarInfo
        }
        mViewBinding.ivFull.setOnClickListener {
            if (isFull) {
                doc2Portrait()
            } else {
               doc2Landscape()
            }
        }
    }

    fun doc2Portrait() {
        (activity as WatchLiveActivity).call(HALF_DOC_SCREEN_KEY, "", null)
        mViewBinding.ivFull.setBackgroundResource(R.mipmap.icon_type_full)
        isFull = !isFull
    }

    private fun doc2Landscape() {
        (activity as WatchLiveActivity).call(FULL_DOC_SCREEN_KEY, "", null)
        mViewBinding.ivFull.setBackgroundResource(R.mipmap.icon_type_full_exit)
        isFull = !isFull
    }

    fun dealMessageData(messageInfo: MessageServer.MsgInfo) {
        when (messageInfo.event) {
            MessageServer.EVENT_PAINTH5DOC -> {
                mViewBinding.flH5Doc.removeAllViews()
                val params = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                params.addRule(RelativeLayout.CENTER_IN_PARENT)
                mViewBinding.flH5Doc.addView(messageInfo.h5DocView, params)
            }
            MessageServer.EVENT_SHOWH5DOC -> {
                if (messageInfo.watchType == 1) {
                    mViewBinding.flH5Doc.visibility = View.VISIBLE
                } else {
                    mViewBinding.flH5Doc.visibility = View.GONE
                }

            }
        }
    }
}