package com.vhall.uimodule.module.watch.watchplayback

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import com.vhall.business.*
import com.vhall.business.data.RequestCallback
import com.vhall.business.data.WebinarInfo
import com.vhall.player.Constants
import com.vhall.player.VHPlayerListener
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseFragment
import com.vhall.uimodule.base.IBase.*
import com.vhall.uimodule.databinding.FragmentWatchPlaybackBinding
import com.vhall.uimodule.module.watch.WatchLiveActivity
import com.vhall.uimodule.utils.CommonUtil
import com.vhall.uimodule.widget.ScrollChooseTypeDialog
import com.vhall.vhss.data.RecordChaptersData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.fixedRateTimer


class WatchPlaybackFragment :
    BaseFragment<FragmentWatchPlaybackBinding>(FragmentWatchPlaybackBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo) =
            WatchPlaybackFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(INFO_KEY, info)
                }
            }
    }

    private lateinit var watchPlayback: WatchPlayback
    lateinit var chatCallBack: ChatServer.Callback
    lateinit var messageCallBack: MessageServer.Callback
    private var definitionList: MutableList<String> = arrayListOf()
    private var timer: Timer? = null

    //是否全屏
    private var isFull = false
    var parentActivity: WatchLiveActivity? = null

    override fun initView() {
        parentActivity = activity as WatchLiveActivity
        starTime()
        arguments?.let {
            webinarInfo = it.getSerializable(INFO_KEY) as WebinarInfo
            initWatchPlayback()
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
        mViewBinding.tvSpeed.setOnClickListener {
            showSpeed()
        }
        mViewBinding.clickPlay.setOnClickListener {
            if (watchPlayback.isPlaying) {
                watchPlayback.stop()
            } else {
                watchPlayback.start()
            }
        }

        mViewBinding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!watchPlayback.isPlaying) {
                    watchPlayback.start()
                }
                val progress = seekBar?.progress
                watchPlayback.seekTo(progress!!.toLong())

            }

        })
    }

    private fun initWatchPlayback() {
        watchPlayback =
            WatchPlayback.Builder()
                .vodPlayView(mViewBinding.vodPlayerView)
                .callback(WatchCallback())
                .chatCallback(chatCallBack)
                .messageCallback(messageCallBack)
                .context(mContext)
                .build()
        watchPlayback.setWebinarInfo(webinarInfo)
        watchPlayback.scaleType = Constants.VideoMode.DRAW_MODE_ASPECTFIT
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

    private fun starTime() {
        timer?.cancel()
        timer = null
        timer = fixedRateTimer("time", true, 1000, 1000) {
            lifecycleScope.launch(Dispatchers.Main) {
                if (watchPlayback.isPlaying) {
                    val playerCurrentPosition = watchPlayback.currentPosition
                    mViewBinding.seekbar.progress = playerCurrentPosition.toInt()
                    mViewBinding.tvTime.text =
                        CommonUtil.converLongTimeToStr(watchPlayback.currentPosition).plus("/")
                            .plus(CommonUtil.converLongTimeToStr(watchPlayback.duration))
                }
            }
        }
    }

    inner class WatchCallback : VHPlayerListener {
        override fun onStateChanged(state: Constants.State?) {
            when (state) {
                Constants.State.START -> {
                    mViewBinding.ivPlay.setBackgroundResource(R.mipmap.icon_play_pause)
                    mViewBinding.seekbar.max = watchPlayback.duration.toInt()
                    mViewBinding.tvTime.text =
                        CommonUtil.converLongTimeToStr(watchPlayback.currentPosition).plus("/")
                            .plus(CommonUtil.converLongTimeToStr(watchPlayback.duration))

                }
                Constants.State.BUFFER -> {
                    mViewBinding.ivPlay.setBackgroundResource(R.mipmap.icon_playing)
                }
                Constants.State.STOP -> {
                    mViewBinding.ivPlay.setBackgroundResource(R.mipmap.icon_playing)
                }
                Constants.State.END -> {
                    mViewBinding.ivPlay.setBackgroundResource(R.mipmap.icon_playing)
                }
            }
        }

        override fun onEvent(event: Int, msg: String?) {
            when (event) {
                Constants.Event.EVENT_DPI_CHANGED ->
                    mViewBinding.tvDefinition.text = CommonUtil.changeDefinition(msg)
                Constants.Event.EVENT_DPI_LIST ->
                    try {
                        val array = JSONArray(msg)
                        definitionList.clear()
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
                Constants.Event.EVENT_CUE_POINT -> {
                    if(msg == null)
                        return;
                    try {
                        val obj = JSONObject()
                        val array = JSONArray(msg)
                        var pointList: MutableList<RecordChaptersData.ListBean>  = arrayListOf()
                        for (i in 0 until array.length()) {
                            val point = array.getJSONObject(i)
                            point.put("title",point.getString("msg"))
                            point.put("created_at",point.getInt("timePoint"))
                        }
                        obj.put("doc_titles",array)
                        var data: RecordChaptersData  = RecordChaptersData(obj)
                        if (data != null && data.list != null && data.list.size!=0) {
                            val a: WatchLiveActivity = activity as WatchLiveActivity
                            a.showVideoPoint(data.list)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
//                    showToast(msg)
                }
                ErrorCode.ERROR_LOGIN_MORE -> {
                    showToast(msg)
                    activity?.finish()
                }
                Watch.EVENT_INIT_PLAYER_SUCCESS ->
                    watchPlayback.start()
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
        if(webinarInfo.is_new_version == 3){
            watchPlayback.requestCommentHistory(
                webinarInfo.webinar_id,
                100,
                page,
                msgId,
                "down",
                "0",
                callback
            )
        }else{
//            watchPlayback.requestCommentHistory(
//                webinarInfo.webinar_id,
//                100,
//                page,
//                callback
//            )
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
    fun seekTo(time: Int) {
        watchPlayback.seekTo(time.toLong())
    }

    override fun onPause() {
        super.onPause()
    }

//    override fun onStop() {
//        super.onStop()
//        if (watchPlayback.isPlaying)
//            watchPlayback.onPause()
//    }

    fun sendChat(msg: String) {
        watchPlayback.sendComment(msg, object : RequestCallback {
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
                watchPlayback.setDefinition(msg)
                scrollChooseTypeDialog!!.dismiss()
            }
        }
        scrollChooseTypeDialog?.show()
    }

    private var scrollSpeedDialog: ScrollChooseTypeDialog? = null
    private fun showSpeed() {
        if (scrollSpeedDialog == null) {
            var speedList: MutableList<String> = arrayListOf()
            speedList.add("0.5")
            speedList.add("1.0")
            speedList.add("1.5")
            speedList.add("2.0")
            scrollSpeedDialog = ScrollChooseTypeDialog(mContext, speedList)
            scrollSpeedDialog!!.setOnItemClickLister { _: Int, msg: String ->
                watchPlayback.setSpeed(msg.toFloat())
                mViewBinding.tvSpeed.text = msg.plus("x")
                scrollSpeedDialog!!.dismiss()
            }
        }
        scrollSpeedDialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        watchPlayback.destroy()
    }
}