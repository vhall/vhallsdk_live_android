package com.vhall.uilibs.interactive.broadcast;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.uilibs.R;
import com.vhall.uilibs.chat.MessageChatData;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.emoji.EmojiUtils;
import com.vhall.uilibs.widget.RadiusBackgroundSpan;
import com.vhall.vhss.data.RoleNameData;


/**
 * @author hkl
 * Date: 2020-06-29 17:19
 */
public class ChatAdapter extends BaseQuickAdapter<MessageChatData, BaseViewHolder> {

    public ChatAdapter() {
        super(R.layout.item_chat_list);
    }

    private RoleNameData roleNameData = new RoleNameData("主持人", "嘉宾", "助理");

    public void updateRoleName(RoleNameData roleNameData) {
        this.roleNameData = roleNameData;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, MessageChatData info) {
        TextView textView = helper.getView(R.id.tv_text);
        String name = " " + BaseUtil.getLimitString(info.getNickname()) + "：";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String showRoleText = "主持人";
        String roleColor = "#FC5659";
        switch (info.getRoleName()) {
            case "1":
                showRoleText = roleNameData.host_name;
                roleColor = "#FC5659";
                break;
            case "3":
                showRoleText = roleNameData.assistant_name;
                roleColor = "#BBBBBB";
                break;
            case "4":
                showRoleText = roleNameData.guest_name;
                roleColor = "#5EA6EC";
                break;
            default:
                showRoleText="";
                break;
        }

        if (!TextUtils.isEmpty(showRoleText)) {
            builder.append(" ");
            SpannableStringBuilder ssb = new SpannableStringBuilder(showRoleText);
            //设置圆角角色
            ssb.setSpan(new RadiusBackgroundSpan(Color.parseColor(roleColor), 18), 0, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new AbsoluteSizeSpan(11, true), 0, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);//设置后面的字体大小
            builder.append(ssb);
        }
        builder.append(name);
        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#e2e2e2")), 0, name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FFFFFF"));
        String text_content = info.getText_content();
        if (!TextUtils.isEmpty(text_content)) {
            builder.append(EmojiUtils.getEmojiText(mContext, info.getText_content()));
            builder.setSpan(colorSpan, 0, text_content.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
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