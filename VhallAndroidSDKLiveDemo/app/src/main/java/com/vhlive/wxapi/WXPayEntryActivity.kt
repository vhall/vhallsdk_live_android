package com.vhlive.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.vhall.vhlive.databinding.ActivitySplashBinding
import java.lang.ref.WeakReference


class WXPayEntryActivity : Activity() , IWXAPIEventHandler  {
    private lateinit var api: IWXAPI
    private var handler: MyHandler? = null

    private class MyHandler(wxEntryActivity: WXPayEntryActivity) : Handler() {
        private val wxEntryActivityWeakReference: WeakReference<WXPayEntryActivity>

        init {
            wxEntryActivityWeakReference = WeakReference<WXPayEntryActivity>(wxEntryActivity)
        }

    }
    private val mViewBinding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = WXAPIFactory.createWXAPI(this, "wxc6c0a273cf2f67f7", false)
        handler = MyHandler(this)
        try {
            val intent = intent
            api.handleIntent(intent, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        api.handleIntent(intent, this)
    }

    override fun onReq(baseReq: BaseReq?) {
        // 处理微信请求
        // 一般不需要特殊处理
        var result = 0
        result = 0;
        if (baseReq!!.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
        }
    }

    override fun onResp(resp: BaseResp?) {
        var result = 0
        result = 0;
        if (resp!!.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
        }
        finish()
    }

    private fun handlePaymentSuccess() {
        // 处理支付成功的业务逻辑
        // 可以发送广播或更新UI
    }

    private fun handlePaymentFailure() {
        // 处理支付失败的业务逻辑
    }

    private fun handlePaymentCancel() {
        // 处理支付取消的业务逻辑
    }
}