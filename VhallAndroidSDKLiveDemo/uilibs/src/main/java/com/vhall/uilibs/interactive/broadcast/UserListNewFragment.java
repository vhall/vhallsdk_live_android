package com.vhall.uilibs.interactive.broadcast;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.utils.VHInternalUtils;
import com.vhall.business_interactive.InterActive;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseFragment;
import com.vhall.uilibs.interactive.base.OnNoDoubleClickListener;
import com.vhall.uilibs.interactive.broadcast.config.RtcConfig;
import com.vhall.uilibs.interactive.dialog.ChooseTypeDialog;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.util.UserManger;
import com.vhall.vhss.data.RoleNameData;
import com.vhall.vhss.data.UserStateListData;
import com.vhall.vhss.data.WebinarInfoData;
import com.vhall.vhss.network.ActivityNetworkRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @author hkl
 */
public class UserListNewFragment extends BaseFragment {

    private InterActive mInterActive = RtcConfig.getInterActive();

    public static UserListNewFragment getInstance(String type, WebinarInfoData roomInfoData, boolean isGuest, boolean canSpeak, boolean canManger) {
        UserListNewFragment fragment = new UserListNewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putBoolean("isGuest", isGuest);
        bundle.putBoolean("canSpeak", canSpeak);
        bundle.putSerializable("roomInfoData", roomInfoData);
        bundle.putBoolean("canManger", canManger);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static String TYPE_ONLINE = "online";
    public static String TYPE_KICK_OUT = "kick_out";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private UserListAdapter adapter;
    private int page = 1;
    //    private UserListPresent present;
    private View inflate;
    /**
     * 是否加载数据
     */
    private boolean canPostData = true;
    /**
     * 是否可以管理用户
     */
    private boolean canManger = true;
    /**
     * 主讲人id
     */
    private String keynoteSpeakId = "-1";
    private String type;
    private boolean isGuest;
    private TextView tvEmpty;
    private WebinarInfoData roomInfoData;
    /**
     * 是不是互动直播
     */
    private boolean canSpeak;

    //嘉宾邀请人上麦权限
    private boolean guestCanInventPermission = false;
    //嘉宾邀请人上麦 有权限且是主讲人
    private boolean guestCanInvent = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflate = inflater.inflate(R.layout.fragment_user_list, container, false);

        type = getArguments().getString("type");
        roomInfoData = (WebinarInfoData) getArguments().getSerializable("roomInfoData");
        if (roomInfoData != null) {
            List<String> permission = roomInfoData.permission;
            if (permission != null) {
                guestCanInventPermission = permission.contains("100037");
            }
            //更新当前主讲人 权限
            if (TextUtils.equals(keynoteSpeakId, roomInfoData.join_info.third_party_user_id) && guestCanInventPermission) {
                guestCanInvent = true;
            } else {
                guestCanInvent = false;
            }
        }
        isGuest = getArguments().getBoolean("isGuest", false);
        canSpeak = getArguments().getBoolean("canSpeak", false);
        canManger = getArguments().getBoolean("canManger", true);
        recyclerView = inflate.findViewById(R.id.recycle_view);
        tvEmpty = inflate.findViewById(R.id.tv_empty);
        refreshLayout = inflate.findViewById(R.id.refresh_layout);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        adapter = new UserListAdapter();
        recyclerView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                refreshUserList();
            }
        });
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                page++;
                refreshUserList();
            }
        }, recyclerView);

        refreshUserList();
        return inflate;
    }

    private RoleNameData roleNameData = new RoleNameData("主持人", "嘉宾", "助理");

    public void refreshUserList() {
        if (!canPostData) {
            return;
        }
        VhallSDK.getRoleName(roomInfoData.webinar.id, new RequestDataCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof RoleNameData) {
                    roleNameData = (RoleNameData) result;
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
        canPostData = false;
        if (TYPE_KICK_OUT.equals(type)) {
            mInterActive.getLimitUserList(page, 10, new RequestDataCallbackV2<UserStateListData>() {
                @Override
                public void onSuccess(UserStateListData result) {
                    canPostData = true;
                    if (refreshLayout != null) {
                        refreshLayout.setRefreshing(false);
                    }

                    tvEmpty.setVisibility(View.GONE);
                    if (result != null) {
                        dealData(result.getList());
                    }
                }

                @Override
                public void onError(int eventCode, String msg) {
                    canPostData = true;
                    if (refreshLayout != null) {
                        refreshLayout.setRefreshing(false);
                    }

                    if (VHInternalUtils.isEmpty(adapter.getData())) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText(R.string.vhall_forbit_member);
                    } else {
                        adapter.loadMoreComplete();
                    }
                }
            });
        } else {
            mInterActive.getOnlineUserList(page, 10, new RequestDataCallbackV2<UserStateListData>() {
                @Override
                public void onSuccess(UserStateListData result) {
                    canPostData = true;
                    if (refreshLayout != null) {
                        refreshLayout.setRefreshing(false);
                    }
                    if (result != null) {
//                        sortUsers(result.getList());
                        dealData(result.getList());
                    }
                }

                @Override
                public void onError(int eventCode, String msg) {
                    canPostData = true;
                    if (refreshLayout != null) {
                        refreshLayout.setRefreshing(false);
                    }
                    if (VHInternalUtils.isEmpty(adapter.getData())) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText(R.string.vhall_no_member);
                    } else {
                        adapter.loadMoreComplete();
                    }
                }
            });
        }
    }


    private void dealData(List<UserStateListData.DataBeen> lists) {
        if (page == 1) {
            if (!ListUtils.isEmpty(lists)) {
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                adapter.setNewData(sortList(lists));

                if (lists.size() < 10) {
                    adapter.loadMoreEnd();
                } else {
                    page++;
                }
            } else {
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        } else {
            if (!ListUtils.isEmpty(lists)) {
                adapter.addData(lists);
                if (lists.size() < 10) {
                    adapter.loadMoreEnd();
                    return;
                }
                adapter.loadMoreComplete();
            } else {
                adapter.loadMoreEnd();
            }
        }
    }

    private List<UserStateListData.DataBeen> sortList(List<UserStateListData.DataBeen> lists) {
        List<UserStateListData.DataBeen> sortList = new ArrayList<>();
        if (TYPE_KICK_OUT.equals(type) || lists.size() < 2) {
            return lists;
        }
        Collections.sort(lists, new Comparator<UserStateListData.DataBeen>() {
            @Override
            public int compare(UserStateListData.DataBeen o1, UserStateListData.DataBeen o2) {
                int order1 = VHInternalUtils.getOrderNum(o1.getRole_name());
                int order2 = VHInternalUtils.getOrderNum(o2.getRole_name());
                if (order1 > order2) {
                    return -1;
                } else if (order1 < order2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return lists;
    }

    public void setMainId(String mainId) {
        keynoteSpeakId = mainId;
        // roomInfoData.join_info.third_party_user_id 等同于  WebinarInfo.user_id
        //更新当前主讲人  主讲人是自己并且有操控上麦的权限

        if (roomInfoData != null) {
            Log.e("vhall_", "roomInfoData.join_info.third_party_user_id     " + roomInfoData.join_info.third_party_user_id);


        }
        if (roomInfoData != null && TextUtils.equals(keynoteSpeakId, roomInfoData.join_info.third_party_user_id) && guestCanInventPermission) {
            guestCanInvent = true;
        } else {
            guestCanInvent = false;
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 第一个是否是设为主讲人
     */
    boolean isKeynote = false;
    /**
     * 是否被禁言
     */
    boolean isBanned = false;
    /**
     * 是否被踢出
     */
    boolean isKickOut = false;

    class UserListAdapter extends BaseQuickAdapter<UserStateListData.DataBeen, BaseViewHolder> {

        public UserListAdapter() {
            super(R.layout.item_user_list);
        }

        /**
         * @author hkl
         * 获取禁言列表/获取踢出用户列表/获取在线用户列表/获取特殊用户列表
         * <p>
         * "role_name": "1", //角色 1主持人2观众3助理4嘉宾
         * "device_type": "1", // 设备类型 1手机端 2PC 3SDK
         * "device_status": "1", // 设备状态  1可以上麦2不可以上麦
         * "is_banned": "1" //是否禁言 1是0否
         */
        @Override
        protected void convert(@NonNull BaseViewHolder helper, final UserStateListData.DataBeen info) {
            ImageView ivAvatar = helper.getView(R.id.iv_avatar);
            Glide.with(getActivity())
                    .load(UserManger.judgePic(info.getAvatar()))
                    .apply(new RequestOptions()
                            .placeholder(R.mipmap.ic_avatar)
                            .error(R.mipmap.ic_avatar))
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(ivAvatar);
            TextView tvRoleTag = helper.getView(R.id.tv_tag);
            TextView tvMic = helper.getView(R.id.tv_mic);
            helper.setText(R.id.tv_name, BaseUtil.getLimitString(info.getNickname()));

            if (TextUtils.equals(info.getAccount_id(), keynoteSpeakId)) {
                helper.getView(R.id.iv_keynote_speak).setVisibility(View.VISIBLE);
            } else {
                helper.getView(R.id.iv_keynote_speak).setVisibility(View.GONE);
            }

            if (TYPE_KICK_OUT.equals(type)) {
                helper.getView(R.id.iv_banned).setVisibility(View.VISIBLE);
                if (1 == info.getIs_kicked()) {
                    helper.getView(R.id.iv_kick_out).setVisibility(View.VISIBLE);
                } else {
                    helper.getView(R.id.iv_kick_out).setVisibility(View.GONE);
                }
                helper.getView(R.id.iv_mic).setVisibility(View.GONE);
                tvMic.setVisibility(View.GONE);
                helper.getView(R.id.iv_keynote_speak).setVisibility(View.GONE);
            } else {
                helper.getView(R.id.iv_kick_out).setVisibility(View.GONE);
                if (1 == info.getDevice_status()) {
                    tvMic.setVisibility(View.VISIBLE);
                    helper.getView(R.id.iv_mic).setVisibility(View.GONE);
                    if (1 == info.getIs_speak()) {
                        tvMic.setTextColor(ContextCompat.getColor(mContext, R.color.color_99));
                        tvMic.setBackgroundResource(R.drawable.shape_user_mic_down);
                        tvMic.setText("下麦");
                    } else {
                        tvMic.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                        tvMic.setText("上麦");
                        tvMic.setBackgroundResource(R.drawable.shape_user_mic_up);
                    }
                } else {
                    helper.getView(R.id.iv_mic).setVisibility(View.VISIBLE);
                    tvMic.setVisibility(View.GONE);
                }
            }
            if (isGuest && !guestCanInvent) {
                tvMic.setVisibility(View.GONE);
            }
            Log.e("vhall_", "guestCanInvent " + guestCanInvent);
            switch (judgeRole(info.getRole_name())) {
                case "1":
                    if (isGuest) {
                        helper.getView(R.id.iv_more).setVisibility(View.GONE);
                    } else {
                        helper.getView(R.id.iv_more).setVisibility(View.VISIBLE);
                    }
                    tvRoleTag.setVisibility(View.VISIBLE);
                    tvRoleTag.setText(roleNameData.host_name);
                    tvRoleTag.setBackgroundResource(R.drawable.shape_user_main);
                    tvMic.setVisibility(View.GONE);
                    helper.getView(R.id.iv_banned).setVisibility(View.GONE);
                    helper.getView(R.id.iv_mic).setVisibility(View.GONE);
                    break;
                case "2":
                    helper.getView(R.id.iv_more).setVisibility(View.VISIBLE);
                    tvRoleTag.setVisibility(View.GONE);
                    break;
                case "3":
                    if (isGuest) {
                        helper.getView(R.id.iv_more).setVisibility(View.GONE);
                    } else {
                        helper.getView(R.id.iv_more).setVisibility(View.VISIBLE);
                    }
                    tvRoleTag.setVisibility(View.VISIBLE);
                    tvRoleTag.setText(roleNameData.assistant_name);
                    tvRoleTag.setBackgroundResource(R.drawable.shape_user_assistant);
                    break;
                case "4":
                    if (isGuest) {
                        helper.getView(R.id.iv_more).setVisibility(View.GONE);
                    } else {
                        helper.getView(R.id.iv_more).setVisibility(View.VISIBLE);
                    }
                    tvRoleTag.setVisibility(View.VISIBLE);
                    tvRoleTag.setText(roleNameData.guest_name);
                    tvRoleTag.setBackgroundResource(R.drawable.shape_user_guest);
                    break;
                default:
                    break;
            }
            if (1 == info.getIs_banned()) {
                helper.getView(R.id.iv_banned).setVisibility(View.VISIBLE);
                tvMic.setVisibility(View.GONE);
            } else {
                helper.getView(R.id.iv_banned).setVisibility(View.GONE);
            }
            if (!canSpeak || "3".equals(judgeRole(info.getRole_name()))) {
                tvMic.setVisibility(View.GONE);
            }
            if (!canManger) {
                helper.getView(R.id.iv_more).setVisibility(View.GONE);
            }
            helper.getView(R.id.iv_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moreClick(info);
                }
            });
            tvMic.setOnClickListener(new OnNoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    setSpeak(info);
                }
            });
        }

        String vssType = "";
        String vssTypeHint = "";

        public void moreClick(final UserStateListData.DataBeen info) {
            if (info == null) {
                return;
            }
            List<String> strings = new ArrayList<>();
            if (TYPE_ONLINE.equals(type)) {
                isKickOut = false;
                if ("1".equals(judgeRole(judgeRole(info.getRole_name())))) {
                    strings.add("设为主讲人");
                    isKeynote = true;
                } else if ("4".equals(judgeRole(info.getRole_name())) && 1 == info.getIs_speak()) {
                    strings.add("设为主讲人");
                    isKeynote = true;
                } else {
                    isKeynote = false;
                }
                if (!"1".equals(judgeRole(info.getRole_name()))) {
                    if (1 == info.getIs_banned()) {
                        strings.add("取消禁言");
                        isBanned = true;
                    } else {
                        strings.add("聊天禁言");
                        isBanned = false;
                    }
                    strings.add("踢出活动");
                }
            } else {
                isKickOut = info.getIs_kicked() == 1;
                if (1 == info.getIs_banned()) {
                    strings.add("取消禁言");
                    isBanned = true;
                } else {
                    strings.add("聊天禁言");
                    isBanned = false;
                }
                if (isKickOut) {
                    strings.add("取消踢出");
                } else {
                    strings.add("踢出活动");
                }
            }
            final ChooseTypeDialog chooseTypeDialog = new ChooseTypeDialog(mContext, strings);
            chooseTypeDialog.setOnItemClickLister(new ChooseTypeDialog.onItemClickLister() {
                @Override
                public void onItemClick(int option) {
                    switch (option) {
                        case 1:
                            if (isKeynote) {
                                if (TextUtils.equals(keynoteSpeakId, info.getAccount_id())) {
                                    ToastUtil.showToast(getContext(), "请勿重复设置主讲人");
                                    return;
                                }
                                mInterActive.setMainSpeaker(info.getAccount_id(), new RequestCallback() {
                                    @Override
                                    public void onSuccess() {
                                        ToastUtil.showToast(getContext(), "设为主讲人成功");
                                    }

                                    @Override
                                    public void onError(int eventCode, String msg) {
                                        ToastUtil.showToast(getContext(), "设为主讲人失败");
                                    }
                                });
                            } else {
                                setBanned(isBanned, info);
                            }
                            break;
                        case 2:
                            if (isKeynote) {
                                setBanned(isBanned, info);
                            } else {
                                setKick(isKickOut, info);
                            }
                            break;
                        case 3:
                            setKick(isKickOut, info);
                            break;
                        default:
                            break;
                    }
                    if (chooseTypeDialog.isShowing()) {
                        chooseTypeDialog.cancel();
                    }
                }
            });
            chooseTypeDialog.show();
        }

        void setSpeak(final UserStateListData.DataBeen info) {
            if (info.getIs_speak() == 1) {
                mInterActive.downMic(info.getAccount_id(), new RequestCallback() {
                    @Override
                    public void onSuccess() {
                        ToastUtil.showToast(getContext(), "下麦" + info.getNickname() + "成功");
                    }

                    @Override
                    public void onError(int eventCode, String msg) {
                        ToastUtil.showToast(getContext(), msg);
                    }
                });

            } else {
                mInterActive.invite(info.getAccount_id(), new RequestCallback() {

                    @Override
                    public void onSuccess() {
                        ToastUtil.showToast(getContext(), "已发送上麦邀请");
                    }

                    @Override
                    public void onError(int eventCode, String msg) {
                        ToastUtil.showToast(getContext(), msg);
                    }
                });
            }
        }

        void setKick(boolean isKickOut, UserStateListData.DataBeen info) {
            if (isKickOut) {
                vssTypeHint = "取消踢出";
                vssType = "0";
            } else {
                vssTypeHint = "踢出";
                vssType = "1";
            }
            mInterActive.setKickOut(info.getAccount_id(), vssType, new RequestCallback() {
                @Override
                public void onSuccess() {
                    ToastUtil.showToast(getContext(), vssTypeHint + "成功");
                }

                @Override
                public void onError(int eventCode, String msg) {
                    ToastUtil.showToast(getContext(), vssTypeHint + "失败");
                }
            });
        }

        void setBanned(boolean isBanned, UserStateListData.DataBeen info) {
            if (isBanned) {
                vssTypeHint = "取消禁言";
                vssType = "0";
            } else {
                vssTypeHint = "禁言";
                vssType = "1";
            }
            mInterActive.setBanned(info.getAccount_id(), vssType, new RequestCallback() {
                @Override
                public void onSuccess() {
                    ToastUtil.showToast(getContext(), vssTypeHint + "成功");
                }

                @Override
                public void onError(int eventCode, String msg) {
                    ToastUtil.showToast(getContext(), vssTypeHint + "失败");
                }
            });
        }
    }
    //1 host-主持人；4 guest-嘉宾；3 assistant-助理

    private String judgeRole(String roleName) {
        switch (roleName) {
            case "host":
            case "1":
                return "1";

            case "guest":
            case "4":
                return "4";

            case "assistant":
            case "3":
                return "3";

            default:
                return "2";
        }
    }
}
