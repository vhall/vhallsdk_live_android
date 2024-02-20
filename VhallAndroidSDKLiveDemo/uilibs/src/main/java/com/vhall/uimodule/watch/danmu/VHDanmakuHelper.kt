package com.vhall.uimodule.watch.danmu

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.TextUtils
import com.vhall.uimodule.utils.emoji.EmojiUtils
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import java.util.HashMap


/*
 * 1、添加依赖库
    implementation 'com.github.ctiao:DanmakuFlameMaster:0.9.25'
    implementation 'com.github.ctiao:ndkbitmap-armv7a:0.9.21'
 * 2、布局文件添加弹幕view
    <master.flame.danmaku.ui.widget.DanmakuView
        android:id="@+id/sv_danmaku"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
 * 3、聊天数据填充弹幕
    danmakuHelper = context?.let { VHDanmakuHelper(it,mViewBinding.svDanmaku) };
    danmakuHelper?.addDanmaku(danmu)
 */

class VHDanmakuHelper constructor(context: Context,danmaku: DanmakuView) {
    private var mDanmuContext: DanmakuContext? = null
    private var mParser: BaseDanmakuParser? = null
    private var danmaView: DanmakuView? = null
    private var mContext: Context? = null

    init {
        mContext = context
        danmaView = danmaku
        if (danmaView != null) {
            initDanmakuView(danmaView!!)
        }
    }

    //初始化弹幕
    fun initDanmakuView(danmakuView: DanmakuView) {
        // 设置最大显示行数
        val maxLinesPair = HashMap<Int, Int>()
        maxLinesPair[BaseDanmaku.TYPE_SCROLL_RL] = 5 // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        val overlappingEnablePair = HashMap<Int, Boolean>()
        overlappingEnablePair[BaseDanmaku.TYPE_SCROLL_RL] = true
        overlappingEnablePair[BaseDanmaku.TYPE_FIX_TOP] = true

//        mViewBinding.svDanmaku.hide()
        mDanmuContext = DanmakuContext.create()
        mDanmuContext!!.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
            .setDuplicateMergingEnabled(false)
            .setScrollSpeedFactor(2.2f)
            .setScaleTextSize(1.2f) //                .setCacheStuffer(new SimpleTextCacheStuffer(), null)
            .setCacheStuffer(SpannedCacheStuffer(), null) // 图文混排使用SpannedCacheStuffer
            //        .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
            .setMaximumLines(maxLinesPair)
            .preventOverlapping(overlappingEnablePair)
        if (danmakuView != null) {
            mParser = object : BaseDanmakuParser() {
                override fun parse(): IDanmakus {
                    return Danmakus()
                }
            }
            danmakuView.setCallback(object : DrawHandler.Callback {
                override fun updateTimer(timer: DanmakuTimer) {}
                override fun drawingFinished() {}
                override fun danmakuShown(danmaku: BaseDanmaku) {}
                override fun prepared() {
                    danmakuView.start()
                }
            })
            danmakuView.prepare(mParser, mDanmuContext)
            danmakuView.showFPS(false)
            danmakuView.enableDanmakuDrawingCache(true)
        }
    }

    //发送弹幕
    fun addDanmaku(danmu: String?) {
        if (danmaView != null && !TextUtils.isEmpty(danmu)) {
            val danmaku = mDanmuContext!!.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL)
            if (danmaku == null || danmaku == null) {
                return
            }
            val spannable: Spannable = EmojiUtils.getEmojiText(mContext, danmu)
            danmaku.text = spannable
            danmaku.padding = 5
            danmaku.priority = 0 // 可能会被各种过滤器过滤并隐藏显示
            danmaku.isLive = true
            danmaku.time = danmaView!!.getCurrentTime() + 1200
            danmaku.textSize = 25f * (mParser!!.displayer.density - 0.6f)
            danmaku.textColor = Color.WHITE
            danmaku.borderColor = Color.TRANSPARENT
            danmaView!!.addDanmaku(danmaku)
        }
    }

    /* 启动弹幕, 内部时钟从0开始;
     * 若是stop之后的start,则start函数内部会清空records;
     * 视频开始的时候需要同时运行此方法.
     */
    fun start() {
        danmaView!!.start()
    }

    /* 暂停, 已经渲染上去的保持不变, 此时发送弹幕无效, 内部时钟暂停;
     * 视频开始的时候需要同时运行此方法.
     */
    fun pause() {
        danmaView!!.pause()
    }

    /* 停止弹幕渲染, 会清空所有; 再发弹幕就无效了; 一切都会停止;
     * 此方法在不再需要弹幕的时候必须调用,否则可能造成内存泄露.
     */
    fun stop(danmu: String?) {
        danmaView!!.stop()
    }
}