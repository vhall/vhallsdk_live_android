package com.vhall.live;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.broadcast.BroadcastActivity;
import com.vhall.uilibs.util.CircleImageView;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.watch.WatchActivity;

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
                if (TextUtils.isEmpty(param.userVhallId)) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    TelephonyManager telephonyMgr = (TelephonyManager) MainActivity.this.getSystemService(TELEPHONY_SERVICE);
                    param.userVhallId = "";
                    param.userAvatar = "";
                    param.userName = Build.BRAND + getResources().getString(R.string.phone_user);
                    param.userCustomId = telephonyMgr.getDeviceId();
                    VhallApplication.setParam(param);
                    initPage();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPage();
    }

    private void initPage() {
        param = VhallApplication.param;
        tv_phone.setText(Build.MODEL);
        tv_name.setText(param.userName);
        //Glide.with(this).load(param.userAvatar).placeholder(R.drawable.icon_default_avatar).into(mCircleViewAvatar);
        Glide.with(this).load(param.userAvatar).into(mCircleViewAvatar);
        if (TextUtils.isEmpty(param.userVhallId)) {
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
