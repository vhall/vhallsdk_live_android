package com.vhall.live;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.VhallSDK;
import com.vhall.business.data.UserInfo;
import com.vhall.business.data.source.UserInfoDataSource;

public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";
    private EditText mTextInputUsername;
    private EditText mTextInputPassword;
    public AlertDialog alertDialog;
    private TextView mVerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mTextInputUsername = this.findViewById(R.id.text_input_username);
        mTextInputPassword = this.findViewById(R.id.text_input_password);
        mVerTextView       = this.findViewById(R.id.label_ver);
        mVerTextView.setText(VhallSDK.getVersion());
    }

    public void login(String username, String userpass) {
        VhallSDK.login(username, userpass, new UserInfoDataSource.UserInfoCallback() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                VhallApplication.param.key=userpass;
                skipMain();
            }

            @Override
            public void onError(int errorCode, String reason) {
                Toast.makeText(LoginActivity.this, reason, Toast.LENGTH_SHORT).show();
            }
        });
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
        skipMain();
    }

    private void skipMain() {
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

}
