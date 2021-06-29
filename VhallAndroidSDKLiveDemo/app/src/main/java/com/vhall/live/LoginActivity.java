package com.vhall.live;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.VhallSDK;
import com.vhall.business.data.ILoginRequest;
import com.vhall.business.data.UserInfo;
import com.vhall.business.data.source.UserInfoDataSource;

/**
 * 登录界面
 */
public class LoginActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "LoginActivity";
    private EditText mTextInputUsername;
    private EditText mTextInputPassword;
    public AlertDialog alertDialog;
    private TextView mVerTextView;
    private RadioButton account_login, id_login;
    private RadioGroup login_type_container;
    private EditText head_net_url, third_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mTextInputUsername = this.findViewById(R.id.text_input_username);
        mTextInputPassword = this.findViewById(R.id.text_input_password);
        mVerTextView = this.findViewById(R.id.label_ver);
        account_login = this.findViewById(R.id.account_login);
        id_login = this.findViewById(R.id.id_login);
        head_net_url = this.findViewById(R.id.head_net_url);
        third_id = this.findViewById(R.id.third_id);
        login_type_container = this.findViewById(R.id.login_type_container);
        mVerTextView.setText(VhallSDK.getVersion());

        login_type_container.setOnCheckedChangeListener(this::onCheckedChanged);
        mWaitingDialog = new ProgressDialog(this);
        mWaitingDialog.setTitle("提示");
        mWaitingDialog.setMessage("请稍后...");
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.account_login) {
            head_net_url.setVisibility(View.INVISIBLE);
            third_id.setVisibility(View.GONE);
            mTextInputUsername.setVisibility(View.VISIBLE);
            mTextInputPassword.setVisibility(View.VISIBLE);
        } else if (checkedId == R.id.id_login) {
            head_net_url.setVisibility(View.VISIBLE);
            third_id.setVisibility(View.VISIBLE);
            mTextInputUsername.setVisibility(View.VISIBLE);
            mTextInputPassword.setVisibility(View.GONE);
        }
    }
    private ProgressDialog mWaitingDialog;

    public void login(String username, String userPass) {
        VhallSDK.login(username, userPass, new LoginCallbackInternal(userPass));
    }

    private class LoginCallbackInternal implements UserInfoDataSource.UserInfoCallback {

        private String userPass;

        public LoginCallbackInternal() {
        }

        public LoginCallbackInternal(String userPass) {
            this.userPass = userPass;
        }

        @Override
        public void onSuccess(UserInfo userInfo) {
            Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
            VhallApplication.param.key = userPass;
            skipMain();
            mWaitingDialog.cancel();
        }

        @Override
        public void onError(int errorCode, String reason) {
            Toast.makeText(LoginActivity.this, reason, Toast.LENGTH_SHORT).show();
            mWaitingDialog.cancel();
        }
    }

//    /**
//     * 判断是否缺少权限
//     */
//    private boolean lacksPermission() {
//        return ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.CAMERA) ==
//                PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.RECORD_AUDIO) ==
//                PackageManager.PERMISSION_DENIED;
//    }

    public void loginClick(View view) {
        mWaitingDialog.show();
        if (account_login.isChecked()) {
            checkUserInfo();
        } else {
            loginByThirdId();
        }

    }

    public void registerClick(View view) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.regist_phone_number, getString(R.string.phone_number)));
            builder.setPositiveButton(R.string.call, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Uri uri = Uri.parse(getString(R.string.phone_number_uri, getString(R.string.phone_number)));
                    Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog = builder.create();
        }
        alertDialog.show();
    }

    public void customerClick(View view) {
        skipMain();
    }

    private void skipMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void loginByThirdId() {
        VhallSDK.loginByThirdId(third_id.getText().toString(), mTextInputUsername.getText().toString(), head_net_url.getText().toString(), new LoginCallbackInternal());
    }


    public void checkUserInfo() {
        String username = mTextInputUsername.getText().toString();
        String password = mTextInputPassword.getText().toString();
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            login(username, password);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

}
