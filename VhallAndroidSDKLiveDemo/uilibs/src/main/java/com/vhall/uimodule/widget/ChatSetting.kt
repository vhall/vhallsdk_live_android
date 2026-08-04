package com.vhall.uimodule.widget

import android.R
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.opengl.Visibility
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.vhall.uimodule.databinding.ChatSettingLayoutBinding


class ChatSetting(context: Context,
                  private val defaultWatchHost: Boolean,
                  private val showOnlyWatchHost:Boolean,
                  private val defaultWatchContext: Boolean,
                  private val showWatchContext:Boolean,
                  private val defaultHideEffect: Boolean,
                    private val showEffect:Boolean,
                ):Dialog(context){


    private lateinit var binding: ChatSettingLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChatSettingLayoutBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        binding.watchChatContext.isChecked = defaultWatchContext;
        binding.watchHost.isChecked = defaultWatchHost;
        binding.hideEffect.isChecked = defaultHideEffect;
        binding.watchChatContext.setOnCheckedChangeListener { switchButton, isChecked ->
            if(isChecked){
                // 选中逻辑
            }else{
                // 取消选中逻辑
            }
        }
        binding.watchHost.setOnCheckedChangeListener { switchButton, isChecked ->
            if(isChecked){
                // 选中逻辑
            }else{
                // 取消选中逻辑
            }
        }
        binding.hideEffect.setOnCheckedChangeListener { switchButton, isChecked ->
            if(isChecked){
                // 选中逻辑
            }else{
                // 取消选中逻辑
            }
        }

        if(!showOnlyWatchHost){
            binding.layouyOnlyWatchHost.visibility = View.GONE
        }
        if(!showEffect){
            binding.layoutHideEffect.visibility = View.GONE
        }
        if(!showWatchContext){
            binding.layoutOnlyWatchText.visibility = View.GONE
        }


        initView();
    }

    fun initView(){
        val window = window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams?.gravity = Gravity.BOTTOM //窗口放在屏幕底部
        window?.attributes = layoutParams

//        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setDimAmount(0f)
    }


    fun getWatchHostStatus(): Boolean = binding.watchHost.isChecked
    fun getWatchContextStatus(): Boolean = binding.watchChatContext.isChecked
    fun getHideEffectStatus(): Boolean = binding.hideEffect.isChecked
}

