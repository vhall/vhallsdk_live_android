package com.vhall.uimodule.module.interactive

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseBottomDialog
import com.vhall.uimodule.databinding.DialogHandUpOperateBinding
import com.vhall.uimodule.widget.ItemClickLister

class HandUpOperateDialog constructor(
    context: Context,
    var isOpenVideo: Boolean,
    var isOpenAudio: Boolean
) :
    BaseBottomDialog(context), View.OnClickListener {
    companion object {
        const val clickTypeVideo = 1
        const val clickTypeAudio = 2
        const val clickTypeCamera = 3
        const val clickTypeHandCancel = 4
        const val clickBeautify = 5
    }

    private var myItemClickLister: ItemClickLister? = null
    fun setOnItemClickLister(myItemClickLister: ItemClickLister) {
        this.myItemClickLister = myItemClickLister
    }

    private lateinit var binding: DialogHandUpOperateBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = View.inflate(context, R.layout.dialog_hand_up_operate, null)
        binding = DialogHandUpOperateBinding.bind(root)
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        binding.tvAudio.setOnClickListener(this)
        binding.tvVideo.setOnClickListener(this)
        binding.tvCamera.setOnClickListener(this)
        binding.tvHandCancel.setOnClickListener(this)
        binding.tvBeautify.setOnClickListener(this)
        binding.root.setOnClickListener(this)
        setVideo(isOpenVideo)
        setAudio(isOpenAudio)
    }

    override fun onClick(v: View) {
        when (v) {
            binding.tvVideo -> {
                myItemClickLister?.onItemClick(clickTypeVideo)
            }
            binding.tvAudio -> {
                myItemClickLister?.onItemClick(clickTypeAudio)
            }
            binding.tvCamera -> {
                myItemClickLister?.onItemClick(clickTypeCamera)
            }
            binding.tvHandCancel -> {
                myItemClickLister?.onItemClick(clickTypeHandCancel)
            }
            binding.tvBeautify -> {
                myItemClickLister?.onItemClick(clickBeautify)
            }
            binding.root -> dismiss()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setVideo(open: Boolean) {
        val dra =
            if (open) mContext.getDrawable(R.drawable.svg_icon_video_open) else mContext.getDrawable(
                R.drawable.svg_icon_video_close
            )
        dra?.setBounds(0, 0, dra.minimumWidth, dra.minimumHeight)
        binding.tvVideo.setCompoundDrawables(null, dra, null, null)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setAudio(open: Boolean) {
        val dra =
            if (open) mContext.getDrawable(R.drawable.svg_icon_audio_open) else mContext.getDrawable(
                R.drawable.svg_icon_audio_close
            )
        dra?.setBounds(0, 0, dra.minimumWidth, dra.minimumHeight)
        binding.tvAudio.setCompoundDrawables(null, dra, null, null)
    }
}
