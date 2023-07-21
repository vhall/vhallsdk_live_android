package com.vhall.uimodule.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.vhall.business.data.WebinarInfo
import com.vhall.uimodule.R
import com.vhall.uimodule.databinding.PopWatchMoreBinding
import com.vhall.uimodule.watch.card.CardListDialog
import com.vhall.uimodule.watch.notice.NoticeListDialog
import com.vhall.uimodule.watch.survey.SurveyListDialog

class WatchMorePop @JvmOverloads constructor(private val mContext: Context, val webinarInfo: WebinarInfo) :
    PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) {


    private var binding: PopWatchMoreBinding

    init {
        val root = View.inflate(mContext, R.layout.pop_watch_more, null)
        binding = PopWatchMoreBinding.bind(root)
        contentView = root
        setBackgroundDrawable(null)
        isOutsideTouchable = true
        isFocusable = true

        binding.ivNotice.setOnClickListener {
            NoticeListDialog(mContext, webinarInfo,  true)
            dismiss()
        }

        binding.ivSurvey.setOnClickListener {
            SurveyListDialog(
                mContext,
                webinarInfo
            )
            dismiss()
        }
        binding.ivCard.setOnClickListener {
            CardListDialog(
                mContext,
                webinarInfo,
                true
            )
            dismiss()
        }
    }
}