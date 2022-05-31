package com.vhall.uilibs.chat;

import android.text.TextUtils;

import com.vhall.business.ChatServer;
import com.vhall.business.VhallSDK;

import java.io.Serializable;
import java.util.List;

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
    private String id;//供问答时使用的 问答id，其他情况无效
    private String url;
    private boolean isMy;
    public String event = eventMsgKey;
    private List<String> image_urls;
    private String image_url;
    private String roleName = "2";
    private String role = "user";
    public ChatServer.ChatInfo.OnlineData onlineData;
    public String target_id;//需要被过滤的消息 用于私聊判断
    public String survey_name;//问卷的昵称  用户自定义的显示

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


    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
        switch (roleName) {
            case "1":
            case "host":
                this.role = "host";
                break;
            case "2":
            case "user":
                this.role = "user";
                break;
            case "3":
            case "assistant":
                this.role = "assistant";
                break;
            case "4":
            case "guest":
                this.role = "guest";
                break;
            default:
                break;
        }
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static MessageChatData getChatData(ChatServer.ChatInfo chatInfo) {
        MessageChatData data = new MessageChatData();
        if (chatInfo == null) {
            return data;
        }
        data.onlineData = chatInfo.onlineData;
        data.setUserId(chatInfo.account_id);
        data.setNickname(chatInfo.user_name);
        data.setAvatar(chatInfo.avatar);
        data.setRoleName(chatInfo.roleName);
        data.setRole(chatInfo.role);
        data.setTime(chatInfo.time);
        if (chatInfo.event != null) {
            data.event = chatInfo.event;
        }
        //处理回复消息
        String textContent = "";
        if (chatInfo.replyMsg != null) {
            textContent = chatInfo.replyMsg.user_name + ": " + chatInfo.replyMsg.content.textContent + "\n" + "回复：";
        }
        if (chatInfo.msgData != null) {
            data.setText_content(textContent + chatInfo.msgData.text);
            data.setType(chatInfo.msgData.type);
            if (TextUtils.isEmpty(data.getType())) {
                data.setType("text");
            }
            data.setImage_url(chatInfo.msgData.resourceUrl);
            data.setImage_urls(chatInfo.msgData.imageUrls);
            data.target_id=chatInfo.msgData.target_id;
        }
        data.setMy(false);
        if (!TextUtils.isEmpty(VhallSDK.getUserId())) {
            String userId = VhallSDK.getUserId();
            data.setMy(TextUtils.equals(chatInfo.account_id, userId));
        }
        return data;
    }
}
