package com.vhall.uimodule.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.vhall.business.VhallSDK

/**
 * @author hkl
 *Date: 2022/12/6 12:09
 */
class ToastUtils {
    companion object {
        private val uiHandler = Handler(Looper.getMainLooper())
        private fun showToast(context: Context?, message: String?) {
            if (context == null) {
                return
            }
            Toast.makeText(context, message ?: "", Toast.LENGTH_SHORT).show()
        }

        private fun showToast(context: Context?, message: Int) {
            if (context == null) {
                return
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }


        fun showToast(text: String?) {
            uiHandler.post { showToast(VhallSDK.mContext, text) }
        }

        fun showToast(strId: Int) {
            uiHandler.post { showToast(VhallSDK.mContext, strId) }
        }
    }
}