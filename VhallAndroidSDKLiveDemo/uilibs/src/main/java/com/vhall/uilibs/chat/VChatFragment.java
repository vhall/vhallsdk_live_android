package com.vhall.uilibs.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
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
public class VChatFragment extends Fragment implements ChatContract.ChatView {

    public static final int CHAT_EVENT_CHAT = 1;
    public static final int CHAT_EVENT_QUESTION = 2;
    private ChatContract.ChatPresenter mPresenter;
    public final int RequestLogin = 0;
    ChatAdapter chatAdapter = new ChatAdapter();
    private int status = -1;
    private RecyclerView lv_chat;

    public static VChatFragment newInstance(int status, boolean isquestion) {
        VChatFragment chatFragment = new VChatFragment();
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
        return inflater.inflate(R.layout.v_chat_fragment, null);
    }


    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lv_chat = getView().findViewById(R.id.recycle_view);
        lv_chat.setLayoutManager(new LinearLayoutManager(getActivity()));
        getView().findViewById(R.id.text_chat_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter != null) {
                    mPresenter.showChatView(false, null, 0);
                }
            }
        });
        status = getArguments().getInt("state");

        lv_chat.setAdapter(chatAdapter);
    }

    @Override
    public void notifyDataChanged(int type, List<ChatServer.ChatInfo> list) {
        if (type == CHAT_EVENT_CHAT) {
            chatAdapter.addData(list);
            if (chatAdapter.getItemCount() > 0)
                lv_chat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void notifyDataChanged(int type, ChatServer.ChatInfo list) {
        if (type == CHAT_EVENT_CHAT) {
            chatAdapter.addData(list);
            if (chatAdapter.getItemCount() > 0)
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
                if (chatEvent == VChatFragment.CHAT_EVENT_CHAT) {
                    mPresenter.sendChat(content);
                } else if (chatEvent == VChatFragment.CHAT_EVENT_QUESTION) {
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
            super(R.layout.v_chat_item);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder viewHolder, ChatServer.ChatInfo chatInfo) {
            TextView tv_chat_content;
            tv_chat_content = viewHolder.getView(R.id.tv_chat_item_text);
            //处理回复消息
            String textContent = "";
            if (chatInfo.replyMsg != null) {
                textContent = chatInfo.replyMsg.user_name + ": " + chatInfo.replyMsg.content.textContent + "\n" + "回复：";
            }
            if (chatInfo.msgData != null) {
                textContent = textContent + chatInfo.msgData.text;
            }
            String msg = "";
            String name = chatInfo.user_name;
            SpannableString styledText;
            switch (chatInfo.event) {
                case MessageChatData.eventMsgKey:
                    if (chatInfo.msgData.type.equals("image")) {
                        if (!TextUtils.isEmpty(chatInfo.msgData.resourceUrl)) {
                            msg = String.format("收到图片---%s", chatInfo.msgData.resourceUrl);
                        } else if (chatInfo.msgData.imageUrls != null) {
                            msg = String.format("收到%d张图片", chatInfo.msgData.imageUrls.size());
                        }
                    } else {
                        msg = textContent;
                    }

                    styledText = new SpannableString(name + ":" + msg);
                    styledText.setSpan(new TextAppearanceSpan(getContext(), R.style.style0), 0, name.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tv_chat_content.setText(styledText);
                    break;
                case MessageChatData.eventCustomKey:
                    msg = textContent;
                    styledText = new SpannableString(name + ":" + msg);
                    styledText.setSpan(new TextAppearanceSpan(getContext(), R.style.style0), 0, name.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tv_chat_content.setText(styledText);
                    break;
                case MessageChatData.eventOnlineKey:
                    tv_chat_content.setText(String.format("%s上线了！角色：%s(%s)", chatInfo.user_name, chatInfo.roleName, chatInfo.role));
                    break;
                case MessageChatData.eventOfflineKey:
                    tv_chat_content.setText(String.format("%s下线了！角色：%s(%s)", chatInfo.user_name, chatInfo.roleName, chatInfo.role));
                    break;
                default:
                    break;
            }
        }

    }
}
