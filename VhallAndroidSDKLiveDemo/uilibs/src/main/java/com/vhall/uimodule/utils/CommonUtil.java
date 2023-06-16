package com.vhall.uimodule.utils;

import static android.Manifest.permission.*;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonUtil {


    // 字符串截断
    public static String getLimitString(String source, int length) {
        if (null != source && source.length() > length) {
            return source.substring(0, length) + "...";
        }
        return source;
    }

    public static final String TIME_PATTERN3 = "yyyy-MM-dd HH:mm:ss";


    public static String converLongTimeToStr(long time) {
        if(time<=0)
            return "00:00:00";

        int times = (int)time/1000;
        int hour = times / 3600;
        int minute = (times - hour * 3600) / 60;
        int second = times % 60;
        return String.format("%02d:%02d:%02d",hour,minute,second);
    }

    public static String converLongTimeToStr1(long time) {
        if(time<=0)
            return "00:00";

        int times = (int)time/1000;
        int minute = times / 60;
        int second = times % 60;
        return String.format("%02d:%02d",minute,second);
    }

    public static String converStringTimeToStr(String startTime) {
        try {
            if (TextUtils.isEmpty(startTime)) {
                return "";
            }
            String[] split = startTime.split(":");
            if (split == null) {
                return "";
            }
            if (split.length == 2) {
                startTime = startTime + ":00";
            }
            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            Date start = dateFormat.parse(startTime);
            if (start != null) {
                long time = start.getTime() - now.getTime();
                if (time < 0) {
                    return converLongTimeToStr2(0);
                }
                return converLongTimeToStr2(time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String converChatTime(String startTime) {
        try {
            if (TextUtils.isEmpty(startTime)) {
                return "";
            }
            String[] split = startTime.split(" ");
            return split[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return startTime;
    }

    public static String converLongTimeToStr2(long time) {
        if(time <= 0)
            time = 0;

        int times  = (int)time/1000;
        int day    = times / 3600 / 24;
        int hour   = (times - day*24*3600)/ 3600;
        int minute = (times - day*24*3600 - hour * 3600) / 60;
        int second = times % 60;
        return String.format("距离开播 <font><big><big><big><big>%02d</big></big></big></big>天 <font><big><big><big><big>%02d</big></big></big></big>时 <big><big><big><big>%02d</big></big></big></big>分 <big><big><big><big>%02d</big></big></big></big>秒", day, hour, minute, second);
    }

    public static String changeRoleNameToString(String roleName) {
        if (TextUtils.isEmpty(roleName)) {
            return "观众";
        }
        String role;
        switch (roleName) {
            case "1":
            case "host":
                role = "主持人";
                break;
            case "3":
            case "assistant":
                role = "助理";
                break;
            case "4":
            case "guest":
                role = "嘉宾";
                break;
            default:
                role = "观众";
                break;
        }
        return role;
    }

    public static String changeDefinition(String definition) {
        if (TextUtils.isEmpty(definition)) {
            return "原画";
        }
        String text = definition;
        switch (definition) {
            case "same":
                text = "原画";
                break;
            case "a":
                text = "纯音频";
                break;
            case "360p":
                text = "标清";
                break;
            case "480p":
                text = "高清";
                break;
            case "720p":
                text = "超高清";
                break;
            case "1080p":
                text = "1080p";
                break;
        }
        return text;
    }

    public static boolean isGrantedPermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (activity.checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public static boolean isGrantedAndRequestPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (isGrantedPermission(activity)) {
            return true;
        }
        activity.requestPermissions(new String[]{CAMERA, RECORD_AUDIO}, requestCode);
        return false;
    }

    //Android12蓝牙权限申请
    public static boolean isGrantedAndRequestBlueToothPermission(Activity activity, int requestCode){
        //compileSdkVersion项目中编译SDK版本大于30申请以下权限可使用
        //Manifest.permission.BLUETOOTH_SCAN、Manifest.permission.BLUETOOTH_ADVERTISE、Manifest.permission.BLUETOOTH_CONNECT
        //若小于30可以直接使用权限对应的字符串
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.R){
            if (activity.checkSelfPermission("android.permission.BLUETOOTH_SCAN")!= PackageManager.PERMISSION_GRANTED
                    || activity.checkSelfPermission("android.permission.BLUETOOTH_ADVERTISE")!= PackageManager.PERMISSION_GRANTED
                    || activity.checkSelfPermission("android.permission.BLUETOOTH_CONNECT")!= PackageManager.PERMISSION_GRANTED){
                activity.requestPermissions(new String[]{
                        "android.permission.BLUETOOTH_SCAN",
                        "android.permission.BLUETOOTH_ADVERTISE",
                        "android.permission.BLUETOOTH_CONNECT"}, requestCode);
                return false;
            }
        }
        return true;
    }

    public static boolean isGrantedAndRequestTelPermission(Activity activity, int requestCode){
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            if (activity.checkSelfPermission("android.permission.READ_PHONE_STATE")!= PackageManager.PERMISSION_GRANTED){
                activity.requestPermissions(new String[]{
                        "android.permission.READ_PHONE_STATE"}, requestCode);
                return false;
            }
        }
        return true;
    }

}
