package com.vhall.uilibs.chat;

import static com.vhall.business.MessageServer.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.CommonUtil;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.util.emoji.EmojiUtils;
import com.vhall.uilibs.watch.minimalist.ChatMessageData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天页的Fragment
 */
public class ChatFragment extends Fragment implements ChatContract.ChatView {

    public static final int CHAT_EVENT_CHAT = 1;
    public static final int CHAT_EVENT_QUESTION = 2;
    public static final int CHAT_EVENT_EXAM = 3;
    private ChatContract.ChatPresenter mPresenter;
    public final int RequestLogin = 0;
    private RecyclerView lv_chat;
    private SwipeRefreshLayout refreshLayout;
    ChatAdapter chatAdapter = new ChatAdapter();
    QuestionAdapter questionAdapter = new QuestionAdapter();
    boolean isquestion = false;
    int status = -1;

    TextView test_send_custom;
    private Activity mActivity;

    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean flag = false;
    private int page = 1;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    public static ChatFragment newInstance(int status, boolean isquestion) {
        ChatFragment chatFragment = new ChatFragment();
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
        View inflate = inflater.inflate(R.layout.chat_fragment, null);

        return inflate;
    }


    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lv_chat = getView().findViewById(R.id.recycle_view);
        refreshLayout = getView().findViewById(R.id.refresh_layout);
        lv_chat.setLayoutManager(new LinearLayoutManager(mActivity));
        test_send_custom = getView().findViewById(R.id.test_send_custom);
        test_send_custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONObject json = new JSONObject();
                try {
                    json.put("key0", "value");
                    json.put("key1", "0000");
                    json.put("key2", "微吼");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mPresenter != null) {
                    mPresenter.sendCustom(json);
                }
            }
        });
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
        if (isquestion) {
            lv_chat.setAdapter(questionAdapter);
        } else {
            lv_chat.setAdapter(chatAdapter);
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mPresenter != null) {
                    mPresenter.acquireChatRecord(page, new ChatServer.ChatRecordCallback() {
                        @Override
                        public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                            if (ListUtils.isEmpty(list)) {
                                showToast("没有更多数据");
                                return;
                            }
                            List<ChatMessageData> chatMessageDataList = new ArrayList<>();
                            for (ChatServer.ChatInfo chatInfo : list) {
                                ChatMessageData e = new ChatMessageData();
                                e.chatInfo = chatInfo;
                                chatMessageDataList.add(e);
                            }
                            chatAdapter.addData(0, chatMessageDataList);
                            page++;
                        }

                        @Override
                        public void onFailed(int errorcode, String messaage) {
                            showToast(messaage);
                        }
                    });
                }
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
            }
        });
        init();
    }

    private void init() {
    }


    @Override
    public void notifyDataChanged(int type, List<ChatServer.ChatInfo> list) {
        if (type == CHAT_EVENT_CHAT) {
            List<ChatMessageData> chatMessageDataList = new ArrayList<>();
            for (ChatServer.ChatInfo chatInfo : list) {
                ChatMessageData e = new ChatMessageData();
                e.chatInfo = chatInfo;
                chatMessageDataList.add(e);
            }
            chatAdapter.addData(chatMessageDataList);
            if (chatAdapter.getItemCount() > 0)
                lv_chat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        } else {
            questionAdapter.addData(list);
            if (questionAdapter.getItemCount() > 0)
                lv_chat.smoothScrollToPosition(questionAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void notifyDataChanged(int type, ChatServer.ChatInfo list) {
        if (type == CHAT_EVENT_CHAT) {
            ChatMessageData e = new ChatMessageData();
            e.chatInfo = list;
            chatAdapter.addData(e);
            if (chatAdapter.getItemCount() > 0)
                lv_chat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        } else {
            questionAdapter.addData(list);
            if (questionAdapter.getItemCount() > 0)
                lv_chat.smoothScrollToPosition(questionAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void notifyDataChanged(int type, MessageServer.MsgInfo data) {
        ChatMessageData e = new ChatMessageData();
        e.msgInfo = data;
        chatAdapter.addData(e);
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
        questionAdapter.setNewData(null);
    }

    @Override
    public void performSend(String content, int chatEvent) {
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(getContext(), "发送的消息不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (status) {
            case VhallUtil.WATCH_PLAYBACK:
            case VhallUtil.BROADCAST://直播 回放界面只能发聊天 必须登录
                mPresenter.sendChat(content);
                break;
            case VhallUtil.WATCH_LIVE://观看直播界面发聊天和问答
                if (chatEvent == ChatFragment.CHAT_EVENT_CHAT) {
                    mPresenter.sendChat(content);
                } else if (chatEvent == ChatFragment.CHAT_EVENT_QUESTION) {
                    mPresenter.sendQuestion(content);
                }
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

    class ChatAdapter extends BaseQuickAdapter<ChatMessageData, BaseViewHolder> {
        public ChatAdapter() {
            super(R.layout.chat_item);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder viewHolder, ChatMessageData chatMessageData) {
            ChatServer.ChatInfo chatInfo = chatMessageData.chatInfo;
            MessageServer.MsgInfo msgInfo = chatMessageData.msgInfo;
            TextView tv_chat_content;
            if (chatInfo != null) {
                if ("survey".equals(chatInfo.event)) {
                    viewHolder.getView(R.id.ll_chat).setVisibility(View.GONE);
                    viewHolder.getView(R.id.ll_survey).setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(chatInfo.name)) {
                        viewHolder.setText(R.id.tv_title, String.format("组织者发布 %s 调查，", chatInfo.name));
                    } else {
                        viewHolder.setText(R.id.tv_title, String.format("组织者发布 %s 调查，", "问卷"));
                    }
                    viewHolder.getView(R.id.tv_join).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (TextUtils.isEmpty(chatInfo.url)) {
                                mPresenter.showSurvey(chatInfo.id);
                            } else {
                                mPresenter.showSurvey(chatInfo.url, "");
                            }
                        }
                    });
                } else {
                    viewHolder.getView(R.id.ll_chat).setVisibility(View.VISIBLE);
                    viewHolder.getView(R.id.ll_survey).setVisibility(View.GONE);
                    tv_chat_content = viewHolder.getView(R.id.tv_chat_content);
                    String avatar = chatInfo.avatar;
                    if (!TextUtils.isEmpty(avatar) && !avatar.startsWith("http")) {
                        avatar = String.format("https:%s", avatar);
                    }
                    ImageView iv_chat_avatar = viewHolder.getView(R.id.iv_chat_avatar);
                    RequestOptions options = new RequestOptions().placeholder(R.drawable.icon_vhall).transform(new CircleCrop());
                    Glide.with(getActivity()).load(avatar).apply(options).into(iv_chat_avatar);
                    viewHolder.setText(R.id.tv_chat_time, chatInfo.time);
                    //处理回复消息
                    String textContent = "";
                    if (chatInfo.replyMsg != null) {
                        textContent = chatInfo.replyMsg.user_name + ": " + chatInfo.replyMsg.content.textContent + "\n" + "回复：";
                    }
                    if (chatInfo.msgData != null) {
                        textContent = textContent + chatInfo.msgData.text;
                    }
                    switch (chatInfo.event) {
                        case ChatServer.eventMsgKey:
                            if (chatInfo.msgData != null)
                                if (!ListUtils.isEmpty(chatInfo.msgData.imageUrls) || !TextUtils.isEmpty(chatInfo.msgData.resourceUrl)) {
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
                            Log.e("vhall_", chatInfo.user_name);
                            tv_chat_content.setVisibility(View.VISIBLE);
                            tv_chat_content.setText(EmojiUtils.getEmojiText(mActivity, textContent), TextView.BufferType.SPANNABLE);
                            viewHolder.setText(R.id.tv_chat_name, chatInfo.user_name + "【自定义消息】");
                            break;
                        case ChatServer.eventOnlineKey:
                            Log.e("vhall_", chatInfo.user_name);
                            viewHolder.setText(R.id.tv_chat_name, String.format("%s上线了！角色：%s(%s)", chatInfo.user_name, chatInfo.roleName, chatInfo.role));
                            tv_chat_content.setVisibility(View.INVISIBLE);
                            break;
                        case ChatServer.eventOfflineKey:
                            viewHolder.setText(R.id.tv_chat_name, String.format("%s下线了！角色：%s(%s)", chatInfo.user_name, chatInfo.roleName, chatInfo.role));
                            tv_chat_content.setVisibility(View.INVISIBLE);
                            break;
                        default:
                            break;
                    }

                }
            }
            if (msgInfo != null) {
                viewHolder.getView(R.id.ll_chat).setVisibility(View.GONE);
                viewHolder.getView(R.id.ll_survey).setVisibility(View.VISIBLE);
                TextView tv_title = viewHolder.getView(R.id.tv_title);
                TextView tv_join = viewHolder.getView(R.id.tv_join);
                ExamInfo examInfo = msgInfo.examInfo;
                if (examInfo != null) {
                    switch (msgInfo.event) {
                        case EVENT_EXAM_PAPER_SEND:
                            tv_join.setVisibility(View.VISIBLE);
                            tv_title.setText(String.format("%s %s 发起了快问快答 ", BaseUtil.getLimitString(examInfo.nick_name), CommonUtil.changeRoleNameToString(examInfo.role_name)));
                            tv_join.setText("点击查看");
                            viewHolder.getView(R.id.tv_join).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mPresenter.showExam(examInfo.paper_id);
                                }
                            });
                            break;
                        case EVENT_EXAM_PAPER_END:
                            tv_join.setVisibility(View.GONE);
                            tv_title.setText(String.format("%s %s 快问快答已经结束 ", BaseUtil.getLimitString(examInfo.nick_name), CommonUtil.changeRoleNameToString(examInfo.role_name)));
                            break;
                        case EVENT_EXAM_PAPER_AUTO_END:
                            tv_join.setVisibility(View.GONE);
                            tv_title.setText("快问快答自动结束");
                            break;
                        case EVENT_EXAM_PAPER_SEND_RANK:
                            tv_join.setVisibility(View.VISIBLE);
                            tv_title.setText("快问快答已经结束");
                            tv_join.setText("成绩排行榜");
                            viewHolder.getView(R.id.tv_join).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mPresenter.showExamRank(examInfo.paper_id);
                                }
                            });
                            break;
                        case EVENT_EXAM_PAPER_AUTO_SEND_RANK:
                            viewHolder.getView(R.id.tv_join).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mPresenter.showExamRank(examInfo.paper_id);
                                }
                            });
                            tv_join.setVisibility(View.VISIBLE);
                            tv_title.setText("快问快答已经结束");
                            tv_join.setText("自动公布成绩排行榜");
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    class QuestionAdapter extends BaseQuickAdapter<ChatServer.ChatInfo, BaseViewHolder> {

        public QuestionAdapter() {
            super(R.layout.chat_question_item);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder viewHolder, ChatServer.ChatInfo data) {
            ChatServer.ChatInfo.QuestionData questionData = data.questionData;
            ImageView iv_question_avatar = viewHolder.getView(R.id.iv_question_avatar);
            ImageView iv_answer_avatar = viewHolder.getView(R.id.iv_answer_avatar);
            RequestOptions options = new RequestOptions().placeholder(R.drawable.icon_vhall);

            viewHolder.setText(R.id.tv_question_name, questionData.nick_name);
            viewHolder.setText(R.id.tv_question_time, questionData.created_time);
            viewHolder.setText(R.id.tv_question_content, EmojiUtils.getEmojiText(mActivity, questionData.content));

            if (questionData.answer != null) {
                viewHolder.getView(R.id.ll_answer).setVisibility(View.VISIBLE);
                viewHolder.setText(R.id.tv_answer_name, questionData.answer.nick_name);
                viewHolder.setText(R.id.tv_answer_time, questionData.answer.created_time);
                viewHolder.setText(R.id.tv_answer_content, EmojiUtils.getEmojiText(mActivity, questionData.answer.content));
                Glide.with(getActivity()).load(questionData.answer.avatar).apply(options).into(iv_answer_avatar);
                Glide.with(getActivity()).load(questionData.avatar).apply(options).into(iv_question_avatar);
            } else {
                Glide.with(getActivity()).load(questionData.avatar).apply(options).into(iv_question_avatar);
                viewHolder.getView(R.id.ll_answer).setVisibility(View.GONE);
            }

        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
