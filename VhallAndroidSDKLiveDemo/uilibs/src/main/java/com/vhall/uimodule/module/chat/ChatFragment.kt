package com.vhall.uimodule.module.chat

import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.vhall.business.*
import com.vhall.business.ChatServer.ChatInfo
import com.vhall.business.ChatServer.ChatRecordCallback
import com.vhall.business.MessageServer.*
import com.vhall.business.data.RequestCallback
import com.vhall.business.data.RequestDataCallbackV2
import com.vhall.business.data.WebinarInfo
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseFragment
import com.vhall.uimodule.base.IBase.*
import com.vhall.uimodule.databinding.FragmentChatBinding
import com.vhall.uimodule.module.gift.GiftListDialog
import com.vhall.uimodule.module.watch.WatchLiveActivity
import com.vhall.uimodule.widget.WatchMorePop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.math.max


class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo, type: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(INFO_KEY, info)
                    putString(TYPE, type)
                }
            }
    }

    private val TYPE = "type"
    private var page = 1
    private var msgId: String? = ""
    private var type: String? = ""
    private var canChat = true
    private var canQa = true
    private var allForbid = false
    private var ownForbid = false
    private var likePermission = 1
    private var likeNum = 0
    var clickLikeNumber = 0
    lateinit var chatAdapter: ChatAdapter
    lateinit var qaAdapter: QAAdapter
    private var watchMorePop: WatchMorePop? = null
    var handsUp = "1"
    override fun initView() {
        val activity: WatchLiveActivity = activity as WatchLiveActivity

        arguments?.let {
            webinarInfo = it.getSerializable(INFO_KEY) as WebinarInfo
            type = it.getString(TYPE)
            chatAdapter = ChatAdapter(mContext, webinarInfo,activity)
            qaAdapter = QAAdapter(mContext, webinarInfo)
            initConfig()
            allForbid = webinarInfo.chatAllForbid
            ownForbid = webinarInfo.chatOwnForbid
            canChat = !allForbid && !ownForbid
            canQa = if (allForbid) {
                webinarInfo.qa_status.equals("0")
            } else {
                true
            }
            likeNum = webinarInfo.like_num
            handsUp = webinarInfo.hands_up
            setPublishIcon()
            if (webinarInfo.status == 4 || webinarInfo.status == 4)
                mViewBinding.ivMore.visibility=View.GONE
            setLikeNum(webinarInfo.like_num)
            setHint()

        }
        val requestOptions: RequestOptions =
            RequestOptions.bitmapTransform(CircleCrop()).placeholder(R.mipmap.icon_avatar)
        Glide.with(mContext).load(VhallSDK.getUserAvatar()).apply(requestOptions)
            .into(mViewBinding.ivAvatar)
        mViewBinding.recycleView.layoutManager = LinearLayoutManager(mContext)
        if (type.equals("chat"))
            mViewBinding.recycleView.adapter = chatAdapter
        else {
            mViewBinding.ivMore.visibility=View.GONE
            mViewBinding.recycleView.adapter = qaAdapter
        }
        mViewBinding.recycleView.setHasFixedSize(true)
        mViewBinding.ivGift.setOnClickListener {
            val giftListDialog = GiftListDialog(mContext, webinarInfo.vss_room_id)
            giftListDialog.show()
        }
        mViewBinding.ivMore.setOnClickListener {
            val location = IntArray(2)
            mViewBinding.ivMore.getLocationOnScreen(location)
            if (watchMorePop == null)
                watchMorePop = WatchMorePop(
                    mContext,
                    webinarInfo
                )
            watchMorePop?.showAtLocation(
                mViewBinding.ivMore,
                Gravity.NO_GRAVITY,
                location[0],
                location[1] - 250
            )
        }
        mViewBinding.refreshLayout.setOnRefreshListener {
            if (type.equals("chat"))
                getChatHistory(activity)
            else {
                mViewBinding.refreshLayout.isRefreshing = false
                showToast("没有更多数据了")
            }
        }
        mViewBinding.tvChat.setOnClickListener {
            if (canChat && type.equals("chat"))
                activity.call(SHOW_INPUT_VIEW_KEY, "", null)
            else if (canQa && type.equals("qa")) {
                activity.call(SHOW_INPUT_VIEW_KEY, "", null)
            }
        }
        mViewBinding.ivPublish.setOnClickListener {
            activity.call(HAND_UP_KEY, "", null)
        }
        val random = Random()
        mViewBinding.ivLike.setOnClickListener {
            clickLikeNumber++
            likeNum++
            likeNumTimer.cancel()
            likeNumTimer.start()
            mViewBinding.pressLike.show(max(2, random.nextInt(5)))
            mViewBinding.tvLikeNum.text = likeNum.toString()
        }
        lifecycleScope.launch {
            delay(300)
            if (type.equals("chat"))
                getChatHistory(activity)
            else
                getQaHistory()
        }
        mViewBinding.ivAvatar.setOnClickListener {
            activity.call(CUSTOMMSG_KEY, "", null)
        }
    }

    private var likeNumTimer: CountDownTimer = object : CountDownTimer(2000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            VhallSDK.userLike(
                webinarInfo.vss_room_id,
                clickLikeNumber.toString(),
                object : RequestCallback {
                    override fun onSuccess() {}
                    override fun onError(errorCode: Int, errorMsg: String) {}
                })
            clickLikeNumber = 0
        }
    }

    private fun setHint() {
        if (type.equals("chat"))
            if (!canChat)
                mViewBinding.tvChat.hint = "禁止发言"
            else
                mViewBinding.tvChat.hint = "参与聊天"
        else if (type.equals("qa")) {
            if (!canQa)
                mViewBinding.tvChat.hint = "禁止发言"
            else
                mViewBinding.tvChat.hint = "快来提问吧"
        }
    }

    private fun initConfig() {

    }
    public fun setPermissions(permissions:JSONObject){
        likePermission = permissions.optString("ui.watch_hide_like").toInt()
        if (likePermission == 0) {
            mViewBinding.ivLike.visibility = View.GONE
            mViewBinding.tvLikeNum.visibility = View.GONE
        }
        if (permissions.optString("ui.hide_gifts").toInt() == 0) {
            mViewBinding.ivGift.visibility = View.GONE
        }
        //回放禁言
        if (webinarInfo.status == 4 || webinarInfo.status == 4)
            canChat =
                permissions.optString("ui.watch_record_no_chatting").toInt() == 0
        setHint()
    }

    private fun getChatHistory(activity: WatchLiveActivity) {
        activity.getHistory(page, msgId, object : ChatServer.ChatRecordCallback {
            override fun onDataLoaded(list: MutableList<ChatInfo>?) {
                mViewBinding.refreshLayout.isRefreshing = false
                if (!list.isNullOrEmpty()) {
                    msgId = list[list.size - 1].msg_id
                    list.reverse()
                    val chatMessageDataList: MutableList<ChatMessageData> = arrayListOf()
                    for (chatInfo in list) {
                        if (null != chatInfo.msgData && !TextUtils.isEmpty(chatInfo.msgData.target_id) && !VhallSDK.getUserId()
                                .equals(chatInfo.msgData.target_id)
                        ) {
                            //demo过滤非面向自己的私聊消息
                            continue
                        }
                        val e = ChatMessageData()
                        e.chatInfo = chatInfo
                        chatMessageDataList.add(e)
                    }
                    chatAdapter.addData(0, chatMessageDataList)
                    if (1 == page) {
                        mViewBinding.recycleView.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                    page++
                }
            }

            override fun onFailed(p0: Int, p1: String?) {
                mViewBinding.refreshLayout.isRefreshing = false
                showToast("聊天历史:"+p1)
            }

        })
    }

    private fun getQaHistory() {
        VhallSDK.getAnswerList(webinarInfo.webinar_id, object : ChatRecordCallback {
            override fun onDataLoaded(list: MutableList<ChatInfo>) {
                if (!list.isNullOrEmpty()) {
                    val chatMessageDataList: MutableList<ChatMessageData> = arrayListOf()
                    for (chatInfo in list) {
                        val e = ChatMessageData()
                        e.chatInfo = chatInfo
                        chatMessageDataList.add(e)
                    }
                    qaAdapter.addData(0, chatMessageDataList)
                    if (qaAdapter.itemCount > 0)
                        mViewBinding.recycleView.smoothScrollToPosition(qaAdapter.itemCount - 1)
                }
            }

            override fun onFailed(errorcode: Int, messaage: String) {
//                showToast(messaage)
            }
        })
    }

    fun dealChatData(chatInfo: ChatInfo) {
        when (chatInfo.event) {
            ChatServer.eventMsgKey -> {
                if (type.equals("chat")) {
                    val e = ChatMessageData()
                    e.chatInfo = chatInfo
                    chatAdapter.addData(e)
                    if (chatAdapter.itemCount > 0)
                        mViewBinding.recycleView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                }
            }

            ChatServer.eventQuestion -> {
                if (type.equals("qa")) {
                    val questionData = chatInfo.questionData
                    // 自己的提问显示
                    //不是自己的公开回答显示
                    if (TextUtils.equals(questionData.join_id, webinarInfo.join_id)
                        || ((questionData.answer != null && !questionData.answer.content.isNullOrEmpty() && questionData.answer.is_open == 1))
                    ) {
                        val e = ChatMessageData()
                        e.chatInfo = chatInfo
                        qaAdapter.addData(e)
                        if (qaAdapter.itemCount > 0)
                            mViewBinding.recycleView.smoothScrollToPosition(qaAdapter.itemCount - 1)
                    }
                }
            }

            ChatServer.eventCustomKey -> {
                showToast("收到自定义消息："+chatInfo.msgData.text)
            }
        }
    }
    @JvmName("setLikeNum1")
    private fun setLikeNum(num: Int) {
        mViewBinding.tvLikeNum.text = if (num > 999) "999+" else num.toString()
        if (likePermission == 1 && num > 0) {
            mViewBinding.tvLikeNum.visibility = View.VISIBLE
        } else {
            mViewBinding.tvLikeNum.visibility = View.GONE
        }
    }

    var isPublish = false

    fun setIsPublish(isPublish: Boolean) {
        this.isPublish = isPublish
        setPublishIcon()
    }

    private fun setPublishIcon() {
        if (handsUp == "0") {
            if (isPublish)
                mViewBinding.ivPublish.visibility = View.VISIBLE
            else
                mViewBinding.ivPublish.visibility = View.GONE
        } else {
            mViewBinding.ivPublish.visibility = View.VISIBLE
        }
    }

    fun dealMessageData(messageInfo: MsgInfo) {
        if (type.equals("chat"))
            when (messageInfo.event) {
                EVENT_GIFT_SEND_SUCCESS, EVENT_SURVEY, EVENT_QUESTION, EVENT_SIGNIN, EVENT_START_LOTTERY, EVENT_END_LOTTERY -> {
                    val e = ChatMessageData()
                    e.msgInfo = messageInfo
                    chatAdapter.addData(e)
                    if (chatAdapter.itemCount > 0)
                        mViewBinding.recycleView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                }
                EVENT_CHAT_FORBID_ALL -> {
                    //问答状态 根据全体禁言判断 如果开启禁言则根据 qa_status判断 1开启 0关闭  如果关闭全体禁言 则直接开启问答
                    //聊天 如果开启 则不可以聊 如果关闭则根据当前的 个人禁言情况 status 1开启 0关闭
                    allForbid = messageInfo.status != 0
                    if (messageInfo.status == 0) {
                        showToast("解除全员禁言")
                        canChat = !ownForbid
                        canQa = true
                    } else {
                        showToast("全员禁言")
                        canChat = false
                        canQa = messageInfo.qa_status.equals("0")
                    }
                    setHint()
                }
                EVENT_DISABLE_CHAT -> {
                    ownForbid = true
                    showToast("您已被禁言")
                    canChat = false
                    setHint()
                }
                EVENT_PERMIT_CHAT -> {
                    ownForbid = false
                    showToast("您已被解除禁言")
                    canChat = !allForbid
                    setHint()
                }
                EVENT_INTERACTIVE_ALLOW_HAND -> {
                    showToast(if (messageInfo.status == 0) "举手按钮关闭" else "举手按钮开启")
                    var statusBundle = Bundle().apply {
                        putBoolean(HAND_UP_KEY_STATUS, messageInfo.status != 0)
                    }
                    (activity as WatchLiveActivity).call(HAND_UP_KEY, "", statusBundle)
                    handsUp = messageInfo.status.toString()
                    setPublishIcon()
                }
            }
        else
            if (messageInfo.event == EVENT_CHAT_FORBID_ALL) {
                canQa = if (messageInfo.status == 0) {
                    true
                } else {
                    messageInfo.qa_status.equals("0")
                }
                setHint()
            }


        if (messageInfo.event == EVENT_PRAISE_TOTAL) {
            likeNum = messageInfo.likeNum
            setLikeNum(messageInfo.likeNum)
        }
    }

}