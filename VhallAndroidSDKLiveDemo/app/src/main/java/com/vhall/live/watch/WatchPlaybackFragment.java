package com.vhall.live.watch;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vhall.business.widget.ContainerLayout;
import com.vhall.live.R;

/**
 * 观看回放的Fragment
 */
public class WatchPlaybackFragment extends Fragment implements WatchContract.PlaybackView {

    WatchContract.PlaybackPresenter mPresenter;
    ContainerLayout rl_video_container;//视频区容器
    ImageView iv_play, iv_fullscreen;
    SeekBar seekbar;
    TextView tv_pos;
    Button btn_changescaletype;
    ProgressBar pb;

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
        btn_changescaletype = (Button) getView().findViewById(R.id.btn_changescaletype);
        pb = (ProgressBar) getView().findViewById(R.id.pb);
        iv_play = (ImageView) getView().findViewById(R.id.iv_play);
        iv_fullscreen = (ImageView) getView().findViewById(R.id.iv_fullscreen);
        seekbar = (SeekBar) getView().findViewById(R.id.seekbar);
        tv_pos = (TextView) getView().findViewById(R.id.tv_pos);
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onPlayClick();
            }
        });
        iv_fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.changeScreenOri();
            }
        });
        btn_changescaletype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.changeScaleType();
            }
        });
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
    public Activity getmActivity() {
        return getActivity();
    }

    @Override
    public void setPlayIcon(boolean isStop) {
        if (isStop) {
            iv_play.setImageResource(R.drawable.icon_play_play);
        } else {
            iv_play.setImageResource(R.drawable.icon_play_pause);
        }
    }

    @Override
    public void setProgressLabel(String text) {
        tv_pos.setText(text);
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
    public void setScaleTypeText(String text) {
        btn_changescaletype.setText(text);
    }

    @Override
    public int changeScreenOri() {
        if (getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        return getActivity().getRequestedOrientation();
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
}
