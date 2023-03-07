package com.vhall.uimodule.module.watch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.gyf.immersionbar.ImmersionBar
import com.vhall.business.ChatServer
import com.vhall.business.MessageServer
import com.vhall.business.VhallSDK
import com.vhall.business.data.WebinarInfo
import com.vhall.logmanager.VLog
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseActivity
import com.vhall.uimodule.base.IBase.*
import com.vhall.uimodule.databinding.ActivityWatchLiveBinding
import com.vhall.uimodule.module.chat.ChatFragment
import com.vhall.uimodule.module.doc.DocFragment
import com.vhall.uimodule.module.interactive.RtcFragment
import com.vhall.uimodule.module.introduction.WebinarInfoFragment
import com.vhall.uimodule.module.sign.SignDialog
import com.vhall.uimodule.module.warmup.WatchWarmUpActivity
import com.vhall.uimodule.module.watch.watchlive.WatchLiveFragment
import com.vhall.uimodule.module.watch.watchplayback.WatchPlaybackFragment
import com.vhall.uimodule.utils.ActivityUtils
import com.vhall.uimodule.utils.CommonUtil
import com.vhall.uimodule.utils.DensityUtils
import com.vhall.uimodule.utils.emoji.InputView
import com.vhall.uimodule.utils.emoji.KeyBoardManager
import com.vhall.uimodule.widget.ExtendTextView

