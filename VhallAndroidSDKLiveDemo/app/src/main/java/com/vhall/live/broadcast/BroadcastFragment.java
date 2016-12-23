package com.vhall.live.broadcast;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.gpuimage.GPUImageRenderer;
import com.vhall.live.R;
import com.vhall.live.VhallApplication;
import com.vinny.vinnylive.CameraFilterView;

/**
 * 发直播的Fragment
 */
public class BroadcastFragment extends Fragment implements BroadcastContract.View, View.OnClickListener {

    private BroadcastContract.Presenter mPresenter;
    private CameraFilterView cameraview;
    private TextView mSpeed;
    private Button mPublish, mChangeCamera, mChangeFlash, mChangeAudio, mChangeFilter;

    private SeekBar seekBar;

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

        seekBar = (SeekBar) getView().findViewById(R.id.boradcast_seekbar_filter);
        seekBar.setProgress(0);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new BroadCastSeekBarListener());

        cameraview.setAutoCloseFilterCallback(new GPUImageRenderer.AutoCloseBaeutyFilter() {
            @Override
            public void onAutoCloseBaeutyFilter() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.switchFilter(true);
                    }
                });
            }
        });

        mPresenter.start();
    }

    private class BroadCastSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (cameraview.isShowFilter() && cameraview.canFilterAdjuster()) {
                cameraview.setFilterAdjuster(i);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBar.setProgress(10);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_publish:
                mPresenter.onstartBtnClick();
                break;
            case R.id.btn_changeAudio:
                mPresenter.changeAudio();
                break;
            case R.id.btn_changeCamera:
                mPresenter.changeCamera();
                break;
            case R.id.btn_changeFlash:
                mPresenter.changeFlash();
                break;
            case R.id.btn_changeFilter:
                mPresenter.switchFilter(false);
                break;
            default:
                break;
        }
    }

    @Override
    public CameraFilterView getCameraView() {
        return cameraview;
    }

    @Override
    public Activity getmActivity() {
        return getActivity();
    }

    @Override
    public void setStartBtnText(String text) {
        mPublish.setText(text);
    }

    @Override
    public void setChangeFlashEnable(boolean enable) {
        mChangeFlash.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setChangeCameraEnable(boolean enable) {
        mChangeCamera.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showMsg(String msg) {
        Toast.makeText(VhallApplication.getApp(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setSpeedText(String text) {
        mSpeed.setText(text);
    }

    @Override
    public void showSeekbar(boolean show) {
        if (show)
            seekBar.setVisibility(View.VISIBLE);
        else
            seekBar.setVisibility(View.GONE);
    }

    @Override
    public void setSeekbarPro(int progress) {
        seekBar.setProgress(progress);
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
}
