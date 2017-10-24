package com.vhall.live;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.vhall.business.VhallSDK;

/**
 * 程序启动页的Activity
 */
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Intent intent;
                if (VhallSDK.isLogin()) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }.sendEmptyMessageDelayed(1, 2000);
    }
}
