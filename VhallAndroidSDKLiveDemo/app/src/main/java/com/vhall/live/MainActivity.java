package com.vhall.live;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.WebinarInfoDataSource;
import com.vhall.live.webWatch.WebViewActivity;
import com.vhall.live.widget.LiveSelectMenuWidget;
import com.vhall.logmanager.L;
import com.vhall.push.VHLivePushFormat;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.broadcast.BroadcastActivity;
import com.vhall.uilibs.broadcast.DirectorPushActivity;
import com.vhall.uilibs.interactive.RtcInternal;
import com.vhall.uilibs.interactive.broadcast.RtcActivity;
import com.vhall.uilibs.interactive.dialog.ChooseTypeDialog;
import com.vhall.uilibs.interactive.dialog.ScrollChooseTypeDialog;
import com.vhall.uilibs.util.HeadsetUtil;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.watch.VWatchActivity;
import com.vhall.uilibs.watch.WatchActivity;
import com.vhall.uilibs.widget.AgreementDialog;
import com.vhall.vhss.data.AgreementData;
import com.vhall.vhss.data.DirectorSeatListData;

import java.util.ArrayList;
import java.util.List;


/**
 * 主界面的Activity
 */
public class MainActivity extends FragmentActivity implements LiveSelectMenuWidget.OnMenuListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    TextView tv_phone, tv_name, tv_login, watch_id, broadcast_id;
    ImageView mCircleViewAvatar;
    Param param = null;

    private ProgressDialog mLoading;
    private Context mContext;

    private LiveSelectMenuWidget select_window;
    private static final int REQUEST_PERMISSIONS = 1;
    public String[] permissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final static String HALF_SCREEN_WATCH = "半屏观看";
    private final static String HALF_SCREEN_NO_DELAY_WATCH = "半屏无延迟观看";
    private final static String ALL_SCREEN_NO_DELAY_WATCH = "全屏无延迟观看";
    private final static String ALL_SCREEN_WATCH = "全屏观看";
    private final static String WATCH_PLAY_BACK = "观看回放";
    private final static String VERTICAL_LIVE = "竖屏直播";
    private final static String VERTICAL_NO_DELAY_LIVE = "竖屏无延迟直播";
    private final static String HORIZONTAL_NO_DELAY_LIVE = "横屏无延迟直播";
    private final static String HORIZONTAL_LIVE = "横屏直播";
    private final static String CLOUD_DIRECTOR = "云导播活动";
    private final static String HOST_INTERACTIVE = "主持人互动";
    private final static String GUEST_INTERACTIVE = "嘉宾互动";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mContext = this;
        HeadsetUtil.init(getApplication());
        tv_phone = this.findViewById(R.id.tv_phone);
        tv_name = this.findViewById(R.id.text_name);
        watch_id = this.findViewById(R.id.watch_id);
        broadcast_id = this.findViewById(R.id.broadcast_id);
        mCircleViewAvatar = this.findViewById(R.id.iv_avatar);
        tv_login = this.findViewById(R.id.tv_login);
        select_window = this.findViewById(R.id.select_window);
        select_window.setListener(this::onClick);
        mLoading = new ProgressDialog(this);
        mLoading.setTitle("提示");
        mLoading.setMessage("加载中...");
        requestPermission();
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
    }


    public void showLoading() {
        mLoading.show();
    }

    public void hideLoading() {
        mLoading.hide();
    }

    @Override
    public void onClick(String name) {
        switch (name) {
            case HALF_SCREEN_WATCH:
            case HALF_SCREEN_NO_DELAY_WATCH:
            case ALL_SCREEN_NO_DELAY_WATCH:
            case ALL_SCREEN_WATCH:
                getAgreement(name);
                break;
            case VERTICAL_LIVE:
                if (RtcInternal.isGrantedPermissionRtc(MainActivity.this, REQUEST_PUSH))
                    startBroadcastActivity(VHLivePushFormat.SCREEN_ORI_PORTRAIT);
                break;
            case HORIZONTAL_LIVE:
                if (RtcInternal.isGrantedPermissionRtc(MainActivity.this, REQUEST_PUSH))
                    startBroadcastActivity(VHLivePushFormat.SCREEN_ORI_LANDSPACE);
                break;
            case CLOUD_DIRECTOR:
                if (RtcInternal.isGrantedPermissionRtc(MainActivity.this, REQUEST_PUSH))
                    startDirectorActivity();
                break;
            case VERTICAL_NO_DELAY_LIVE:
                if (RtcInternal.isGrantedPermissionRtc(MainActivity.this, REQUEST_PUSH))
                    startNoDelayBroadcastActivity(VHLivePushFormat.SCREEN_ORI_PORTRAIT);
                break;
            case HORIZONTAL_NO_DELAY_LIVE:
                if (RtcInternal.isGrantedPermissionRtc(MainActivity.this, REQUEST_PUSH))
                    startNoDelayBroadcastActivity(VHLivePushFormat.SCREEN_ORI_LANDSPACE);
                break;
            case HOST_INTERACTIVE:
                if (RtcInternal.isGrantedPermissionRtc(MainActivity.this, REQUEST_PUSH))
                    startRtcActivity();
                break;
            case GUEST_INTERACTIVE:
                guestJoinMeeting();
                break;
        }
    }

    void guestJoinMeeting() {
        if (TextUtils.isEmpty(param.watchId) || TextUtils.isEmpty(param.guestPwd)) {
            ToastUtil.showToast("请输入直播id或嘉宾口令");
        } else {
            requestGuestWeninarInfo();
        }
    }

    void requestGuestWeninarInfo() {
        showLoading();
        //嘉宾加入
        VhallSDK.joinWebinar(param.watchId, param.guestPwd, VhallSDK.getUserName(), param.guestAvatar, "2", new WebinarInfoDataSource.LoadWebinarInfoCallback() {
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

    void startRtcActivity() {
        if (!TextUtils.isEmpty(param.broId)) {
            requestRtcInfo();
        } else {
            ToastUtil.showToast(MainActivity.this, R.string.app_please_input_rtc_id);
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
                L.e(TAG, "success");
                hideLoading();

                if (!TextUtils.equals("3", webinarInfo.webinar_type)) {
                    ToastUtil.showToast(MainActivity.this, "只支持互动直播");
                    return;
                }
                jumpRtcActivity(webinarInfo);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                L.e(TAG, errorMsg + "");
                hideLoading();
                ToastUtil.showToast(MainActivity.this, errorMsg + "");
            }
        }, true);
    }


    //跳转到互动activity
    void jumpRtcActivity(WebinarInfo webinarInfo) {
        RtcActivity.startActivity(MainActivity.this, webinarInfo, param);
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
        if (param != null) {
            watch_id.setText(String.format("观看端id-%s", param.watchId));
            broadcast_id.setText(String.format("发起端id-%s", param.broId));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onBroadcastLandspace(View view) {
        select_window.showMenus(VERTICAL_NO_DELAY_LIVE, HORIZONTAL_NO_DELAY_LIVE, VERTICAL_LIVE, HORIZONTAL_LIVE, CLOUD_DIRECTOR);
    }

    public void onBroadcastPortrait(View view) {
        select_window.showMenus(HOST_INTERACTIVE, GUEST_INTERACTIVE);
    }

    private List<String> stringDirector = new ArrayList<>();
    private ChooseTypeDialog director;

    private void initDirector() {
        if (director == null) {
            stringDirector.clear();
            stringDirector.add("以主持人身份发起直播");
            stringDirector.add("以视频推流形式推流到云导播");
            director = new ChooseTypeDialog(mContext, stringDirector);
            director.setOnItemClickLister(new ChooseTypeDialog.onItemClickLister() {
                @Override
                public void onItemClick(int option) {
                    switch (option) {
                        case 1:
                            startDirectorBroadcastActivity();
                            break;
                        case 2:
                            initCameraStan();
                            break;
                        default:
                            break;
                    }
                }
            });
        }
        director.show();
    }

    //选择云导播推流机位
    private void initCameraStan() {
        List<String> strings = new ArrayList<>();
        List<Boolean> booleans = new ArrayList<>();
        //获取机位列表
        VhallSDK.getDirectorSeatList(param.broId, new RequestDataCallback() {
            @Override
            public void onSuccess(Object result) {
                DirectorSeatListData directorData = (DirectorSeatListData) result;
                if (directorData == null || directorData.list == null || directorData.list.size() <= 0) {
                    ToastUtil.showToast("errorData");
                    return;
                }
                for (int i = 0; i < directorData.list.size(); i++) {
                    DirectorSeatListData.ListBean directorSeatListData = directorData.list.get(i);
                    strings.add(directorSeatListData.name + ((TextUtils.equals(directorSeatListData.status, "0") ? "" : "：已占用")));
                    booleans.add(TextUtils.equals(directorSeatListData.status, "0"));
                }
                ScrollChooseTypeDialog scrollDirectorDialog = new ScrollChooseTypeDialog(MainActivity.this, strings, booleans);
                scrollDirectorDialog.setOnItemClickLister((option, content) -> {
                    DirectorSeatListData.ListBean directorSeatListData = directorData.list.get(option);
                    selectSeat(directorSeatListData);
                });
                scrollDirectorDialog.show();
            }

            @Override
            public void onError(int eventCode, String msg) {
                ToastUtil.showToast(msg);
            }
        });

    }

    //云导播机位占用-成功到机位推流页面
    private void selectSeat(DirectorSeatListData.ListBean directorSeatListData) {
        VhallSDK.directorSelectSeat(param.broId, directorSeatListData.seat_id, new RequestCallback() {
            @Override
            public void onSuccess() {
                param.seatId = directorSeatListData.seat_id;
                Intent intent = new Intent(MainActivity.this, DirectorPushActivity.class);
                intent.putExtra("param", param);
                startActivity(intent);
            }

            @Override
            public void onError(int eventCode, String msg) {
                ToastUtil.showToast(msg);
            }
        });
    }

    private void startDirectorActivity() {
        VhallSDK.getBaseWebinarInfo(param.broId, new RequestDataCallback() {
            @Override
            public void onSuccess(Object result) {
                WebinarInfo webinarInfo = (WebinarInfo) result;
                if (webinarInfo.is_director != 1) {
                    Toast.makeText(MainActivity.this, "当前直播不是云导播活动请退出", Toast.LENGTH_SHORT).show();
                    return;
                }
                param.webinar_id = webinarInfo.webinar_id;
                param.noDelay = false;
                param.isDirector = true;
                //横竖屏 0 竖屏 1横屏
                if (0 == webinarInfo.webinar_show_type) {
                    param.screenOri = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else {
                    param.screenOri = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                }
                VhallSDK.getDirectorStatus(param.broId, new RequestDataCallback() {
                    @Override
                    public void onSuccess(Object director_status) {
                        // 云导播台状态， 0： 未开启， 1：已开启
                        initDirector();
                        //云导播活动有开启云导播
                        if (TextUtils.equals((String) director_status, "0"))
                            director.setViewUnClickable();
                        else
                            director.setViewClickable();
                    }

                    @Override
                    public void onError(int eventCode, String errorMsg) {
                        ToastUtil.showToast(errorMsg);
                    }
                });
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                ToastUtil.showToast(errorMsg);
            }
        });
    }


    //主持人依然调用之前的接口
    private void startDirectorBroadcastActivity() {
        Intent intent = new Intent(this, BroadcastActivity.class);
        VhallSDK.initBroadcast(param.broId, param.broToken, param.broName, new WebinarInfoDataSource.LoadWebinarInfoCallback() {
            @Override
            public void onWebinarInfoLoaded(String jsonStr, WebinarInfo webinarInfo) {
                if (webinarInfo.is_director != 1) {
                    Toast.makeText(MainActivity.this, "当前直播不是云导播活动请退出", Toast.LENGTH_SHORT).show();
                    return;
                }
                param.vssToken = webinarInfo.vss_token;
                param.vssRoomId = webinarInfo.vss_room_id;
                param.join_id = webinarInfo.join_id;
                param.webinar_id = webinarInfo.webinar_id;
                param.inav_num = webinarInfo.inav_num;
                param.noDelay = false;
                param.isDirector = true;
                //横竖屏 0 竖屏 1横屏
                if (0 == webinarInfo.webinar_show_type) {
                    param.screenOri = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else {
                    param.screenOri = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                }
                intent.putExtra("param", param);
                intent.putExtra("webinarInfo", webinarInfo);
                startActivity(intent);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startBroadcastActivity(int orientation) {
        Intent intent = new Intent(this, BroadcastActivity.class);
        VhallSDK.initBroadcast(param.broId, param.broToken, param.broName, new WebinarInfoDataSource.LoadWebinarInfoCallback() {
            @Override
            public void onWebinarInfoLoaded(String jsonStr, WebinarInfo webinarInfo) {
                if (webinarInfo.is_director == 1) {
                    Toast.makeText(MainActivity.this, "当前直播是云导播活动请退出", Toast.LENGTH_SHORT).show();
                    return;
                }
                param.vssToken = webinarInfo.vss_token;
                param.vssRoomId = webinarInfo.vss_room_id;
                param.join_id = webinarInfo.join_id;
                param.webinar_id = webinarInfo.webinar_id;
                param.screenOri = orientation;
                param.inav_num = webinarInfo.inav_num;
                param.noDelay = false;
                intent.putExtra("param", param);
                intent.putExtra("webinarInfo", webinarInfo);
                startActivity(intent);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startNoDelayBroadcastActivity(int orientation) {
        Intent intent = new Intent(this, BroadcastActivity.class);
        VhallSDK.initBroadcast(param.broId, param.broToken, param.broName, new WebinarInfoDataSource.LoadWebinarInfoCallback() {
            @Override
            public void onWebinarInfoLoaded(String jsonStr, WebinarInfo webinarInfo) {
                if (webinarInfo.no_delay_webinar != 1) {
                    Toast.makeText(MainActivity.this, "当前直播是常规直播", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (webinarInfo.is_director == 1) {
                    Toast.makeText(MainActivity.this, "当前直播是云导播活动请退出", Toast.LENGTH_SHORT).show();
                    return;
                }
                param.vssToken = webinarInfo.vss_token;
                param.vssRoomId = webinarInfo.vss_room_id;
                param.join_id = webinarInfo.join_id;
                param.webinar_id = webinarInfo.webinar_id;
                param.screenOri = orientation;
                param.inav_num = webinarInfo.inav_num;
                param.noDelay = true;
                intent.putExtra("param", param);
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
        select_window.showMenus(HALF_SCREEN_NO_DELAY_WATCH, ALL_SCREEN_NO_DELAY_WATCH, HALF_SCREEN_WATCH, ALL_SCREEN_WATCH);
    }


    public void onWatchPlayback(View view) {
        getAgreement(WATCH_PLAY_BACK);
    }

    public void onSetParam(View view) {
        Intent intent = new Intent(this, SetParamActivity.class);
        intent.putExtra("param", param);
        startActivity(intent);
    }

    public void onH5Watch(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("param", param);
        startActivity(intent);
    }

    public void onHeadClick(View view) {
        if (VhallSDK.isLogin()) {
            Intent intent = new Intent(this, LiveListActivity.class);
            startActivity(intent);
        }
    }

    public void onHNoDelayScreenClick() {
        Intent intent = new Intent(this, WatchActivity.class);
        intent.putExtra("param", param);
        intent.putExtra("type", VhallUtil.WATCH_LIVE);
        intent.putExtra("no_delay", true);
        startActivity(intent);
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

    public void onNoDelayVScreenClick() {
        Intent intent = new Intent(this, VWatchActivity.class);
        intent.putExtra("param", param);
        intent.putExtra("type", VhallUtil.WATCH_LIVE);
        intent.putExtra("no_delay", true);
        startActivity(intent);
    }


    AgreementDialog agreementDialog;

    private void getAgreement(String name) {
        /**
         * param.watchId 活动id
         * AgreementData 观看协议数据类
         */
        VhallSDK.getAgreement(param.watchId, new RequestDataCallback() {
            @Override
            public void onSuccess(Object result) {
                AgreementData agreementData = (AgreementData) result;
                // is_open  声明状态  0:关 1:开
                // is_agree 当前用户是否同意 0:未同意 1:同意
                if (agreementData != null && TextUtils.equals("1", agreementData.is_open) && !TextUtils.equals("1", agreementData.is_agree)) {
                    if (agreementDialog == null) {
                        agreementDialog = new AgreementDialog(MainActivity.this);
                        agreementDialog.setOnItemClickLister(new AgreementDialog.OnItemClickLister() {
                            @Override
                            public void onItemClick() {
                                // 调用了这个接口 就不会展示弹窗第二次
                                VhallSDK.setUserAgreeAgreement(param.watchId, new RequestCallback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(int errorCode, String errorMsg) {
                                        ToastUtil.showToast(errorMsg);
                                    }
                                });
                                jumpWatch(name);
                            }

                            @Override
                            public void jumpWeb(String url) {
                                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                                intent.putExtra("url", url);
                                startActivity(intent);
                            }
                        });
                    }
                    agreementDialog.setAgreementData(agreementData);
                    agreementDialog.show();
                } else {
                    jumpWatch(name);
                }
            }

            @Override
            public void onError(int eventCode, String msg) {
                ToastUtil.showToast(msg);
                jumpWatch(name);
            }
        });
    }

    private void jumpWatch(String name) {
        switch (name) {
            case HALF_SCREEN_WATCH:
                onHScreenClick();
                break;
            case HALF_SCREEN_NO_DELAY_WATCH:
                onHNoDelayScreenClick();
                break;
            case ALL_SCREEN_NO_DELAY_WATCH:
                onNoDelayVScreenClick();
                break;
            case ALL_SCREEN_WATCH:
                onVScreenClick();
                break;

            case WATCH_PLAY_BACK:
                Intent intent = new Intent(this, WatchActivity.class);
                intent.putExtra("param", param);
                intent.putExtra("type", VhallUtil.WATCH_PLAYBACK);
                startActivity(intent);
                break;
        }
    }
    public static final int REQUEST_PUSH = 0;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PUSH) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            } else {
                ToastUtil.showToast("未获取到音视频设备权限，请前往获取权限");
            }
        }
    }
}
