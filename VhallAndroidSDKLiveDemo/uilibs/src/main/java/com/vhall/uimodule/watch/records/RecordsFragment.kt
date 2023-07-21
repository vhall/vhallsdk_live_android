package com.vhall.uimodule.watch.records

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
import com.vhall.uimodule.watch.WatchLiveActivity
import com.vhall.vhss.data.RecordsData

class RecordsFragment : BaseFragment<FragmentChaptersBinding>(FragmentChaptersBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo, type: String) =
            RecordsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(IBase.INFO_KEY, info)
                    putString(TYPE, type)
                }
            }
    }
    private val TYPE = "type"
    private var type: String? = ""
    lateinit var rcordAdapter: RecordAdapter
    override fun initView() {
        val activity: WatchLiveActivity = activity as WatchLiveActivity
        arguments?.let {
            webinarInfo = it.getSerializable(IBase.INFO_KEY) as WebinarInfo
            type = it.getString(TYPE)
            rcordAdapter = RecordAdapter(mContext, webinarInfo)
            rcordAdapter.setOnItemClickListener(OnItemClickListener { baseQuickAdapter: BaseQuickAdapter<*, *>?, view: View?, i: Int ->
                var recodID = (rcordAdapter.data.get(i).record_id).toString();
                activity.openRecod(recodID)
            })
        }
        mViewBinding.recycleView.layoutManager = LinearLayoutManager(mContext)
        mViewBinding.recycleView.adapter = rcordAdapter
        mViewBinding.recycleView.setHasFixedSize(true)
        mViewBinding.refreshLayout.setOnRefreshListener {
            loadData()
        }
        loadData()
    }

    private fun loadData(){
        VhallSDK.getRecordList(
            webinarInfo.webinar_id,0,100,
            object : RequestDataCallbackV2<RecordsData?> {
                override fun onSuccess(data: RecordsData?) {
                    mViewBinding.refreshLayout.isRefreshing = false
                    if (data != null) {
                        rcordAdapter.setList(data.list)
                    }
                }
                override fun onError(errorCode: Int, errorMsg: String) {
                    mViewBinding.refreshLayout.isRefreshing = false
                }
            })
    }
}