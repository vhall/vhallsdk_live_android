package com.vhall.uimodule.module.interactive

import androidx.recyclerview.widget.DiffUtil

/**
 * @author：vhall  Email：jooperge@163.com
 * 描述：
 * 修改历史:
 * <p>
 * 创建于： 2022/12/28
 */
open class MiniRenderDiffCallback : DiffUtil.ItemCallback<StreamData>() {
    override fun areItemsTheSame(oldStream: StreamData, newStream: StreamData): Boolean {
        return oldStream.streamUserId === newStream.streamUserId
    }

    override fun areContentsTheSame(oldStream: StreamData, newStream: StreamData): Boolean {
        return (oldStream.name.equals(newStream.name)
                && oldStream.camera == newStream.camera)
    }
}