package com.vhall.uimodule.module.introduction

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.vhall.business.data.WebinarInfo
import com.vhall.uimodule.base.BaseFragment
import com.vhall.uimodule.base.IBase.INFO_KEY
import com.vhall.uimodule.databinding.FragmentInfoBinding
import com.vhall.uimodule.utils.CommonUtil


class WebinarInfoFragment : BaseFragment<FragmentInfoBinding>(FragmentInfoBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo) =
            WebinarInfoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(INFO_KEY, info)
                }
            }
    }

    override fun initView() {
        arguments?.let {
            webinarInfo = it.getSerializable(INFO_KEY) as WebinarInfo
        }
        mViewBinding.tvTime.text = webinarInfo.start_time
        mViewBinding.tvTitle.text = CommonUtil.getLimitString(webinarInfo.subject, 8)
        var introduction: String? = webinarInfo.introduction
        if (introduction.isNullOrEmpty() || TextUtils.equals("<p></p>", webinarInfo.introduction)) {
            mViewBinding.tvEmpty.visibility = View.VISIBLE
            mViewBinding.web.visibility = View.GONE
        } else {
            val wordBreakConfig = "<body style=\"word-wrap:break-word;\"> </body>"
            introduction = wordBreakConfig.plus(introduction)
            introduction =
                introduction.replace("<img", "<img style=\"max-width:100%;height:auto\" ")
            mViewBinding.web.loadDataWithBaseURL("", introduction, "text/html", "utf-8", null)
            mViewBinding.tvEmpty.visibility = View.GONE
            mViewBinding.web.setBackgroundColor(0)
            mViewBinding.web.background.alpha = 0
        }
    }

}