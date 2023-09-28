package com.vhall.uimodule.main


import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.vhall.business.VhallSDK
import com.vhall.business.data.WatchAuthInfo
import com.vhall.business.data.WebinarInfo
import com.vhall.business.data.source.WebinarInfoDataSource
import com.vhall.business.data.source.WebinarInfoDataSource.LoadWebinarInfoCallback
import com.vhall.business.data.source.WebinarInfoDataSource.WatchAuthCallback
import com.vhall.logmanager.VLog
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseActivity
import com.vhall.uimodule.dao.UserDataStore
import com.vhall.uimodule.databinding.ActivityMainBinding
import com.vhall.uimodule.login.LoginActivity
import com.vhall.uimodule.watch.warmup.WatchBaseWarmUpActivity
import com.vhall.uimodule.watch.warmup.WatchWarmUpActivity
import com.vhall.uimodule.watch.WatchLiveActivity
import com.vhall.uimodule.publish.PublishActivity
import com.vhall.uimodule.utils.CommonUtil
import com.vhall.uimodule.webview.WebviewActivity
import com.vhall.uimodule.widget.EditDialog
import com.vhall.uimodule.widget.EditDialog.ClickLister
import com.vhall.uimodule.widget.OnNoDoubleClickListener
import com.vhall.zxing.client.android.CaptureActivity
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun initView() {
        super.initView()
        getData()
        mViewBinding.tvLoginOut.setOnClickListener {
            VhallSDK.logout()
            finish()
            startActivity(Intent(mContext, LoginActivity::class.java))
        }
        val requestOptions: RequestOptions =
            RequestOptions.bitmapTransform(CircleCrop()).placeholder(R.mipmap.icon_avatar)
        Glide.with(mContext).load(VhallSDK.getUserAvatar()).apply(requestOptions)
            .into(mViewBinding.ivAvatar)
        mViewBinding.tvName.text = VhallSDK.getUserNickname()
        mViewBinding.tvJoin.setOnClickListener(object : OnNoDoubleClickListener() {
            override fun onNoDoubleClick(v: View?) {
                prepareJoin()
            }
        })
        mViewBinding.tvJoinH5.setOnClickListener(object : OnNoDoubleClickListener() {
            override fun onNoDoubleClick(v: View?) {
                var url:String? = null
                if (mViewBinding.edWatchId.text.toString().isNotEmpty())
                    url =  VhallSDK.NEW_SURVEY_URL+"/v3/lives/embedclient/watch/"+mViewBinding.edWatchId.text.toString()
                WebviewActivity.startActivity(this@MainActivity,url)
            }
        })
        mViewBinding.tvPushlish.setOnClickListener(object : OnNoDoubleClickListener() {
            override fun onNoDoubleClick(v: View?) {
                if (mViewBinding.edWatchId.text.toString().isNotEmpty()) {
                    if (CommonUtil.isGrantedAndRequestPermission(this@MainActivity, 101)){
                        showLoading(null, "正在加载活动信息")
                        doInitPublish()
                    }else{
                        showToast(getString(R.string.app_permission_av_none1))
                    }
                }
                else {
                    showToast("请填写活动id")
                }
            }
        })
        mViewBinding.ivQrcode.setOnClickListener(object : OnNoDoubleClickListener() {
            override fun onNoDoubleClick(v: View?) {
                launchQrcodeActivity()
            }
        })
    }

    private fun prepareJoin() {
        if (mViewBinding.edWatchId.text.toString().isNotEmpty()) {
            showLoading(null, "正在加载活动信息")
            if (mViewBinding.mainAuthmodel.isChecked) {
                doQueryWatchAuth()
            } else {
                doInitWatch()
            }
        }
    }

    private fun doQueryWatchAuth() {
        VhallSDK.queryWatchAuth(mViewBinding.edWatchId.text.toString(),
            object : WebinarInfoDataSource.WatchAuthQueryCallback {
                override fun onError(
                    code: Int,
                    msg: String?
                ) {
                    finishLoading()
                    showToast(msg)
                    VLog.d("WatchAuth", "---> code:$code, msg:$msg")
                }

                override fun onSucceed(authQueryInfo: WatchAuthInfo.QueryInfo) {
                    if (authQueryInfo.auth_status) {//需要校验权限
                        finishLoading()
                        parseForSecondCheck(authQueryInfo)
                    } else {
                        doInitWatch()
                    }
                }
            })
    }

    private fun doCheckWatchAuth(verifyValue: String, authInfo: WatchAuthInfo.QueryInfo) {
        VhallSDK.checkWatchAuth(
            mViewBinding.edWatchId.text.toString(), authInfo.type, verifyValue,
            object : WatchAuthCallback {
                override fun onFailed(
                    code: Int,
                    msg: String?,
                    authInfo: WatchAuthInfo?
                ) {
                    finishLoading()
                    showToast(msg)
                    VLog.d("WatchAuth", "---> code:$code, msg:$msg")
                }

                override fun onSucceed(authInfo: WatchAuthInfo) {
                    doInitWatch()
                    VLog.d(
                        "WatchAuth",
                        "---> visitor_id:${authInfo.visitor_id}, join_id:${authInfo.join_id}, status:${authInfo.status}"
                    )
                }
            })
    }

    private fun parseForSecondCheck(authInfo: WatchAuthInfo.QueryInfo?) {
        if (authInfo?.type.equals("1")) {
            showContentDialog(getString(R.string.watch_auth_hint_pwd), authInfo)
        } else {
            showContentDialog(getString(R.string.watch_auth_hint_whitelist), authInfo)
        }
    }

    private fun showContentDialog(hint: String, authInfo: WatchAuthInfo.QueryInfo?) {
        var dialog = EditDialog(this, object : ClickLister {
            override fun onClickCancel() {
                showToast("取消")
            }

            override fun onClickConfirm(content: String) {
                if (!TextUtils.isEmpty(content) && null != authInfo) {
                    showLoading(null, "正在加载活动信息")
                    doCheckWatchAuth(content, authInfo)
                } else {
                    val errMsg = if (TextUtils.isEmpty(content)) "输入内容不能为空" else "authInfo is null"
                    showToast(errMsg)
                }
            }
        })
        dialog.apply {
            setEditHint(hint)
            show()
        }
    }

    private fun doInitWatch() {
        VhallSDK.initWatch(
            mViewBinding.edWatchId.text.toString(),
            "",
            "",
            true,
            object : LoadWebinarInfoCallback {
                override fun onError(p0: Int, errorMsg: String?) {
                    finishLoading()
                    showToast(errorMsg)
                }

                override fun onWebinarInfoLoaded(js: String?, info: WebinarInfo) {
                    //活动状态 1直播；2预告；3结束；4回放或点播；5录播
                    webinarInfo = info
                    saveData()
                    finishLoading()
                    when (info.status) {
                        1, 4, 5 -> {
                            if (CommonUtil.isGrantedAndRequestPermission(
                                    this@MainActivity,
                                    100
                                )
                            )
                                WatchLiveActivity.startActivity(mContext, info)
                        }
                        2 -> {
                            WatchBaseWarmUpActivity.startActivityForResult(this@MainActivity, info)
                        }
                        3 -> {
                            showToast("直播已结束")
                        }
                        else -> {}
                    }
                }
            }
        )
    }

    private fun  doInitPublish(){
        val intent = Intent(this, PublishActivity::class.java)
        VhallSDK.initBroadcast(
            mViewBinding.edWatchId.text.toString(),
            "",
            "",
            object : LoadWebinarInfoCallback {
                override fun onWebinarInfoLoaded(jsonStr: String, info: WebinarInfo) {
                    webinarInfo = info
                    saveData()
                    finishLoading()
                    if (webinarInfo.is_director == 1) {
                        Toast.makeText(this@MainActivity, "当前直播是云导播活动请退出", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }
                    intent.putExtra("webinarInfo", webinarInfo)
                    startActivity(intent)
                }

                override fun onError(errorCode: Int, errorMsg: String) {
                    finishLoading()
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            })
    }

    var watchId = ""
    private fun getData() {
        lifecycleScope.launch {
            watchId = UserDataStore.getWatchId(mContext)
            mViewBinding.edWatchId.setText(watchId)
        }
    }

    private fun saveData() {
        lifecycleScope.launch {
            UserDataStore.saveWatch(
                mViewBinding.edWatchId.text.toString(), "", "", mContext
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == WatchWarmUpActivity.CODE_REQUEST) {
            prepareJoin()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (!CommonUtil.isGrantedPermission(this@MainActivity)) {
                showToast("没有权限影响轮训和上麦")
            }
            WatchLiveActivity.startActivity(mContext,webinarInfo)
        }else if (requestCode == 101) {
            if (CommonUtil.isGrantedPermission(this@MainActivity)) {
                showLoading(null, "正在加载活动信息")
                doInitPublish()
            } else {
                showToast("没有相机和麦克风权限无法发起直播")
            }
        }else if (requestCode == 102) {
            if (!CommonUtil.isGrantedPermission(this@MainActivity)) {
                showToast("没有相机权限无法扫描")
            }
        }
    }

    //以下为扫描二维码代码
    private val launcherActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        if (result != null && result?.resultCode == RESULT_OK) {
            var qrcodeStr: String = result?.data?.getStringExtra("qrcode").toString()
            qrcodeStr= qrcodeStr?.trim()?.lowercase().toString()
            if(qrcodeStr != null && qrcodeStr.length>0 && qrcodeStr.startsWith("http")){
                var url= qrcodeStr.split("?")[0]
                var urlList= url.split("/")
                mViewBinding.edWatchId.setText(urlList.last())
            } else {
                showToast("请扫描正确的二维码")
            }
        }
    }

    //二维码扫描
    fun launchQrcodeActivity(){
        if (CommonUtil.isGrantedAndRequestPermission(this, 102)){
            launcherActivity.launch(Intent(this, CaptureActivity::class.java))
        }else{
            showToast(getString(R.string.app_permission_av_none1))
        }
    }
}