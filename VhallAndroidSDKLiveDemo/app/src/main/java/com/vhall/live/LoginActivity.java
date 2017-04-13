package com.vhall.live;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import android.widget.EditText;
import android.widget.Toast;

import com.vhall.business.VhallSDK;
import com.vhall.business.data.UserInfo;
import com.vhall.business.data.source.UserInfoDataSource;
import com.vhall.uilibs.Param;

/**
 * 登录界面
 */
public class LoginActivity extends Activity {

    public static final String TAG = "LoginActivity";
    private EditText mTextInputUsername;
    private EditText mTextInputPassword;
    public AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mTextInputUsername = (EditText) this.findViewById(R.id.text_input_username);
        mTextInputPassword = (EditText) this.findViewById(R.id.text_input_password);
    }

    public void login(String username, String userpass) {
        VhallSDK.getInstance().login(username, userpass, new UserInfoDataSource.UserInfoCallback() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                if (userInfo != null) {
                    Param param = VhallApplication.param;
                    param.userAvatar = userInfo.avatar;
                    param.userVhallId = userInfo.user_id;
                    param.userCustomId = userInfo.account;
                    param.userName = userInfo.nick_name;
                    VhallApplication.setParam(param);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onError(int errorCode, String reason) {
                Toast.makeText(LoginActivity.this, "" + reason, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "errorCode:" + errorCode + " errorReason:" + reason);
            }
        });
    }


    public void loginClick(View view) {
        checkUserInfo();
    }

    public void registClick(View view) {
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
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void checkUserInfo() {
        String username = mTextInputUsername.getText().toString();
        String password = mTextInputPassword.getText().toString();
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            login(username, password);
        }
    }
}
