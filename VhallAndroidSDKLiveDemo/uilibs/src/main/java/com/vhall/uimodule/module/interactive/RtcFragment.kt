package com.vhall.uimodule.module.interactive

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.vhall.business.*
import com.vhall.business.MessageServer.*
import com.vhall.business.data.RequestCallback
import com.vhall.business.data.RequestDataCallbackV2
import com.vhall.business.data.WebinarInfo
import com.vhall.business_interactive.InterActive
import com.vhall.business_interactive.Rtc
import com.vhall.uimodule.base.BaseFragment
import com.vhall.uimodule.base.IBase.INFO_KEY
import com.vhall.uimodule.databinding.FragmentRtcBinding
import com.vhall.uimodule.module.interactive.HandUpOperateDialog.Companion.clickTypeAudio
import com.vhall.uimodule.module.interactive.HandUpOperateDialog.Companion.clickTypeCamera
import com.vhall.uimodule.module.interactive.HandUpOperateDialog.Companion.clickTypeHandCancel
import com.vhall.uimodule.module.interactive.HandUpOperateDialog.Companion.clickTypeVideo
import com.vhall.uimodule.module.watch.WatchLiveActivity
import com.vhall.uimodule.widget.ItemClickLister
import com.vhall.vhallrtc.client.Room
import com.vhall.vhallrtc.client.Room.VHRoomStatus
import com.vhall.vhallrtc.client.Stream
import com.vhall.vhallrtc.client.VHRenderView
import com.vhall.vhss.data.RoomToolsStatusData
import org.json.JSONObject
import org.vhwebrtc.SurfaceViewRenderer
import vhall.com.vss2.module.room.MessageTypeData.*


