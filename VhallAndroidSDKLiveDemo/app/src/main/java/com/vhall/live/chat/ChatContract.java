package com.vhall.live.chat;

import com.vhall.business.ChatServer;
import com.vhall.live.BasePresenter;
import com.vhall.live.BaseView;

import java.util.List;

/**
 * 观看页的接口类
 */
public class ChatContract {
    
    public interface ChatView extends BaseView<ChatPresenter> {
        void notifyDataChanged(ChatServer.ChatInfo data);

        void notifyDataChanged(List<ChatServer.ChatInfo> list);

        void showToast(String content);

        void clearInputContent();
    }

    public interface ChatPresenter extends BasePresenter {

        void sendChat(String text);

        void sendQuestion(String content, String vhall_id);

        void onLoginReturn();

    }


}