class WatchLiveActivity :
    BaseActivity<ActivityWatchLiveBinding>(ActivityWatchLiveBinding::inflate) {

    companion object {
        fun startActivity(context: Context, info: WebinarInfo) {
            val intent = Intent(context, WatchLiveActivity::class.java)
            intent.putExtra(INFO_KEY, info)
            context.startActivity(intent)
        }

        fun startActivityForResult(activity: Activity, info: WebinarInfo, requestCode: Int) {
            val intent = Intent(activity, WatchLiveActivity::class.java)
            intent.putExtra(INFO_KEY, info)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    private lateinit var docTab: TabLayout.Tab
    private lateinit var chatTab: TabLayout.Tab
    private lateinit var infoTab: TabLayout.Tab
    private lateinit var qaTab: TabLayout.Tab
    private var selectTab: TabLayout.Tab? = null

    private lateinit var chatFragment: ChatFragment
    private lateinit var qaFragment: ChatFragment
    private lateinit var docFragment: DocFragment
    private lateinit var webinarInfoFragment: WebinarInfoFragment
    private val docTag = 1
    private val chatTag = 2
    private val qaTag = 3
    private val infoTag = 4
    private var isChat = true

    private var inputView: InputView? = null
    private var showFragment: Fragment? = null
    private var rtcFragment: RtcFragment? = null
    private var watchLiveFragment: WatchLiveFragment? = null
    private var watchPlaybackFragment: WatchPlaybackFragment? = null
    private val chatCallBack = ChatCallback()
    private val messageCallback = MessageCallback()

    private var mSignDialog: SignDialog? = null

    private var videoHeight = 0

    private var isPublish = false
    private var pv = 0
    private var pvVirtual = 0
    private var online = 0
    private var onlineVirtual = 0
    private var showDoc = false
    private var showNotice = false
    private var docFull = false
    private var liveVideoFull = false
    private var selectQAPosition = 2
    private var mUIHandler: Handler? = null
        get() {
            if (null == field) {
                field = Handler(Looper.getMainLooper())
            }
            return field
        }
    private var mNoticeCountDownRunnable: NoticeCountDownRunnable? = null

    override fun initView() {
        super.initView()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webinarInfo = intent.getSerializableExtra(INFO_KEY) as WebinarInfo

        chatFragment = ChatFragment.newInstance(webinarInfo, "chat")
        qaFragment = ChatFragment.newInstance(webinarInfo, "qa")
        docFragment = DocFragment.newInstance(webinarInfo)
        webinarInfoFragment = WebinarInfoFragment.newInstance(webinarInfo)

        mViewBinding.tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectTab = tab
                when (tab?.tag) {
                    docTag -> {
                        ActivityUtils.changeFragmentToActivity(
                            supportFragmentManager,
                            docFragment, showFragment,
                            mViewBinding.flTab.id
                        )
                        showFragment = docFragment
                    }
                    chatTag -> {
                        ActivityUtils.changeFragmentToActivity(
                            supportFragmentManager,
                            chatFragment, showFragment,
                            mViewBinding.flTab.id
                        )
                        showFragment = chatFragment
                        isChat = true
                    }
                    qaTag -> {
                        ActivityUtils.changeFragmentToActivity(
                            supportFragmentManager,
                            qaFragment, showFragment,
                            mViewBinding.flTab.id
                        )
                        showFragment = qaFragment
                        isChat = false
                    }
                    infoTag -> {
                        ActivityUtils.changeFragmentToActivity(
                            supportFragmentManager,
                            webinarInfoFragment, showFragment,
                            mViewBinding.flTab.id
                        )
                        showFragment = webinarInfoFragment
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        initTable()
        initINPUT()
        initWatchData()
        videoHeight = DensityUtils.getScreenWidth() * 9 / 16
        mViewBinding.flVideo.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        mViewBinding.flVideo.layoutParams.height = videoHeight
        mViewBinding.ivClose.setOnClickListener { doFinish() }
        mViewBinding.tvNotice.setDrawableClickListener { position ->
            when (position) {
                ExtendTextView.DRAWABLE_RIGHT -> {
                    showNotice = false
                    mViewBinding.tvNotice.visibility = View.GONE
                }

            }
        }
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
                if (isChat) {
                    watchPlaybackFragment?.sendChat(msg)
                    watchLiveFragment?.sendChat(msg)
                } else {
                    watchLiveFragment?.sendQA(msg)
                }
            }
        }
    }


    private fun initTable() {
        with(mViewBinding) {
            chatTab = tab.newTab().setText("聊天")
            chatTab.tag = chatTag

            qaTab = tab.newTab().setText(webinarInfo.question_name)
            qaTab.tag = qaTag

            infoTab = tab.newTab().setText("简介")
            infoTab.tag = infoTag

            docTab = tab.newTab().setText("文档")
            docTab.tag = docTag
            tab.addTab(docTab)
            tab.addTab(chatTab)
            if (webinarInfo.question_status == 1 && webinarInfo.status == 1) {
                updateTextView()
                tab.addTab(qaTab)
            }
            tab.addTab(infoTab)
            tab.removeTab(docTab)
            tab.selectTab(chatTab)
        }
    }

    private fun updateTextView() {
        try {
            // 单行显示
            val view = qaTab.view
            val clas = view.javaClass
            val field = clas.getDeclaredField("defaultMaxLines")
            field.isAccessible = true
            field.setInt(view, 1)
        } catch (e: Exception) {
        }
    }
    private fun initWatchData() {
        pv = webinarInfo.pv
        pvVirtual = webinarInfo.pvVirtual
        online = webinarInfo.online
        onlineVirtual = webinarInfo.onlineVirtual
        setLookNum()
        mViewBinding.tvTitle.text =
            CommonUtil.getLimitString(webinarInfo.subject, 8)
        mViewBinding.tvHostName.text =
            CommonUtil.getLimitString(webinarInfo.hostName, 8)
        val requestOptions: RequestOptions =
            RequestOptions.bitmapTransform(CircleCrop()).placeholder(R.mipmap.icon_avatar)
        Glide.with(mContext).load(webinarInfo.hostAvatar).apply(requestOptions)
            .into(mViewBinding.ivHostAvatar)
        when (webinarInfo.status) {
            1 -> {
                watchLiveFragment = WatchLiveFragment.newInstance(webinarInfo)
                watchLiveFragment?.chatCallBack = chatCallBack
                watchLiveFragment?.messageCallBack = messageCallback
                ActivityUtils.addFragmentToActivity(
                    supportFragmentManager,
                    watchLiveFragment,
                    mViewBinding.flVideo.id
                )
            }
            4, 5 -> {
                mViewBinding.tvLookNum.visibility = View.GONE
                watchPlaybackFragment =
                    WatchPlaybackFragment.newInstance(webinarInfo)
                watchPlaybackFragment?.chatCallBack = chatCallBack
                watchPlaybackFragment?.messageCallBack = messageCallback
                ActivityUtils.addFragmentToActivity(
                    supportFragmentManager,
                    watchPlaybackFragment,
                    mViewBinding.flVideo.id
                )
            }
            else -> {
                showToast("直播结束")
                doFinish()
            }
        }
    }


    override fun call(method: String?, arg: String?, extras: Bundle?): Bundle {
        when (method) {
            HALF_WATCH_SCREEN_KEY -> {
                mViewBinding.flVideo.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mViewBinding.flVideo.layoutParams.height = videoHeight
                if (webinarInfo.webinar_show_type == 1) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                liveVideoFull = false
            }
            FULL_WATCH_SCREEN_KEY -> {
                mViewBinding.flVideo.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mViewBinding.flVideo.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                if (webinarInfo.webinar_show_type == 1) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                liveVideoFull = true
            }
            HALF_DOC_SCREEN_KEY -> {
                mViewBinding.flVideo.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mViewBinding.flVideo.layoutParams.height = videoHeight

                mViewBinding.flTab.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mViewBinding.flTab.layoutParams.height = 0

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                mViewBinding.rlUser.visibility = View.VISIBLE
                mViewBinding.tvNotice.visibility = if (showNotice) View.VISIBLE else View.GONE
                docFull = false
            }
            FULL_DOC_SCREEN_KEY -> {
                mViewBinding.flVideo.layoutParams.width = 0
                mViewBinding.flVideo.layoutParams.height = 0

                mViewBinding.flTab.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mViewBinding.flTab.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                mViewBinding.rlUser.visibility = View.GONE
                mViewBinding.tvNotice.visibility = View.GONE
                docFull = true
            }
            SHOW_INPUT_VIEW_KEY -> {
                inputView?.show(false, if (isChat) "参与聊天" else "快来提问吧")
            }
            HAND_UP_KEY -> {
                if (isPublish) {
                    if (null == extras || !extras.containsKey(HAND_UP_KEY_STATUS)) {
                        rtcFragment?.showHandUpOperate()
                    }
                } else {
                    if (extras?.containsKey(HAND_UP_KEY_STATUS) == true) {
                        if (!extras.getBoolean(HAND_UP_KEY_STATUS)) {
                            watchLiveFragment?.hintApplyHandUpDialog()
                        }
                    } else {
                        if (CommonUtil.isGrantedAndRequestPermission(this@WatchLiveActivity, 101))
                            watchLiveFragment?.showApplyHandUpDialog()
                    }
                }
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
        qaFragment.dealChatData(chatInfo)
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
        }
    }

    private fun setLookNum() {
        val pvNum: Int = pv + pvVirtual
        val onlineNum = online + onlineVirtual
        mViewBinding.tvHotNum.text = (if (pvNum > 999) "999+" else pvNum.toString())
        mViewBinding.tvLookNum.text = (if (onlineNum > 999) "999+" else onlineNum.toString())
    }

    fun enterInteractive() {
        watchLiveFragment?.hintApplyHandUpDialog()
        mViewBinding.rlUser.visibility = View.GONE
        isPublish = true
        watchLiveFragment?.stop()
        watchLiveFragment?.setOPSDelayTime(0)
        rtcFragment = RtcFragment.newInstance(webinarInfo)
        ActivityUtils.changeFragmentToActivity(
            supportFragmentManager,
            rtcFragment,
            watchLiveFragment,
            mViewBinding.flVideo.id
        )

        chatFragment.setIsPublish(isPublish)
        try {
            qaFragment.setIsPublish(isPublish)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun continueWithLive() {
        mViewBinding.rlUser.visibility = View.VISIBLE
        watchLiveFragment?.start()
        watchLiveFragment?.setOPSDelayTime(-1)
    }

    fun leaveInteractive() {
        isPublish = false
        ActivityUtils.remove(
            supportFragmentManager,
            rtcFragment
        )
        ActivityUtils.show(
            supportFragmentManager,
            watchLiveFragment
        )
        if (isChat) {
            chatFragment.setIsPublish(isPublish)
        } else {
            qaFragment.setIsPublish(isPublish)
        }
        rtcFragment = null
    }

    override fun dealMessageData(messageInfo: MessageServer.MsgInfo) {
        super.dealMessageData(messageInfo)
        chatFragment.dealMessageData(messageInfo)
        qaFragment.dealMessageData(messageInfo)
        rtcFragment?.dealMessageData(messageInfo)
        docFragment.dealMessageData(messageInfo)
        watchLiveFragment?.dealMessageData(messageInfo)

        when (messageInfo.event) {
            MessageServer.EVENT_KICKOUT -> {
                showToast("您被踢出了房间")
                doFinish()
            }
            MessageServer.EVENT_OVER -> {
                showToast("直播结束")
                if (watchLiveFragment != null)
                    doFinish()
            }
            MessageServer.EVENT_RESTART -> {
                showToast("直播开始了")
            }
            MessageServer.EVENT_INTERACTIVE_DOWN_MIC -> {
                leaveInteractive()
                continueWithLive()
            }
            MessageServer.EVENT_INTERACTIVE_ALLOW_MIC -> {
                enterInteractive()
            }
            MessageServer.EVENT_NOTICE -> {
                mViewBinding.tvNotice.visibility = if (docFull) View.GONE else View.VISIBLE
                var contentStr: String?
                val limit = 40
                if (messageInfo.content.length < limit) {
                    var placeHolder = ""
                    for (time in 1..(limit - messageInfo.content.length)) {
                        placeHolder = placeHolder.plus("0")
                    }

                    contentStr = messageInfo.content.plus(placeHolder)
                } else {
                    contentStr = messageInfo.content
                }
                val spannableString = SpannableString(contentStr)
                val foregroundColorSpan = ForegroundColorSpan(Color.TRANSPARENT)
                spannableString.setSpan(foregroundColorSpan, messageInfo.content.length, contentStr!!.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                mViewBinding.tvNotice.text = spannableString
                showNotice = true
                if (messageInfo.announceRemainingMS > 0) {
                    //倒计时结束后关闭公告横幅
                    mUIHandler?.apply {
                        mNoticeCountDownRunnable?.let {
                            removeCallbacks(it)
                        } ?: let {
                            mNoticeCountDownRunnable = NoticeCountDownRunnable()
                        }
                        postDelayed(
                            mNoticeCountDownRunnable!!,
                            messageInfo.announceRemainingMS
                        )
                    }
                } else if (messageInfo.announceRemainingMS == 0L) {
                    //长显
                } else {
                    //已超时
                    mViewBinding.tvNotice.visibility = View.GONE
                }
            }
            MessageServer.EVENT_SIGNIN -> {
                mSignDialog = SignDialog(mContext, webinarInfo, messageInfo).apply { show() }
            }
            MessageServer.EVENT_SIGN_END -> {
                mSignDialog?.dismiss()
            }
            MessageServer.EVENT_SHOWH5DOC -> {
                showDoc = if (messageInfo.watchType == 1) {
                    if (!showDoc) {
                        mViewBinding.tab.selectedTabPosition
                        docTab = mViewBinding.tab.newTab().setText("文档")
                        docTab.tag = docTag
                        mViewBinding.tab.addTab(docTab, 1)
                        selectTab?.select()
                    }
                    true
                } else {
                    if (showDoc) {
                        if (docFull) {
                            docFragment.doc2Portrait()
                        }
                        mViewBinding.tab.removeTab(docTab)
                        if (mViewBinding.tab.selectedTabPosition == 1)
                            chatTab.select()
                        else
                            selectTab?.select()
                    }
                    false
                }
            }
            MessageServer.EVENT_QUESTION -> {
                var questionName = "问答"
                if (!TextUtils.isEmpty(messageInfo.question_name))
                    questionName = messageInfo.question_name

                showToast(questionName + "功能已" + if (messageInfo.status == 0) "关闭" else "开启")
                if (messageInfo.status == 1) {
                    qaTab = mViewBinding.tab.newTab().setText(questionName)
                    qaTab.tag = qaTag
                    selectQAPosition = if (showDoc) 2 else 1
                    updateTextView()
                    mViewBinding.tab.addTab(qaTab, selectQAPosition)
                    qaFragment.setIsPublish(isPublish)
                    selectTab?.select()
                } else {
                    mViewBinding.tab.removeTab(qaTab)
                    if (mViewBinding.tab.selectedTabPosition == selectQAPosition)
                        chatTab.select()
                    else
                        selectTab?.select()

                }
            }
        }
    }

    fun getHistory(page: Int, msgId: String?, callback: ChatServer.ChatRecordCallback) {
        watchLiveFragment?.getHistory(page, msgId, callback)
        watchPlaybackFragment?.getHistory(page, msgId, callback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (!CommonUtil.isGrantedPermission(this@WatchLiveActivity)) {
                showToast(getString(R.string.app_permission_av_none))
            } else
                watchLiveFragment?.showApplyHandUpDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        inputView?.destroyed()
        mNoticeCountDownRunnable?.let {
            mUIHandler?.removeCallbacks(it)
            null
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == ORIENTATION_LANDSCAPE) {
            ImmersionBar.hideStatusBar(window)
        } else {
            ImmersionBar.showStatusBar(window)
        }
    }

    fun dealWithInviteDialog() {
        if (docFull) {
            docFragment?.doc2Portrait()
        }
        if (liveVideoFull) {
            watchLiveFragment?.video2Portrait()
        }
    }

    private inner class NoticeCountDownRunnable : Runnable {
        override fun run() {
            mViewBinding.tvNotice.visibility = View.GONE
        }
    }
}