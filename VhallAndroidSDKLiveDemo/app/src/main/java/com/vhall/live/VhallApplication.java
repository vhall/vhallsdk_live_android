package com.vhall.live;

import android.app.Application;

import com.vhall.business.VhallSDK;

/**
 * 主Application类
 */
public class VhallApplication extends Application {

    private static VhallApplication app;
    public static String user_vhall_id = "";
    public static String user_customer_id = "";

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        VhallSDK.init(this, getResources().getString(R.string.vhall_app_key), getResources().getString(R.string.vhall_app_secret_key));
    }

    public static VhallApplication getApp() {
        return app;
    }

}
