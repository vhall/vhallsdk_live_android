package com.vhall.uilibs.util;


import com.vhall.business.WatchLive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用方法工具类
 */
public class VhallUtil {

    public static final int BROADCAST = 0x00;
    public static final int WATCH_LIVE = 0x01;
    public static final int WATCH_PLAYBACK = 0x02;

//    public static String getFixType(int type) {
//        String typeStr = "";
//        switch (type) {
//            case WatchLive.FIT_DEFAULT:
//                typeStr = "FIT_DEFAULT";
//                break;
//            case WatchLive.FIT_CENTER_INSIDE:
//                typeStr = "FIT_CENTER_INSIDE";
//                break;
//            case WatchLive.FIT_X:
//                typeStr = "FIT_X";
//                break;
//            case WatchLive.FIT_Y:
//                typeStr = "FIT_Y";
//                break;
//            case WatchLive.FIT_XY:
//                typeStr = "FIT_XY";
//                break;
//            default:
//                break;
//        }
//        return typeStr;
//    }

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

    public static boolean IsPhone(String phone) {
        Pattern pattern = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }
}
