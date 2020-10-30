package com.vhall.uilibs.interactive;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.vhall.uilibs.R;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

import org.json.JSONObject;


public class InteractiveFragment extends Fragment implements InteractiveContract.InteractiveFraView, View.OnClickListener {
    private InteractiveContract.InteractiveFraPresenter mPresenter;

    private Context mContext;
    private RelativeLayout mLocal;
    private ImageView mDownMic, mSwitchCamera, mSwitchAudio, mSwitchVideo, mLocalViewLayer;
    private LinearLayout mContainar;
    private int width, height;
    boolean hasVideoOpen = false;
    boolean hasAudioOpen = false;

    @Override
    public void setPresenter(InteractiveContract.InteractiveFraPresenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        height = VhallUtil.dp2px(mContext, 90);
        width = VhallUtil.dp2px(mContext, 120);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interactive, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    private void initView() {
        mContainar = getView().findViewById(R.id.containar);
        mLocal = getView().findViewById(R.id.local);
        mLocalViewLayer = getView().findViewById(R.id.local_view_layer);
        mDownMic = getView().findViewById(R.id.image_down_mic);
        mDownMic.setOnClickListener(this);
        mSwitchCamera = getView().findViewById(R.id.image_switch_camera);
        mSwitchCamera.setOnClickListener(this);
        mSwitchVideo = getView().findViewById(R.id.image_switch_video);
        mSwitchVideo.setOnClickListener(this);
        mSwitchAudio = getView().findViewById(R.id.image_switch_audio);
        mSwitchAudio.setOnClickListener(this);
    }

    public static InteractiveFragment newInstance() {
        return new InteractiveFragment();
    }

    @Override
    public void addLocalView(VHRenderView view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mLocal.addView(view, 0, params);
    }

    @Override
    public void updateVideoFrame(int status) {
        if (status == 1) {
            mSwitchVideo.setImageDrawable(getResources().getDrawable(R.drawable.icon_video_open));
            mLocalViewLayer.setVisibility(View.GONE);
            hasVideoOpen = false;
        } else {
            mSwitchVideo.setImageDrawable(getResources().getDrawable(R.drawable.icon_video_close));
            mLocalViewLayer.setVisibility(View.VISIBLE);
            hasVideoOpen = true;
        }
    }

    @Override
    public void updateAudioFrame(int status) {
        if (status == 1) {
            mSwitchAudio.setImageDrawable(getResources().getDrawable(R.drawable.icon_audio_open));
            hasAudioOpen = false;
        } else {
            mSwitchAudio.setImageDrawable(getResources().getDrawable(R.drawable.icon_audio_close));
            hasAudioOpen = true;
        }
    }

    @Override
    public void addStream(VHRenderView view) {
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        mContainar.addView(view, 0, params);
    }

    @Override
    public void removeStream(Stream stream) {
        if (stream == null) return;
        int childCount = mContainar.getChildCount();
        for (int i = 0; i < childCount; i++) {
            VHRenderView view = (VHRenderView) mContainar.getChildAt(i);
            if (view.getStream().userId == stream.userId) {
                view.release();
                mContainar.removeView(view);
                break;
            }
        }
    }

    @Override
    public void updateStream(Stream stream) {
        //流状态变更 ，如需同步订阅端 音视频禁止状态标识可在此处处理；
        JSONObject obj = stream.muteStream;
        boolean muteAudio = obj.optBoolean("audio");// true 禁音、false 未禁音
        boolean muteVideo = obj.optBoolean("video");// true 禁视频、 false 未禁视频

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.image_down_mic) {
            mPresenter.onDownMic();
        } else if (i == R.id.image_switch_camera) {
            mPresenter.onSwitchCamera();
        } else if (i == R.id.image_switch_video) {
            mPresenter.onSwitchVideo(hasVideoOpen);
            // hasVideoOpen = !hasVideoOpen;
        } else if (i == R.id.image_switch_audio) {
            mPresenter.onSwitchAudio(hasAudioOpen);
            // hasAudioOpen = !hasAudioOpen;
        }
    }

    @Override
    public void onDestroy() {
        Log.e("onDestroy", "onDestroy");
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
        super.onDestroy();
    }
}
