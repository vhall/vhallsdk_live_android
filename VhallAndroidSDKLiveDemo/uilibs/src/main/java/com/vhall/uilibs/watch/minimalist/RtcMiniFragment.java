package com.vhall.uilibs.watch.minimalist;

import static com.vhall.uilibs.util.ToastUtil.showToast;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business_interactive.InterActive;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.bean.StreamData;
import com.vhall.uilibs.util.DensityUtils;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

import org.vhwebrtc.SurfaceViewRenderer;

// 上麦成功之后进入，自定义显示布局 互动房间

public class RtcMiniFragment extends Fragment {

    private InterActive interactive;
    private WebinarInfo webinarInfoData;
    private VHRenderView shareRenderView;
    private VHRenderView mainRenderView;
    private LinearLayout llRenderView;
    protected Activity mActivity;
    private String bigShowUserId;
    private int renderViewHeight = 0;
    private int renderViewWidth = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    public static RtcMiniFragment newInstance() {
        return new RtcMiniFragment();
    }

    public void init(WebinarInfo webinarInfoData, String bigShowUserId) {
        this.bigShowUserId = bigShowUserId;
        this.webinarInfoData = webinarInfoData;
    }

    public InterActive getInteractive() {
        return interactive;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_rtc_mini, container, false);
        initRtc();
        llRenderView = inflate.findViewById(R.id.ll_render_view);
        mainRenderView = inflate.findViewById(R.id.main_render_view);
        shareRenderView = inflate.findViewById(R.id.share_render_view);
        shareRenderView.setVisibility(View.GONE);
        mainRenderView.init(null, null);
        mainRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        mainRenderView.setZOrderMediaOverlay(true);
        mainRenderView.setZOrderOnTop(true);

        shareRenderView.init(null, null);
        shareRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        shareRenderView.setZOrderMediaOverlay(true);
        shareRenderView.setZOrderOnTop(true);

        renderViewWidth = DensityUtils.getScreenWidth() / 5;
        renderViewHeight = renderViewWidth * 9 / 16;

        ViewGroup.LayoutParams layoutParams = mainRenderView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = DensityUtils.getScreenWidth() * 9 / 16 - renderViewHeight;
        mainRenderView.setLayoutParams(layoutParams);

