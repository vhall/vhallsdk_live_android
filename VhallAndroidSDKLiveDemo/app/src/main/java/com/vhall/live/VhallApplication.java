package com.vhall.live;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.vhall.business.VhallSDK;
import com.vhall.uilibs.Param;
import com.vhall.vhalllive.CameraFilterView;

/**
 * 主Application类
 */
public class VhallApplication extends Application {


    public static Param param;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        getParam();
        VhallSDK.init(this, getResources().getString(R.string.vhall_app_key), getResources().getString(R.string.vhall_app_secret_key));
        VhallSDK.setLogEnable(false);
    }

    public Param getParam() {
        if (param == null) {
            param = new Param();
            SharedPreferences sp = this.getSharedPreferences("set", MODE_PRIVATE);
            TelephonyManager telephonyMgr = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
            param.broId = sp.getString("broid", "");
            param.broToken = sp.getString("brotoken", "");
            param.pixel_type = sp.getInt("pixeltype", CameraFilterView.TYPE_HDPI);
            param.videoBitrate = sp.getInt("videobitrate", 500);
            param.videoFrameRate = sp.getInt("videoframerate", 20);

            param.watchId = sp.getString("watchid", "");
            param.key = sp.getString("key", "");
            param.bufferSecond = sp.getInt("buffersecond", 2);

            param.userVhallId = sp.getString("uservhallid", "");
            param.userCustomId = sp.getString("usercustomid", telephonyMgr.getDeviceId());
            param.userName = sp.getString("username", Build.BRAND + getString(R.string.phone_user));
            param.userAvatar = sp.getString("useravatar", "");

        }
        return param;
    }

    public static void setParam(Param mParam) {
        if (param == null)
            return;
        param = mParam;
        SharedPreferences sp = context.getSharedPreferences("set", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("broid", param.broId);
        editor.putString("brotoken", param.broToken);
        editor.putInt("pixeltype", param.pixel_type);
        editor.putInt("videobitrate", param.videoBitrate);
        editor.putInt("videoframerate", param.videoFrameRate);

        editor.putString("watchid", param.watchId);
        editor.putString("key", param.key);
        editor.putInt("buffersecond", param.bufferSecond);


        editor.putString("uservhallid", param.userVhallId);
        editor.putString("usercustomid", param.userCustomId);
        editor.putString("username", param.userName);
        editor.putString("useravatar", param.userAvatar);

        editor.commit();

    }
}
