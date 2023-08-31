package com.vhall.uimodule.publish

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.vhall.beautify.VHBeautifyKit
import com.vhall.business.Broadcast
import com.vhall.business.ChatServer
import com.vhall.business.MessageServer
import com.vhall.business.VhallSDK
import com.vhall.business.data.RequestCallback
import com.vhall.business.data.WebinarInfo
import com.vhall.player.Constants
import com.vhall.player.VHPlayerListener
import com.vhall.push.VHLivePushConfig
import com.vhall.push.VHLivePushFormat
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseFragment
import com.vhall.uimodule.base.IBase
import com.vhall.uimodule.databinding.FragmentPublishBinding
import com.vhall.uimodule.widget.OutDialog
import com.vhall.uimodule.widget.OutDialogBuilder


class PublishFragment : BaseFragment<FragmentPublishBinding>(FragmentPublishBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo) =
            PublishFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(IBase.INFO_KEY, info)
                }
            }
    }

    private var iFaceBeautySwitch: IFaceBeautySwitch? = null
    private var beautyDialog: OutDialog? = null

    fun setIFaceBeautySwitch(iFaceBeautySwitch: IFaceBeautySwitch?) {
        this.iFaceBeautySwitch = iFaceBeautySwitch
    }
    var parentActivity: PublishActivity? = null
    private var broadcast: Broadcast? = null
    lateinit var chatCallBack: ChatServer.Callback
    lateinit var messageCallBack: MessageServer.Callback
    private var isPublishing = false
    private var isFlashOpen = false
    private var isMirror = false
    private var mode = VHLivePushFormat.DRAW_MODE_ASPECTFILL

    override fun initView() {
        parentActivity = activity as PublishActivity
        arguments?.let {
            webinarInfo = it.getSerializable(IBase.INFO_KEY) as WebinarInfo
            initPublish()
        }

        //开始直播
        mViewBinding.tvStart.setOnClickListener { startLive() }
        //结束直播/返回按钮
        mViewBinding.btnBack.setOnClickListener {
            if(isPublishing) {
                showEndLiveDialog()
            }else{
                parentActivity?.finish()
            }
        }
        //画面填充模式
        mViewBinding.cameraview.setCameraDrawMode(mode)
        mViewBinding.tvMode.setOnClickListener { changeMode() }
        //闪光灯
        mViewBinding.btnChangeFlash.setOnClickListener {
            isFlashOpen = !isFlashOpen
            getBroadcast()!!.changeFlash(isFlashOpen)
            mViewBinding.btnChangeFlash.setBackgroundResource(if (isFlashOpen) R.drawable.img_round_flash_open else R.drawable.img_round_flash_close )
        }
        //切换相机
        mViewBinding.btnChangeCamera.setOnClickListener {
            val cameraId = getBroadcast()!!.changeCamera()
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(cameraId, cameraInfo)
            val hide:Boolean= (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
            mViewBinding.btnChangeFlash.setVisibility(if (hide) View.VISIBLE else View.GONE)
            if(hide)
                mViewBinding.btnChangeFlash.setBackgroundResource(R.drawable.img_round_flash_close)
        }
        //声音采集开关
        mViewBinding.btnChangeAudio.setOnClickListener {
            val isMute = getBroadcast()!!.isMute
            getBroadcast()!!.isMute = !isMute
            mViewBinding.btnChangeAudio.setBackgroundResource(if (isMute) R.drawable.img_round_audio_close else R.drawable.img_round_audio_open)
        }
        //闪光灯
        mViewBinding.btnMirror.setOnClickListener {
            isMirror = !isMirror
            mViewBinding.cameraview.setMirror(isMirror)
            mViewBinding.btnMirror.setBackgroundResource(if (isMirror) R.drawable.icon_mirror_normal else R.drawable.icon_mirror_selected )
        }

        mViewBinding.btnChangeFilter.setOnClickListener {
            VHBeautifyKit.getInstance().setBeautifyEnable(true)
            //  showPopupWindow();  //之前的美颜只能选等级
            if (VHBeautifyKit.getInstance().setBeautifyEnable(true)) {
                    iFaceBeautySwitch!!.changeVisibility()
            } else {
                if (beautyDialog == null) {
                    beautyDialog = OutDialogBuilder().layout(R.layout.dialog_beauty_no_serve)
                        .build(activity)
                }
                beautyDialog!!.show()
            }
        }
    }

    /*
     * 发起直播步骤
     * https://saas-doc.vhall.com/opendocs/show/1227
     */
    private fun initPublish() {
        // Broadcast.Builder() 设置推流参数
        getBroadcast()!!.setWebinarInfo(webinarInfo)
    }
    fun startLive() {
        parentActivity!!.showLoading(null,"努力推流中...")
        broadcast?.start(object :RequestCallback{
            override fun onSuccess() {
//                    startBroadcastSuccess(true)
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                parentActivity!!.finishLoading()
                startLiveSuccess(false)
                showMsg(errorMsg)
            }
        })
    }
    fun stopLive() {
        val broId: String
        val broToken: String
        broId = webinarInfo.webinar_id
        broToken = webinarInfo.broadcastToken

        VhallSDK.finishBroadcast(
            broId,
            broToken,
            getBroadcast(),
            false,
            object : RequestCallback {
                override fun onSuccess() {
                }
                override fun onError(errorCode: Int, reason: String) {
                    showMsg(reason)
                }
            })
        broadcast?.stop()
    }
    override fun onPause() {
        super.onPause()
        //停止直播
        if (isPublishing) {
            getBroadcast()!!.stop()
        }
    }
    override fun onResume() {
        super.onResume()
        //自动恢复推流？
    }
    override fun onDestroy() {
        mViewBinding.cameraview.releaseCapture()
        getBroadcast()?.destroy()
        super.onDestroy()
    }

    //调整摄像头预览画面填充模式
    fun changeMode(){
        if (mode == VHLivePushFormat.DRAW_MODE_ASPECTFILL) {
            getBroadcast()!!.changeMode(VHLivePushFormat.DRAW_MODE_ASPECTFIT)
            mode = VHLivePushFormat.DRAW_MODE_ASPECTFIT
            mViewBinding.tvMode.setText("FIT")
        } else if (mode == VHLivePushFormat.DRAW_MODE_ASPECTFIT) {
            getBroadcast()!!.changeMode(VHLivePushFormat.DRAW_MODE_NONE)
            mode = VHLivePushFormat.DRAW_MODE_NONE
            mViewBinding.tvMode.setText("NONE")
        } else if (mode == VHLivePushFormat.DRAW_MODE_NONE) {
            getBroadcast()!!.changeMode(VHLivePushFormat.DRAW_MODE_ASPECTFILL)
            mode = VHLivePushFormat.DRAW_MODE_ASPECTFILL
            mViewBinding.tvMode.setText("FILL")
        }
    }
    //直播成功
    fun startLiveSuccess(success: Boolean) {
        isPublishing = success
        if (success) {
            mViewBinding.tvStart.setVisibility(View.GONE)
            mViewBinding.btnBack.setBackgroundResource(R.drawable.vhall_icon_live_pause)
        } else {
            mViewBinding.tvStart.setVisibility(View.VISIBLE)
            mViewBinding.tvUploadSpeed.setText("")
            mViewBinding.btnBack.setBackgroundResource(R.drawable.icon_round_back)
        }
    }
    //结束直播确认弹窗
    fun showEndLiveDialog() {
        val alertDialog = AlertDialog.Builder(parentActivity)
            .setTitle("提示")
            .setMessage("您是否要结束直播？")
            .setPositiveButton("结束",{ dialog, which -> stopLive() })
            .setNegativeButton("取消", null)
            .create()
        alertDialog.show()
    }
    //初始化推流实例
    private fun getBroadcast(): Broadcast? {
        if (broadcast == null) {
            val config = VHLivePushConfig(VHLivePushFormat.PUSH_MODE_XHD)
            //不设置 美颜瘦脸没有效果
            config.screenOri = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //横竖屏设置 重要
            //可不设置
//            config.videoFrameRate = 25 //帧率
//            config.videoBitrate = 500 //码率
            //2 音频直播
            if (webinarInfo.layout == 2 || webinarInfo.layout == 4) {
                config.streamType = VHLivePushFormat.STREAM_TYPE_A
                mViewBinding.cameraview.setVisibility(View.GONE)
//                mView.showMsg("音频直播没有画面")
            } else {
                config.streamType = VHLivePushFormat.STREAM_TYPE_AV
            }
            val builder = Broadcast.Builder()
                .cameraView(mViewBinding.cameraview)
                .config(config)
                .callback(BroadcastEventCallback())
                .messageCallback(messageCallBack)
                .chatCallback(chatCallBack)
            broadcast = builder.build()
        }
        return broadcast
    }

    inner class BroadcastEventCallback : VHPlayerListener {
        override fun onStateChanged(state: Constants.State?) {
//            TODO("Not yet implemented")
            Log.e("===onStateChanged",state.toString())
            when (state) {
                Constants.State.START -> {
                    parentActivity!!.finishLoading()
                    startLiveSuccess(true)
                }
                Constants.State.STOP -> {
                    startLiveSuccess(false)
                }
                else -> {}
            }
        }

        override fun onEvent(event: Int, msg: String?) {
//            TODO("Not yet implemented")
            when (event) {
                Constants.Event.EVENT_UPLOAD_SPEED -> {
                    mViewBinding.tvUploadSpeed.setText(msg + "kb/s")
                }
                Constants.Event.EVENT_NETWORK_UNOBS -> {
                    showMsg("网络通畅!")
                    mViewBinding.tvUploadSpeed.setTextColor(0x00ff00)
                }
                Constants.Event.EVENT_NETWORK_OBS -> {
                    showMsg("网络环境差!")
                    mViewBinding.tvUploadSpeed.setTextColor(0xff0000)
                }
                else -> {}
            }
        }

        override fun onError(errorCode: Int, innerErrorCode: Int, msg: String?) {
//            TODO("Not yet implemented")
            showMsg(msg)
        }
    }

    fun showMsg(msg: String?) {
        if (this.isAdded) {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }
}