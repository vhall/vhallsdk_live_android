package com.vhall.live;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.WebinarInfoDataSource;
import com.vhall.live.webWatch.WebViewActivity;
import com.vhall.live.widget.LiveSelectMenuWidget;
import com.vhall.logmanager.L;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.broadcast.BroadcastActivity;
import com.vhall.uilibs.interactive.broadcast.RtcActivity;
import com.vhall.uilibs.util.HeadsetUtil;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.util.UserManger;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.watch.VWatchActivity;
import com.vhall.uilibs.watch.WatchActivity;
import com.vhall.vhss.TokenManger;
import com.vhall.vhss.data.GuestJoinInfoData;

import java.util.UUID;


/**
 * 主界面的Activity
 */
public class MainActivity extends FragmentActivity implements LiveSelectMenuWidget.OnMenuListener{
    private final static String TAG = MainActivity.class.getSimpleName();

    TextView tv_phone, tv_name, tv_login;
    ImageView mCircleViewAvatar;
    Param param = null;

    private ProgressDialog mLoading;

    private LiveSelectMenuWidget select_window;
    private static final int REQUEST_PERMISSIONS = 1;
    public String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final static String HALF_SCREEN_WATCH = "半屏观看";
    private final static String ALL_SCREEN_WATCH = "全屏观看";
    private final static String VERTICAL_LIVE = "竖屏直播";
    private final static String HORIZONTAL_LIVE = "横屏直播";
    private final static String HOST_INTERACTIVE = "主持人互动";
    private final static String GUEST_INTERACTIVE = "嘉宾互动";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        HeadsetUtil.init(getApplication());
        tv_phone = this.findViewById(R.id.tv_phone);
        tv_name = this.findViewById(R.id.text_name);
        mCircleViewAvatar = this.findViewById(R.id.iv_avatar);
        tv_login = this.findViewById(R.id.tv_login);
        select_window = this.findViewById(R.id.select_window);
        select_window.setListener(this::onClick);
        mLoading = new ProgressDialog(this);
        mLoading.setTitle("提示");
        mLoading.setMessage("加载中...");

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


    public void showLoading(){
        mLoading.show();
    }

    public void hideLoading(){
        mLoading.hide();
    }

    @Override
    public void onClick(String name) {
        switch (name){
            case HALF_SCREEN_WATCH:
                onHScreenClick();
                break;
            case ALL_SCREEN_WATCH:
                onVScreenClick();
                break;
            case VERTICAL_LIVE:
                startBroadcastActivity(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case HORIZONTAL_LIVE:
                startBroadcastActivity(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case HOST_INTERACTIVE:
                startRtcActivity();
                break;
            case GUEST_INTERACTIVE:
                guestJoinMeeting();
                break;
        }
    }

    void guestJoinMeeting(){
        if(TextUtils.isEmpty(param.watchId) || TextUtils.isEmpty(param.guestPwd)){
            ToastUtil.showToast("请输入直播id或嘉宾口令");
        }else{
            requestGuestWeninarInfo();
        }
    }

    void requestGuestWeninarInfo(){
        showLoading();

        //嘉宾加入
        VhallSDK.joinWebinar(param.watchId, param.guestPwd, VhallSDK.getUserName(), param.guestAvatar,"2", new WebinarInfoDataSource.LoadWebinarInfoCallback() {
            @Override
            public void onWebinarInfoLoaded(String jsonStr, WebinarInfo webinarInfo) {
                RtcActivity.startActivity(MainActivity.this, true, webinarInfo);
                hideLoading();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                ToastUtil.showToast(errorMsg);
                hideLoading();
            }
        });
    }

    void startRtcActivity(){
        if(!TextUtils.isEmpty(param.broId)){
            requestRtcInfo();
        }else{
            ToastUtil.showToast(MainActivity.this,R.string.app_please_input_rtc_id);
        }
    }

    /**
     * 请求主播端互动信息
     */
    private void requestRtcInfo() {
        showLoading();
        VhallSDK.initBroadcast(param.broId, param.broToken, param.broName, new WebinarInfoDataSource.LoadWebinarInfoCallback() {
            @Override
            public void onWebinarInfoLoaded(String jsonStr, WebinarInfo webinarInfo) {
                L.e(TAG,"success");
                hideLoading();
                jumpRtcActivity(webinarInfo);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                L.e(TAG,errorMsg);
                hideLoading();
                ToastUtil.showToast(MainActivity.this,errorMsg);
            }
        },true);
    }


    //跳转到互动activity
    void jumpRtcActivity( WebinarInfo webinarInfo){
        RtcActivity.startActivity(MainActivity.this,webinarInfo,param);
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
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
        RequestOptions requestOptions = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.drawable.icon_default_avatar);
        Glide.with(this).load(VhallSDK.getUserAvatar()).apply(requestOptions).into(mCircleViewAvatar);
        tv_name.setText(TextUtils.isEmpty(VhallSDK.getUserNickname()) ? Build.BRAND + getString(R.string.phone_user) : VhallSDK.getUserNickname());

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
        select_window.showMenus(VERTICAL_LIVE,HORIZONTAL_LIVE);
    }

    public void onBroadcastPortrait(View view) {
        select_window.showMenus(HOST_INTERACTIVE,GUEST_INTERACTIVE);
    }

    private void startBroadcastActivity(int orientation) {
        Intent intent = new Intent(this, BroadcastActivity.class);
        VhallSDK.initBroadcast(param.broId, param.broToken,param.broName, new WebinarInfoDataSource.LoadWebinarInfoCallback() {
            @Override
            public void onWebinarInfoLoaded(String jsonStr, WebinarInfo webinarInfo) {
                param.vssToken = webinarInfo.vss_token;
                param.vssRoomId = webinarInfo.vss_room_id;
                param.join_id = webinarInfo.join_id;
                param.webinar_id = webinarInfo.webinar_id;
                param.screenOri = orientation;
                param.inav_num = webinarInfo.inav_num;
                intent.putExtra("param",param);
                intent.putExtra("webinarInfo", webinarInfo);
                startActivity(intent);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onWatchLive(View view) {
//        findViewById(R.id.select_window).setVisibility(View.VISIBLE);
        select_window.showMenus(HALF_SCREEN_WATCH,ALL_SCREEN_WATCH);
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

    public void onH5Watch(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("param",param);
        startActivity(intent);
    }

    public void onHeadClick(View view) {
        if (VhallSDK.isLogin()) {
            Intent intent = new Intent(this, LiveListActivity.class);
            startActivity(intent);
        }
    }


    public void onHScreenClick() {
        Intent intent = new Intent(this, WatchActivity.class);
        intent.putExtra("param", param);
        intent.putExtra("type", VhallUtil.WATCH_LIVE);
        startActivity(intent);
    }

    public void onVScreenClick() {
        Intent intent = new Intent(this, VWatchActivity.class);
        intent.putExtra("param", param);
        intent.putExtra("type", VhallUtil.WATCH_LIVE);
        startActivity(intent);
    }
}
