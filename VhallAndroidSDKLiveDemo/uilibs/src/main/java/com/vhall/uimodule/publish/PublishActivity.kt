package com.vhall.uimodule.publish

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import com.gyf.immersionbar.ImmersionBar
import com.vhall.beautifykit.control.FaceBeautyControlView
import com.vhall.business.ChatServer
import com.vhall.business.MessageServer
import com.vhall.business.VhallSDK
import com.vhall.business.data.WebinarInfo
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseActivity
import com.vhall.uimodule.base.BaseBottomDialog
import com.vhall.uimodule.base.IBase.*
import com.vhall.uimodule.beautysource.FaceBeautyDataFactory
import com.vhall.uimodule.databinding.ActivityPublishBinding
import com.vhall.uimodule.utils.ActivityUtils
import com.vhall.uimodule.utils.CommonUtil
import com.vhall.uimodule.utils.emoji.InputView
import com.vhall.uimodule.utils.emoji.KeyBoardManager
import com.vhall.uimodule.watch.chat.ChatFragment
import com.vhall.uimodule.watch.warmup.WatchWarmUpActivity

class PublishActivity :
    BaseActivity<ActivityPublishBinding>(ActivityPublishBinding::inflate) {

    companion object {
        fun startActivity(context: Context, info: WebinarInfo) {
            val intent = Intent(context, PublishActivity::class.java)
            intent.putExtra(INFO_KEY, info)
            context.startActivity(intent)
        }

        fun startActivityForResult(activity: Activity, info: WebinarInfo, requestCode: Int) {
            val intent = Intent(activity, PublishActivity::class.java)
            intent.putExtra(INFO_KEY, info)
            activity.startActivityForResult(intent, requestCode)
        }
    }



    private lateinit var chatFragment: ChatFragment
    private lateinit var publishFragment: PublishFragment

    private var inputView: InputView? = null

    private val chatCallBack = ChatCallback()
    private val messageCallback = MessageCallback()

    private var mCurDialog: BaseBottomDialog? = null//当前弹出的Dialog

    private var videoHeight = 0

    public var isPublish = false
    private var pv = 0
    private var pvVirtual = 0
    private var online = 0
    private var onlineVirtual = 0

    private var noDelay = false
    private var isDirector = false

    private var mUIHandler: Handler? = null
        get() {
            if (null == field) {
                field = Handler(Looper.getMainLooper())
            }
            return field
        }

    override fun initView() {
        super.initView()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        webinarInfo = intent.getSerializableExtra(INFO_KEY) as WebinarInfo

        requestedOrientation =  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        chatFragment = ChatFragment.newInstance(webinarInfo, "chat")
        publishFragment = PublishFragment.newInstance(webinarInfo)
        publishFragment?.chatCallBack = chatCallBack
        publishFragment?.messageCallBack = messageCallback
        ActivityUtils.addFragmentToActivity(
            supportFragmentManager,
            publishFragment,
            R.id.publishFrame
        )
//        ActivityUtils.addFragmentToActivity(
//            supportFragmentManager,
//            chatFragment,
//            R.id.chatFrame
//        )
        initINPUT()

//        mViewBinding.ivClose.setOnClickListener { doFinish() }

        publishFragment.setIFaceBeautySwitch(IFaceBeautySwitch { this@PublishActivity.changeVisibility() })
        initBeautifyData(1)
    }

    override fun checkNonEditArea(): Boolean = false;

    private fun doFinish() {
        setResult(WatchWarmUpActivity.CODE_REQUEST)
        finish()
    }

    override fun onBackPressed() {
        doFinish()
    }

    private fun initINPUT() {
        inputView = InputView(
            this,
            KeyBoardManager.getKeyboardHeight(this),
            KeyBoardManager.getKeyboardHeightLandspace(this)
        )
        inputView!!.add2Window(this)
        inputView!!.setOnHeightReceivedListener { screenOri, height ->
            if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                KeyBoardManager.setKeyboardHeight(mContext, height)
            } else {
                KeyBoardManager.setKeyboardHeightLandspace(mContext, height)
            }
        }
        inputView!!.setOnSendClickListener { msg, _ ->
            if (msg.isNullOrEmpty()) {
                showToast("不能输入空")
            } else {
//                watchLiveFragment?.sendQA(msg)
            }
        }
    }

    override fun call(method: String?, arg: String?, extras: Any?): Bundle {
        when (method) {
            SHOW_INPUT_VIEW_KEY -> {
                inputView?.show(false,"参与聊天")
            }
            CUSTOMMSG_KEY -> {//发送自定义消息

            }
        }
        return super.call(method, arg, extras)
    }

    override fun dealChatData(chatInfo: ChatServer.ChatInfo) {
        super.dealChatData(chatInfo)
        if (null != chatInfo.msgData) {
            if (!TextUtils.isEmpty(chatInfo.msgData.target_id)) {
                if (!VhallSDK.getUserId().equals(chatInfo.msgData.target_id)) {
                    //demo过滤非面向自己的私聊消息
                    return
                }
            }
        }
        chatFragment.dealChatData(chatInfo)
        when (chatInfo.event) {
            ChatServer.eventOnlineKey -> {
                pv++
                if (chatInfo.onlineData != null) {
                    online = chatInfo.onlineData.concurrent_user
                }
                setLookNum()
            }
            ChatServer.eventOfflineKey -> {
                if (chatInfo.onlineData != null) {
                    online = chatInfo.onlineData.concurrent_user
                }
                setLookNum()
            }
            ChatServer.eventVirtualUpdate -> {
                if (chatInfo.virtualNumUpdateData != null) {
                    pvVirtual += chatInfo.virtualNumUpdateData.update_pv
                    onlineVirtual += chatInfo.virtualNumUpdateData.update_online_num
                }
                setLookNum()
            }
            ChatServer.eventCustomKey -> {

            }
        }
    }

    private fun setLookNum() {
        val pvNum: Int = pv + pvVirtual
        val onlineNum = online + onlineVirtual
//        mViewBinding.tvHotNum.text = (if (pvNum > 999) "999+" else pvNum.toString())
//        mViewBinding.tvLookNum.text = (if (onlineNum > 999) "999+" else onlineNum.toString())
    }

    override fun dealMessageData(messageInfo: MessageServer.MsgInfo) {
        super.dealMessageData(messageInfo)
        chatFragment.dealMessageData(messageInfo)

        when (messageInfo.event) {
            MessageServer.EVENT_KICKOUT -> {
                showToast("您被踢出了房间")
                doFinish()
            }
            MessageServer.EVENT_OVER -> {
                showToast("直播结束")
            }
            MessageServer.EVENT_RESTART -> {
                showToast("直播开始了")
            }
            MessageServer.EVENT_INTERACTIVE_DOWN_MIC -> {
                if(isPublish){

                }
            }
            MessageServer.EVENT_INTERACTIVE_ALLOW_MIC -> {

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (!CommonUtil.isGrantedPermission(this@PublishActivity)) {
                showToast(getString(R.string.app_permission_av_none))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        inputView?.destroyed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == ORIENTATION_LANDSCAPE) {
            ImmersionBar.hideStatusBar(window)
        } else {
            ImmersionBar.showStatusBar(window)
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

        }
        return super.onKeyDown(keyCode, event);
    }

    // 高级美颜相关
    private var mFaceBeautyControlView: FaceBeautyControlView? = null
    private var mFaceBeautyDataFactory: FaceBeautyDataFactory? = null

    private fun changeVisibility() {
        //新的美颜
        if (mFaceBeautyControlView!!.visibility == View.VISIBLE) {
            mFaceBeautyControlView!!.visibility = View.GONE
        } else {
            mFaceBeautyControlView!!.visibility = View.VISIBLE
        }
    }

    private fun initBeautifyData(orientation: Int) {
        mFaceBeautyDataFactory = FaceBeautyDataFactory(this)
        mFaceBeautyControlView = findViewById(R.id.faceBeautyControlView)
        mFaceBeautyControlView!!.setMainTabVisibility(false, true, true, false)
        mFaceBeautyControlView!!.setSelectLineVisible()
        mFaceBeautyControlView!!.changeOrientation(orientation)
        mFaceBeautyControlView!!.bindDataFactory(mFaceBeautyDataFactory!!)
    }
}