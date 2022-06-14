package com.vhall.uilibs.watch;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vhall.player.vod.VodPlayerView;
import com.vhall.uilibs.R;

import java.util.List;

/**
 * 观看回放的Fragment
 */
public class WatchPlaybackFragment extends Fragment implements WatchContract.PlaybackView, View.OnClickListener {
    private final String TAG = WatchPlaybackFragment.class.getName();
    WatchContract.PlaybackPresenter mPresenter;
    VodPlayerView surface_view;//视频区容器
    ImageView iv_play, btn_changescaletype;
    SeekBar seekbar;
    TextView tv_current_time, tv_end_time, tv_play_speed;
    ProgressBar pb;
    ImageView iv_dlna_playback,btn_mute;
    RadioGroup rg_quality;
    Context mContext;

    public static WatchPlaybackFragment newInstance() {
        WatchPlaybackFragment articleFragment = new WatchPlaybackFragment();
        return articleFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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
        surface_view = getView().findViewById(R.id.surface_view);
        btn_changescaletype = getView().findViewById(R.id.btn_change_scale_type);
        iv_dlna_playback = getView().findViewById(R.id.iv_dlna_playback);
        btn_mute = getView().findViewById(R.id.btn_mute);
        rg_quality = getView().findViewById(R.id.rg_quality);
        btn_changescaletype.setOnClickListener(this);
        iv_dlna_playback.setOnClickListener(this);
        btn_mute.setOnClickListener(this);
        pb = (ProgressBar) getView().findViewById(R.id.pb);
        iv_play = (ImageView) getView().findViewById(R.id.iv_play);
        iv_play.setOnClickListener(this);
        getView().findViewById(R.id.iv_fullscreen).setOnClickListener(this);
        seekbar = (SeekBar) getView().findViewById(R.id.seekbar);
        tv_current_time = (TextView) getView().findViewById(R.id.tv_current_time);
        tv_end_time = (TextView) getView().findViewById(R.id.tv_end_time);
        getView().findViewById(R.id.image_action_back).setOnClickListener(this);
        tv_play_speed = (TextView) getView().findViewById(R.id.tv_play_speed);
        tv_play_speed.setOnClickListener(this);
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
        //自动播放
        mPresenter.onPlayClick();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.startPlay();
            }
        },500);
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
        if (show) {
            pb.setVisibility(View.VISIBLE);
        } else {
            pb.setVisibility(View.GONE);
        }
    }

    @Override
    public VodPlayerView getVideoView() {
        return surface_view;
    }

    @Override
    public void setScaleTypeText(int text) {
        switch (text) {
            case 0://none
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_default));
                break;
            case 1://fit
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_center));
                break;
            case 2://fill
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_xy));
                break;
                default:
                    break;
        }
    }

    @Override
    public void setQuality(List<String> qualities) {
        if (qualities != null && qualities.size() > 0) {
            for (int i = 0; i < qualities.size(); i++) {
                RadioButton button = new RadioButton(mContext);
                button.setText(qualities.get(i));
                rg_quality.addView(button);
            }

            rg_quality.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton rb = group.findViewById(checkedId);
                    String text = rb.getText().toString();
                    mPresenter.onSwitchPixel(text);
                }
            });

        }
    }

    @Override
    public void setQualityChecked(String dpi) {
        int count = rg_quality.getChildCount();
        if (TextUtils.isEmpty(dpi) || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            RadioButton rb = (RadioButton) rg_quality.getChildAt(i);
            if (rb.getText().equals(dpi)) {
                rb.setChecked(true);
            }
        }
    }

    @Override
    public void setPlaySpeedText(String text) {
        tv_play_speed.setText(text);
    }

    @Override
    public void onStop() {
        super.onStop();
//        mPresenter.onFragmentStop();
        //mPresenter.startPlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onFragmentDestory();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        surface_view.getLayoutParams().width=ViewGroup.LayoutParams.MATCH_PARENT;
        surface_view.getLayoutParams().height=ViewGroup.LayoutParams.MATCH_PARENT;
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
        } else if (i == R.id.iv_dlna_playback) {
            // Todo 投屏相关
             mPresenter.showDevices();
        } else if (i == R.id.tv_play_speed) {
            mPresenter.setSpeed();
        }
    }

    // 静音 默认关闭
    private boolean mute = false;

}
