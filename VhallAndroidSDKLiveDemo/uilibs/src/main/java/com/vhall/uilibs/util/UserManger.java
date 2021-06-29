package com.vhall.uilibs.util;

import android.text.TextUtils;

/**
 * @author hkl
 * Date: 2019-08-23 11:12
 */
public class UserManger {
    public static String judgePic(String pic) {
        if (TextUtils.isEmpty(pic)) {
            return "";
        }
        if (pic.startsWith("//")) {
            pic = "https:" + pic;
        } else if (!pic.startsWith("http")) {
            pic = "https://t-alistatic01.e.vhall.com/upload/" + pic;
        }
        if (pic.contains("?x-oss-process=image/resize")) {
            return pic;
        }
        return pic + "?x-oss-process=image/resize,w_500,h_250";
    }


    public static boolean isHost(String role) {
        return ("1".equals(role) || "host".equals(role));
    }
}
