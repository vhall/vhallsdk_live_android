package com.vhall.uilibs.broadcast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vhall.beautify.VHBeautifyKit;
import com.vhall.beautifykit.control.FaceBeautyControlView;
import com.vhall.business.Broadcast;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.WebinarInfoDataSource;
import com.vhall.net.NetBroadcastReceiver;
import com.vhall.net.NetUtil;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.push.VHLivePushConfig;
import com.vhall.push.VHLivePushFormat;
import com.vhall.push.VHVideoCaptureView;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.beautysource.FaceBeautyDataFactory;
import com.vhall.uilibs.interactive.RtcInternal;
import com.vhall.uilibs.interactive.dialog.OutDialog;
import com.vhall.uilibs.interactive.dialog.OutDialogBuilder;
import com.vhall.uilibs.util.ToastUtil;

// 云导播活动 机位选择进入页面
public class DirectorPushActivity extends FragmentActivity implements View.OnClickListener {
    private ImageView mChangeCamera, mChangeAudio, mChangeFilter, mBackBtn;
    private Broadcast broadcast;
    private VHVideoCaptureView captureView;
    private Param param;
    private NetUtil netUtil;
    private TextView tvTitle;
    private WebinarInfo webinarInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_director_push);
        mChangeCamera = findViewById(R.id.iv_change_camera);
        mChangeCamera.setOnClickListener(this);
        mChangeAudio = findViewById(R.id.iv_voice);
        mChangeAudio.setOnClickListener(this);
        mChangeFilter = findViewById(R.id.iv_beauty);
        mChangeFilter.setOnClickListener(this);
        mBackBtn = findViewById(R.id.iv_close);
        captureView = findViewById(R.id.captureView);
        tvTitle = findViewById(R.id.tv_title);
        mBackBtn.setOnClickListener(this);
        param = (Param) getIntent().getSerializableExtra("param");
        monitorNetWork();
        if (param.screenOri == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void monitorNetWork() {
        netUtil = new NetUtil(this, new NetBroadcastReceiver.NetChangeListener() {
            @Override
            public void onChangeListener(int status) {
                if (RtcInternal.isNetworkConnected(DirectorPushActivity.this)) {
                    Log.e("vhall_", "monitorNetWork rePush");
                    rePush();
                } else {
                    ToastUtil.showToast("当前网络异常");
                }
            }
        });
    }

    private void getBroadcast() {
        VHLivePushConfig config = new VHLivePushConfig();
        //不设置 美颜瘦脸没有效果
        config.screenOri = param.screenOri;//横竖屏设置 重要
        config.streamType = VHLivePushFormat.STREAM_TYPE_AV;

        Broadcast.Builder builder = new Broadcast.Builder()
                .cameraView(captureView)
                .config(config)
                .callback(listener);
        broadcast = builder.build();
        broadcast.changeCamera();
        VhallSDK.initDirector(param.broId, param.seatId, new WebinarInfoDataSource.LoadWebinarInfoCallback() {
            @Override
            public void onWebinarInfoLoaded(String jsonStr, WebinarInfo result) {
                webinarInfo = result;
                //进来直接推流 此页面不支持消息等任何功能
                broadcast.setWebinarInfo(webinarInfo);
                //在这里初始化美颜不然没有效果
                if (param.screenOri == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    initBeautifyData(0);
                } else {
                    initBeautifyData(1);
                }
                tvTitle.setText(String.format("推流到云导播台-%s", webinarInfo.seatName));
                //自动推流
                broadcast.start(new RequestCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        ToastUtil.showToast(errorMsg);
                    }
                });
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                ToastUtil.showToast(errorMsg);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (broadcast != null)
            broadcast.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webinarInfo == null) {
            getBroadcast();
        } else {
            rePush();
        }
    }

    private void rePush() {
        if (param == null) {
            ToastUtil.showToast("error data");
            finish();
            return;
        }
        if (broadcast != null && webinarInfo != null) {
            VhallSDK.directorSelectSeat(param.broId, param.seatId, new RequestCallback() {
                @Override
                public void onSuccess() {
                    broadcast.start(new RequestCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            ToastUtil.showToast(errorMsg);
                        }
                    });
                }

                @Override
                public void onError(int eventCode, String msg) {
                    ToastUtil.showToast(msg);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcast != null)
            broadcast.stop();
        if (netUtil != null)
            netUtil.release();
    }

    private VHPlayerListener listener = new VHPlayerListener() {
        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case START:
                    ToastUtil.showToast("直播开始");
                    break;
                case BUFFER:

                    break;
                case STOP:

                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {
        }

        @Override
        public void onError(int errorCode, int innerErrorCode, String msg) {
            ToastUtil.showToast(msg);
        }
    };

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_close) {
            showEndLiveDialog();
        } else if (i == R.id.iv_voice) {
            if (broadcast != null) {
                boolean isMute = broadcast.isMute();
                broadcast.setMute(!isMute);
                setAudioBtnImage(!isMute);
            }
        } else if (i == R.id.iv_change_camera) {
            if (broadcast != null) {
                broadcast.changeCamera();
            }
        } else if (i == R.id.iv_beauty) {
            if (VHBeautifyKit.getInstance().isBeautifyAuthEnable()) {
                changeVisibility();
            } else {
                if (beautyDialog == null) {
                    beautyDialog = new OutDialogBuilder().layout(R.layout.dialog_beauty_no_serve)
                            .build(this);
                }
                beautyDialog.show();
            }
        }
    }

    public void showEndLiveDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("您是否要结束直播？")
                .setPositiveButton("结束", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        alertDialog.show();
    }

    public void setAudioBtnImage(boolean open) {
        if (open) {
            mChangeAudio.setBackgroundResource(R.drawable.img_round_audio_open);
        } else {
            mChangeAudio.setBackgroundResource(R.drawable.img_round_audio_close);
        }
    }

    // 高级美颜相关
    private FaceBeautyControlView mFaceBeautyControlView;
    private FaceBeautyDataFactory mFaceBeautyDataFactory;

    private void changeVisibility() {
        //新的美颜
        if (mFaceBeautyControlView.getVisibility() == View.VISIBLE) {
            mFaceBeautyControlView.setVisibility(View.GONE);
        } else {
            mFaceBeautyControlView.setVisibility(View.VISIBLE);
        }
    }

    private OutDialog beautyDialog;

    private void initBeautifyData(int orientation) {
        mFaceBeautyDataFactory = new FaceBeautyDataFactory(this);
        mFaceBeautyControlView = findViewById(R.id.faceBeautyControlView);
        mFaceBeautyControlView.setMainTabVisibility(false, true, true, false);
        mFaceBeautyControlView.setSelectLineVisible();
        // 0 横屏 1 竖屏
        mFaceBeautyControlView.changeOrientation(orientation);
        mFaceBeautyControlView.bindDataFactory(mFaceBeautyDataFactory);
    }
}