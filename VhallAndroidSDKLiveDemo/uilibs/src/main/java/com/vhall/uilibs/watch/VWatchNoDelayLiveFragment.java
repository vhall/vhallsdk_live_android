package com.vhall.uilibs.watch;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
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
import com.vhall.uilibs.util.emoji.EmojiUtils;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;
import com.vhall.vhss.data.ScrollInfoData;
import com.vhall.vhss.network.ChatNetworkRequest;

import org.webrtc.SurfaceViewRenderer;

import java.util.HashMap;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

/**
 * 观看全屏无延迟直播直播的Fragment
 * <p>
 * 不支持 水印 弹幕 跑马灯 分辨率 暂停
 */
public class VWatchNoDelayLiveFragment extends Fragment implements WatchContract.LiveView, WatchContract.LiveNoDelayView, View.OnClickListener {

    private WatchContract.LivePresenter mPresenter;
    private TextView fragmentDownloadSpeed;
    private RelativeLayout mContainerLayout;
    private BaseDanmakuParser mParser;

    private ImageView btn_change_scaletype;
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


    public static VWatchNoDelayLiveFragment newInstance(WebinarInfo webinarInfoData) {
        webinarInfo = webinarInfoData;
        return new VWatchNoDelayLiveFragment();
    }

    @Override
    public void setPresenter(WatchContract.LivePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.v_watch_live_fragment, container, false);
        initView(root);
        reFreshView();
        return root;
    }

    private void initView(View root) {
        mContainerLayout = root.findViewById(R.id.rl_container);
        fragmentDownloadSpeed = root.findViewById(R.id.fragment_download_speed);
        fragmentDownloadSpeed.setVisibility(View.GONE);
        btn_change_scaletype = root.findViewById(R.id.btn_change_scaletype);

        shareRenderView = root.findViewById(R.id.share_render_view);
        shareRenderView.init(null, null);
        shareRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        shareRenderView.setVisibility(View.GONE);
        root.findViewById(R.id.image_action_back).setOnClickListener(this);
        if (webinarInfo != null && webinarInfo.getWebinarInfoData() != null) {
            mainUserId = webinarInfo.getWebinarInfoData().roomToolsStatusData.doc_permission;
        }
        mPresenter.start();
        // 互动无延迟直播
        if (webinarInfo != null && TextUtils.equals(webinarInfo.webinar_type, "3")) {
            btn_change_scaletype.setVisibility(View.GONE);
            noDelayCl = LayoutInflater.from(getActivity()).inflate(R.layout.item_v_no_delay_view, null);
            mContainerLayout.removeAllViews();
            mContainerLayout.addView(noDelayCl, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            llRenderView = noDelayCl.findViewById(R.id.ll_render_view);
            mainRenderView = noDelayCl.findViewById(R.id.main_render_view);
            mainRenderView.init(null, null);
            mainRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
            renderViewWidth = DensityUtils.getScreenWidth() / 5;
            renderViewHeight = renderViewWidth * 9 / 15;
            mSwitchCamera = noDelayCl.findViewById(R.id.image_switch_camera);
            mSwitchCamera.setOnClickListener(this);
            mSwitchVideo = noDelayCl.findViewById(R.id.image_switch_video);
            mSwitchVideo.setOnClickListener(this);
            mSwitchAudio = noDelayCl.findViewById(R.id.image_switch_audio);
            group_public = noDelayCl.findViewById(R.id.group_public);
            mSwitchAudio.setOnClickListener(this);
        }

        btn_change_scaletype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renderView != null) {
                    if (mode == 0) {
                        renderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
                        mode = 1;
                    } else if (mode == 1) {
                        renderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                        mode = 2;
                    } else if (mode == 2) {
                        renderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeNone);
                        mode = 0;
                    }
                }
            }
        });
    }

    private int mode = 0;

    @Override
    public RelativeLayout getWatchLayout() {
        return mContainerLayout;
    }

    @Override
    public void setPlayPicture(boolean state) {

    }

    @Override
    public void setDownSpeed(String text) {
    }

    @Override
    public void showLoading(boolean isShow) {

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

    /**
     * 切换分辨率
     *
     * @param map 0 : 无效不可用  1 ：有效可用
     */
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
    public void setScrollInfo(ScrollInfoData scrollInfo) {

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPresenter != null)
            mPresenter.onDownMic(true);
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

    //视频无延迟的view
    private VHRenderView renderView;

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
                newRenderView.getHolder().setFormat(PixelFormat.TRANSPARENT);
                newRenderView.setZOrderMediaOverlay(true);
                newRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                mContainerLayout.removeAllViews();
                renderView = newRenderView;
                mContainerLayout.addView(newRenderView);
            } else {
                stream.removeAllRenderView();
                if (stream.userId.equals(mainUserId)) {
                    stream.addRenderView(mainRenderView);
                } else {
                    VHRenderView vhRenderView = new VHRenderView(getActivity());
                    vhRenderView.init(null, null);
                    stream.addRenderView(vhRenderView);
                    Log.e("vhall_", vhRenderView.getStream() + "   add ");
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
                            Log.e("vhall_", " remove");
                            llRenderView.removeView(view);
                            stream.removeAllRenderView();
                            return;
                        }
                    }
                }
            }
        }
    }


    //当前是不是在互动房间
    //上麦后回去到其他互动房间
    private boolean leaveRoom = false;

    private void addShareView(Stream stream) {
        shareRenderView.setVisibility(View.VISIBLE);
        llRenderView.setVisibility(View.GONE);
        noDelayCl.setVisibility(View.GONE);
        stream.removeAllRenderView();
        stream.addRenderView(shareRenderView);
    }

    private void removeShareView(Stream stream) {
        shareRenderView.setVisibility(View.GONE);
        llRenderView.setVisibility(View.VISIBLE);
        noDelayCl.setVisibility(View.VISIBLE);
        stream.removeAllRenderView();
    }
}
