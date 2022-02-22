package com.vhall.live;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.vhall.business.VhallSDK;
import com.vhall.business.utils.VHInternalUtils;
import com.vhall.push.VHLivePushFormat;
import com.vhall.uilibs.Param;

/**
 * 主界面的Activity
 */
public class SetParamActivity extends FragmentActivity {
    Param param;
    EditText et_bro_token, et_bro_name, et_bro_id, et_video_bitrate, et_video_framerate, et_watch_id, et_key, et_buffersecond, et_guest_pwd, et_guest_avatar;
    TextView et_userid, et_usernickname;
    RadioGroup rg_type;
    RadioButton rb_hdpi, rb_xhdpi, radioButtonHD, radioButtonSD, radioButtonUHD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_activity);
        param = VhallApplication.param;
        et_bro_token = (EditText) this.findViewById(R.id.et_bro_token);
        et_bro_name = (EditText) this.findViewById(R.id.et_bro_name);
        et_bro_id = (EditText) this.findViewById(R.id.et_bro_id);
        et_video_bitrate = (EditText) this.findViewById(R.id.et_video_bitrate);
        et_video_framerate = (EditText) this.findViewById(R.id.et_video_framerate);
        et_guest_pwd = (EditText) this.findViewById(R.id.et_guest_pwd);
        et_guest_avatar = (EditText) this.findViewById(R.id.et_guest_avatar);
        et_watch_id = (EditText) this.findViewById(R.id.et_watch_id);
        et_key = (EditText) this.findViewById(R.id.et_key);
        et_buffersecond = (EditText) this.findViewById(R.id.et_buffersecond);
        et_userid = (TextView) this.findViewById(R.id.et_userid);
        et_usernickname = (TextView) this.findViewById(R.id.et_usernickname);

        rg_type = (RadioGroup) this.findViewById(R.id.rg_type);
        rb_hdpi = (RadioButton) this.findViewById(R.id.rb_hdpi);
        rb_xhdpi = (RadioButton) this.findViewById(R.id.rb_xhdpi);

        radioButtonHD = this.findViewById(R.id.interactive_param_hd);
        radioButtonSD = this.findViewById(R.id.interactive_param_sd);
        radioButtonUHD = this.findViewById(R.id.interactive_param_uhd);
        radioButtonSD.isChecked();
    }

    @Override
    protected void onResume() {
        super.onResume();
        param = VhallApplication.getParam();
        initData();
    }

    @SuppressLint("MissingPermission")
    private void initData() {
        et_bro_token.setText(param.broToken);
        et_guest_pwd.setText(param.guestPwd);
        et_bro_id.setText(param.broId);
        et_bro_name.setText(param.broName);
        et_guest_avatar.setText(param.guestAvatar);
        et_video_bitrate.setText(String.valueOf(param.videoBitrate));
        et_video_framerate.setText(String.valueOf(param.videoFrameRate));
        et_watch_id.setText(param.watchId);
        et_key.setText(param.key);
        et_buffersecond.setText(String.valueOf(param.bufferSecond));
        TelephonyManager telephonyMgr = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        et_userid.setText(TextUtils.isEmpty(VhallSDK.getUserName()) ? telephonyMgr.getDeviceId() : VhallSDK.getUserName());
        et_usernickname.setText(TextUtils.isEmpty(VhallSDK.getUserNickname()) ? Build.BRAND + getString(R.string.phone_user) : VhallSDK.getUserNickname());
        if (param.pixel_type == VHLivePushFormat.PUSH_MODE_HD) {
            rb_hdpi.setChecked(true);
        } else if (param.pixel_type == VHLivePushFormat.PUSH_MODE_XXHD) {
            rb_xhdpi.setChecked(true);
        }
//        try {
//            PackageManager pm = getPackageManager();
//            pm.getPackageInfo("com.vhall.appset", PackageManager.GET_ACTIVITIES);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            findViewById(R.id.app_set).setVisibility(View.GONE);
//        }
    }

    public void backClick(View view) {
        param.broToken = et_bro_token.getText().toString();
        param.broId = et_bro_id.getText().toString();
        param.broName = et_bro_name.getText().toString();
        int videoBitrate = 0;
        try {
            videoBitrate = Integer.parseInt(et_video_bitrate.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        param.videoBitrate = videoBitrate == 0 ? 500 : videoBitrate;
        int videoFrameRate = 0;
        try {
            videoFrameRate = Integer.parseInt(et_video_framerate.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        param.videoFrameRate = videoFrameRate == 0 ? 20 : videoFrameRate;

        param.watchId = et_watch_id.getText().toString();
        param.key = et_key.getText().toString();

        param.guestPwd = et_guest_pwd.getText().toString();
        param.guestAvatar = et_guest_avatar.getText().toString();
        int bufferSeconds = -1;
        try {
            bufferSeconds = Integer.parseInt(et_buffersecond.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        param.bufferSecond = bufferSeconds;

        if (rb_hdpi.isChecked()) {
            param.pixel_type = VHLivePushFormat.PUSH_MODE_HD;
        } else if (rb_xhdpi.isChecked()) {
            param.pixel_type = VHLivePushFormat.PUSH_MODE_XXHD;
        }

//        if (radioButtonSD.isChecked()) {
//            param.interactive_definition = VHILSS.SD;
//        } else if (radioButtonHD.isChecked()) {
//            param.interactive_definition = VHILSS.HD;
//        } else if (radioButtonUHD.isChecked()) {
//            param.interactive_definition = VHILSS.UHD;
//        }

        VhallApplication.setParam(param);

        finish();

    }

    @Override
    public void onBackPressed() {
        backClick(null);
    }

}
