package com.vhall.live.vhall

import android.content.Context
import com.vhall.business.VhallSDK

/**
 * @author：vhall  Email：jooperge@163.com
 * 描述：
 * 修改历史:
 * <p>
 * 创建于： 2023/2/9
 */
class EvConfigProvider {

    companion object {
        var appKey: String? = "申请的 appKey"
        var appSecretKey: String? = "申请的 appSecretKey"
        var rsaPrivateKey: String? = null
        fun doConfigEnv(context: Context) {
            VhallSDK.setRsaPrivateKey(rsaPrivateKey)
            VhallSDK.init(
                context,
                appKey,
                appSecretKey
            )
        }
    }
}