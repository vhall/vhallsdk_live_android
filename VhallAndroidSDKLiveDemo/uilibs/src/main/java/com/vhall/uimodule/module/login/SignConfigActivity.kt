package com.vhall.uimodule.module.login

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.lifecycleScope
import com.vhall.business.utils.SignatureUtil
import com.vhall.uimodule.base.BaseActivity
import com.vhall.uimodule.base.UIModuleProvider
import com.vhall.uimodule.dao.UserDataStore
import com.vhall.uimodule.databinding.ActivitySignConfigBinding
import com.vhall.uimodule.utils.AESUtils
import com.vhall.uimodule.utils.copy
import com.vhall.uimodule.widget.MainListPop
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

import com.vhall.zxing.client.android.CaptureActivity
import com.vhall.uimodule.R
import com.vhall.uimodule.module.watch.WatchLiveActivity
import com.vhall.uimodule.utils.CommonUtil

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
            showToast("已复制")
        }
        mViewBinding.edAppName.setOnClickListener {
            mViewBinding.edAppName.text.toString().copy(mContext)
            showToast("已复制")
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
        mViewBinding.ivQrcode.setOnClickListener {
            launchQRCodeActivity()
        }
    }

    //以下为扫描二维码代码
    private val launcherActivity = registerForActivityResult( StartActivityForResult()) { result: ActivityResult? ->
        if (result != null && result?.resultCode == RESULT_OK) {
            val qrcodeStr: String = result?.data?.getStringExtra("qrcode").toString()
            if(qrcodeStr.length>64){
                try {
                    val jsonstr = AESUtils.decrypt(qrcodeStr.replace(" ","+"),"EDaaff63bcB4d4M9")
                    if(jsonstr.length>0){
                        val userInfo: JSONObject = JSONObject(jsonstr)
                        val sha1 = userInfo["s1"].toString()
                        val ar = userInfo["ar"].toString()
                        mViewBinding.edAppKey.setText(userInfo["a"].toString())
                        mViewBinding.edAppSecretKey.setText(userInfo["as"].toString())
                        if(ar.length==0 || sha1.length==0)
                            showToast("请填写Android包名签名")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            else {
                showToast("扫描二维码错误")
            }
        }
    }

    fun launchQRCodeActivity(){
        if (CommonUtil.isGrantedAndRequestPermission(this, 103)){
            launcherActivity.launch(Intent(this, CaptureActivity::class.java))
        }else{
            showToast("没有相机权限影响扫描二维码")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 103) {
            if (!CommonUtil.isGrantedPermission(this@SignConfigActivity)) {
                showToast("没有相机权限影响扫描二维码")
            }
            launcherActivity.launch(Intent(this, CaptureActivity::class.java))
        }
    }

}