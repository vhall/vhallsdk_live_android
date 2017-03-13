package com.vhall.uilibs.broadcast;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.gpuimage.GPUImageRenderer;
import com.vhall.uilibs.R;
import com.vinny.vinnylive.CameraFilterView;

/**
 * 发直播的Fragment
 */
public class BroadcastFragment extends Fragment implements BroadcastContract.View, View.OnClickListener {

    private BroadcastContract.Presenter mPresenter;
    private CameraFilterView cameraview;
    private TextView mSpeed;
    private Button mPublish, mChangeCamera, mChangeFlash, mChangeAudio, mChangeFilter, mBackBtn;

    private Activity mActivity;
    private PopupWindow mPopupWindow;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    public static BroadcastFragment newInstance() {
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
        cameraview = (CameraFilterView) getView().findViewById(R.id.cameraview);
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

        cameraview.setAutoCloseFilterCallback(new GPUImageRenderer.AutoCloseBaeutyFilter() {
            @Override
            public void onAutoCloseBaeutyFilter() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cameraview.setFilterToBeauty(false);
                    }
                });
            }
        });
        mPresenter.start();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_publish) {
            mPresenter.onstartBtnClick();
        } else if (i == R.id.btn_changeAudio) {
            mPresenter.changeAudio();
        } else if (i == R.id.btn_changeCamera) {
            mPresenter.changeCamera();
        } else if (i == R.id.btn_changeFlash) {
            mPresenter.changeFlash();
        } else if (i == R.id.btn_changeFilter) {
            showPopupWindow();
        } else if (i == R.id.btn_back) {
            getActivity().finish();
        } else {
        }
    }

    @Override
    public CameraFilterView getCameraView() {
        return cameraview;
    }

    @Override
    public void initCamera(int piexl_type) {
        cameraview.init(piexl_type, mActivity, new RelativeLayout.LayoutParams(0, 0));
        if (cameraview.getNumberOfCameras() > 1)
            setCameraBtnEnable(true);
        else
            setCameraBtnEnable(false);
    }

    @Override
    public void setStartBtnImage(boolean start) {
        if (start)
            mPublish.setBackgroundResource(R.drawable.icon_start_bro);
        else
            mPublish.setBackgroundResource(R.drawable.icon_pause_bro);
    }

    @Override
    public void setFlashBtnImage(boolean open) {
        if (open)
            mChangeFlash.setBackgroundResource(R.drawable.img_round_flash_open);
        else
            mChangeFlash.setBackgroundResource(R.drawable.img_round_flash_close);
    }

    @Override
    public void setAudioBtnImage(boolean open) {
        if (open)
            mChangeAudio.setBackgroundResource(R.drawable.img_round_audio_open);
        else
            mChangeAudio.setBackgroundResource(R.drawable.img_round_audio_close);
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
        if (this.isAdded())
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setSpeedText(String text) {
        mSpeed.setText(text);
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraview.pause();
        mPresenter.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraview.resume();
        mPresenter.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestory();
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
                        cameraview.setFilterToBeauty(false);
                    } else if (checkedId == R.id.radio_1) {
                        if (!cameraview.isShowFilter())
                            cameraview.setFilterToBeauty(true);
                        cameraview.setFilterAdjuster(20);
                    } else if (checkedId == R.id.radio_2) {
                        if (!cameraview.isShowFilter())
                            cameraview.setFilterToBeauty(true);
                        cameraview.setFilterAdjuster(40);
                    } else if (checkedId == R.id.radio_3) {
                        if (!cameraview.isShowFilter())
                            cameraview.setFilterToBeauty(true);
                        cameraview.setFilterAdjuster(60);
                    } else if (checkedId == R.id.radio_4) {
                        if (!cameraview.isShowFilter())
                            cameraview.setFilterToBeauty(true);
                        cameraview.setFilterAdjuster(80);
                    } else if (checkedId == R.id.radio_5) {
                        if (!cameraview.isShowFilter())
                            cameraview.setFilterToBeauty(true);
                        cameraview.setFilterAdjuster(100);
                    }
                    Toast.makeText(mActivity, "checked:" + checkedId, Toast.LENGTH_SHORT).show();
                }
            });
            mPopupWindow = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        }


        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(
                android.R.color.transparent));

        mPopupWindow.showAsDropDown(mChangeFilter, -18, 0);
    }
}
