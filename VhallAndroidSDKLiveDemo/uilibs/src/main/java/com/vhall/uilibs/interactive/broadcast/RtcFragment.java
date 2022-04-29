package com.vhall.uilibs.interactive.broadcast;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.utils.VHInternalUtils;
import com.vhall.business_interactive.InterActive;
import com.vhall.business_interactive.SimpleRoomCallbackV2;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseFragment;
import com.vhall.uilibs.interactive.bean.InteractiveUser;
import com.vhall.uilibs.interactive.bean.StreamData;
import com.vhall.uilibs.interactive.broadcast.config.RtcConfig;
import com.vhall.uilibs.interactive.broadcast.present.IBroadcastContract;
import com.vhall.uilibs.util.DensityUtils;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.RenViewUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.util.UserManger;
import com.vhall.uilibs.util.VhallGlideUtils;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.util.handler.WeakHandler;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;
import com.vhall.vhss.CallBack;
import com.vhall.vhss.data.RoleNameData;
import com.vhall.vhss.data.WebinarInfoData;

import org.json.JSONException;
import org.json.JSONObject;
import org.vhwebrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.vhall.ilss.VHInteractive.CANVAS_LAYOUT_PATTERN_GRID_1;
import static com.vhall.ilss.VHInteractive.CANVAS_LAYOUT_PATTERN_TILED_6_1T5D;


/**
 * @author hkl
 * 互动页面
 */
public class RtcFragment extends BaseFragment implements ViewPager.OnPageChangeListener, IBroadcastContract.RtcFragmentView {

    private final static String KEY_ORIENTATION = "orientation";

