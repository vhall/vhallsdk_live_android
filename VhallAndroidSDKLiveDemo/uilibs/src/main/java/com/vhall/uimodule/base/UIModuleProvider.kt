package com.vhall.uimodule.base

/**
 * @author：jooper  Email：jooperge@163.com
 * 描述：
 * 修改历史:
 * <p>
 * 创建于： 2023/3/1
 */
open class UIModuleProvider {
    companion object {
        private var mUIModuleProxy: IUIModuleProxy? = null
        fun inject(uimodule: IUIModuleProxy?) {
            mUIModuleProxy = uimodule
        }

        internal fun doSignConfig() {
            mUIModuleProxy?.apply {
                onSignatureConfig()
            }
        }
    }
}