class RtcFragment :
    BaseFragment<FragmentRtcBinding>(FragmentRtcBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo) =
            RtcFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(INFO_KEY, info)
                }
            }
    }

    private lateinit var interactive: InterActive
    private lateinit var adapter: RenderAdapter
    private var handUpOperateDialog: HandUpOperateDialog? = null
    private val CAMERA_VIDEO = 2 //摄像头
    private val CAMERA_AUDIO = 1 //麦克风
    private val CAMERA_DEVICE_OPEN = 1
    private val CAMERA_DEVICE_CLOSE = 0
    private var isOpenVideo = true
    private var isOpenAudio = true
    private var isDisconnected = false

    //主讲人
    private var mainId = ""

    //可以复用直播的消息，直播中的互动可以不用设置消息
    private var chatCallBack: ChatServer.Callback? = null
    private var messageCallBack: Callback? = null

    override fun initView() {
        arguments?.let {
            webinarInfo = it.getSerializable(INFO_KEY) as WebinarInfo
            initInteractive()
            mainId = webinarInfo.mainId
        }
        adapter = RenderAdapter(mainId)
        val gridLayoutManager = GridLayoutManager(mContext, 2, GridLayoutManager.HORIZONTAL, false)
        mViewBinding.recycleView.layoutManager = gridLayoutManager
        mViewBinding.recycleView.adapter = adapter
        mViewBinding.recycleView.setHasFixedSize(true)
        mViewBinding.recycleView.setItemViewCacheSize(0)
    }

    fun showHandUpOperate() {
        if (handUpOperateDialog == null) {
            handUpOperateDialog = HandUpOperateDialog(mContext, isOpenVideo, isOpenAudio)
            handUpOperateDialog?.setOnItemClickLister(object : ItemClickLister {
                override fun onItemClick(type: Int) {
                    when (type) {
                        clickTypeVideo -> {
                            interactive.switchDevice(
                                CAMERA_VIDEO,
                                if (isOpenVideo) CAMERA_DEVICE_CLOSE else CAMERA_DEVICE_OPEN,
                                object : RequestCallback {
                                    override fun onSuccess() {}
                                    override fun onError(errorCode: Int, errorMsg: String) {
                                        showToast(errorMsg)
                                    }
                                })
                        }
                        clickTypeAudio -> {
                            interactive.switchDevice(
                                CAMERA_AUDIO,
                                if (isOpenAudio) CAMERA_DEVICE_CLOSE else CAMERA_DEVICE_OPEN,
                                object : RequestCallback {
                                    override fun onSuccess() {}
                                    override fun onError(errorCode: Int, errorMsg: String) {
                                        showToast(errorMsg)
                                    }
                                })
                        }
                        clickTypeCamera -> {
                            interactive.localStream.switchCamera()
                        }
                        clickTypeHandCancel -> {
                            interactive.unpublish(object : RequestCallback {
                                override fun onError(errorCode: Int, errorMsg: String?) {
                                    showToast(errorMsg)
                                }

                                override fun onSuccess() {
                                    showToast("下麦成功")
                                    handUpOperateDialog?.dismiss()
                                }
                            })
                        }
                    }
                }
            })
        }
        handUpOperateDialog?.show()
    }

    private fun initInteractive() {
        interactive = InterActive(mContext, RoomCallback(), chatCallBack, messageCallBack)
        interactive.init(false, webinarInfo, object : RequestCallback {
            override fun onSuccess() {
                setLocalView()
                interactive.enterRoom()
            }

            override fun onError(errorCode: Int, errorMsg: String) {
                showToast(errorMsg)
                activity?.finish()
            }
        })

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun onDownMic() {
        interactive.unpublish(object : RequestCallback {
            override fun onSuccess() {}
            override fun onError(errorCode: Int, errorMsg: String) {}
        })
    }

    private fun setLocalView() {
        var vhRenderView = VHRenderView(context)
        vhRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit)
        vhRenderView.init(null, null)
        interactive.setLocalView(
            vhRenderView,
            Stream.VhallStreamType.VhallStreamTypeAudioAndVideo,
            null
        )
    }

    inner class RoomCallback : Rtc.RoomCallback {

        override fun onDidConnect() {
            interactive.publish()
        }

        override fun onDidError() {
        }

        override fun onDidPublishStream() {
            adapter.addNewData(StreamData(interactive.localStream))
        }

        override fun onDidUnPublishStream() {
            adapter.removeData(StreamData(interactive.localStream))
        }

        override fun onDidSubscribeStream(stream: Stream?, newRenderView: VHRenderView?) {
            /**
             * 视频轮巡 需要过滤不显示
             */
            if (stream != null && stream.streamType == 5) {
                return
            }
            adapter.addNewData(StreamData(stream))
        }

        override fun onDidRemoveStream(room: Room?, stream: Stream?) {
            /**
             * 视频轮巡 需要过滤不显示
             */
            if (stream != null && stream.streamType == 5) {
                return
            }
            adapter.removeData(StreamData(stream))
        }

        override fun onDidUpdateOfStream(stream: Stream?, jsonObject: JSONObject?) {
            super.onDidUpdateOfStream(stream, jsonObject)
            adapter.changeItemData(StreamData(stream))
        }

        override fun onDidRoomStatus(room: Room?, vhRoomStatus: VHRoomStatus?) {
            when (vhRoomStatus) {
                VHRoomStatus.VHRoomStatusDisconnected -> {
                    //断开链接-断网或者弱网
                    isDisconnected = true
                    activity?.finish()
                }
                VHRoomStatus.VHRoomStatusError -> {
                    if (!isDisconnected) {
                        activity?.finish()
                        showToast("互动房间链接失败")
                    }
                }
                VHRoomStatus.VHRoomStatusConnected -> {
                    //断网重联成功 需要判断自己是否还在上麦列表
                    if (isDisconnected)
                        judgePublish()
                    isDisconnected = false
                }
            }
        }
    }

    private fun judgePublish() {
        VhallSDK.getRoomToolsState(webinarInfo.vss_room_id,
            object : RequestDataCallbackV2<RoomToolsStatusData> {
                override fun onError(errorCode: Int, errorMsg: String?) {
                    showToast(errorMsg)
                }

                override fun onSuccess(data: RoomToolsStatusData) {
                    if (data.speaker_list.isNotEmpty())
                        for (i in data.speaker_list.indices) {
                            if (data.speaker_list[i].account_id.equals(VhallSDK.getUserId())) {
                                interactive.publish()
                                return
                            }
                        }
                    (activity as WatchLiveActivity).leaveInteractive()
                }
            })
    }

    fun dealMessageData(messageInfo: MsgInfo) {
        when (messageInfo.event) {
            EVENT_SWITCH_DEVICE -> {
                if (messageInfo.device == CAMERA_AUDIO) { // 1 麦克风
                    switchAudioFrame(messageInfo.status)
                    //1 开 0关
                    handUpOperateDialog?.setAudio(messageInfo.status == 1)
                } else {
                    switchVideoFrame(messageInfo.status)
                    //2 摄像头
                    handUpOperateDialog?.setVideo(messageInfo.status == 1)
                    update(webinarInfo.user_id, messageInfo.status == 0)
                }
            }
            EVENT_VRTC_BIG_SCREEN_SET -> {
                //更新第一个view
                adapter.setMainId(messageInfo.roomJoinId)
                mViewBinding.recycleView.scrollToPosition(0)
            }
            else -> {
                val responseImMessageInfo = messageInfo.responseImMessageInfo
                if (responseImMessageInfo != null) {
                    try {
                        val objData = JSONObject(responseImMessageInfo.data)
                        val type = objData.optString("type")
                        val userId = objData.optString("target_id")//被操控的人的id
                        when (type) {
                            MESSAGE_VRTC_FRAMES_FORBID -> {
                                update(userId, true)
                            }
                            MESSAGE_VRTC_FRAMES_DISPLAY -> {
                                update(userId, false)
                            }
                        }
                    } catch (e: Exception) {
                        showToast(e.message)
                    }
                }
            }

        }
    }

    private fun update(userId: String, show: Boolean) {
        if (userId == VhallSDK.getUserId()) {
            adapter.updateCameraStatus(userId, show)
        }
    }

    private fun switchVideoFrame(status: Int) = if (status == CAMERA_DEVICE_OPEN) { //1打开
        interactive.localStream.unmuteVideo(null)
        isOpenVideo = true
    } else { // 0禁止
        interactive.localStream.muteVideo(null)
        isOpenVideo = false
    }

    private fun switchAudioFrame(status: Int) = if (status == CAMERA_DEVICE_OPEN) {
        interactive.localStream.unmuteAudio(null)
        isOpenAudio = true
    } else {
        interactive.localStream.muteAudio(null)
        isOpenAudio = false
    }

    override fun onDestroy() {
        super.onDestroy()
        handUpOperateDialog?.dismiss()
        onDownMic()
        interactive.onDestroy()
    }
}