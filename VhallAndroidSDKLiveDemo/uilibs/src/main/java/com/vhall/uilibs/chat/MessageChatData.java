package com.vhall.uilibs.chat;

import android.text.TextUtils;

import com.vhall.business.ChatServer;
import com.vhall.business.VhallSDK;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import vhall.com.vss.data.ResponseChatInfo;

/**
 * @author hkl
 * Date: 2019-07-03 15:49
 */
public class MessageChatData implements Serializable {
    private String text_content;
    private String type;
    private String room_id;
    private String nickname;
    private String avatar;
    private String userId;
    private String time;
    private String id;
    private String url;
    private boolean isMy;
    public String event = eventMsgKey;
    private List<String> image_urls;
    private String image_url;

    public static final String eventOnlineKey = "online";// 上线
    public static final String eventOfflineKey = "offline";// 下线
    public static final String eventMsgKey = "msg";// 聊天
    public static final String eventSurveyKey = "survey";//问卷
    public static final String eventCustomKey = "custom_broadcast";// 自定义消息
    public static final String eventQuestion = "question";//提问和回答

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText_content() {
        return text_content;
    }

    public void setText_content(String text_content) {
        this.text_content = text_content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isMy() {
        return isMy;
    }

    public void setMy(boolean my) {
        isMy = my;
    }

    public List<String> getImage_urls() {
        return image_urls;
    }

    public void setImage_urls(List<String> image_urls) {
        this.image_urls = image_urls;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public static MessageChatData getChatData(ChatServer.ChatInfo chatInfo) {
        MessageChatData data = new MessageChatData();
        if (chatInfo == null) {
            return data;
        }
        data.setUserId(chatInfo.account_id);
        data.setNickname(chatInfo.user_name);
        data.setAvatar(chatInfo.avatar);
        data.setType("text");
        data.event = chatInfo.event;
        if (chatInfo.msgData != null) {
            data.setText_content(chatInfo.msgData.text);
        }
        data.setMy(false);
        if (!TextUtils.isEmpty(VhallSDK.getUserId())) {
            String userId =VhallSDK.getUserId();
            data.setMy(Objects.equals(chatInfo.account_id, userId));
        }
        return data;
    }

    public static MessageChatData getChatData(ResponseChatInfo chatInfo) {
        MessageChatData data = new MessageChatData();
        if (chatInfo == null) {
            return data;
        }
        data.setUserId(chatInfo.getThird_party_user_id());
        data.setNickname(chatInfo.getNickname());
        data.setAvatar(chatInfo.getAvatar());
        if (chatInfo.getData() != null) {
            data.setType(chatInfo.getData().getType());
            data.setText_content(chatInfo.getData().getText_content());
        }
        data.setTime(chatInfo.getDate_time());
        data.setMy(false);
        if (!TextUtils.isEmpty(VhallSDK.getUserId())) {
            String userId =VhallSDK.getUserId();
            data.setMy(Objects.equals(chatInfo.getThird_party_user_id(), userId));
        }
        return data;
    }
}
