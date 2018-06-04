package com.vhall.live;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.vhall.business.VhallSDK;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.util.CircleImageView;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.broadcast.BroadcastActivity;
import com.vhall.uilibs.watch.WatchActivity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * 主界面的Activity
 */
public class MainActivity extends FragmentActivity {

    TextView tv_phone, tv_name, tv_login;
    CircleImageView mCircleViewAvatar;

    Param param = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        tv_phone = (TextView) this.findViewById(R.id.tv_phone);
        tv_name = (TextView) this.findViewById(R.id.text_name);
        mCircleViewAvatar = (CircleImageView) this.findViewById(R.id.iv_avatar);
        tv_login = (TextView) this.findViewById(R.id.tv_login);

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!VhallSDK.isLogin()) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    VhallSDK.logout();
                    initPage();
                }
            }
        });
        requestPermission();
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT < 23) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) { // 如果之前请求拒绝返回true
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("设置权限");
                alertDialog.setMessage("没有获取摄像头的权限,请去设置中开启").setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                    }
                });
                alertDialog.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) { //PERMISSION_GRANTED 已授权
                    //videoCapture.changeCamera(0);
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "权限" + permissions[i] + "申请失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPage();
    }

    private void initPage() {
        param = VhallApplication.param;
        tv_phone.setText(Build.MODEL);
        tv_name.setText(TextUtils.isEmpty(VhallSDK.getUserNickname()) ? Build.BRAND + getString(R.string.phone_user) : VhallSDK.getUserNickname());
        if (!TextUtils.isEmpty(VhallSDK.getUserAvatar())) {
            Glide.with(this).load(VhallSDK.getUserAvatar()).into(mCircleViewAvatar);
        } else {
            mCircleViewAvatar.setImageDrawable(getResources().getDrawable(R.drawable.icon_default_avatar));
        }
        if (!VhallSDK.isLogin()) {
            tv_login.setText(R.string.login);
        } else {
            tv_login.setText(R.string.logoff);
            mCircleViewAvatar.setBackground(getResources().getDrawable(R.drawable.icon_default_avatar));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void onBroadcastLandspace(View view) {
        Intent intent = new Intent(this, BroadcastActivity.class);
        intent.putExtra("param", param);
        intent.putExtra("ori", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        startActivity(intent);
    }

    public void onBroadcastPortrait(View view) {
        Intent intent = new Intent(this, BroadcastActivity.class);
        intent.putExtra("param", param);
        intent.putExtra("ori", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        startActivity(intent);
    }

    public void onWatchLive(View view) {

        Intent intent = new Intent(this, WatchActivity.class);
        intent.putExtra("param", param);
        intent.putExtra("type", VhallUtil.WATCH_LIVE);
        startActivity(intent);
    }

    public void onWatchPlayback(View view) {
        Intent intent = new Intent(this, WatchActivity.class);
        intent.putExtra("param", param);
        intent.putExtra("type", VhallUtil.WATCH_PLAYBACK);
        startActivity(intent);
    }

    public void onSetParam(View view) {
        Intent intent = new Intent(this, SetParamActivity.class);
        intent.putExtra("param", param);
        startActivity(intent);
    }
}
