package com.vhall.live.login;

import com.vhall.live.BasePresenter;
import com.vhall.live.BaseView;

/**
 * 登录界面的View接口类
 */
public class LoginContract {
    interface View extends BaseView<Presenter> {
        void backReslt();

        void showToast(String message);
    }

    interface Presenter extends BasePresenter {
        void login(String username, String userpass);
    }
}
