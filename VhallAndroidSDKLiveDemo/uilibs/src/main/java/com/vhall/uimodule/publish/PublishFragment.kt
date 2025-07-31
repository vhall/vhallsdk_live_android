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
import com.vhall.business.ErrorCode
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
        fun newInstance(info: WebinarInfo,isV2:Boolean) =
            PublishFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(IBase.INFO_KEY, info)
                    putBoolean(IBase.V2_KEY, isV2)
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
    private var isPreMirror = false
    private var mode = VHLivePushFormat.DRAW_MODE_ASPECTFILL
    private var frontCamera = true;
    private var isStart = false;
    private var isCameraOpen = true;

    override fun initView() {
        parentActivity = activity as PublishActivity
        arguments?.let {
            webinarInfo = it.getSerializable(IBase.INFO_KEY) as WebinarInfo
            isV2 = it.getBoolean(IBase.V2_KEY, false)
            initPublish()
        }

        //开始直播
        mViewBinding.tvStart.setOnClickListener { startLive() }
        //V2版本重新推流使用
        mViewBinding.rePushStream.setOnClickListener { rePushStream() }
        mViewBinding.rePushStream.visibility = View.GONE;
        //结束直播/返回按钮
        mViewBinding.btnBack.setOnClickListener {
            if(isPublishing) {
                showEndLiveDialog()
            }else{
                parentActivity?.finish()
            }
        }
        //使用V2 版本后，需要使用BroadCast进行画面填充模式设置
        //mViewBinding.cameraview.setCameraDrawMode(mode)
        getBroadcast()!!.setCameraDrawMode(mode);
        mViewBinding.tvMode.setOnClickListener { changeMode() }
        //闪光灯
        mViewBinding.btnChangeFlash.setOnClickListener {
            isFlashOpen = !isFlashOpen
            getBroadcast()!!.changeFlash(isFlashOpen)
            mViewBinding.btnChangeFlash.setBackgroundResource(if (isFlashOpen) R.drawable.img_round_flash_open else R.drawable.img_round_flash_close )
        }
        //切换相机
        mViewBinding.btnChangeCamera.setOnClickListener {
            if(isV2){
                frontCamera = !frontCamera;
                getBroadcast()!!.changeCameraV2(frontCamera);
                mViewBinding.btnChangeFlash.setVisibility(if (frontCamera) View.GONE  else View.VISIBLE);
            }else{
                val cameraId = getBroadcast()!!.changeCamera()
                val cameraInfo = Camera.CameraInfo()
                Camera.getCameraInfo(cameraId, cameraInfo)
                val hide:Boolean= (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                mViewBinding.btnChangeFlash.setVisibility(if (hide) View.VISIBLE else View.GONE)
                mViewBinding.btnPremirror.setVisibility(if (hide) View.GONE else View.VISIBLE)
                if(hide)
                    mViewBinding.btnChangeFlash.setBackgroundResource(R.drawable.img_round_flash_close)
            }
        }
        //声音采集开关
        mViewBinding.btnChangeAudio.setOnClickListener {
            val isMute = getBroadcast()!!.isMute
            getBroadcast()!!.setMute(!isMute);
            mViewBinding.btnChangeAudio.setBackgroundResource(if (!isMute) R.drawable.img_round_audio_close else R.drawable.img_round_audio_open);
        }

        //摄像头开关
        mViewBinding.btnCameraEnable.setOnClickListener {
            isCameraOpen = !isCameraOpen;
            getBroadcast()!!.setVideoCapture(isCameraOpen);
            mViewBinding.btnCameraEnable.setBackgroundResource(if (isCameraOpen) R.mipmap.icon_camera_open else R.mipmap.icon_camera_close)
        }

        mViewBinding.btnMirror.setOnClickListener {
            isMirror = !isMirror
            //V1/V2 版本使用使用broadCast对象进行镜像设置。
            //mViewBinding.cameraview.setMirror(isMirror)
            getBroadcast()!!.setMirror(isMirror);
            mViewBinding.btnMirror.setBackgroundResource(if (isMirror) R.drawable.icon_mirror_normal else R.drawable.icon_mirror_selected )
        }

        mViewBinding.btnPremirror.setOnClickListener {
            isPreMirror = !isPreMirror
            //使用broadCast对象进行镜像设置。V2 版本使用 getBroadcast()!!.setMirror(isMirror); 设置镜像
           // mViewBinding.cameraview.setFrontPreViewMirror(isPreMirror);
            getBroadcast()!!.setFrontPreViewMirror(isPreMirror);
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

    private fun rePushStream(){
        // 未结束直播，仅重新推流使用此方法
        getBroadcast()!!.pushStream(object :RequestCallback{
            override fun onSuccess() {
                isStart = true;
                startLiveSuccess(true);
            }
            override fun onError(errorCode: Int, errorMsg: String?) {
                parentActivity!!.finishLoading()
                startLiveSuccess(false)
                showMsg(errorMsg)
            }
        });
        mViewBinding.rePushStream.visibility = View.GONE;
    }

    fun startLive() {
        parentActivity!!.showLoading(null,"努力推流中...")
        getBroadcast()?.start(object :RequestCallback{
            override fun onSuccess() {
                isStart = true;
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
        //V2版本stop默认会关闭摄像头。
        getBroadcast()?.stop();
        //V2版本可以重新开启摄像头或者根据业务状态显示遮罩图。
        getBroadcast()?.setVideoCapture(isCameraOpen);
        isStart = false;
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
        if(isStart){
            //V2版本触发重新设置摄像头。
            getBroadcast()!!.setVideoCapture(isCameraOpen);
            //未结束直播，仅重新推流使用此方法
            getBroadcast()!!.pushStream(object :RequestCallback{
                override fun onSuccess() {
                    isStart = true;
                    startLiveSuccess(true);
                }
                override fun onError(errorCode: Int, errorMsg: String?) {
                    parentActivity!!.finishLoading()
                    startLiveSuccess(false)
                    showMsg(errorMsg)
                }
            });
        }
    }
    override fun onDestroy() {
        //mViewBinding.cameraview.releaseCapture()
        getBroadcast()?.destroy() // broadcast内部释放时会调用cameraview.releaseCapture()方法，外层不需要进行释放
        super.onDestroy()
    }

    //调整摄像头预览画面填充模式
    fun changeMode(){
        if (mode == VHLivePushFormat.DRAW_MODE_ASPECTFILL) {
            getBroadcast()!!.setCameraDrawMode(VHLivePushFormat.DRAW_MODE_ASPECTFIT)
            mode = VHLivePushFormat.DRAW_MODE_ASPECTFIT
            mViewBinding.tvMode.setText("FIT")
        } else if (mode == VHLivePushFormat.DRAW_MODE_ASPECTFIT) {
            getBroadcast()!!.setCameraDrawMode(VHLivePushFormat.DRAW_MODE_NONE)
            mode = VHLivePushFormat.DRAW_MODE_NONE
            mViewBinding.tvMode.setText("NONE")
        } else if (mode == VHLivePushFormat.DRAW_MODE_NONE) {
            getBroadcast()!!.setCameraDrawMode(VHLivePushFormat.DRAW_MODE_ASPECTFILL)
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
            if (webinarInfo.webinar_show_type == 0) {
              config.screenOri = VHLivePushFormat.SCREEN_ORI_PORTRAIT
            } else {
              config.screenOri = VHLivePushFormat.SCREEN_ORI_LANDSPACE
            }
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
                .cameraView(mViewBinding.cameraview)//使用V2版本不可以通过cameraview控制设备。需要使用broadcast控制镜像、填充模式、切换设备、闪光灯等操作；
                .config(config)
                .callback(BroadcastEventCallback())
                .messageCallback(messageCallBack)
                .chatCallback(chatCallBack)
                .setContext(mContext)//使用V2版本必传参数
                .isBuildV2(isV2)//V2版本提供更可靠的推流稳定性，V2版本开通需联系技术支持人员进行咨询。
                .setLicenseKey("")//使用V2版本集成到您的app中是，需要联系技术支持人员协助开通获取license信息。
                .setLicenseUrl("")//使用V2版本集成到您的app中是，需要联系技术支持人员协助开通获取license信息。
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
            if(isV2 && errorCode == ErrorCode.ERROR_V2_DISCONNECTED){
                //停止推流或收到ErrorCode.ERROR_V2_DISCONNECTED 事件会默认关闭摄像头。应用层需重新开启摄像头
                getBroadcast()!!.setVideoCapture(isCameraOpen);
                mViewBinding.rePushStream.visibility = View.VISIBLE;
            }
            showMsg(msg)
        }
    }

    fun showMsg(msg: String?) {
        if (this.isAdded) {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }
}