package com.vhall.uimodule.base;


import android.os.Bundle;

import com.vhall.uimodule.utils.ToastUtils;


/**
 * @author hkl
 * Date: 2022/12/8 13:58
 */
public interface IBase {
    String INFO_KEY = "webinarInfo";
    String V2_KEY = "isV2";
    String HALF_WATCH_SCREEN_KEY = "half_watch_screen";
    String FULL_WATCH_SCREEN_KEY = "full_watch_screen";
    String HALF_DOC_SCREEN_KEY = "half_doc_screen";
    String FULL_DOC_SCREEN_KEY  = "full_doc_screen";
    String SHOW_INPUT_VIEW_KEY = "show_input_view";
    String HAND_UP_KEY = "hand_up";
    String HAND_UP_KEY_STATUS = "hand_up_status";
    String CUSTOMMSG_KEY = "custom_msg";
    String EXIT_FULL_SCREEN = "exit_full_screen";//退出全屏
    String SHOW_GOODS_CARD = "SHOW_GOODS_CARD";//显示商品卡片
    String SHOW_GOODS_TAB = "SHOW_GOODS_TAB";//显示商品卡片
    String SHOW_GOODS_DETAILS = "SHOW_GOODS_DETAILS";//显示商品详情

    default void showToast(String s) {
        ToastUtils.Companion.showToast(s);
    }

    default void showToast(int s) {
        ToastUtils.Companion.showToast(s);
    }

    default Bundle call(String method, String arg, Object extras) {
        return new Bundle();
    }
} 