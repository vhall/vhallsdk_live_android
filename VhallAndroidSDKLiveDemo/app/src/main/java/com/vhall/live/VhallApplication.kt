package com.vhall.live

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import com.vhall.business.VhallSDK
import com.vhall.httpclient.api.VHNetApi
import com.vhall.httpclient.core.VHGlobalConfig
import com.vhall.live.vhall.EvConfig.setEnvironment
import com.vhall.logmanager.VLog
import com.vhall.uimodule.base.IUIModuleProxy
import com.vhall.uimodule.base.UIModuleProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 主Application类
 */
class VhallApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
        initVhallSdk()
        UIModuleProvider.inject(object : IUIModuleProxy {
            override fun onSignatureConfig() {
                GlobalScope.launch {
                    setEnvironment(this@VhallApplication)
                }
            }
        })
    }

    private fun initVhallSdk() {
        if (isAppProcess) {
            VhallSDK.setRsaPrivateKey(RSA_PRIVATE_KEY)

            GlobalScope.launch {
                setEnvironment(this@VhallApplication)
            }
            val vhGlobalConfig = VHGlobalConfig.Builder()
                .setEnableLog(true)
                .build()
            VHNetApi.getNetApi().setGlobalConfig(vhGlobalConfig)
            VLog.setLogLevel(VLog.LogLevel.VERBOSE)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    /**
     * 判断该进程是否是app进程
     *
     */
    private val isAppProcess: Boolean
        get() {
            val processName = processName
            return processName != null && processName.equals(
                this.packageName,
                ignoreCase = true
            )
        }

    companion object {
        var context: Context? = null
        const val RSA_PRIVATE_KEY = "如果需要，请从控制台获取"

        /**
         * 获取运行该方法的进程的进程名
         *
         * @return 进程名称
         */
        val processName: String?
            get() {
                val processId = Process.myPid()
                var processName: String? = null
                val manager = context!!.getSystemService(ACTIVITY_SERVICE) as ActivityManager
                val iterator: Iterator<*> = manager.runningAppProcesses.iterator()
                while (iterator.hasNext()) {
                    val processInfo = iterator.next() as ActivityManager.RunningAppProcessInfo
                    try {
                        if (processInfo.pid == processId) {
                            processName = processInfo.processName
                            return processName
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return processName
            }
    }
}