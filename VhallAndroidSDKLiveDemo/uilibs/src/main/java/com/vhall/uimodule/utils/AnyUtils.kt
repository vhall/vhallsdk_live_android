package com.vhall.uimodule.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import java.util.*

/**
 * @author hkl
 *Date: 2022/12/6 17:17
 */

fun String.copy(context: Context) {
    val mClipData = ClipData.newPlainText("Label", this)
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(mClipData)
}

internal fun Any.log(msg: String?, level: Int = Log.VERBOSE) {
    val wrappedMsg = ">>>$msg"
    val TAG = "vhall_"
    when (level) {
        Log.DEBUG -> Log.d(TAG, wrappedMsg)
        Log.INFO -> Log.i(TAG, wrappedMsg)
        Log.WARN -> Log.w(TAG, wrappedMsg)
        Log.ERROR -> Log.e(TAG, wrappedMsg)
        else -> Log.v(TAG, wrappedMsg)
    }
}

