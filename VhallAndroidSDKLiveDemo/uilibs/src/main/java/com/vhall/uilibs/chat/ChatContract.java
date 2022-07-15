package com.vhall.uilibs.chat;

import android.content.Context;

import com.vhall.business.ChatServer;
import com.vhall.business.data.Survey;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.BaseView;
import com.vhall.uilibs.util.emoji.InputUser;

import org.json.JSONObject;

import java.util.List;


/**
 * 观看页的接口类
 */
public class ChatContract {

    public interface ChatView extends BaseView<ChatPresenter> {
        void notifyDataChanged(int type, ChatServer.ChatInfo data);
        void notifyDataChanged(int type, List<ChatServer.ChatInfo> list);



        void showToast(String content);

        void clearChatData();

        //TODO 该方法实际使用场景
        Context getContext();

        void performSend(String content, int chatEvent);
    }

    public interface ChatPresenter extends BasePresenter {

        void showChatView(boolean emoji, InputUser user, int limit);

        void sendChat(String text);

        void sendCustom(JSONObject text);

        void sendQuestion(String content);

        void onLoginReturn();

        void showSurvey(String url, String title);

        void showSurvey(String surveyid);

    }

}
