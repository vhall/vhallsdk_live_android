package com.vhall.live.login;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.vhall.live.R;
import com.vhall.live.utils.ActivityUtils;


/**
 * 登录界面的Activity
 */
public class LoginActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.login_Frame);
        if (loginFragment == null) {
            loginFragment = LoginFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    loginFragment, R.id.login_Frame);
        }

        new LoginPresenter(loginFragment);
    }
}
