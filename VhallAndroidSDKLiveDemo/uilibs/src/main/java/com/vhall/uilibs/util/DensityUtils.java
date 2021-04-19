package com.vhall.uilibs.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

public class DensityUtils {

    private DensityUtils() {
        throw new AssertionError();
    }

    /**
     * DisplayMetrics
     *
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    /**
     * 屏幕密度,系统源码注释不推荐使用
     */
    public static final float DENSITY = Resources.getSystem()
            .getDisplayMetrics().density;

    /**
     * 屏幕密度
     */
    public static float sDensity = 0f;

    public static float getDensity(Context context) {
        if (sDensity == 0f) {
            sDensity = getDisplayMetrics(context).density;
        }
        return sDensity;
    }

    public static float dpToPx(float dpValue) {
        return dpValue * DENSITY;
    }

    public static float dpToPx(Context context, float dpValue) {
        return dpValue * getDensity(context);
    }

    public static float pxToDp(float pxValue) {
        return pxValue / DENSITY;
    }

    public static int dpToPxInt(float dpValue) {
        return (int) (dpValue * DENSITY + 0.5f);
    }

    public static int pxToDpInt(float pxValue) {
        return (int) (pxValue / DENSITY + 0.5f);
    }

    public static int spToPxInt(float spValue) {
        return (int) (spValue * DENSITY + 0.5f);
    }

    /**
     * 单位转换: dp -> px
     *
     * @param dp
     * @return
     */
    public static int dpToPxInt(Context context, int dp) {
        return (int) (getDensity(context) * dp + 0.5);
    }

    /**
     * 单位转换:px -> dp
     *
     * @param px
     * @return
     */
    public static int pxToDpInt(Context context, int px) {
        return (int) (px / getDensity(context) + 0.5);
    }


    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    /**
     * 屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    /**
     * 屏幕高度
     *
     * @return
     */
    public static int getScreenHeight(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     * @return 状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        // 获得状态栏高度
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    public static boolean isNavigationBarShow(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(context).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static int getNavigationBarHeight(Context context) {
        if (!isNavigationBarShow(context)) {
            return 0;
        }
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static boolean hasNavigationBar() {
        //Emulator
        if (Build.FINGERPRINT.startsWith("generic"))
            return true;

        int id = Resources.getSystem().getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && Resources.getSystem().getBoolean(id);
    }

    public static void hideNavigationBar(Activity activity) {
        if (hasNavigationBar()) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 获取自己的toolbar高度
     */
    public static int getToolbarHeight(Context context) {
        return dpToPxInt(45f);
    }

    /**
     * enable true隐藏顶部状态栏, false显示
     */
    public static void full(Activity activity, boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(lp);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(attr);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
}
