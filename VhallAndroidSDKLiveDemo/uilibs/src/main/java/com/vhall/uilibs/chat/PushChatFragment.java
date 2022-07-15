package com.vhall.uilibs.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.ChatServer;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.util.emoji.EmojiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天页的Fragment
 */
public class PushChatFragment extends Fragment implements ChatContract.ChatView {

    public static final int CHAT_EVENT_CHAT = 1;
    public static final int CHAT_EVENT_QUESTION = 2;

    public static final int CHAT_NORMAL = 0x00;
    public static final int CHAT_SURVEY = 0x01;
    private ChatContract.ChatPresenter mPresenter;
    public final int RequestLogin = 0;
    RecyclerView lv_chat;
    ChatAdapter chatAdapter = new ChatAdapter();
    boolean isquestion = false;
    int status = -1;
    private Activity mActivity;

    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean flag = false;
    private int messageCount = 0;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    public static PushChatFragment newInstance(int status, boolean isquestion) {
        PushChatFragment chatFragment = new PushChatFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("question", isquestion);
        bundle.putInt("state", status);
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.push_chat_fragment, null);
    }


    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lv_chat = getView().findViewById(R.id.recycle_view);
        lv_chat.setLayoutManager(new LinearLayoutManager(mActivity));
        getView().findViewById(R.id.text_chat_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter != null) {
                    mPresenter.showChatView(false, null, 0);
                }
            }
        });
        isquestion = getArguments().getBoolean("question");
        status = getArguments().getInt("state");

        lv_chat.setAdapter(chatAdapter);

        init();
    }

    private void init() {
    }


    @Override
    public void notifyDataChanged(int type, List<ChatServer.ChatInfo> list) {
        if (type == CHAT_EVENT_CHAT) {
            chatAdapter.addData(list);
            if (chatAdapter.getItemCount()>0)
            lv_chat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void notifyDataChanged(int type, ChatServer.ChatInfo list) {
        if (type == CHAT_EVENT_CHAT) {
            chatAdapter.addData(list);
            if (chatAdapter.getItemCount()>0)
            lv_chat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void showToast(String content) {
        if (this.isAdded()) {
            Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void clearChatData() {
        chatAdapter.setNewData(null);
    }

    @Override
    public void performSend(String content, int chatEvent) {
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(getContext(), "发送的消息不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (status) {
            case VhallUtil.BROADCAST://直播界面只能发聊天
                mPresenter.sendChat(content);
                break;
            case VhallUtil.WATCH_LIVE://观看直播界面发聊天和问答
                if (chatEvent == PushChatFragment.CHAT_EVENT_CHAT) {
                    mPresenter.sendChat(content);
                } else if (chatEvent == PushChatFragment.CHAT_EVENT_QUESTION) {
                    mPresenter.sendQuestion(content);
                }
                break;
            case VhallUtil.WATCH_PLAYBACK://回放界面只能发评论(发评论必须保证登陆)
                mPresenter.sendChat(content);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RequestLogin == requestCode) {
            if (resultCode == getActivity().RESULT_OK) {
                if (mPresenter != null) {
                    mPresenter.onLoginReturn();
                }
            }
        }
    }

    @Override
    public void setPresenter(ChatContract.ChatPresenter presenter) {
        mPresenter = presenter;
    }

    class ChatAdapter extends BaseQuickAdapter<ChatServer.ChatInfo, BaseViewHolder> {
        public ChatAdapter() {
            super(R.layout.push_chat_item);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder viewHolder, ChatServer.ChatInfo chatInfo) {
            TextView tv_chat_content = viewHolder.getView(R.id.tv_chat_content);
            TextView tv_chat_time = viewHolder.getView(R.id.tv_chat_time);
            String avatar = chatInfo.avatar;
            if (!TextUtils.isEmpty(avatar) && !avatar.startsWith("http")) {
                avatar = String.format("https:%s", avatar);
            }
            ImageView iv_chat_avatar = viewHolder.getView(R.id.iv_chat_avatar);
            RequestOptions options = new RequestOptions().placeholder(R.drawable.icon_vhall).transform(new CircleCrop());
            Glide.with(getActivity()).load(avatar).apply(options).into(iv_chat_avatar);
            tv_chat_time.setText(chatInfo.time);
            //处理回复消息
            String textContent = "";
            if (chatInfo.replyMsg != null) {
                textContent = chatInfo.replyMsg.user_name + ": " + chatInfo.replyMsg.content.textContent + "\n" + "回复：";
            }
            if (chatInfo.msgData != null) {
                textContent = textContent + chatInfo.msgData.text;
            }
            messageCount++;
            int textColor = messageCount % 3 == 0 ? Color.rgb(130, 180, 150) :
                    messageCount % 3 == 1 ? Color.rgb(120, 170, 230) : Color.rgb(190, 170, 80);

            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    if (chatInfo.msgData != null)
                        if (chatInfo.msgData.type.equals("image")) {
                            StringBuilder builder = new StringBuilder();
                            builder.append("收到图片");
                            List<String> image_urls = chatInfo.msgData.imageUrls;
                            if (image_urls != null && image_urls.size() > 0) {
                                for (String u : image_urls) {
                                    builder.append(String.format("%s;\n", u));
                                }
                            } else if (!TextUtils.isEmpty(chatInfo.msgData.resourceUrl)) {
                                builder.append(chatInfo.msgData.resourceUrl);
                            }
                            viewHolder.setText(R.id.tv_chat_content, builder.toString());
                        } else {
                            viewHolder.setText(R.id.tv_chat_content, EmojiUtils.getEmojiText(mActivity, textContent));
                        }
                    tv_chat_content.setVisibility(View.VISIBLE);
                    viewHolder.setText(R.id.tv_chat_name, chatInfo.user_name + "角色：" + chatInfo.roleName + "(" + chatInfo.role + ")" + " 用户ID：" + chatInfo.account_id);
                    break;
                case ChatServer.eventCustomKey:
                    tv_chat_content.setVisibility(View.VISIBLE);
                    tv_chat_content.setText(EmojiUtils.getEmojiText(mActivity, textContent), TextView.BufferType.SPANNABLE);
                    viewHolder.setText(R.id.tv_chat_name, chatInfo.user_name + "【自定义消息】");
                    break;
                case ChatServer.eventOnlineKey:
                    viewHolder.setText(R.id.tv_chat_name, String.format("%s上线了！角色：%s(%s)", chatInfo.user_name, chatInfo.roleName, chatInfo.role));
                    tv_chat_content.setVisibility(View.INVISIBLE);
                    break;
                case ChatServer.eventOfflineKey:
                    viewHolder.setText(R.id.tv_chat_name, String.format("%s下线了！角色：%s(%s)", chatInfo.user_name, chatInfo.roleName, chatInfo.role));
                    tv_chat_content.setVisibility(View.INVISIBLE);
                    break;
                default:
                    messageCount--;
                    break;
            }
            tv_chat_content.setTextColor(textColor);
            tv_chat_time.setTextColor(textColor);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        flag = false;
    }
}
