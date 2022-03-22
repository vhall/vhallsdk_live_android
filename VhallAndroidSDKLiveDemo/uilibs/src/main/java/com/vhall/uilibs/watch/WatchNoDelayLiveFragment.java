package com.vhall.uilibs.watch;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vhall.business.data.WebinarInfo;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.DensityUtils;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;
import com.vhall.vhss.data.ScrollInfoData;

import org.vhwebrtc.SurfaceViewRenderer;

import java.util.HashMap;

/**
 * 观看无延时直播的Fragment
 * 不支持 水印 弹幕 跑马灯 分辨率 暂停
 */
public class WatchNoDelayLiveFragment extends Fragment implements WatchContract.LiveView, WatchContract.LiveNoDelayView, View.OnClickListener {

    private WatchContract.LivePresenter mPresenter;

    private ImageView clickOrientation, clickStart, mVrButton;

    private TextView fragmentDownloadSpeed;
    private RelativeLayout mContainerLayout;
    private ImageView btn_change_scaletype;
    ImageView btn_danmaku;
    ProgressBar progressbar;
    private Activity context;

    private ImageView iv_dlna;
    private static WebinarInfo webinarInfo;
    private String mainUserId;
    private View noDelayCl;
    private VHRenderView shareRenderView;
    private VHRenderView mainRenderView;
    private LinearLayout llRenderView;
    private int renderViewHeight = 0;
    private int renderViewWidth = 0;

    private ImageView mSwitchCamera, mSwitchAudio, mSwitchVideo;
    private Group group_public;


    boolean hasVideoOpen = false;
    boolean hasAudioOpen = false;


    public static WatchNoDelayLiveFragment newInstance(WebinarInfo webinarInfoData) {
        webinarInfo = webinarInfoData;
        return new WatchNoDelayLiveFragment();
    }

