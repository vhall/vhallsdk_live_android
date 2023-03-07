package com.vhall.uimodule.module.login

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.vhall.business.utils.SignatureUtil
import com.vhall.uimodule.base.BaseActivity
import com.vhall.uimodule.base.UIModuleProvider
import com.vhall.uimodule.dao.UserDataStore
import com.vhall.uimodule.databinding.ActivitySignConfigBinding
import com.vhall.uimodule.utils.copy
import com.vhall.uimodule.widget.MainListPop
import kotlinx.coroutines.launch

class SignConfigActivity :
    BaseActivity<ActivitySignConfigBinding>(ActivitySignConfigBinding::inflate) {
    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, SignConfigActivity::class.java))
        }
    }

    override fun initView() {
        mViewBinding.ivBack.setOnClickListener { finish() }
        mViewBinding.edAppName.text = SignatureUtil.getPackageName(mContext)
        mViewBinding.edAppRsa.text = SignatureUtil.getSignatureSHA1(mContext)

        lifecycleScope.launch {
            mViewBinding.edAppKey.setText(UserDataStore.getAppKey(mContext))
            mViewBinding.edAppSecretKey.setText(UserDataStore.getAppSecret(mContext))
        }
        mViewBinding.edAppRsa.setOnClickListener {
            mViewBinding.edAppRsa.text.toString().copy(mContext)
        }
        mViewBinding.edAppName.setOnClickListener {
            mViewBinding.edAppName.text.toString().copy(mContext)
        }
        mViewBinding.ivQ.setOnClickListener {
            MainListPop(
                mContext,
                "如何设置签名",
                "APPKey及APP SecretKey从微吼控制台开发设置中获取，参考文档开通账号/权限；将签名信息复制到微吼控制台开发设置中对应的位置，保存即可"
            ).show(mViewBinding.ivQ, 0, 0)
        }
        mViewBinding.tvSave.setOnClickListener {
            lifecycleScope.launch {
                val appKey = mViewBinding.edAppKey.text.toString()
                val appSecretKey = mViewBinding.edAppSecretKey.text.toString()
                if (appKey.isEmpty() || appSecretKey.isEmpty()) {
                    showToast("请完善app key或app secret key")
                } else {
                    UserDataStore.saveToAppKey(appKey, appSecretKey, mContext)
                    UIModuleProvider.doSignConfig()
                    showToast("保存成功")
                    finish()
                }
            }
        }
    }
}