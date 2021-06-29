package com.vhall.uilibs.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.vhall.business.VhallSDK;


/**
 * @author hkl
 */

public class ToastUtil {

    private static Handler uiHandler = new Handler(Looper.getMainLooper());
    public static void showToast(Context context, String message) {
        if(context == null){
            return;
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public static void showToast(Context context, int message) {
        if(context == null){
            return;
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public static void showToast(final String text){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                showToast(VhallSDK.mContext,text);
            }
        });
    }

    public static void showToast(final int strId){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                showToast(VhallSDK.mContext,strId);
            }
        });
    }
}
