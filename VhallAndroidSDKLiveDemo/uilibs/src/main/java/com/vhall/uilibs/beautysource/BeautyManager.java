package com.vhall.uilibs.beautysource;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author hkl
 * Date: 2022/3/3 2:55 下午
 */
public class BeautyManager {

    private static final String SHARED_PREFERENCES_NAME = "vhall_sdk_beauty";

    //获取 当前选择的第几个滤镜
    public static int getFilterSelectFromCatch(Context context) {
        if (context == null) {
            return 1;
        }
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int filterSelect = sp.getInt("filter_select", 1);
        return filterSelect;
    }

    //存储 当前选择的第几个滤镜
    public static void setFilterSelect(int filterSelect, Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("filter_select", filterSelect);
        editor.apply();
    }

    public static boolean getBeautySwitchFromCatch(Context context) {
        if (context == null) {
            return true;
        }
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean beautySwitch = sp.getBoolean("beauty_switch", true);
        return beautySwitch;
    }

    public static void setBeautySwitch(boolean beautySwitch, Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("beauty_switch", beautySwitch);
        editor.apply();
    }

    public static double getBeautyItemNumFromCatch(Context context, String key, double origin) {
        if (context == null) {
            return origin;
        }
        try {
            SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            String num = sp.getString(key, String.valueOf(origin));
            origin = Double.parseDouble(num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return origin;
    }

    //设置当前进度 瘦脸的大小
    public static void setBeautyItemNum(String key, String value, Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }
} 