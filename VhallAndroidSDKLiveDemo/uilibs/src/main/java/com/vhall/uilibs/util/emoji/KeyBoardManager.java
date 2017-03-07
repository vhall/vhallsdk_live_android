package com.vhall.uilibs.util.emoji;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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
}
