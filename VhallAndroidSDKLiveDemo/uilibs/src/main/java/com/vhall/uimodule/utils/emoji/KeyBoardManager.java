package com.vhall.uimodule.utils.emoji;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Method;


/**
 * 打开或关闭软键盘
 */
public class KeyBoardManager {
    /**
     * 打卡软键盘
     *
     * @param mEditText 输入框
     * @param mContext  上下文
     */
    public static void openKeyboard(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
//		imm.showSoftInput(mEditText,InputMethodManager.SHOW_FORCED);
    }

    /**
     * 关闭软键盘
     *
     * @param mEditText 输入框
     * @param mContext  上下文
     */
    public static void closeKeyboard(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    public static int getKeyboardHeight(Context context) {
        SharedPreferences sp = context.getSharedPreferences("vhall", Context.MODE_PRIVATE);
        return sp.getInt("keyboard_height", 0);
    }

    public static void setKeyboardHeight(Context context, int height) {
        SharedPreferences sp = context.getSharedPreferences("vhall", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("keyboard_height", height);
        editor.commit();
    }

    public static int getKeyboardHeightLandspace(Context context) {
        SharedPreferences sp = context.getSharedPreferences("vhall", Context.MODE_PRIVATE);
        return sp.getInt("keyboard_height_landspace", 0);
    }

    public static void setKeyboardHeightLandspace(Context context, int height) {
        SharedPreferences sp = context.getSharedPreferences("vhall", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("keyboard_height_landspace", height);
        editor.commit();
    }

    /**
     * 获取虚拟按键的高度
     * @param context 上下文
     * @return
     */
    public static int getVirtualButtonHeight(Context context) {
        int vh = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - display.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

    /**
     * 判断是否存在虚拟按键 例如小米MIX2(全面屏)
     * @param context 上下文
     * @return
     */
    public static boolean hasVirtualButton(Context context) {
        Resources res = context.getResources();
        boolean hasVirtral;
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            hasVirtral = res.getBoolean(resourceId);
            String navBarOverride = getNavBarOverride();
            if (navBarOverride.equals(1)) {
                hasVirtral = false;
            } else if (navBarOverride.equals(0)) {
                hasVirtral = true;
            }
            return hasVirtral;
        } else {
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    /**
     * 判断虚拟按键栏是否重写
     * @return
     */
    private static String getNavBarOverride() {
        String sNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
            }
        }
        return sNavBarOverride;
    }

    /**
     * 获取虚拟按键的高度
     *
     * @param context 上下文
     * @return
     */
    public static int getVirtualButtonHeightDoc(Context context) {
        int vh = 0;
        if (!hasVirtualButtonDoc((Activity) context)) {
            return 0;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - display.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

    private static final String NAVIGATION = "navigationBarBackground";

    /**
     * 判断是否存在虚拟按键 例如小米MIX2(全面屏)
     *
     * @param activity 上下文
     * @return
     */
    public static boolean hasVirtualButtonDoc(Activity activity) {
        ViewGroup vp = (ViewGroup) activity.getWindow().getDecorView();
        if (vp != null) {
            for (int i = 0; i < vp.getChildCount(); i++) {
                vp.getChildAt(i).getContext().getPackageName();
                if (vp.getChildAt(i).getId() != -1 && NAVIGATION.equals(activity.getResources().getResourceEntryName(vp.getChildAt(i).getId()))) {
                    return true;
                }
            }
        }
        return false;
    }



}
