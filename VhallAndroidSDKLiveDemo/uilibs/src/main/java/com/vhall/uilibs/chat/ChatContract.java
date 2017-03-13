package com.vhall.uilibs.chat;

import com.vhall.business.ChatServer;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.BaseView;
import com.vhall.uilibs.util.emoji.InputUser;

import java.util.List;

/**
 * 观看页的接口类
 */
public class ChatContract {

    public interface ChatView extends BaseView<ChatPresenter> {
        void notifyDataChanged(ChatServer.ChatInfo data);

        void notifyDataChanged(List<ChatServer.ChatInfo> list);

        void showToast(String content);

        void clearChatData();

        void performSend(String content, int chatEvent);
    }

    public interface ChatPresenter extends BasePresenter {

        void showChatView(boolean emoji, InputUser user, int limit);

        void sendChat(String text);

        void sendQuestion(String content);

        void onLoginReturn();

        void onFreshData();

        void showSurvey(String surveyid);

    }


}
