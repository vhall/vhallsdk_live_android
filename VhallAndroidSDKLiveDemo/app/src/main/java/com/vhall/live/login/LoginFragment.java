package com.vhall.live.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vhall.live.R;
import com.vhall.live.VhallApplication;

/**
 * 登陆界面的Fragment
 */
public class LoginFragment extends Fragment implements LoginContract.View, View.OnClickListener {

    private LoginContract.Presenter mPresenter;
    private EditText et_login_username;
    private EditText et_login_userpass;
    private Button login;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        login = (Button) this.getView().findViewById(R.id.btn_login);
        login.setOnClickListener(this);
        et_login_username = (EditText) this.getView().findViewById(R.id.et_login_name);
        et_login_userpass = (EditText) this.getView().findViewById(R.id.et_login_pass);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                String userName = et_login_username.getText().toString();
                String password = et_login_userpass.getText().toString();
                mPresenter.login(userName, password);
                break;
            default:
                break;
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(VhallApplication.getApp(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void backReslt() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

}
