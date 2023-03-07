package com.vhall.uimodule.module.watch.watchlive

import android.os.Bundle
import android.util.Log
import com.vhall.business.*
import com.vhall.business.data.RequestCallback
import com.vhall.business.data.WebinarInfo
import com.vhall.player.Constants
import com.vhall.player.VHPlayerListener
import com.vhall.player.stream.play.IVHVideoPlayer
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseFragment
import com.vhall.uimodule.base.IBase.*
import com.vhall.uimodule.databinding.FragmentWatchLiveBinding
import com.vhall.uimodule.module.watch.ApplyHandUpDialog
import com.vhall.uimodule.module.watch.WatchLiveActivity
import com.vhall.uimodule.utils.CommonUtil
import com.vhall.uimodule.widget.ItemClickLister
import com.vhall.uimodule.widget.OutDialog
import com.vhall.uimodule.widget.OutDialogBuilder
import com.vhall.uimodule.widget.ScrollChooseTypeDialog
import org.json.JSONArray
import org.json.JSONException
import java.util.*


class WatchLiveFragment :
    BaseFragment<FragmentWatchLiveBinding>(FragmentWatchLiveBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo) =
            WatchLiveFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(INFO_KEY, info)
                }
            }
    }

    private lateinit var watchLive: WatchLive
    lateinit var chatCallBack: ChatServer.Callback
    lateinit var messageCallBack: MessageServer.Callback
    private var definitionList: MutableList<String> = arrayListOf()

    //是否全屏
    private var isFull = false
    var parentActivity: WatchLiveActivity? = null

    override fun initView() {
        parentActivity = activity as WatchLiveActivity

        arguments?.let {
            webinarInfo = it.getSerializable(INFO_KEY) as WebinarInfo
            initWatchLive()
        }
        mViewBinding.ivFull.setOnClickListener {
            if (isFull) {
               video2Portrait()
            } else {
                video2Landscape()
            }
        }
        mViewBinding.tvDefinition.setOnClickListener {
            showChooseDefinition()
        }
        mViewBinding.clickPlay.setOnClickListener {
            if (watchLive.isPlaying) {
                stop()
            } else {
                start()
            }
        }
    }

    fun video2Portrait() {
        parentActivity?.call(HALF_WATCH_SCREEN_KEY, "", null)
        mViewBinding.ivFull.setBackgroundResource(R.drawable.svg_ic_full)
        isFull = !isFull
    }

    private fun video2Landscape() {
        parentActivity?.call(FULL_WATCH_SCREEN_KEY, "", null)
        mViewBinding.ivFull.setBackgroundResource(R.drawable.svg_ic_full_exit)
        isFull = !isFull
    }

    fun stop() {
        watchLive.stop()
    }

    fun start() {
        watchLive.start()
    }

    fun setOPSDelayTime(delayMs: Int) {
        watchLive.setOPSDelay(delayMs)
    }

    private fun initWatchLive() {
        watchLive =
            WatchLive.Builder()
                .containerLayout(mViewBinding.rlVideo)
                .callback(WatchCallback())
                .chatCallback(chatCallBack)
                .messageCallback(messageCallBack)
                .context(mContext)
                .build()
        watchLive.setWebinarInfo(webinarInfo)
        watchLive.scaleType = IVHVideoPlayer.DRAW_MODE_ASPECTFIT
    }

    inner class WatchCallback : VHPlayerListener {
        override fun onStateChanged(state: Constants.State?) {
            when (state) {
                Constants.State.START -> {
                    mViewBinding.ivPlay.setBackgroundResource(R.mipmap.icon_play_pause)
                }
                Constants.State.BUFFER -> {
                    mViewBinding.ivPlay.setBackgroundResource(R.mipmap.icon_playing)
                }
                Constants.State.STOP -> {
                    mViewBinding.ivPlay.setBackgroundResource(R.mipmap.icon_playing)
                }
                else -> {}
            }
        }

        override fun onEvent(event: Int, msg: String?) {
            when (event) {
                Constants.Event.EVENT_DPI_CHANGED ->
                    mViewBinding.tvDefinition.text = CommonUtil.changeDefinition(msg)
                Constants.Event.EVENT_DPI_LIST ->
                    try {
                        definitionList.clear()
                        val array = JSONArray(msg)
                        for (i in 0 until array.length()) {
                            definitionList.add(array.getString(i))
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                Constants.Event.EVENT_VIDEO_SIZE_CHANGED -> Log.i(
                    "",
                    msg!!
                )
                ErrorCode.ERROR_LOGIN_MORE -> {
                    showToast(msg)
                    activity?.finish()
                }
                Watch.EVENT_INIT_PLAYER_SUCCESS ->
                    start()
            }
        }

        override fun onError(errorCode: Int, innerErrorCode: Int, msg: String?) {
            showToast(msg)
        }
    }

    /**
     * 获取当前房间聊天列表
     *
     * @param page        获取条目节点，默认为1
     * @param limit       获取条目数量，最大100
     * @param msg_id      获取条目数量，聊天记录 锚点消息id,此参数存在时anchor_path 参数必须存在
     * @param anchor_path 锚点方向，up 向上查找，down 向下查找,此参数存在时 msg_id 参数必须存在
     * @param is_role     0：不筛选主办方 1：筛选主办方 默认是0
     */
    fun getHistory(page: Int, msgId: String?, callback: ChatServer.ChatRecordCallback) {
        watchLive.acquireChatRecord(
            page,
            100,
            msgId,
            "down",
            "0",
            callback
        )
    }

    fun sendChat(msg: String) {
        watchLive.sendChat(msg, object : RequestCallback {
            override fun onError(p0: Int, p1: String?) {
                showToast(p1)
            }

            override fun onSuccess() {
            }

        })
    }

    fun sendQA(msg: String) {
        watchLive.sendQuestion(msg, object : RequestCallback {
            override fun onError(p0: Int, p1: String?) {
                showToast(p1)
            }

            override fun onSuccess() {
            }

        })
    }

    private var scrollChooseTypeDialog: ScrollChooseTypeDialog? = null
    private fun showChooseDefinition() {
        if (scrollChooseTypeDialog == null) {
            scrollChooseTypeDialog = ScrollChooseTypeDialog(mContext, definitionList)
            scrollChooseTypeDialog!!.setOnItemClickLister { _: Int, msg: String ->
                mViewBinding.tvDefinition.text = CommonUtil.changeDefinition(msg)
                watchLive.definition = msg
                scrollChooseTypeDialog!!.dismiss()
            }
        }
        scrollChooseTypeDialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        watchLive.destroy()
    }

    private var showInvited: OutDialog? = null
    private fun showInvited() {
        if (showInvited == null) {
            showInvited = OutDialogBuilder()
                .title("邀请您上麦，是否同意？")
                .tv1("拒绝")
                .tv2("同意")
                .clickLister1 {
                    // 1接受，2拒绝
                    replyInvitation(2)
                }
                .clickLister2 {
                    if (CommonUtil.isGrantedAndRequestPermission(activity, 102)){
                        replyInvitation(1)
                    }else{
                        showToast(getString(R.string.app_permission_av_none))
                    }
                }
                .build(activity)
        }
        showInvited?.show()
    }

    private fun replyInvitation(type: Int) {
        watchLive.replyInvitation(webinarInfo.webinar_id, type, object : RequestCallback {
            override fun onSuccess() {
                if (type == 1) {
                    showToast("上麦成功")
                    parentActivity?.enterInteractive()
                } else
                    showToast("拒绝上麦成功")
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                showToast(errorMsg)
            }
        })
    }

    fun dealMessageData(messageInfo: MessageServer.MsgInfo) {
        when (messageInfo.event) {
            MessageServer.EVENT_INVITED_MIC -> {
                parentActivity?.dealWithInviteDialog()
                showInvited()
            }
        }
    }


    var applyHandUpDialog: ApplyHandUpDialog? = null
    fun showApplyHandUpDialog() {
        if (applyHandUpDialog == null) {
            applyHandUpDialog =
                ApplyHandUpDialog(activity)
            applyHandUpDialog?.setOnItemClickLister(object : ItemClickLister {
                override fun onItemClick(type: Int) {
                    watchLive.onRaiseHand(webinarInfo.webinar_id, type, object : RequestCallback {
                        override fun onError(errorCode: Int, errorMsg: String?) {
                            showToast(errorMsg)
                        }

                        override fun onSuccess() {
                            if (type == 1) {
                                showToast("申请成功")
                                applyHandUpDialog?.handUp()
                            } else {
                                applyHandUpDialog?.cancelHandUp()
                            }
                        }
                    })
                }

            });
        }
        applyHandUpDialog?.show()
    }

    fun hintApplyHandUpDialog() {
        applyHandUpDialog?.cancelHandUp()
        applyHandUpDialog?.takeIf {
            it.isShowing
        }?.apply {
            dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!CommonUtil.isGrantedPermission(activity)) {
            showToast(getString(R.string.app_permission_av_none))
        } else
            replyInvitation(1)
    }
}