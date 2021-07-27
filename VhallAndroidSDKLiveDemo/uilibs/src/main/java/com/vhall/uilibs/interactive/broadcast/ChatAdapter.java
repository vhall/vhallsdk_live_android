package com.vhall.uilibs.interactive.broadcast;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.uilibs.R;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.emoji.EmojiUtils;
import com.vhall.uilibs.widget.MyImageSpan;

import vhall.com.vss2.data.VssMessageChatData;


/**
 * @author hkl
 * Date: 2020-06-29 17:19
 */
public class ChatAdapter extends BaseQuickAdapter<MessageChatData, BaseViewHolder> {

    public ChatAdapter() {
        super(R.layout.item_chat_list);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, MessageChatData info) {
        TextView textView = helper.getView(R.id.tv_text);
        String name = "  " + BaseUtil.getLimitString(info.getNickname()) + "：";
        SpannableStringBuilder builder = new SpannableStringBuilder(name);
        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#e2e2e2")), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FFFFFF"));
        String text_content = info.getText_content();
        if (!TextUtils.isEmpty(text_content)) {
            builder.append(EmojiUtils.getEmojiText(mContext, info.getText_content()));
            builder.setSpan(colorSpan, name.length(), text_content.length() + name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        MyImageSpan imageSpan;
        switch (info.getRoleName()) {
            case "1":
                imageSpan = new MyImageSpan(mContext, R.drawable.icon_chat_main);
                builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                break;
            case "3":
                imageSpan = new MyImageSpan(mContext, R.drawable.icon_chat_assistan);
                builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                break;
            case "4":
                imageSpan = new MyImageSpan(mContext, R.drawable.icon_chat_guest);
                builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                break;
            default:
                break;
        }

        if (info.getType().equals("image")) {
            builder.append("收到图片");
            if (info.getImage_urls() != null && info.getImage_urls().size() > 0) {
                for (String u:info.getImage_urls()){
                    builder.append(String.format("%s;\n",u));
                }
            } else if (!TextUtils.isEmpty(info.getImage_url())) {
                builder.append(info.getImage_url());
            }
        }
        textView.setText(builder);
    }
}