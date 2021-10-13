package com.vhall.live;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import com.vhall.business.ErrorCode;
import com.vhall.business.VhallSDK;
import com.vhall.business.core.IVHSDKListener;
import com.vhall.push.VHLivePushFormat;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.util.ToastUtil;
import java.util.Iterator;


public class VhallApplication extends MultiDexApplication {

    private final static String RSA_PRIVATE_KEY = "RSA 私钥";

    public static Param param;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        getParam();

        if (isAppProcess()) {
            if (isAppProcess()) {
                VhallSDK.setLogEnable(false);
                //RSA 校验 必须设置否则校验不通过 从控制台 获取
                VhallSDK.setRsaPrivateKey(RSA_PRIVATE_KEY);
                VhallSDK.init(this, "appKey", "appSecretKey");
            }
        }

        VhallSDK.addVHListener(mVHListener);
    }


    /**
     * 要求 必须通过全局变量进行注册   内部类注册可能会被回收并且接收不到重要事件回调
     */
    private IVHSDKListener mVHListener = (code, tips, args, bundle) -> {
        if(ErrorCode.ERROR_TOKEN_EXPIRE == code){
            // TODO: 6/29/21 可以尝试重新登陆SDK
            ToastUtil.showToast("抱歉token 已过期,请重新登陆");
        }
    };



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Param getParam() {
        param = new Param();
        SharedPreferences sp = context.getSharedPreferences("set", MODE_PRIVATE);

        //发直播，直播间ID
        param.broId = sp.getString("broid", "465735486");
        //直播时候修改昵称
        param.broName = sp.getString("broname", VhallSDK.getUserName());
        //发直播token
        param.broToken = sp.getString("brotoken", "8734e1c56b8b5b6f1f4ce1b1c072121a");
        //直播分辨率类型
        param.pixel_type = sp.getInt("pixeltype", VHLivePushFormat.PUSH_MODE_HD);
        //发直播视频码率
        param.videoBitrate = sp.getInt("videobitrate", 500);
        //发直播视频帧率
        param.videoFrameRate = sp.getInt("videoframerate", 15);
        //看直播，直播间ID
        param.watchId = sp.getString("watchid", "673590744"); // 412768506  465735486(正式)
        //直播间密码
        param.key = sp.getString("key", "");
        //缓冲时长
        param.bufferSecond = sp.getInt("buffersecond", 6);
        param.guestAvatar = sp.getString("guestAvatar", "https://t-alistatic01.e.vhall.com/upload/users/face-imgs/67/cf/67cf18a4250bc48ec9d1eb3ed82b741d.gif");

        return param;
    }

    public static void setParam(Param mParam) {
        if (param == null) {
            return;
        }
        param = mParam;
        SharedPreferences sp = context.getSharedPreferences("set", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("broid", param.broId);
        editor.putString("brotoken", param.broToken);
        editor.putString("guestAvatar", param.guestAvatar);
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
