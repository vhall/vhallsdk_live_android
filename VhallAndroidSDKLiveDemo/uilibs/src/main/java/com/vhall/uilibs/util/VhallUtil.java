package com.vhall.uilibs.util;


import android.content.Context;


/**
 * 常用方法工具类
 */
public class VhallUtil {

    public static final int BROADCAST = 0x00;
    public static final int WATCH_LIVE = 0x01;
    public static final int WATCH_PLAYBACK = 0x02;
    private static final String TAG = "VhallUtil";

    /**
     * 将长整型值转化成字符串
     *
     * @param time
     * @return
     */
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
        if (hour > 0) {
            return strHour + ":" + strMinute + ":" + strSecond;
        } else {
            return "00:" + strMinute + ":" + strSecond;
        }
    }

    public static int converTimeStrToSecond(String time) {
        String s = time;
        int index1 = s.indexOf(":");
        int index2 = s.indexOf(":", index1 + 1);
        int hour = Integer.parseInt(s.substring(0, index1));
        int minute = Integer.parseInt(s.substring(index1 + 1, index2));
        int second = Integer.parseInt(s.substring(index2 + 1));
        return hour * 60 * 60 + minute * 60 + second;
    }

    /**
     * 将dp值转换为px值
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
