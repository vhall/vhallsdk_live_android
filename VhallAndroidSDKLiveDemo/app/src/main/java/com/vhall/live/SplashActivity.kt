package com.vhall.live

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.vhall.business.VhallSDK
import com.vhall.live.databinding.ActivitySplashBinding
import com.vhall.uimodule.login.LoginActivity
import com.vhall.uimodule.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private val mViewBinding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)
        lifecycleScope.launch {
            delay(2000)
            val intent: Intent = if (!VhallSDK.isLogin())
                Intent(this@SplashActivity, LoginActivity::class.java)
            else
                Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}