    public static RtcFragment getInstance(String orientation, WebinarInfo webinarInfo, MessageServer.Callback callback, ChatServer.Callback chatCallback) {
        RtcFragment fragment = new RtcFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ORIENTATION, orientation);
        fragment.setArguments(bundle);
        fragment.mMessageCallback = callback;
        fragment.mChatCallback = chatCallback;
        fragment.mWebinarInfo = webinarInfo;
        fragment.roomInfo = webinarInfo.getWebinarInfoData();
        return fragment;
    }


    private View rootView;
    private ViewPager viewPager;
    private List<View> views = new ArrayList<>();
    private List<StreamData> streams = new ArrayList<>();
    private ViewPageAdapter adapter;
    private boolean hasShare = false;
    private VHRenderView shareView;
    /**
     * 主讲人用户id
     */
    private String mainId = "-1";
    private WebinarInfoData roomInfo;
    private LinearLayout pointLayout;
    private int selectPoint = 0;
    /**
     * 主讲人小流
     */
    private Stream mainStream = null;
    private boolean isPublic = false;
    private String orientation;
    private UpdateMainStreamLister updateMainStreamLister;
    private InterActive mInteractive;
    private Stream localStream;

    public void setUpdateMainStreamLister(UpdateMainStreamLister updateMainStreamLister) {
        this.updateMainStreamLister = updateMainStreamLister;
    }

    /**
     * 主播是否推直播
     */
    private boolean isPush = false;
    /**
     * 到文档列表不暂停
     */
    private boolean isStop = true;

    public void setStop(boolean stop) {
        isStop = stop;
    }

    /**
     * 直播结束
     */
    private boolean finish = false;

    public Stream getLocalStream() {
        return localStream;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    /**
     * 互动工具
     *
     * @return
     */
    public InterActive getInteractive() {
        return mInteractive;
    }

    private static final int handlerWhat = 101;
    //每次刷新view间隔
    private static final int handlerTime = 1000;
    private final WeakHandler refreshHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == handlerWhat) {
                setViews();
            }
            return false;
        }
    });

    public void updateViewHandler() {
        refreshHandler.removeMessages(handlerWhat);
        refreshHandler.sendEmptyMessageDelayed(handlerWhat, handlerTime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        orientation = getArguments().getString(KEY_ORIENTATION);

        rootView = inflater.inflate(R.layout.fragment_broadcast_rtc, container, false);
        viewPager = rootView.findViewById(R.id.viewpager);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewPager.getLayoutParams();

        layoutParams.width = Math.min(DensityUtils.getScreenHeight(), DensityUtils.getScreenWidth());
        layoutParams.height = Math.min(DensityUtils.getScreenHeight(), DensityUtils.getScreenWidth());

        if ("1".equals(orientation)) {
            //横屏
            layoutParams.gravity = Gravity.CENTER;
            pointLayout = rootView.findViewById(R.id.ll_point2);
        } else {
            pointLayout = rootView.findViewById(R.id.ll_point);
            layoutParams.topMargin = DensityUtils.dpToPxInt(100);
        }
        viewPager.setLayoutParams(layoutParams);
        adapter = new ViewPageAdapter(views);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        adapter.notifyDataSetChanged();
        return rootView;

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int position) {
        selectPoint = position;
        setChoose(selectPoint);


        if (hasShare && position == 0) {
            return;
        }
        if (hasShare) {
            changeStreamVideo(position - 1);
        } else {
            changeStreamVideo(position);
        }
    }

    /**
     * 节省内存 自动关闭其他页面的画面
     * 加载当前页面时候 在打开
     * <p>
     * 因为文档显示 所以 主画面不关闭 自己的也不关
     *
     * @param position 当前选的页面 不包含共享桌面那一页
     */
    private void changeStreamVideo(int position) {
        if (ListUtils.isEmpty(views)) {
            return;
        }
        if (views.size() <= 2) {
            for (int i = 0; i < streams.size(); i++) {
                StreamData streamData = streams.get(i);
                if (streamData.getCamera() == 1) {
                    streamData.getStream().unmuteVideo(null);
                }
            }
            return;
        }
        int a, b;
        a = position * 4;
        b = a + 3;
        for (int i = 0; i < streams.size(); i++) {
            StreamData streamData = streams.get(i);
            if (i >= a && i <= b) {
                if (streamData.getCamera() == 1) {
                    streamData.getStream().unmuteVideo(null);
                }
            } else {
                if (!TextUtils.equals(streamData.getStream().userId, mainId) && !TextUtils.equals(streamData.getStream().userId, roomInfo.getJoin_info().third_party_user_id) && !streamData.getStream().isLocal) {
                    streamData.getStream().muteVideo(null);
                }
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public boolean isPublish() {
        return isPush;
    }

    private WebinarInfo mWebinarInfo;

    public void setRoomInfo(Context context, CallBack callBack) {
        setRoomInfo(context, null, callBack);
    }

    public void setRoomInfo(Context context, VHRenderView renderView, final CallBack callBack) {
        if (roomInfo != null) {
            this.roomInfo = mWebinarInfo.getWebinarInfoData();
            if (roomInfo.roomToolsStatusData != null && !TextUtils.isEmpty(roomInfo.roomToolsStatusData.doc_permission)) {
                mainId = roomInfo.roomToolsStatusData.doc_permission;
            } else {
                mainId = String.valueOf(roomInfo.getWebinar().userinfo.user_id);
            }
            if (mInteractive == null) {
                mInteractive = new InterActive(context, mRoomCallback, mChatCallback, mMessageCallback);
            }
            /**
             * 主持人 建议 5 嘉宾建议根据上麦人数调整
             *  definition 设置上麦分辨率
             */
            int definition = 3;
            if (mWebinarInfo.inav_num > 5 && 10 >= mWebinarInfo.inav_num) {
                definition = 2;
            } else if (mWebinarInfo.inav_num > 10) {
                definition = 1;
            }
            if (UserManger.isHost(roomInfo.getJoin_info().role_name)) {
                definition = 5;
            }
            mInteractive.init(mWebinarInfo, new RequestCallback() {
                @Override
                public void onSuccess() {
                    if (callBack != null) {
                        callBack.onSuccess("onSuccess");
                    }
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    if (callBack != null) {
                        callBack.onError(errorCode, errorMsg);
                    }
                }
            });
            VHRenderView tempRenderView = new VHRenderView(context);
            if (renderView == null) {
                renderView = tempRenderView;
            }
            mInteractive.setDefinition(definition);
            mInteractive.setLocalView(renderView, Stream.VhallStreamType.VhallStreamTypeAudioAndVideo, null);
            localStream = mInteractive.getLocalStream();
            RtcConfig.setInteractive(mInteractive);
        }
    }

    public void enterRoom() {
        mInteractive.enterRoom();
    }


    class RoomCallback extends SimpleRoomCallbackV2 {

        @Override
        public void onDidConnect(Room room, JSONObject jsonObject) {
            if (mInteractive != null) {
                mInteractive.publish();
            }
        }

        @Override
        public void onDidError(Room room, Room.VHRoomErrorStatus vhRoomErrorStatus, String s) {
            for (Stream stream : room.getRemoteStreams()) {
                removeStream(stream);
            }
        }

        @Override
        public void onDidPublishStream(Room room, Stream stream) {
            Log.e("rtc", "onDidPublishStream");
            isPublic = true;
            if (updateMainStreamLister != null) {
                updateMainStreamLister.setIsPublic(true);
            }
            boolean add = true;
            if (!ListUtils.isEmpty(streams)) {
                for (int i = 0; i < streams.size(); i++) {
                    StreamData streamData = streams.get(i);
                    if (streamData.getStream().userId.equals(stream.userId)) {
                        streams.remove(streamData);
                        streams.add(new StreamData(stream));
                        add = false;
                    }
                }
            }
            if (add) {
                streams.add(new StreamData(stream));
            }
            if (TextUtils.equals(stream.userId, mainId)) {
                mainStream = stream;
            }
            updateViewHandler();
        }

        @Override
        public void onDidUnPublishStream(Room room, Stream stream) {
            isPublic = false;
            if (updateMainStreamLister != null) {
                updateMainStreamLister.setIsPublic(false);
            }
            streams.remove(new StreamData(stream));
            if (TextUtils.equals(stream.userId, mainId)) {
                mainStream = null;
            }
            updateViewHandler();
        }

        @Override
        public void onDidSubscribeStream(Room room, Stream stream) {
            addStream(stream);
        }

        @Override
        public void onDidUnSubscribeStream(Room room, Stream stream) {
            removeStream(stream);
        }

        @Override
        public void onDidChangeStatus(Room room, Room.VHRoomStatus vhRoomStatus) {
            switch (vhRoomStatus) {
                case VHRoomStatusDisconnected:// 异常退出
                    //TODO 销毁页面
                    break;
                case VHRoomStatusError:
                    Log.e("rtc", "VHRoomStatusError");
                    if (getActivity() != null) {
                        baseShowToast("当前房间异常");
                        getActivity().finish();
                    }
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
        public void onDidRemoveStream(Room room, Stream stream) {
            removeStream(stream);
        }

        @Override
        public void onDidUpdateOfStream(Stream stream, JSONObject jsonObject) {
            updateStream(stream);
        }

        @Override
        public void onStreamMixed(JSONObject jsonObject) {
            if (!isPush) {
                JSONObject config = new JSONObject();
                try {
                    /**
                     * adaptiveLayoutMode
                     * 支持 16人画面
                     */
                    config.put("profile", Room.getProfile(Room.BROADCAST_VIDEO_PROFILE_1080P_1));
                    config.put("adaptiveLayoutMode", 2);
                    config.put("precast_pic_exist", false);
                    broadCast(config, new CallBack() {
                        @Override
                        public void onSuccess(Object result) {
                            isPush = true;
                            //允许上麦
                            mInteractive.setHandsUp("1", null);
                            onLiveSuccess();
                        }

                        @Override
                        public void onError(int eventCode, String msg) {
                            baseShowToast(msg);
                            getActivity().finish();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onLiveSuccess() {
        if (updateMainStreamLister != null) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateMainStreamLister.onLiveSuccess();
                    }
                });
            }
        }
    }


    private RoomCallback mRoomCallback = new RoomCallback();

    private MessageServer.Callback mMessageCallback;
    private ChatServer.Callback mChatCallback;


    public void setMainId(String mainId) {
        if (TextUtils.equals(this.mainId, mainId)) {
            return;
        }
        this.mainId = mainId;
        updateViewHandler();
    }

    public void updatePic(String userId, int voice, int camera) {
        if (ListUtils.isEmpty(streams)) {
            return;
        }
        updatePicImpl(userId, voice, camera);
    }

    public void updateRoleName(RoleNameData roleNameData) {
        RenViewUtils.updateRoleName(roleNameData);
        if (ListUtils.isEmpty(views)) {
            return;
        }
        for (int i = 0; i < views.size(); i++) {
            setViewPic(true, i, 0, 0, "");
        }
    }

    private void updatePicImpl(String userId, int voice, int camera) {
        for (int i = 0; i < streams.size(); i++) {
            StreamData streamData = streams.get(i);
            if (userId.equals(streamData.getStream().userId)) {
                if (voice != -1) {
                    streamData.setVoice(voice);
                    streams.remove(i);
                    streams.add(i, streamData);
                    setViewPic(i, voice, camera, "");
                    return;
                }
                if (camera != -1) {
                    streamData.setCamera(camera);
                    streams.remove(i);
                    streams.add(i, streamData);
                    setViewPic(i, voice, camera, streamData.getAvatar());
                    return;
                }
            }
        }
    }

    /**
     * 下麦自己
     */
    public void noSpeak() {
        if (mInteractive != null) {
            if (localStream != null) {
                mInteractive.unpublish(null);
            }
            //1主播
            if (UserManger.isHost(roomInfo.getJoin_info().getRole_name())) {
                mInteractive.broadCastRoom(2, 0, null);
            } else {
                mInteractive.setUserNoSpeak(null);
            }
        }
    }

    private void setViewPic(int i, int voice, int camera, String avatar) {
        setViewPic(false, i, voice, camera, avatar);
    }

    private void setViewPic(boolean isRole, int i, int voice, int camera, String avatar) {
        if (ListUtils.isEmpty(views)) {
            return;
        }
        View view;
        /**
         * 当前的view 在一页4个中的第几个
         */
        int viewLocation = i % 4;
        /**
         *当前的view 在第几页
         */
        int viewNumber = i < 4 ? 0 : i / 4;

        if (hasShare) {
            view = views.get(viewNumber + 1);
        } else {
            view = views.get(viewNumber);
        }
        StreamData streamData = streams.get(i);
        switch (viewLocation) {
            case 0:
                if (isRole) {
                    TextView viewById = view.findViewById(R.id.tv_name1);
                    RenViewUtils.setRole(viewById, streamData.getRole(), streamData.getName(), mContext);
                    return;
                }
                if (voice != -1) {
                    boolean hasAudio = 1 == streamData.getVoice();
                    if (hasAudio) {
                        view.findViewById(R.id.iv_audio1).setVisibility(View.GONE);
                    } else {
                        view.findViewById(R.id.iv_audio1).setVisibility(View.VISIBLE);
                    }
                }
                if (camera != -1) {
                    boolean hasVideo = 1 == streamData.getCamera();
                    if (!hasVideo) {
                        ImageView ivAvatar = view.findViewById(R.id.avatar1);
                        ivAvatar.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.iv_video1).setVisibility(View.VISIBLE);
                        VhallGlideUtils.loadCircleImage(mContext, UserManger.judgePic(avatar), R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivAvatar);
                    } else {
                        view.findViewById(R.id.avatar1).setVisibility(View.GONE);
                        view.findViewById(R.id.iv_video1).setVisibility(View.GONE);
                    }
                }
                break;
            case 1:
                if (isRole) {
                    TextView viewById = view.findViewById(R.id.tv_name2);
                    RenViewUtils.setRole(viewById, streamData.getRole(), streamData.getName(), mContext);
                    return;
                }
                if (voice != -1) {
                    boolean hasAudio = 1 == streamData.getVoice();
                    if (hasAudio) {
                        view.findViewById(R.id.iv_audio2).setVisibility(View.GONE);
                    } else {
                        view.findViewById(R.id.iv_audio2).setVisibility(View.VISIBLE);
                    }
                }
                if (camera != -1) {
                    boolean hasVideo = 1 == streamData.getCamera();
                    if (!hasVideo) {
                        ImageView ivAvatar = view.findViewById(R.id.avatar2);
                        ivAvatar.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.iv_video2).setVisibility(View.VISIBLE);
                        VhallGlideUtils.loadCircleImage(mContext, UserManger.judgePic(avatar), R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivAvatar);
                    } else {
                        view.findViewById(R.id.avatar2).setVisibility(View.GONE);
                        view.findViewById(R.id.iv_video2).setVisibility(View.GONE);
                    }
                }
                break;
            case 2:
                if (isRole) {
                    TextView viewById = view.findViewById(R.id.tv_name3);
                    RenViewUtils.setRole(viewById, streamData.getRole(), streamData.getName(), mContext);
                    return;
                }
                if (voice != -1) {
                    boolean hasAudio = 1 == streamData.getVoice();
                    if (hasAudio) {
                        view.findViewById(R.id.iv_audio3).setVisibility(View.GONE);
                    } else {
                        view.findViewById(R.id.iv_audio3).setVisibility(View.VISIBLE);
                    }
                }
                if (camera != -1) {
                    boolean hasVideo = 1 == streamData.getCamera();
                    if (!hasVideo) {
                        ImageView ivAvatar = view.findViewById(R.id.avatar3);
                        ivAvatar.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.iv_video3).setVisibility(View.VISIBLE);
                        VhallGlideUtils.loadCircleImage(mContext, UserManger.judgePic(avatar), R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivAvatar);
                    } else {
                        view.findViewById(R.id.avatar3).setVisibility(View.GONE);
                        view.findViewById(R.id.iv_video3).setVisibility(View.GONE);
                    }
                }
                break;
            case 3:
                if (isRole) {
                    TextView viewById = view.findViewById(R.id.tv_name4);
                    RenViewUtils.setRole(viewById, streamData.getRole(), streamData.getName(), mContext);
                    return;
                }
                if (voice != -1) {
                    boolean hasAudio = 1 == streamData.getVoice();
                    if (hasAudio) {
                        view.findViewById(R.id.iv_audio4).setVisibility(View.GONE);
                    } else {
                        view.findViewById(R.id.iv_audio4).setVisibility(View.VISIBLE);
                    }
                }
                if (camera != -1) {
                    boolean hasVideo = 1 == streamData.getCamera();
                    if (!hasVideo) {
                        ImageView ivAvatar = view.findViewById(R.id.avatar4);
                        ivAvatar.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.iv_video4).setVisibility(View.VISIBLE);
                        VhallGlideUtils.loadCircleImage(mContext, UserManger.judgePic(avatar), R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivAvatar);
                    } else {
                        view.findViewById(R.id.avatar4).setVisibility(View.GONE);
                        view.findViewById(R.id.iv_video4).setVisibility(View.GONE);
                    }
                }
                break;
            default:
                break;
        }

    }

    private void sortStreams() {
        Collections.sort(streams, new Comparator<StreamData>() {
            @Override
            public int compare(StreamData o1, StreamData o2) {
                InteractiveUser user1 = VhallUtil.parseStreamUser(o1.getStream());
                InteractiveUser user2 = VhallUtil.parseStreamUser(o2.getStream());
                int order1 = VHInternalUtils.getOrderNum(user1.role);
                int order2 = VHInternalUtils.getOrderNum(user2.role);
                if (order1 > order2) {
                    return -1;
                } else if (order1 < order2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    /**
     * 重新设置视图
     */
    private void setViews() {
        if (ListUtils.isEmpty(streams)) {
            clear();
            return;
        }
        sortStreams();
        StreamData main = null;
        for (StreamData streamData : streams) {
            Stream stream = streamData.getStream();
            if (stream == null || TextUtils.isEmpty(stream.streamId)) {
                streams.remove(new StreamData(stream));
            } else {
                if (TextUtils.equals(stream.userId, String.valueOf(roomInfo.getWebinar().userinfo.user_id))) {
                    main = streamData;
                }
            }
        }
        if (main != null && !TextUtils.isEmpty(main.getStream().streamId)) {
            streams.remove(main);
            streams.add(0, main);
        }
        views.clear();
        if (hasShare && shareView != null) {
            views.add(shareView);
        }
        List<View> view = RenViewUtils.getViews(streams, mainId, mContext);
        if (!ListUtils.isEmpty(view)) {
            views.addAll(view);
            adapter.notifyDataSetChanged();
            for (int i = 0; i < streams.size(); i++) {
                if (streams.get(i) == null) {
                    return;
                }
                if (TextUtils.equals(mainId, streams.get(i).getStream().userId)) {
                    mainStream = streams.get(i).getStream();
                    if (updateMainStreamLister != null) {
                        updateMainStreamLister.updateMainStream(mainStream);
                    }
                    if (!hasShare && isPush) {
                        setLayout(mainStream, CANVAS_LAYOUT_PATTERN_TILED_6_1T5D);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
        viewPager.setCurrentItem(selectPoint);
        if (hasShare) {
            if (selectPoint == 0) {
                changeStreamVideo(0);
            } else {
                changeStreamVideo(selectPoint - 1);
            }
        } else {
            changeStreamVideo(selectPoint);
        }
        setChoose(selectPoint);
    }

    private void setLayout(Stream mainStream, int layout) {
        if (mInteractive != null && roomInfo != null) {
            if (!UserManger.isHost(roomInfo.getJoin_info().getRole_name())) {
                return;
            }
            if (mainStream != null) {
                //旁路主讲人设置未大画面
                mainStream.setMixLayoutMainScreen("", null);
            }
            mInteractive.broadCastRoom(1, layout, null);

        } else {
            Log.e("BroadcastRtcFragment", "基本信息为空");
        }
    }

    private void setChoose(int selectPoint) {
        if (pointLayout == null)
            return;
        if (pointLayout.getChildCount() != views.size()) {
            pointLayout.removeAllViews();
            for (int i = 0; i < views.size(); i++) {
                ImageView imageView = new ImageView(mContext);
                imageView.setBackgroundResource(R.drawable.select_viewpage_point);
                pointLayout.addView(imageView);
            }
        }
        if (pointLayout.getChildCount() < 2) {
            pointLayout.setVisibility(View.GONE);
            return;
        } else {
            pointLayout.setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < pointLayout.getChildCount(); i++) {
            if (selectPoint == i) {
                pointLayout.getChildAt(i).setEnabled(true);
            } else {
                pointLayout.getChildAt(i).setEnabled(false);
            }
        }
    }

    public void clear() {
        if (!ListUtils.isEmpty(views))
            for (View view : views) {
                if (view instanceof OnlyRenderView) {
                    ((OnlyRenderView) view).destroy();
                }
            }
        views.clear();
        streams.clear();
        if (pointLayout != null)
            pointLayout.removeAllViews();
        selectPoint = 0;
        if (hasShare && shareView != null) {
            views.add(shareView);
            setChoose(selectPoint);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 添加共享画面
     */
    private void addShareView(Stream stream) {
        hasShare = true;
        shareView = new VHRenderView(mContext);
        shareView.init(null, null);
        shareView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        stream.addRenderView(shareView);
        views.add(0, shareView);
        adapter.notifyDataSetChanged();
        setLayout(stream, CANVAS_LAYOUT_PATTERN_GRID_1);
        setChoose(selectPoint);
    }

    /**
     * 移除共享画面
     */
    private void removeShareView() {
        if (views == null) {
            return;
        }
        if (hasShare && shareView != null) {
            hasShare = false;
            views.remove(0);
            pointLayout.removeViewAt(0);
            setChoose(selectPoint);
            shareView.release();
            shareView = null;
            adapter.notifyDataSetChanged();
        }
        if (mainStream != null) {
            setLayout(mainStream, CANVAS_LAYOUT_PATTERN_TILED_6_1T5D);
        } else {
            setLayout(null, CANVAS_LAYOUT_PATTERN_TILED_6_1T5D);
        }
    }

    public Stream getMainStream() {
        return mainStream;
    }

    @Override
    public void onDestroy() {
        clear();
        RenViewUtils.destroy();
        super.onDestroy();
    }


    void addStream(Stream stream) {
        /**
         *    VhallStreamTypeOnlyAudio = 0,
         *    VhallStreamTypeOnlyVideo = 1,
         *    VhallStreamTypeAudioAndVideo = 2,
         *    VhallStreamTypeScreen = 3,  // 屏幕共享
         *    VhallStreamTypeFile = 4 //插播
         *    VhallStreamTypeVideoPatral = 5 //  视频轮训 需要过滤不显示
         */


        /**
         *  视频轮训 需要过滤不显示
         */
        if (stream.getStreamType() == 5) {
            return;
        }

        /**
         *只要订阅到有人插播视频了，就把麦克风关一下。 解决插播时文件声音混入别人说话的声音。
         */
        if (stream.getStreamType() == 4 && isPublic) {
            mInteractive.switchDevice(roomInfo.join_info.third_party_user_id, "1", "0", null);
        }
        if (stream.getStreamType() == 3 || stream.getStreamType() == 4) {
            addShareView(stream);
            return;
        }
        boolean add = true;
        if (!ListUtils.isEmpty(streams)) {
            for (int i = 0; i < streams.size(); i++) {
                StreamData streamData = streams.get(i);
                if (streamData.getStream().userId.equals(stream.userId)) {
                    streams.remove(streamData);
                    streams.add(new StreamData(stream));
                    add = false;
                }
            }
        }
        if (add) {
            streams.add(new StreamData(stream));
        }
        updateViewHandler();
        if (TextUtils.equals(stream.userId, mainId)) {
            mainStream = stream;
        }
    }

    void removeStream(Stream stream) {
        if (stream == null) {
            return;
        }
        /**
         *  视频轮训 需要过滤不显示
         */
        if (stream.getStreamType() == 5) {
            return;
        }

        stream.removeAllRenderView();
        if (stream.getStreamType() == 3 || stream.getStreamType() == 4) {
            removeShareView();
            return;
        }
        streams.remove(new StreamData(stream));
        updateViewHandler();
        /**
         * 修改断流 收回主讲人 修改为 下麦收回主讲人
         */
//        if (TextUtils.equals(stream.userId, mainId)) {
//            mainStream = null;
//            if (!TextUtils.equals(stream.userId, String.valueOf(roomInfo.getWebinar().userinfo.user_id)) && UserManger.isHost(roomInfo.getJoin_info().getRole_name())) {
//                mInteractive.setMainSpeaker(roomInfo.webinar.userinfo.user_id, new TipsCallback());
//            }
//        }
    }


    void updateStream(Stream stream) {
        if (stream == null) {
            return;
        }
        if (TextUtils.equals(stream.userId, mainId)) {
            mainStream = stream;
            if (updateMainStreamLister != null) {
                updateMainStreamLister.updateMainStream(mainStream);
            }
        }
        for (int i = 0; i < streams.size(); i++) {
            StreamData streamData = streams.get(i);
            if (streamData == null) {
                return;
            }
            if (stream.streamId.equals(streamData.getStream().streamId)) {
                streams.remove(i);
                streamData.setStream(stream);
                streams.add(i, streamData);
                return;
            }
        }
    }

    /**
     * 轻享逻辑
     * 上麦后默认推旁路直播
     * SaaS 无需处理
     *
     * @param jsonObject
     */
    public void broadCast(JSONObject jsonObject, final CallBack callback) {
        if (UserManger.isHost(roomInfo.join_info.role_name)) {
            mInteractive.broadCastRoom(1, jsonObject, callback);
        }
    }

    /**
     * 轻享逻辑
     * 上麦后默认推旁路直播
     * SaaS 无需处理
     *
     * @param type
     */
    public void broadCast(int type, final CallBack callback) {
        if (UserManger.isHost(roomInfo.join_info.role_name)) {

            mInteractive.broadCastRoom(1, type, callback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (roomInfo != null && mInteractive != null && UserManger.isHost(roomInfo.getJoin_info().getRole_name()) && isPush) {
            rePush();
        }
        isStop = true;
    }

    /**
     * 1、后台一段时间摄像头会被释放
     * 2、重新获取摄像头需要初始化本地stream
     * 3、重新调用推流publish
     */
    private void rePush() {
        VHRenderView tempRenderView = new VHRenderView(getActivity());
        mInteractive.setLocalView(tempRenderView, Stream.VhallStreamType.VhallStreamTypeAudioAndVideo, null);
        localStream = mInteractive.getLocalStream();
        mInteractive.unpublished();
        mInteractive.publish();
        if (deviceStatus != null) {
            //从后台进入前台重置麦克风 摄像头状态
            mInteractive.switchDevice(mWebinarInfo.user_id, "1", deviceStatus.getAudioStatus() ? "1" : "0", null);
            mInteractive.switchDevice(mWebinarInfo.user_id, "2", deviceStatus.getVideoStatus() ? "1" : "0", null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (finish) {
            return;
        }
        if (isStop && isPublic && roomInfo != null) {
            mInteractive.unpublish(null);
        }
    }


    public void finish() {
        if (mInteractive == null) {
            return;
        }
        if (isPublic) {
            noSpeak();
        }

        mInteractive = null;
        isPublic = false;
    }

    @Override
    public void onDestroyView() {
        finish();
        super.onDestroyView();
    }

    /**
     * 主持人断网重连接时调用
     */
    public interface UpdateMainStreamLister {
        void updateMainStream(Stream mainStream);

        void setIsPublic(boolean isPublic);

        /**
         * 开播成功
         */
        void onLiveSuccess();
    }

    private IDeviceStatus deviceStatus;

    public void setDeviceStatus(IDeviceStatus status) {
        this.deviceStatus = status;
    }


    public interface IDeviceStatus {
        /**
         * 获取音频状态
         *
         * @return
         */
        boolean getAudioStatus();

        /**
         * 获取视频状态
         *
         * @return
         */
        boolean getVideoStatus();
    }

    @Override
    public void showToast(String content) {
        ToastUtil.showToast(content);
    }

    @Override
    public void sendMsg(String msg, String type, CallBack callBack) {
        if (mInteractive != null)
            mInteractive.sendMsg(msg, type, callBack);
    }
}
