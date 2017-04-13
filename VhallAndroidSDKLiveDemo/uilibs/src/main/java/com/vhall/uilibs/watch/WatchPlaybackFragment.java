package com.vhall.uilibs.watch;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.WatchLive;
import com.vhall.business.widget.ContainerLayout;
import com.vhall.uilibs.R;

/**
 * 观看回放的Fragment
 */
public class WatchPlaybackFragment extends Fragment implements WatchContract.PlaybackView, View.OnClickListener {

    WatchContract.PlaybackPresenter mPresenter;
    ContainerLayout rl_video_container;//视频区容器
    ImageView iv_play, btn_changescaletype;
    SeekBar seekbar;
    TextView tv_current_time, tv_end_time;
    ProgressBar pb;

    public static boolean isShowToast = true;

    public static WatchPlaybackFragment newInstance() {
        WatchPlaybackFragment articleFragment = new WatchPlaybackFragment();
        return articleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.watch_playback_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rl_video_container = (ContainerLayout) getView().findViewById(R.id.rl_video_container);
        btn_changescaletype = (ImageView) getView().findViewById(R.id.btn_change_scale_type);
        btn_changescaletype.setOnClickListener(this);
        pb = (ProgressBar) getView().findViewById(R.id.pb);
        iv_play = (ImageView) getView().findViewById(R.id.iv_play);
        iv_play.setOnClickListener(this);
        getView().findViewById(R.id.iv_fullscreen).setOnClickListener(this);
        seekbar = (SeekBar) getView().findViewById(R.id.seekbar);
        tv_current_time = (TextView) getView().findViewById(R.id.tv_current_time);
        tv_end_time = (TextView) getView().findViewById(R.id.tv_end_time);
        getView().findViewById(R.id.image_action_back).setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPresenter.onProgressChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPresenter.onStopTrackingTouch(seekBar);
            }
        });
        mPresenter.start();
    }

    @Override
    public void setPresenter(WatchContract.PlaybackPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void setPlayIcon(boolean isStop) {
        if (isStop) {
            iv_play.setImageResource(R.drawable.vhall_icon_live_play);
        } else {
            iv_play.setImageResource(R.drawable.vhall_icon_live_pause);
        }
    }

    @Override
    public void setProgressLabel(String currentTime, String max) {
        tv_current_time.setText(currentTime);
        tv_end_time.setText(max);
    }

    @Override
    public void setSeekbarMax(int max) {
        seekbar.setMax(max);
    }

    @Override
    public void setSeekbarCurrentPosition(int position) {
        seekbar.setProgress(position);
    }

    @Override
    public void showProgressbar(boolean show) {
        if (show)
            pb.setVisibility(View.VISIBLE);
        else
            pb.setVisibility(View.GONE);
    }

    @Override
    public ContainerLayout getContainer() {
        return rl_video_container;
    }

    @Override
    public void setScaleTypeText(int text) {
        switch (text) {
            case WatchLive.FIT_DEFAULT:
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_default));
                break;
            case WatchLive.FIT_CENTER_INSIDE:
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_center));
                break;
            case WatchLive.FIT_X:
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_x));
                break;
            case WatchLive.FIT_Y:
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_y));
                break;
            case WatchLive.FIT_XY:
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_xy));
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.onFragmentStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onFragmentDestory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.image_action_back) {
            getActivity().finish();
        } else if (i == R.id.iv_play) {
            mPresenter.onPlayClick();
        } else if (i == R.id.iv_fullscreen) {
            mPresenter.changeScreenOri();
        } else if (i == R.id.btn_change_scale_type) {
            mPresenter.changeScaleType();
        }
    }
}
