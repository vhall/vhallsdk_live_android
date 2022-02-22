package com.vhall.uilibs.interactive.broadcast.cache;//package com.vhall.uilibs.interactive.broadcast.cache;
//
//import android.text.TextUtils;
//import com.vhall.business.ChatServer;
//import com.vhall.business.MessageServer;
//import com.vhall.business.utils.VHInternalUtils;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 互动缓存管理
// */
//public class InteractiveManager {
//    private Map<String,InactiveUserBean> mUserBeanCached = new HashMap<>();
//    public static class InactiveUserBean{
//        public String userId;
//        public String role;
//        public String nickName;
//        public String avatar;
//
//        public InactiveUserBean(String userId, String role, String nickName, String avatar) {
//            this.userId = userId;
//            this.role = VHInternalUtils.parseRoleNameToNum(role);
//            this.nickName = nickName;
//            this.avatar = avatar;
//        }
//    }
//
//    static InteractiveManager sManager;
//    static {
//        sManager = new InteractiveManager();
//    }
//
//    public static InteractiveManager share(){
//        return sManager;
//    }
//
//    private InteractiveManager() {
//        mMsgProcessorMap.put(MessageServer.EVENT_VRTC_CONNECT_SUCCESS, new Processor() {
//            @Override
//            public void onMessage(MessageServer.MsgInfo msg) {
//                String userId = getMsgInfoUserId(msg);
//                if(!TextUtils.isEmpty(userId)){
//                    String avatar = "";
//                    if(msg.mOriginData != null){
//                        avatar = msg.mOriginData.optString("avatar");
//                    }
//                    mUserBeanCached.put(userId,new InactiveUserBean(userId,msg.role,msg.nick_name,avatar));
//                    mNumberChangedListener.onChanged();
//                }
//            }
//        });
//        mMsgProcessorMap.put(MessageServer.EVENT_INTERACTIVE_HAND, new Processor() {
//            @Override
//            public void onMessage(MessageServer.MsgInfo msg) {
//                String userId = getMsgInfoUserId(msg);
//                if(!TextUtils.isEmpty(userId)){
//                    String avatar = "";
//                    if(msg.mOriginData != null){
//                        avatar = msg.mOriginData.optString("avatar");
//                    }
//                    mUserBeanCached.put(userId,new InactiveUserBean(userId,msg.role,msg.nick_name,avatar));
//                    mNumberChangedListener.onChanged();
//                }
//            }
//        });
//        mMsgProcessorMap.put(MessageServer.EVENT_INTERACTIVE_DOWN_MIC, new Processor() {
//            @Override
//            public void onMessage(MessageServer.MsgInfo msg) {
//                //cache 100条数据
//                if(mUserBeanCached.size() < 100){
//                    return;
//                }
//                String userId = getMsgInfoUserId(msg);
//                if(mUserBeanCached.containsKey(userId)){
//                    mUserBeanCached.remove(userId);
//                }
//            }
//        });
//
//
//        mChatProcessorMap.put(ChatServer.eventOnlineKey, new ChatProcessor() {
//            @Override
//            public void onMessage(ChatServer.ChatInfo chatInfo) {
//                String userId = getChatInfoUserId(chatInfo);
//                if(!TextUtils.isEmpty(userId)){
//                    String avatar = "";
//                    if(chatInfo.mOriginData != null){
//                        avatar = chatInfo.mOriginData.optString("avatar");
//                    }
//                    mUserBeanCached.put(userId,new InactiveUserBean(userId,chatInfo.role,chatInfo.user_name,avatar));
//                }
//            }
//        });
//    }
//
//    private Map<Integer,Processor> mMsgProcessorMap = new HashMap<>();
//    private Map<String,ChatProcessor> mChatProcessorMap = new HashMap<>();
//
//    /**
//     * msg 消息处理
//     * @param msg
//     */
//    public void onMessage(MessageServer.MsgInfo msg){
//        if(mMsgProcessorMap.containsKey(msg.event)){
//            mMsgProcessorMap.get(msg.event).onMessage(msg);
//        }
//    }
//
//    /**
//     * 聊天消息处理
//     * @param chatInfo
//     */
//    public void onMessage(ChatServer.ChatInfo chatInfo){
//        if(mChatProcessorMap.containsKey(chatInfo.event)){
//            mChatProcessorMap.get(chatInfo.event).onMessage(chatInfo);
//        }
//    }
//
//    private String getChatInfoUserId(ChatServer.ChatInfo chatInfo){
//        String userId = chatInfo.account_id;
//        if(TextUtils.isEmpty(userId)){
//            if(chatInfo.mOriginData != null){
//                userId = chatInfo.mOriginData.optString("room_join_id");
//            }
//        }
//        return userId;
//    }
//
//    public String getMsgInfoUserId(MessageServer.MsgInfo msg){
//        String userId = msg.targetId;
//        if(TextUtils.isEmpty(userId)){
//            userId = msg.roomJoinId;
//        }
//        return userId;
//    }
//
//
//    interface Processor{
//        void onMessage(MessageServer.MsgInfo msg);
//    }
//
//    interface ChatProcessor{
//        void onMessage(ChatServer.ChatInfo msg);
//    }
//
//
//    public InactiveUserBean getInactiveUserBean(String userId){
//        if(!mUserBeanCached.containsKey(userId)){
//            return null;
//        }
//        return mUserBeanCached.get(userId);
//    }
//
//
//    public interface InteractiveNumberChangedListener{
//        void onChanged();
//    }
//
//    private InteractiveNumberChangedListener mNumberChangedListener = new InteractiveNumberChangedListener() {
//        @Override
//        public void onChanged() {
//
//        }
//    };
//
//    public void setmNumberChangedListener(InteractiveNumberChangedListener mNumberChangedListener) {
//        this.mNumberChangedListener = mNumberChangedListener;
//    }
//}
