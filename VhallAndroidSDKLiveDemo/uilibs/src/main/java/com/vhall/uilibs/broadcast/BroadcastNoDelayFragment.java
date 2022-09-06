package com.vhall.uilibs.broadcast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.vhall.push.VHVideoCaptureView;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.dialog.OutDialog;
import com.vhall.uilibs.interactive.dialog.OutDialogBuilder;
import com.vhall.vhallrtc.client.VHRenderView;

import org.vhwebrtc.SurfaceViewRenderer;

/**
 * 发无延时的 视频直播 Fragment
 */
public class BroadcastNoDelayFragment extends Fragment implements BroadcastContract.View, View.OnClickListener {

    private BroadcastContract.Presenter mPresenter;
    private VHVideoCaptureView cameraview;
    private TextView mSpeed;
    private Button mPublish, mChangeCamera, mChangeFlash, mChangeAudio, mChangeFilter, mBackBtn;
    private TextView tvMode;
    private SeekBar seekBar;

    private Activity mActivity;
    private PopupWindow mPopupWindow;
    private VHRenderView renderView;

    private IFaceBeautySwitch iFaceBeautySwitch;

    private static int orientation;
    public void setIFaceBeautySwitch(IFaceBeautySwitch iFaceBeautySwitch) {
        this.iFaceBeautySwitch = iFaceBeautySwitch;
    }

    public static BroadcastNoDelayFragment newInstance(int ori) {
        orientation = ori;
        return new BroadcastNoDelayFragment();
    }

    private OutDialog beautyDialog;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    public static BroadcastNoDelayFragment newInstance() {
        return new BroadcastNoDelayFragment();
    }

    @Override
    public void setPresenter(BroadcastContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.broadcast_nodelay_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cameraview = (VHVideoCaptureView) getView().findViewById(R.id.cameraview);
        cameraview.setVisibility(View.GONE);
        mSpeed = (TextView) getView().findViewById(R.id.tv_upload_speed);
        mSpeed.setVisibility(View.GONE);
        renderView = getView().findViewById(R.id.render_view);
        renderView.init(null, null);
        renderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
        renderView.setVisibility(View.VISIBLE);
        mPublish = (Button) getView().findViewById(R.id.btn_publish);
        mPublish.setOnClickListener(this);
        mChangeCamera = (Button) getView().findViewById(R.id.btn_changeCamera);
        mChangeCamera.setOnClickListener(this);
        mChangeFlash = (Button) getView().findViewById(R.id.btn_changeFlash);
        mChangeFlash.setVisibility(View.GONE);
        mChangeAudio = (Button) getView().findViewById(R.id.btn_changeAudio);
        mChangeAudio.setOnClickListener(this);
        mChangeFilter = (Button) getView().findViewById(R.id.btn_changeFilter);
        mChangeFilter.setOnClickListener(this);
        mBackBtn = (Button) getView().findViewById(R.id.btn_back);
        mBackBtn.setOnClickListener(this);
        tvMode = getView().findViewById(R.id.tv_mode);
        tvMode.setOnClickListener(this);
        tvMode.setVisibility(View.GONE);
        mPresenter.setRenderView(renderView);
        mPresenter.start();
        seekBar = (SeekBar) getView().findViewById(R.id.seekbar);
        seekBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_publish) {
            if (cameraAvailable) {
                mPresenter.onstartBtnClick(false);
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
//            showPopupWindow();
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
        } else {
        }
    }

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
        if (mPresenter != null) {
            mPresenter.onPause();
            mPresenter.stopBroadcast();
        }
    }

    boolean cameraAvailable = true;

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter != null)
            mPresenter.onResume();
    }


    @Override
    public void onDestroy() {
        if (mPresenter != null)
            mPresenter.destroyBroadcast();
        super.onDestroy();
    }


    private void showPopupWindow() {
        if (mPopupWindow == null) {
            View contentView = LayoutInflater.from(mActivity).inflate(
                    R.layout.popupwindow_layout, null);
            RadioGroup radioGroup = (RadioGroup) contentView.findViewById(R.id.rg_filter);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radio_0) {
                    } else {

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
