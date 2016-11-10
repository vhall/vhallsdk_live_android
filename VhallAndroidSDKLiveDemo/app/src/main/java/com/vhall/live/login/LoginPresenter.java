package com.vhall.live.login;


import com.vhall.business.VhallSDK;
import com.vhall.live.VhallApplication;

/**
 * 登陆界面的Presenter
 */
public class LoginPresenter implements LoginContract.Presenter {
    private LoginContract.View loginView;

    public LoginPresenter(LoginContract.View view) {
        loginView = view;
        loginView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void login(String username, String userpass) {
        VhallSDK.getInstance().login(username, userpass, new VhallSDK.LoginResponseParamCallback() {
            @Override
            public void success(String vhall_id, String customer_id) {
                VhallApplication.user_vhall_id = vhall_id;
                VhallApplication.user_customer_id = customer_id;
                loginView.backReslt();
            }

            @Override
            public void failed(int errorCode, String reason) {
                loginView.showToast(reason);
            }
        });
    }
}