    @Override
    public void setPresenter(WatchContract.LivePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.watch_live_fragment, container, false);
        initView(root);
        reFreshView();
        return root;
    }

    private void initView(View root) {
        iv_dlna = (ImageView) root.findViewById(R.id.iv_dlna);
        clickStart = (ImageView) root.findViewById(R.id.click_rtmp_watch);
        clickOrientation = (ImageView) root.findViewById(R.id.click_rtmp_orientation);
        mContainerLayout = (RelativeLayout) root.findViewById(R.id.rl_container);
        fragmentDownloadSpeed = (TextView) root.findViewById(R.id.fragment_download_speed);
        mVrButton = (ImageView) root.findViewById(R.id.btn_headtracker);
        btn_change_scaletype = (ImageView) root.findViewById(R.id.btn_change_scaletype);
        btn_danmaku = (ImageView) root.findViewById(R.id.btn_danmaku);
        btn_danmaku.setVisibility(View.GONE);
        iv_dlna.setVisibility(View.GONE);
        clickStart.setVisibility(View.GONE);
        mVrButton.setVisibility(View.GONE);
        fragmentDownloadSpeed.setVisibility(View.GONE);
        clickOrientation.setVisibility(View.GONE);
        btn_change_scaletype.setVisibility(View.GONE);
        progressbar = (ProgressBar) root.findViewById(R.id.progressbar);

        shareRenderView = root.findViewById(R.id.share_render_view);
        shareRenderView.init(null, null);
        shareRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
//        shareRenderView.setZOrderOnTop(true);
        shareRenderView.setVisibility(View.GONE);
        root.findViewById(R.id.image_action_back).setOnClickListener(this);
        if (mPresenter != null) {
            mPresenter.start();
        }
        if (webinarInfo != null && webinarInfo.getWebinarInfoData() != null) {
            mainUserId = webinarInfo.getWebinarInfoData().roomToolsStatusData.doc_permission;
        }
        // 互动无延迟直播
        if (webinarInfo != null && TextUtils.equals(webinarInfo.webinar_type, "3")) {
            noDelayCl = LayoutInflater.from(getActivity()).inflate(R.layout.item_no_delay_view, null);
            mContainerLayout.removeAllViews();
            mContainerLayout.addView(noDelayCl);
            mSwitchCamera = noDelayCl.findViewById(R.id.image_switch_camera);
            mSwitchCamera.setOnClickListener(this);
            mSwitchVideo = noDelayCl.findViewById(R.id.image_switch_video);
            mSwitchVideo.setOnClickListener(this);
            mSwitchAudio = noDelayCl.findViewById(R.id.image_switch_audio);
            group_public = noDelayCl.findViewById(R.id.group_public);
            mSwitchAudio.setOnClickListener(this);
            llRenderView = noDelayCl.findViewById(R.id.ll_render_view);
            mainRenderView = noDelayCl.findViewById(R.id.main_render_view);
            mainRenderView.init(null, null);
            mainRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
            renderViewWidth = DensityUtils.getScreenWidth() / 5;
            renderViewHeight = renderViewWidth * 9 / 15;
            mContainerLayout.post(new Runnable() {
                @Override
                public void run() {
                    int height = mContainerLayout.getHeight();
                    ViewGroup.LayoutParams layoutParams = mainRenderView.getLayoutParams();
                    layoutParams.height = height - renderViewHeight;
                    layoutParams.width = height * 15 / 9;
                    mainRenderView.setLayoutParams(layoutParams);
                }
            });
        }
    }

    @Override
    public RelativeLayout getWatchLayout() {
        return mContainerLayout;
    }


    @Override
    public void setPlayPicture(boolean state) {

    }

    @Override
    public void setDownSpeed(String text) {
        fragmentDownloadSpeed.setText(text);
    }

    @Override
    public void showLoading(boolean isShow) {
        if (isShow) {
            progressbar.setVisibility(View.VISIBLE);
        } else {
            progressbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.image_action_back) {
            getActivity().onBackPressed();
        } else if (i == R.id.image_switch_camera) {
            mPresenter.onSwitchCamera();
        } else if (i == R.id.image_switch_video) {
            mPresenter.onSwitchVideo(hasVideoOpen);
        } else if (i == R.id.image_switch_audio) {
            mPresenter.onSwitchAudio(hasAudioOpen);
        }
    }

    @Override
    public void showRadioButton(HashMap map) {
    }

    @Override
    public void setScaleButtonText(int type) {
    }

    @Override
    public void addDanmu(String danmu) {
    }

    @Override
    public void reFreshView() {
    }

    @Override
    public void liveFinished() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().finish();
        }
    }

    @Override
    public void updateMain(String mainId) {
        mainUserId = mainId;
        Stream beforeMainStream = mainRenderView.getStream();
        Stream nowMainStream = null;
        if (beforeMainStream != null) {
            beforeMainStream.removeAllRenderView();
        }
        for (int i = 0; i < llRenderView.getChildCount(); i++) {
            VHRenderView vhRenderView = (VHRenderView) llRenderView.getChildAt(i);
            if (vhRenderView != null && vhRenderView.getStream() != null && vhRenderView.getStream().userId.equals(mainUserId)) {
                llRenderView.removeViewAt(i);
                nowMainStream = vhRenderView.getStream();
                vhRenderView.release();
                break;
            }
        }
        if (nowMainStream != null) {
            nowMainStream.removeAllRenderView();
            nowMainStream.addRenderView(mainRenderView);
        }
        VHRenderView vhRenderView = new VHRenderView(getActivity());
        vhRenderView.init(null, null);
        beforeMainStream.addRenderView(vhRenderView);
        llRenderView.addView(vhRenderView, renderViewWidth, renderViewHeight);
    }

    @Override
    public void updateStream(boolean del, Stream stream, VHRenderView newRenderView) {
        if (webinarInfo == null || stream == null || stream.streamId == null || stream.userId == null) {
            return;
        }
        if (leaveRoom) {
            return;
        }

        if (!del) {
            if (stream.getStreamType() == 3 || stream.getStreamType() == 4) {
                addShareView(stream);
                return;
            }
            if (!TextUtils.equals("3", webinarInfo.webinar_type)) {
                //音视频布局
                newRenderView.getHolder().setFormat(PixelFormat.TRANSPARENT);
                newRenderView.setZOrderMediaOverlay(true);
                newRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
                mContainerLayout.removeAllViews();
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.alignWithParent = true;
                mContainerLayout.addView(newRenderView, params);
            } else {
                //互动布局
                stream.removeAllRenderView();
                if (stream.userId.equals(mainUserId)) {
                    stream.addRenderView(mainRenderView);
                } else {
                    VHRenderView vhRenderView = new VHRenderView(getActivity());
                    vhRenderView.init(null, null);
                    stream.addRenderView(vhRenderView);
                    llRenderView.addView(vhRenderView, renderViewWidth, renderViewHeight);
                }
            }
        } else {
            if (stream.getStreamType() == 3 || stream.getStreamType() == 4) {
                removeShareView(stream);
                return;
            }
            if (!TextUtils.equals("3", webinarInfo.webinar_type)) {
                mContainerLayout.removeAllViews();
            } else {
                if (stream.userId.equals(mainUserId)) {
                    stream.removeAllRenderView();
                } else {
                    int childCount = llRenderView.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        VHRenderView view = (VHRenderView) llRenderView.getChildAt(i);
                        if (view != null && view.getStream() != null && view.getStream().streamId.equals(stream.streamId)) {
                            view.release();
                            llRenderView.removeView(view);
                            stream.removeAllRenderView();
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateVideoFrame(int status) {
        if (status == 1) {
            mSwitchVideo.setImageDrawable(getResources().getDrawable(R.drawable.icon_video_open));
            hasVideoOpen = false;
        } else {
            mSwitchVideo.setImageDrawable(getResources().getDrawable(R.drawable.icon_video_close));
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
    public void enterInteractive(boolean isEnter) {
        if (group_public != null) {
            group_public.setVisibility(isEnter ? View.VISIBLE : View.GONE);

            if (isEnter) {
                mPresenter.onUpMic();
            } else {
                mPresenter.onDownMic(false);
            }
        }

    }

    //当前是不是在互动房间
    //上麦后回去到其他互动房间
    private boolean leaveRoom = false;


    private void addShareView(Stream stream) {
        shareRenderView.setVisibility(View.VISIBLE);
        stream.removeAllRenderView();
        stream.addRenderView(shareRenderView);
        noDelayCl.setVisibility(View.GONE);
        llRenderView.setVisibility(View.GONE);
    }

    private void removeShareView(Stream stream) {
        shareRenderView.setVisibility(View.GONE);
        noDelayCl.setVisibility(View.VISIBLE);
        llRenderView.setVisibility(View.VISIBLE);
        stream.removeAllRenderView();
    }

    @Override
    public void setScrollInfo(final ScrollInfoData scrollInfo) {
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mPresenter != null) {
            mPresenter.onDownMic(true);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
        super.onDestroy();
    }
}
