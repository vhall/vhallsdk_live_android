package com.vhall.live.vhall

import android.annotation.SuppressLint
import android.content.Context
import com.vhall.business.VhallSDK
import com.vhall.business.utils.SignatureUtil
import com.vhall.uimodule.dao.UserDataStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * @author hkl
 * Date: 2022/12/27 11:03
 */
object EvConfig {
    @SuppressLint("ServiceCast")
    fun setEnvironment(
        context: Context
    ) {
        VhallSDK.setPackageCheck(
            SignatureUtil.getPackageName(context),
            SignatureUtil.getSignatureSHA1(context)
        )

        GlobalScope.launch {
            EvConfigProvider.appKey = UserDataStore.getAppKey(context)
            EvConfigProvider.appSecretKey = UserDataStore.getAppSecret(context)
            EvConfigProvider.doConfigEnv(context)
        }
    }
}