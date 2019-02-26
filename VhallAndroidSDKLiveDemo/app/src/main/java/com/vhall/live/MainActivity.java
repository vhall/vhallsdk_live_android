package com.vhall.live;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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

/**
 * 主界面的Activity
 */
public class MainActivity extends FragmentActivity {
    TextView tv_phone, tv_name, tv_login;
    CircleImageView mCircleViewAvatar;
    Param param = null;

    private static final int REQUEST_PERMISSIONS = 1;
    public String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        tv_phone = this.findViewById(R.id.tv_phone);
        tv_name = this.findViewById(R.id.text_name);
        mCircleViewAvatar = this.findViewById(R.id.iv_avatar);
        tv_login = this.findViewById(R.id.tv_login);

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
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
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
        param.screenOri = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        intent.putExtra("param", param);
        startActivity(intent);
    }

    public void onBroadcastPortrait(View view) {
        Intent intent = new Intent(this, BroadcastActivity.class);
        param.screenOri = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        intent.putExtra("param", param);
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
