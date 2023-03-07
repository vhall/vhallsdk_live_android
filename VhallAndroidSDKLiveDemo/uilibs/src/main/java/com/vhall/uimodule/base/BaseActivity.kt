package com.vhall.uimodule.base

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.vhall.business.ChatServer
import com.vhall.business.MessageServer
import com.vhall.business.data.WebinarInfo
import com.vhall.uimodule.utils.KeyboardsUtils
import com.vhall.uimodule.utils.MiniBaseCallBack

open class BaseActivity<VB : ViewBinding>(val inflateFunc: (LayoutInflater) -> VB) :
    AppCompatActivity(), IBase {
    protected val mViewBinding by lazy { inflateFunc(layoutInflater) }
    protected lateinit var mContext: Context
    protected lateinit var webinarInfo: WebinarInfo
    private var mLoadingView: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)
        mContext = this
        initView()
    }

    open fun initView() {
    }

    open fun dealChatData(chatInfo: ChatServer.ChatInfo) {
    }

    open fun dealMessageData(messageInfo: MessageServer.MsgInfo) {
    }

    inner class ChatCallback : MiniBaseCallBack.SimpleChatCallback() {
        override fun onChatMessageReceived(chatInfo: ChatServer.ChatInfo?) {
            super.onChatMessageReceived(chatInfo)
            if (chatInfo != null) {
                dealChatData(chatInfo)
            }
        }
    }

    inner class MessageCallback : MiniBaseCallBack.SimpleMessageEventCallback() {
        override fun onEvent(messageInfo: MessageServer.MsgInfo?) {
            super.onEvent(messageInfo)
            if (messageInfo != null) {
                dealMessageData(messageInfo)
            }
        }
    }

    /**
     * 是否检查actiivty内非输入区域关键软键盘
     */
    open fun checkNonEditArea(): Boolean = true

    open fun showLoading(title: String?, message: String?) {
        loadingView().apply {
            title?.let { setTitle(it) }
            message?.let { setMessage(it) }
            setCancelable(false)
            show()
        }
    }

    open fun finishLoading() {
        loadingView().dismiss()
    }

    private fun loadingView(): ProgressDialog {
        return mLoadingView ?: let {
            mLoadingView = ProgressDialog(mContext)
            mLoadingView!!
        }
    }

    override fun onDestroy() {
        if (mLoadingView?.isShowing == true) {
            mLoadingView?.dismiss()
        }
        mLoadingView = null
        super.onDestroy()
    }

    @CallSuper
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (checkNonEditArea() && ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (KeyboardsUtils.isShouldHideKeyBord(view, ev)) {
                KeyboardsUtils.hintKeyBoards(view)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}