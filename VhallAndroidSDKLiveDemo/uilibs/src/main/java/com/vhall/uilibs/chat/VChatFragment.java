package com.vhall.uilibs.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.bumptech.glide.request.RequestOptions;
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

    public static final int CHAT_NORMAL = 0x00;
    public static final int CHAT_SURVEY = 0x01;
    private ChatContract.ChatPresenter mPresenter;
    public final int RequestLogin = 0;
    private ListView lv_chat;
    private List<ChatServer.ChatInfo> questionData = new ArrayList<ChatServer.ChatInfo>();
    private List<MessageChatData> chatInfoList = new ArrayList<>();
    private ChatAdapter chatAdapter = new ChatAdapter();
    private QuestionAdapter questionAdapter = new QuestionAdapter();
    private boolean isquestion = false;
    private int status = -1;

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
        lv_chat = getView().findViewById(R.id.lv_chat);
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
    }

    @Override
    public void notifyDataChangedChat(MessageChatData data) {
        if (chatInfoList.size() > 10) {
            chatInfoList.remove(0);
        }
        chatInfoList.add(data);
        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyDataChangedQe(ChatServer.ChatInfo data) {
        if (questionData.size() > 10) {
            questionData.remove(0);
        }
        questionData.add(data);
        questionAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyDataChangedChat(int type, List<MessageChatData> list) {
        chatInfoList.addAll(list);
        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyDataChangedQe(int type, List<ChatServer.ChatInfo> list) {
        if (questionData.size() > 10) {
            questionData.remove(0);
        }
        questionData.addAll(list);
        questionAdapter.notifyDataSetChanged();
    }

    @Override
    public void showToast(String content) {
        if (this.isAdded()) {
            Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void clearChatData() {
        if (questionData != null) {
            questionData.clear();
        }
        if (chatInfoList != null) {
            chatInfoList.clear();
        }
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

    class ChatAdapter extends BaseAdapter {

        @Override
        public int getItemViewType(int position) {
            if ("survey".equals(chatInfoList.get(position).event)) {
                return CHAT_SURVEY;
            } else {
                return CHAT_NORMAL;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getCount() {
            return chatInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return chatInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            ChatSurveyHolder surveyHolder;
            final MessageChatData data = chatInfoList.get(position);
            switch (getItemViewType(position)) {
                case CHAT_NORMAL:
                    if (convertView == null) {
                        convertView = View.inflate(getActivity(), R.layout.v_chat_item, null);
                        viewHolder = new ViewHolder();
                        viewHolder.item_text = convertView.findViewById(R.id.tv_chat_item_text);
                        convertView.setTag(viewHolder);
                    } else {
                        viewHolder = (ViewHolder) convertView.getTag();
                    }

                    String msg = "";
                    String name = data.getNickname();
                    SpannableString styledText;
                    switch (data.event) {
                        case MessageChatData.eventMsgKey:
                            if (data.getType().equals("image")) {
                                if (!TextUtils.isEmpty(data.getImage_url())) {
                                    msg = String.format("收到图片---%s", data.getImage_url());
                                } else if (data.getImage_urls() != null) {
                                    msg = String.format("收到%d张图片", data.getImage_urls().size());
                                }
                            } else {
                                msg = data.getText_content();
                            }

                            styledText = new SpannableString(name + ":" + msg);
                            styledText.setSpan(new TextAppearanceSpan(getContext(), R.style.style0), 0, name.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewHolder.item_text.setText(styledText);
                            break;
                        case MessageChatData.eventCustomKey:
                            msg = data.getText_content();

                            styledText = new SpannableString(name + ":" + msg);
                            styledText.setSpan(new TextAppearanceSpan(getContext(), R.style.style0), 0, name.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewHolder.item_text.setText(styledText);
                            break;
                        case MessageChatData.eventOnlineKey:
                            viewHolder.item_text.setText(String.format("%s上线了！角色：%s(%s)", data.getNickname(),data.getRoleName(),data.getRole()));
                            break;
                        case MessageChatData.eventOfflineKey:
                            viewHolder.item_text.setText(String.format("%s下线了！角色：%s(%s)", data.getNickname(),data.getRoleName(),data.getRole()));
                            break;
                        default:
                            break;
                    }
                    break;
                case CHAT_SURVEY:
                    if (convertView == null) {
                        convertView = View.inflate(getActivity(), R.layout.chat_item_survey, null);
                        surveyHolder = new ChatSurveyHolder();
                        surveyHolder.tv_join = convertView.findViewById(R.id.tv_join);
                        convertView.setTag(surveyHolder);
                    } else {
                        surveyHolder = (ChatSurveyHolder) convertView.getTag();
                    }
                    surveyHolder.tv_join.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (TextUtils.isEmpty(data.getUrl())) {
                                mPresenter.showSurvey(data.getId());
                            } else {
                                mPresenter.showSurvey(data.getUrl(), "");
                            }
                        }
                    });
                    break;
                default:
                    break;
            }
            return convertView;
        }
    }

    class QuestionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return questionData.size();
        }

        @Override
        public Object getItem(int position) {
            return questionData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.chat_question_item, null);
                viewHolder = new Holder();
                viewHolder.iv_question_avatar = convertView.findViewById(R.id.iv_question_avatar);
                viewHolder.tv_question_content = convertView.findViewById(R.id.tv_question_content);
                viewHolder.tv_question_name = convertView.findViewById(R.id.tv_question_name);
                viewHolder.tv_question_time = convertView.findViewById(R.id.tv_question_time);

                viewHolder.ll_answer = convertView.findViewById(R.id.ll_answer);
                viewHolder.iv_answer_avatar = convertView.findViewById(R.id.iv_answer_avatar);
                viewHolder.tv_answer_content = convertView.findViewById(R.id.tv_answer_content);
                viewHolder.tv_answer_name = convertView.findViewById(R.id.tv_answer_name);
                viewHolder.tv_answer_time = convertView.findViewById(R.id.tv_answer_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (Holder) convertView.getTag();
            }
            ChatServer.ChatInfo data = questionData.get(position);
            ChatServer.ChatInfo.QuestionData questionData = data.questionData;
            RequestOptions options = new RequestOptions().placeholder(R.drawable.icon_vhall);
            if (questionData != null && !TextUtils.isEmpty(questionData.avatar)) {
                Glide.with(getActivity()).load(questionData.avatar).apply(options).into(viewHolder.iv_question_avatar);
            }
            //TODO 头像设置
            viewHolder.tv_question_name.setText(questionData.nick_name);
            viewHolder.tv_question_time.setText(questionData.created_time);
            viewHolder.tv_question_content.setText(EmojiUtils.getEmojiText(getContext(), questionData.content), TextView.BufferType.SPANNABLE);
            if (questionData.answer != null) {
                viewHolder.ll_answer.setVisibility(View.VISIBLE);
                viewHolder.tv_answer_content.setText(EmojiUtils.getEmojiText(getContext(), questionData.answer.content), TextView.BufferType.SPANNABLE);
                viewHolder.tv_answer_name.setText(questionData.answer.nick_name);
                viewHolder.tv_answer_time.setText(questionData.answer.created_time);
                Glide.with(getActivity()).load(questionData.answer.avatar).apply(options).into(viewHolder.iv_answer_avatar);
                Glide.with(getActivity()).load(questionData.avatar).apply(options).into(viewHolder.iv_question_avatar);
            } else {
                Glide.with(getActivity()).load(data.avatar).apply(options).into(viewHolder.iv_question_avatar);
                viewHolder.ll_answer.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView item_text;
    }

    static class ChatSurveyHolder {
        TextView tv_join;
    }

    static class Holder {
        ImageView iv_question_avatar;
        TextView tv_question_content;
        TextView tv_question_time;
        TextView tv_question_name;

        LinearLayout ll_answer;
        ImageView iv_answer_avatar;
        TextView tv_answer_content;
        TextView tv_answer_time;
        TextView tv_answer_name;
    }
}
