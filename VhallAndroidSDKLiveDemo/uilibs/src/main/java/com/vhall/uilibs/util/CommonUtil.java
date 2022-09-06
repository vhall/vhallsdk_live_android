package com.vhall.uilibs.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;

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


}
