package com.vhall.uilibs.watch.minimalist;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.emoji.EmojiUtils;
import com.vhall.uilibs.widget.RadiusBackgroundSpan;

import java.util.List;

/**
 * @author hkl
 * Date: 2022/7/21 18:01
 */
public class MiniChatAdapter extends BaseQuickAdapter<ChatMessageData, BaseViewHolder> {
    private Context mContext;

    public MiniChatAdapter(Context context) {
        super(R.layout.item_chat_list);
        mContext = context;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, ChatMessageData messageData) {
        ChatServer.ChatInfo chatInfo = messageData.chatInfo;
        MessageServer.MsgInfo msgInfo = messageData.msgInfo;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        ImageView ivGift = viewHolder.getView(R.id.iv_gift);
        String name;
        if (msgInfo != null) {
            if (msgInfo.event == MessageServer.EVENT_GIFT_SEND_SUCCESS) {
                MessageServer.GiftInfoData giftInfoData = msgInfo.giftInfoData;
                if (giftInfoData != null) {
                    ivGift.setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(giftInfoData.gift_image_url).apply(new RequestOptions()).into(ivGift);
                    name = " " + BaseUtil.getLimitString(giftInfoData.gift_user_nickname,8) + " ";
                    builder.append(name);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#A6FFFFFF")), 0, name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    builder.append("送出 ");
                    builder.append(giftInfoData.gift_name);
                    viewHolder.setText(R.id.tv_text, builder);
                }
            }
        } else if (chatInfo != null) {
            ivGift.setVisibility(View.GONE);
            name = " " + BaseUtil.getLimitString(chatInfo.user_name,8) + " ";
            String showRoleText = "主持人";
            String roleColor = "#FC5659";
            String roleBgColor = "#FC5659";
            switch (chatInfo.roleName) {
                case "1":
                    showRoleText = "主持人";
                    roleBgColor = "#26FC5659";
                    roleColor = "#FC5659";
                    break;
                case "3":
                    showRoleText = "助理";
                    roleBgColor = "#26BBBBBB";
                    roleColor = "#BBBBBB";
                    break;
                case "4":
                    showRoleText = "嘉宾";
                    roleBgColor = "#260A7FF5";
                    roleColor = "#0A7FF5";
                    break;
                default:
                    showRoleText = "";
                    break;
            }
            if (!TextUtils.isEmpty(showRoleText)) {
                builder.append(" ");
                SpannableStringBuilder ssb = new SpannableStringBuilder(showRoleText);
                //设置圆角角色
                ssb.setSpan(new RadiusBackgroundSpan(Color.parseColor(roleBgColor), 18, Color.parseColor(roleColor)), 0, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new AbsoluteSizeSpan(11, true), 0, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);//设置后面的字体大小
                builder.append(ssb);
                builder.append(name);
                builder.setSpan(new ForegroundColorSpan(Color.parseColor("#A6FFFFFF")), showRoleText.length() - 1, name.length() + showRoleText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            } else {
                builder.append(name);
                builder.setSpan(new ForegroundColorSpan(Color.parseColor("#A6FFFFFF")), 0, name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }

            String textContent = "";
            if (chatInfo.replyMsg != null) {
                textContent = chatInfo.replyMsg.user_name + ": " + chatInfo.replyMsg.content.textContent + "\n" + "回复：";
            }
            if (chatInfo.msgData != null) {
                textContent = textContent + chatInfo.msgData.text;
            }
            if (!TextUtils.isEmpty(textContent)) {
                builder.append(EmojiUtils.getEmojiText(mContext, textContent));
            }
            //处理回复消息
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    if (chatInfo.msgData != null)
                        if (!ListUtils.isEmpty(chatInfo.msgData.imageUrls) || !TextUtils.isEmpty(chatInfo.msgData.resourceUrl)) {
                            builder.append("收到图片");
                        }
                    viewHolder.setText(R.id.tv_text, builder);
                    break;
                case ChatServer.eventOnlineKey:
                    viewHolder.setText(R.id.tv_text, String.format("%s上线了！角色：%s(%s)", chatInfo.user_name, chatInfo.roleName, chatInfo.role));
                    break;
                case ChatServer.eventOfflineKey:
                    viewHolder.setText(R.id.tv_text, String.format("%s下线了！角色：%s(%s)", chatInfo.user_name, chatInfo.roleName, chatInfo.role));
                    break;
                default:
                    break;
            }
        }
    }
} 