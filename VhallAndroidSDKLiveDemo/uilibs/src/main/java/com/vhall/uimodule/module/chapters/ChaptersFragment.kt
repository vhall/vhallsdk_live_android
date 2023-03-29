package com.vhall.uimodule.module.chapters

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.vhall.business.VhallSDK
import com.vhall.business.data.RequestDataCallbackV2
import com.vhall.business.data.WebinarInfo
import com.vhall.uimodule.base.BaseFragment
import com.vhall.uimodule.base.IBase
import com.vhall.uimodule.databinding.FragmentChaptersBinding
import com.vhall.uimodule.module.watch.WatchLiveActivity
import com.vhall.vhss.data.RecordChaptersData

class ChaptersFragment : BaseFragment<FragmentChaptersBinding>(FragmentChaptersBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo, type: String) =
            ChaptersFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(IBase.INFO_KEY, info)
                    putString(TYPE, type)
                }
            }
    }
    private val TYPE = "type"
    private var type: String? = ""
    lateinit var chapterAdapter: ChapterAdapter
    lateinit var pointList: Collection<RecordChaptersData.ListBean>
    override fun initView() {
        val activity: WatchLiveActivity = activity as WatchLiveActivity
        arguments?.let {
            webinarInfo = it.getSerializable(IBase.INFO_KEY) as WebinarInfo
            type = it.getString(TYPE)
            chapterAdapter = ChapterAdapter(mContext, webinarInfo)
            chapterAdapter.setOnItemClickListener(OnItemClickListener { baseQuickAdapter: BaseQuickAdapter<*, *>?, view: View?, i: Int ->
                var seekTime = (chapterAdapter.data.get(i).created_at * 1000).toInt();
                activity.seekTo(seekTime)
            })
        }
        mViewBinding.recycleView.layoutManager = LinearLayoutManager(mContext)
        mViewBinding.recycleView.adapter = chapterAdapter
        mViewBinding.recycleView.setHasFixedSize(true)
        mViewBinding.refreshLayout.setOnRefreshListener {
            loadData()
        }
        loadData()
    }

    private fun loadData(){
        if (!type.equals("chapters")) {
            chapterAdapter.setList(pointList)
            return;
        }

        VhallSDK.getRecordChaptersList(
            webinarInfo.record_id,
            object : RequestDataCallbackV2<RecordChaptersData?> {
                override fun onSuccess(data: RecordChaptersData?) {
                    mViewBinding.refreshLayout.isRefreshing = false
                    if (data != null) {
                        chapterAdapter.setList(data.list)
                    }
                }
                override fun onError(errorCode: Int, errorMsg: String) {
                    mViewBinding.refreshLayout.isRefreshing = false
                }
            })
    }

    fun showVideoPoint(list: Collection<RecordChaptersData.ListBean>?) {
        if (list != null) {
            pointList = list
        };
    }
}