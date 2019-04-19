package com.vhall.live;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.vhall.business.VhallSDK;
import com.vhall.push.VHLivePushFormat;
import com.vhall.uilibs.Param;

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
        VhallSDK.setLogEnable(true);
    }

    public Param getParam() {
        if (param == null) {
            param = new Param();
            SharedPreferences sp = this.getSharedPreferences("set", MODE_PRIVATE);

            param.broId = sp.getString("broid", "465735486");
            param.broToken = sp.getString("brotoken", "64195372a716a8917700cba538a34b37");
            param.pixel_type = sp.getInt("pixeltype", VHLivePushFormat.PUSH_MODE_HD);
            param.videoBitrate = sp.getInt("videobitrate", 500);
            param.videoFrameRate = sp.getInt("videoframerate", 15);

            param.watchId = sp.getString("watchid", "943462262"); // 412768506  465735486(正式)
            param.key = sp.getString("key", "");
            param.bufferSecond = sp.getInt("buffersecond", 6);
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

        editor.commit();

    }
}
