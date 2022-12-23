package com.vhall.uilibs.util;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.FrameLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonUtil {

    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static String converLongTimeToStr(long time) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;

        long hour = (time) / hh;
        long minute = (time - hour * hh) / mi;
        long second = (time - hour * hh - minute * mi) / ss;

        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;
        return strHour + ":" + strMinute + ":" + strSecond;
    }

    public static String converTimeToStr(long time) {
        int mi = 60;
        long minute = (time) / mi;
        long second = (time - minute * mi);

        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;
        if (minute > 0)
            return strMinute + "分" + strSecond + "秒";
        else
            return strSecond + "秒";
    }

    public String showLikeNum(int num) {
        String showNum;
        if (num < 1000) {
            showNum = String.valueOf(num);
        } else if (num < 10000) {
            int num1 = num / 100;
            showNum = ((double) num1 / 10) + "K";
        } else if (num < 100000) {
            int num1 = num / 1000;
            showNum = ((double) num1 / 10) + "W";
        } else if (num < 1000000) {
            showNum = (num / 10000) + "W";
        } else {
            showNum = ("100W");
        }
        return showNum;
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

    public static String converLongTimeToStr2(long time) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int da = hh * 24;

        long day = (time) / da;
        long hour = (time - da * day) / hh;
        long minute = (time - da * day - hour * hh) / mi;
        long second = (time - da * day - hour * hh - minute * mi) / ss;

        String strDay = day < 10 ? "0" + day : "" + day;
        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;

        return String.format("距离开播 <font><big><big><big><big>%s</big></big></big></big>天 <font><big><big><big><big>%s</big></big></big></big>时 <big><big><big><big>%s</big></big></big></big>分 <big><big><big><big>%s</big></big></big></big>秒", strDay, strHour, strMinute, strSecond);
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

    public static String examConverTimeToStr(String pushTime) {
        try {
            long time = Long.parseLong(pushTime)*60;
            int mi = 60;
            long minute = (time) / mi;
            long second = (time - minute * mi);

            String strMinute = minute < 10 ? "0" + minute : "" + minute;
            String strSecond = second < 10 ? "0" + second : "" + second;
            if (minute > 0)
                return strMinute + "：" + strSecond;
            else
                return strSecond;
        } catch (Exception e) {
            return pushTime;
        }
    }
}
