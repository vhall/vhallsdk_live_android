package com.vhall.uimodule.utils;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;

/**
 * @author hkl
 * Date: 2022/7/25 15:22
 */
public class MiniBaseCallBack {
    public static class SimpleMessageEventCallback implements MessageServer.Callback{

        @Override
        public void onEvent(MessageServer.MsgInfo messageInfo) {

        }

        @Override
        public void onMsgServerConnected() {

        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void onMsgServerClosed() {

        }
    }
    public static class SimpleChatCallback implements ChatServer.Callback{

        @Override
        public void onChatServerConnected() {

        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {

        }

        @Override
        public void onChatServerClosed() {

        }

        @Override
        public void onChatServerKickOff() {

        }
    }
} 