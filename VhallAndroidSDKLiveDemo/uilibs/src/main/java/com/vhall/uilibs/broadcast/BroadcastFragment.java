package com.vhall.uilibs.broadcast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.beautify.VHBeautifyKit;
import com.vhall.push.VHLivePushFormat;
import com.vhall.push.VHVideoCaptureView;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.dialog.OutDialog;
import com.vhall.uilibs.interactive.dialog.OutDialogBuilder;

/**
 * 发直播的Fragment
 */
public class BroadcastFragment extends Fragment implements BroadcastContract.View, View.OnClickListener {

    private BroadcastContract.Presenter mPresenter;
    private VHVideoCaptureView cameraview;
    private TextView mSpeed;
    private Button mPublish, mChangeCamera, mChangeFlash, mChangeAudio, mChangeFilter, mBackBtn;
    private TextView tvMode;
    private SeekBar seekBar;

    private Activity mActivity;
    private PopupWindow mPopupWindow;
    private IFaceBeautySwitch iFaceBeautySwitch;


    public void setIFaceBeautySwitch(IFaceBeautySwitch iFaceBeautySwitch) {
        this.iFaceBeautySwitch = iFaceBeautySwitch;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    private static int orientation;

    public static BroadcastFragment newInstance(int ori) {
        orientation = ori;
        return new BroadcastFragment();
    }

    @Override
    public void setPresenter(BroadcastContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.broadcast_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cameraview = (VHVideoCaptureView) getView().findViewById(R.id.cameraview);
        cameraview.setCameraDrawMode(VHLivePushFormat.DRAW_MODE_ASPECTFILL);
        mSpeed = (TextView) getView().findViewById(R.id.tv_upload_speed);
        mPublish = (Button) getView().findViewById(R.id.btn_publish);
        mPublish.setOnClickListener(this);
        mChangeCamera = (Button) getView().findViewById(R.id.btn_changeCamera);
        mChangeCamera.setOnClickListener(this);
        mChangeFlash = (Button) getView().findViewById(R.id.btn_changeFlash);
        mChangeFlash.setOnClickListener(this);
        mChangeAudio = (Button) getView().findViewById(R.id.btn_changeAudio);
        mChangeAudio.setOnClickListener(this);
        mChangeFilter = (Button) getView().findViewById(R.id.btn_changeFilter);
        mChangeFilter.setOnClickListener(this);
        mBackBtn = (Button) getView().findViewById(R.id.btn_back);
        mBackBtn.setOnClickListener(this);
        tvMode = getView().findViewById(R.id.tv_mode);
        tvMode.setOnClickListener(this);
        mPresenter.start();
        seekBar = (SeekBar) getView().findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPresenter.setVolumeAmplificateSize(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_publish) {
            if (cameraAvailable) {
                mPresenter.onstartBtnClick();
            } else {
                Toast.makeText(mActivity, "camera is not available...", Toast.LENGTH_SHORT).show();
            }
        } else if (i == R.id.btn_changeAudio) {
            mPresenter.changeAudio();
        } else if (i == R.id.btn_changeCamera) {
            mPresenter.changeCamera();
        } else if (i == R.id.btn_changeFlash) {
            mPresenter.changeFlash();
        } else if (i == R.id.btn_changeFilter) {
            //  showPopupWindow();  //之前的美颜只能选等级
            if (VHBeautifyKit.getInstance().isBeautifyAuthEnable()) {
                if (iFaceBeautySwitch != null) {
                    iFaceBeautySwitch.changeVisibility();
                }
            } else {
                if (beautyDialog == null) {
                    beautyDialog = new OutDialogBuilder().layout(R.layout.dialog_beauty_no_serve)
                            .build(getActivity());
                }
                beautyDialog.show();
            }

        } else if (i == R.id.tv_mode) {
            mPresenter.changeMode();
        } else if (i == R.id.btn_back) {
            getActivity().finish();
        }
    }

    private OutDialog beautyDialog;

    @Override
    public VHVideoCaptureView getCameraView() {
        return cameraview;
    }

    @Override
    public void setStartBtnImage(boolean start) {
        if (start) {
            mPublish.setBackgroundResource(R.drawable.icon_start_bro);
        } else {
            mPublish.setBackgroundResource(R.drawable.icon_pause_bro);
        }
    }

    @Override
    public void setFlashBtnImage(boolean open) {
        if (open) {
            mChangeFlash.setBackgroundResource(R.drawable.img_round_flash_open);
        } else {
            mChangeFlash.setBackgroundResource(R.drawable.img_round_flash_close);
        }
    }

    @Override
    public void setAudioBtnImage(boolean open) {
        if (open) {
            mChangeAudio.setBackgroundResource(R.drawable.img_round_audio_open);
        } else {
            mChangeAudio.setBackgroundResource(R.drawable.img_round_audio_close);
        }
    }

    @Override
    public void setFlashBtnEnable(boolean enable) {
        mChangeFlash.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setCameraBtnEnable(boolean enable) {
        mChangeCamera.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showMsg(String msg) {
        if (this.isAdded()) {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setSpeedText(String text) {
        mSpeed.setText(text);
    }

    @Override
    public void setModeText(String mode) {
        tvMode.setText(mode);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.stopBroadcast();
    }

    boolean cameraAvailable = true;

    @Override
    public void onResume() {
        super.onResume();
        //自动恢复推流？
//        cameraAvailable = cameraview.resume();
        cameraAvailable = cameraview.isEnabled();
        Log.e("broadcast", "cameraAvai:" + cameraAvailable);
        //auto startBro or not
    }

    @Override
    public void onDestroy() {
        cameraview.releaseCapture();
        mPresenter.destroyBroadcast();
        super.onDestroy();
    }


    //之前的美颜 只能选择等级
    private void showPopupWindow() {
        if (mPopupWindow == null) {
            View contentView = LayoutInflater.from(mActivity).inflate(
                    R.layout.popupwindow_layout, null);
            RadioGroup radioGroup = (RadioGroup) contentView.findViewById(R.id.rg_filter);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radio_0) {
                        cameraview.setFilterEnable(false);
                    } else {
                        cameraview.setFilterEnable(true);
                        int level = 1;
                        if (checkedId == R.id.radio_1) {
                            level = 1;
                        } else if (checkedId == R.id.radio_2) {
                            level = 2;
                        } else if (checkedId == R.id.radio_3) {
                            level = 3;
                        } else if (checkedId == R.id.radio_4) {
                            level = 4;
                        } else if (checkedId == R.id.radio_5) {
//                            cameraview.setFilterAdjuster(100);
                            level = 5;
                        }
                        cameraview.setBeautyLevel(level);
                    }
                }
            });
            mPopupWindow = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        }


        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(
                android.R.color.transparent));

        mPopupWindow.showAsDropDown(mChangeFilter, -18, 0);
    }


    @Override
    public void showEndLiveDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("提示")
                .setMessage("您是否要结束直播？")
                .setPositiveButton("结束", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        alertDialog.show();
    }
}
