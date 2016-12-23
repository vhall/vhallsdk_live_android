package com.vhall.live.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.vhall.business.HttpDataSource;
import com.vhall.business.utils.LogManager;
import com.vhall.live.R;
import com.vhall.live.main.MainActivity;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }.sendEmptyMessageDelayed(1, 2000);

//        testCode();

    }

    private void testCode() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("access_token", "lalala");
        map.put("user_id", "hahaha");
        HttpDataSource.post("webinar", "start", map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogManager.e("tag", "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogManager.e("tag", "onResponse:" + response.body().string());
            }
        });
    }
}
