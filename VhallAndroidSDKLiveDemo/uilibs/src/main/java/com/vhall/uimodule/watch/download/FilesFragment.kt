package com.vhall.uimodule.watch.download

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.vhall.business.VhallSDK
import com.vhall.business.data.RequestDataCallbackV2
import com.vhall.business.data.WebinarInfo
import com.vhall.logmanager.VLog
import com.vhall.uimodule.base.BaseFragment
import com.vhall.uimodule.base.IBase
import com.vhall.uimodule.databinding.FragmentChaptersBinding
import com.vhall.uimodule.watch.WatchLiveActivity
import com.vhall.vhss.data.FilesData

class FilesFragment : BaseFragment<FragmentChaptersBinding>(FragmentChaptersBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo, type: String) =
            FilesFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(IBase.INFO_KEY, info)
                    putString(TYPE, type)
                }
            }
    }
    private val TYPE = "type"
    private var type: String? = ""
    lateinit var fileAdapter: FileAdapter
    override fun initView() {
        val activity: WatchLiveActivity = activity as WatchLiveActivity
        arguments?.let {
            webinarInfo = it.getSerializable(IBase.INFO_KEY) as WebinarInfo
            type = it.getString(TYPE)
            fileAdapter = FileAdapter(mContext, webinarInfo)
            fileAdapter.setOnItemClickListener(OnItemClickListener { baseQuickAdapter: BaseQuickAdapter<*, *>?, view: View?, i: Int ->
                var file_id = (fileAdapter.data.get(i).file_id).toString();
//                activity.openRecod(recodID)
                VhallSDK.getFileDownLoadUrl(webinarInfo.webinar_id,
                    webinarInfo.file_download_menu,file_id,
                    object : RequestDataCallbackV2<String?> {
                    override fun onSuccess(result: String?) {
                        VLog.d("FilesFragment",result)
                        if (result != null && result.contains("https://")) {
                            //通过浏览器打开URL
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setData(Uri.parse(result))
                            startActivity(intent)
                        }
                    }
                    override fun onError(errorCode: Int, errorMsg: String) {
                        showToast(errorMsg)
                    }
                })
            })
        }
        mViewBinding.recycleView.layoutManager = LinearLayoutManager(mContext)
        mViewBinding.recycleView.adapter = fileAdapter
        mViewBinding.recycleView.setHasFixedSize(true)
        mViewBinding.refreshLayout.setOnRefreshListener {
            loadData()
        }
        loadData()
    }

    public fun refreshData(){
        mViewBinding.refreshLayout.isRefreshing = true
        loadData()
    }

    private fun loadData(){
        VhallSDK.getFilesList(webinarInfo.webinar_id,webinarInfo.file_download_menu,
            object : RequestDataCallbackV2<FilesData?> {
                override fun onSuccess(data: FilesData?) {
                    mViewBinding.refreshLayout.isRefreshing = false
                    if (data != null) {
                        fileAdapter.setList(data.list)
                    }
                }
                override fun onError(errorCode: Int, errorMsg: String) {
                    mViewBinding.refreshLayout.isRefreshing = false
                }
            })
    }
}