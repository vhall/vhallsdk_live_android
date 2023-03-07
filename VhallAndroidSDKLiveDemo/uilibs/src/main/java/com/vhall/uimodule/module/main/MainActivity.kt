package com.vhall.uimodule.module.main


import android.content.Intent
import android.text.TextUtils
import android.view.View
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
import com.vhall.uimodule.module.login.LoginActivity
import com.vhall.uimodule.module.warmup.WatchBaseWarmUpActivity
import com.vhall.uimodule.module.warmup.WatchWarmUpActivity
import com.vhall.uimodule.module.watch.WatchLiveActivity
import com.vhall.uimodule.utils.CommonUtil
import com.vhall.uimodule.widget.EditDialog
import com.vhall.uimodule.widget.EditDialog.ClickLister
import com.vhall.uimodule.widget.OnNoDoubleClickListener
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
        }
    }
}