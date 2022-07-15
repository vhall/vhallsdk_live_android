package com.vhall.uilibs.interactive;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;

import com.vhall.httpclient.utils.OKHttpUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;

public class RtcInternal {

    public static final int REQUEST_PUSH = 100;
    public static final int REQUEST_ROUND = 101;
    public static String reportUrl = "https://dc.e.vhall.com";

    public static void report(String userId, String webinar_id, Context context) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(webinar_id) || TextUtils.isEmpty(reportUrl)) {
            return;
        }
        String reportHost = String.format("%s/login?k=%s&id=%s&s=%s&token=%s", reportUrl, "606001", "Android" + System.currentTimeMillis(), UUID.randomUUID().toString(), getToken(userId, webinar_id, context));
        Request request = (new Request.Builder()).url(reportHost).get().build();

        OKHttpUtils.createOkClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }


    private static String getToken(String userId, String webinar_id, Context context) {
        JSONObject collectionParam = new JSONObject();
        try {
            collectionParam.put("user_id", userId);
            collectionParam.put("webinar_id", webinar_id);
            collectionParam.put("t_start", dateToString());
            collectionParam.put("os", "10");
            collectionParam.put("device_id", TextUtils.isEmpty(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)) ? "" : Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            collectionParam.put("type", "6");
            collectionParam.put("user_agent", "user_agent");
            collectionParam.put("entry_time", dateToString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String collectionStr = collectionParam.toString();
        return new String(Base64.encode(collectionStr.getBytes(), 2));
    }

    public static final String TIME_PATTERN3 = "yyyy-MM-dd HH:mm:ss";

    public static String dateToString() {
        SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN3);
        try {
            return format.format(Calendar.getInstance());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String dateToString2(String date) {
        SimpleDateFormat format = new SimpleDateFormat(TIME_PATTERN3);
        try {
            Date d2 = format.parse(date);
            int minutes = d2.getMinutes();
            return d2.getHours() + ":" + (minutes <= 9 ? "0" + minutes : minutes);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String dateToString3(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            return format.format(Calendar.getInstance());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 判断是否有网络链接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    public static boolean isGrantedPermissionRtc(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (activity.checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        activity.requestPermissions(new String[]{CAMERA, RECORD_AUDIO}, requestCode);
        return false;
    }


    public static boolean isCheckPermissionRtc(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (activity.checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }



}
