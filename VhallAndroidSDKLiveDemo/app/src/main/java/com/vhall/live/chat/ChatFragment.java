package com.vhall.live.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.vhall.business.ChatServer;
import com.vhall.live.R;
import com.vhall.live.VhallApplication;
import com.vhall.live.data.Param;
import com.vhall.live.login.LoginActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天页的Fragment
 */
public class ChatFragment extends Fragment implements ChatContract.ChatView, View.OnClickListener {

    private ChatContract.ChatPresenter mPresenter;
    public final int RequestLogin = 0;
    ListView lv_chat;
    Button btnSendMsg;
    EditText editContent;
    List<ChatServer.ChatInfo> chatData = new ArrayList<ChatServer.ChatInfo>();
    ChatAdapter chatAdapter = new ChatAdapter();
    QuestionAdapter questionAdapter = new QuestionAdapter();
    boolean isquestion = false;
    int status = -1;

    public static ChatFragment newInstance(int status , boolean isquestion) {
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
        return inflater.inflate(R.layout.chat_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lv_chat = (ListView) getView().findViewById(R.id.lv_chat);
        editContent = (EditText) getView().findViewById(R.id.edit_content);
        btnSendMsg = (Button) getView().findViewById(R.id.btn_send_msg);
        btnSendMsg.setOnClickListener(this);

        isquestion = getArguments().getBoolean("question");
        status = getArguments().getInt("state");
        if (isquestion) {
            lv_chat.setAdapter(questionAdapter);
        } else {
            lv_chat.setAdapter(chatAdapter);
        }
    }

    @Override
    public void notifyDataChanged(ChatServer.ChatInfo data) {
        chatData.add(data);
        if (isquestion) {
            questionAdapter.notifyDataSetChanged();
        } else {
            chatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataChanged(List<ChatServer.ChatInfo> list) {
        chatData.addAll(list);
        if (isquestion)
            questionAdapter.notifyDataSetChanged();
        else
            chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void showToast(String content) {
        Toast.makeText(VhallApplication.getApp(), content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void clearInputContent() {
        editContent.setText("");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_msg:
                if (status == Param.BROADCAST || !TextUtils.isEmpty(VhallApplication.user_vhall_id)) {
                    String content = editContent.getText().toString();
                    if (isquestion) {
                        mPresenter.sendQuestion(content, VhallApplication.user_vhall_id);
                    } else
                        mPresenter.sendChat(content);
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent, RequestLogin);
                }
                break;
            default:
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RequestLogin == requestCode) {
            if (resultCode == getActivity().RESULT_OK) {
                mPresenter.onLoginReturn();
            }
        }
    }

    @Override
    public void setPresenter(ChatContract.ChatPresenter presenter) {
        mPresenter = presenter;
    }

    class ChatAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return chatData.size();
        }

        @Override
        public Object getItem(int position) {
            return chatData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.chat_item, null);
                viewHolder = new ViewHolder();
                viewHolder.iv_chat_avatar = (ImageView) convertView.findViewById(R.id.iv_chat_avatar);
                viewHolder.tv_chat_content = (TextView) convertView.findViewById(R.id.tv_chat_content);
                viewHolder.tv_chat_name = (TextView) convertView.findViewById(R.id.tv_chat_name);
                viewHolder.tv_chat_time = (TextView) convertView.findViewById(R.id.tv_chat_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ChatServer.ChatInfo data = chatData.get(position);
            Glide.with(getActivity()).load(data.avatar).placeholder(R.drawable.icon_vhall_108).into(viewHolder.iv_chat_avatar);
            switch (data.event) {
                case ChatServer.eventMsgKey:
                    viewHolder.tv_chat_content.setVisibility(View.VISIBLE);
                    viewHolder.tv_chat_content.setText(data.msgData.text);
                    viewHolder.tv_chat_name.setText(data.user_name);
                    break;
                case ChatServer.eventOnlineKey:
                    viewHolder.tv_chat_name.setText(data.user_name + "上线了！");
                    viewHolder.tv_chat_content.setVisibility(View.INVISIBLE);
                    break;
                case ChatServer.eventOfflineKey:
                    viewHolder.tv_chat_name.setText(data.user_name + "下线了！");
                    viewHolder.tv_chat_content.setVisibility(View.INVISIBLE);
                    break;
            }
            viewHolder.tv_chat_time.setText(data.time);
            return convertView;
        }
    }

    class QuestionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return chatData.size();
        }

        @Override
        public Object getItem(int position) {
            return chatData.get(position);
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
                viewHolder.iv_question_avatar = (ImageView) convertView.findViewById(R.id.iv_question_avatar);
                viewHolder.tv_question_content = (TextView) convertView.findViewById(R.id.tv_question_content);
                viewHolder.tv_question_name = (TextView) convertView.findViewById(R.id.tv_question_name);
                viewHolder.tv_question_time = (TextView) convertView.findViewById(R.id.tv_question_time);

                viewHolder.ll_answer = (LinearLayout) convertView.findViewById(R.id.ll_answer);
                viewHolder.iv_answer_avatar = (ImageView) convertView.findViewById(R.id.iv_answer_avatar);
                viewHolder.tv_answer_content = (TextView) convertView.findViewById(R.id.tv_answer_content);
                viewHolder.tv_answer_name = (TextView) convertView.findViewById(R.id.tv_answer_name);
                viewHolder.tv_answer_time = (TextView) convertView.findViewById(R.id.tv_answer_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (Holder) convertView.getTag();
            }
            ChatServer.ChatInfo data = chatData.get(position);
            ChatServer.ChatInfo.QuestionData questionData = data.questionData;
            //Glide.with(getActivity()).load(data.avatar).placeholder(R.drawable.icon_vhall_108).into(viewHolder.iv_question_avatar);
            //TODO 头像设置
            viewHolder.tv_question_name.setText(questionData.nick_name);
            viewHolder.tv_question_time.setText(questionData.created_at);
            viewHolder.tv_question_content.setText(questionData.content);
            if (questionData.answer != null) {
                viewHolder.ll_answer.setVisibility(View.VISIBLE);
                viewHolder.tv_answer_content.setText(questionData.answer.content);
                viewHolder.tv_answer_name.setText(questionData.answer.nick_name);
                viewHolder.tv_answer_time.setText(questionData.answer.created_at);
            } else {
                viewHolder.ll_answer.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView iv_chat_avatar;
        TextView tv_chat_content;
        TextView tv_chat_name;
        TextView tv_chat_time;
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
