package com.vhall.uilibs.util;

import android.content.Context;
import android.graphics.Point;
import android.text.InputFilter;
import android.view.Display;
import android.view.WindowManager;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseUtil {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 观看人数，点赞数处理
     *
     * @param number
     * @return
     */
    public static String intChange2Str(int number) {
        String str = "";
        if (number <= 0) {
            str = "1";
        } else if (number < 10000) {
            str = String.valueOf(number);
        } else {
            double d = (double) number;
            double num = d / 10000;//1.将数字转换成以万为单位的数字
            BigDecimal b = new BigDecimal(num);
            double f1 = b.setScale(2, BigDecimal.ROUND_FLOOR).doubleValue();
            DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
            str = decimalFormat.format(f1) + "w";
        }
        return str;
    }


    // 字符串截断
    public static String getLimitString(String source, int length) {
        if (null != source && source.length() > length) {
//            int reallen = 0;
            return source.substring(0, length) + "...";
        }
        return source;
    }
    // 字符串截断
    public static String getLimitString(String source) {
        if (null != source && source.length() > 6) {
//            int reallen = 0;
            return source.substring(0, 6) + "...";
        }
        return source;
    }

    // 字符串截断
    public static String getLimitStringWithoutNode(String source, int length) {
        if (null != source && source.length() > length) {
            return source.substring(0, length);
        }
        return source;
    }

    public static String getLimitStringDim(String source) {
        if (null != source && source.length() >= 2) {
            return source.charAt(0) + "***" + source.charAt(source.length() - 1);
        }
        return source;
    }


    public static String timestamp2Date(String str_num) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (str_num.length() == 13) {
            String date = sdf.format(new Date(toLong(str_num)));
            return date;
        } else {
            String date = sdf.format(new Date(toInt(str_num) * 1000L));
            return date;
        }
    }

    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    public static int toInt(String obj) {
        if (obj == null) {
            return 0;
        }
        return Integer.parseInt(obj);
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int screenHeight;
        Point point = new Point();
        defaultDisplay.getSize(point);
        screenHeight = point.y;
        return screenHeight;
    }

    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int screenWidth;
        Point point = new Point();
        defaultDisplay.getSize(point);
        screenWidth = point.x;
        return screenWidth;
    }
    public static void setEditTextLengthLimit(EditText editText, int length) {
        editText.setFilters( new InputFilter[]{new InputFilter.LengthFilter(length)});
    }
}