        return inflate;
    }

    private void initRtc() {
        //因为和直播在一个页面，且直播一直存在所以 复用直播的消息messageEventCallback，chatCallBack webinarInfoData
        interactive = new InterActive(getContext(), new RoomCallback(), null, null);
        interactive.init(false, webinarInfoData, new RequestCallback() {
            @Override
            public void onSuccess() {
                setLocalView();
                interactive.enterRoom();
                /**
                 * since 6.3.1
                 * 互动特殊事件通知
                 * 因为直播里面有被其他人踢出监听所以不用设置
                 *
                 *  interactive.setListener
                 */
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                showToast(errorMsg);
            }
        });

    }

    VHRenderView vhRenderView;

    private void setLocalView() {
        vhRenderView = new VHRenderView(getContext());
        vhRenderView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        vhRenderView.init(null, null);
        interactive.setLocalView(vhRenderView, Stream.VhallStreamType.VhallStreamTypeAudioAndVideo, null);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    public void updateBigShow(String mainId) {
        if (mActivity.isFinishing()) {
            return;
        }
        bigShowUserId = mainId;
        //之前显示的大画面
        Stream beforeMainStream = mainRenderView.getStream();
        //之后显示的大画面
        Stream nowMainStream = null;
        if (beforeMainStream != null) {
            beforeMainStream.removeAllRenderView();
        }
        for (int i = 0; i < llRenderView.getChildCount(); i++) {
            MiniRenderView vhRenderView = (MiniRenderView) llRenderView.getChildAt(i);
            if (vhRenderView != null && vhRenderView.getStream() != null && vhRenderView.getStream().userId.equals(bigShowUserId)) {
                llRenderView.removeViewAt(i);
                nowMainStream = vhRenderView.getStream();
                vhRenderView.destroy();
                break;
            }
        }
        if (nowMainStream != null) {
            nowMainStream.removeAllRenderView();
            nowMainStream.addRenderView(mainRenderView);
        }
        if (beforeMainStream != null) {
            MiniRenderView MiniRenderView = new MiniRenderView(getContext(), new StreamData(beforeMainStream));
            llRenderView.addView(MiniRenderView, renderViewWidth, renderViewHeight);
        }
    }

    public void updateStream(boolean del, Stream stream, MiniRenderView newRenderView) {
        if (mActivity.isFinishing() || stream == null || stream.streamId == null || stream.userId == null) {
            return;
        }
        if (!del) {
            if (stream.getStreamType() == 3 || stream.getStreamType() == 4) {
                addShareView(stream);
                return;
            }
            stream.removeAllRenderView();
            if (stream.userId.equals(bigShowUserId)) {
                stream.addRenderView(mainRenderView);
            } else {
                MiniRenderView MiniRenderView = new MiniRenderView(getContext(), new StreamData(stream));
                llRenderView.addView(MiniRenderView, renderViewWidth, renderViewHeight);
            }

        } else {
            if (stream.getStreamType() == 3 || stream.getStreamType() == 4) {
                removeShareView(stream);
                return;
            }
            if (stream.userId.equals(bigShowUserId)) {
                stream.removeAllRenderView();
            } else {
                stream.removeAllRenderView();
                llRenderView.removeView(newRenderView);
                newRenderView.destroy();
            }
        }
    }

    private void removeShareView(Stream stream) {
        shareRenderView.setVisibility(View.GONE);
        llRenderView.setVisibility(View.VISIBLE);
        mainRenderView.setVisibility(View.VISIBLE);
        stream.removeAllRenderView();
    }

    private void addShareView(Stream stream) {
        shareRenderView.setVisibility(View.VISIBLE);
        llRenderView.setVisibility(View.GONE);
        mainRenderView.setVisibility(View.GONE);
        stream.removeAllRenderView();
        stream.addRenderView(shareRenderView);
    }

    class RoomCallback implements InterActive.RoomCallback {

        @Override
        public void onDidConnect() {//进入房间 推流
            if (interactive != null) {
                interactive.publish();
            }
        }

        @Override
        public void onDidError() {//进入房间失败
        }

        @Override
        public void onDidPublishStream() {// 上麦
            updateStream(false, interactive.getLocalStream(), null);
        }

        @Override
        public void onDidUnPublishStream() {//下麦

        }

        @Override
        public void onDidSubscribeStream(Stream stream, final VHRenderView newRenderView) {
            /**
             *  视频轮巡 需要过滤不显示
             */
            if (stream != null && stream.getStreamType() == 5) {
                return;
            }
            updateStream(false, stream, null);
        }

        @Override
        public void onDidRoomStatus(Room room, Room.VHRoomStatus vhRoomStatus) {
            switch (vhRoomStatus) {
                case VHRoomStatusDisconnected:// 异常退出
                    break;
                case VHRoomStatusError:
                    mActivity.finish();
                    break;
                case VHRoomStatusReady:
                    break;
                case VHRoomStatusConnected: // 重连进房间
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onDidRemoveStream(Room room, final Stream stream) {
            /**
             *  视频轮巡 需要过滤不显示
             */
            if (stream != null && stream.getStreamType() == 5) {
                return;
            }
            if (stream != null && (stream.getStreamType() == 3 || stream.getStreamType() == 4)) {
                removeShareView(stream);
                return;
            }
            int childCount = llRenderView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                MiniRenderView view = (MiniRenderView) llRenderView.getChildAt(i);
                if (stream != null && view != null && view.getStream() != null && view.getStream().streamId.equals(stream.streamId)) {
                    updateStream(true, stream, view);
                }
            }
        }
    }

    public void onDownMic() {
        interactive.unpublish(new RequestCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (interactive != null) {
            onDownMic();
            interactive.onDestroy();
        }
    }
}