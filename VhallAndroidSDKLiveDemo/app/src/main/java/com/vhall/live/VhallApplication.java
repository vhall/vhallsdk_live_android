package com.vhall.live;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.vhall.business.VhallSDK;
import com.vhall.push.VHLivePushFormat;
import com.vhall.uilibs.Param;

import java.util.Iterator;

import vhall.com.vss.VssSdk;

import static com.vhall.business.VhallSDK.getUserId;

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
        VhallSDK.setLogEnable(true);

        VhallSDK.init(this, getResources().getString(R.string.vhall_app_key), getResources().getString(R.string.vhall_app_secret_key));
        VssSdk.getInstance().init(getApplicationContext(), getUserId());

        if (isAppProcess()) {
        }
    }

    public Param getParam() {
        if (param == null) {
            param = new Param();
            SharedPreferences sp = this.getSharedPreferences("set", MODE_PRIVATE);

            //发直播，直播间ID
            param.broId = sp.getString("broid", "465735486");
            //发直播token
            param.broToken = sp.getString("brotoken", "8734e1c56b8b5b6f1f4ce1b1c072121a");
            //直播分辨率类型
            param.pixel_type = sp.getInt("pixeltype", VHLivePushFormat.PUSH_MODE_HD);
            //发直播视频码率
            param.videoBitrate = sp.getInt("videobitrate", 500);
            //发直播视频帧率
            param.videoFrameRate = sp.getInt("videoframerate", 15);
            //看直播，直播间ID
            param.watchId = sp.getString("watchid", "943462262"); // 412768506  465735486(正式)
            //直播间密码
            param.key = sp.getString("key", "");
            //缓冲时长
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

    /**
     * 判断该进程是否是app进程
     *
     * @return
     */
    public boolean isAppProcess() {
        String processName = getProcessName();
        if (processName == null || !processName.equalsIgnoreCase(this.getPackageName())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取运行该方法的进程的进程名
     *
     * @return 进程名称
     */
    public static String getProcessName() {
        int processId = android.os.Process.myPid();
        String processName = null;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Iterator iterator = manager.getRunningAppProcesses().iterator();
        while (iterator.hasNext()) {
            ActivityManager.RunningAppProcessInfo processInfo = (ActivityManager.RunningAppProcessInfo) (iterator.next());
            try {
                if (processInfo.pid == processId) {
                    processName = processInfo.processName;
                    return processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return processName;
    }

}
