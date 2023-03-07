package com.vhall.uimodule.module.chat;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.ChatServer;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uimodule.R;
import com.vhall.uimodule.utils.CommonUtil;
import com.vhall.uimodule.widget.RadiusBackgroundSpan;

/**
 * @author hkl
 * Date: 2022/7/21 18:01
 */
public class QAAdapter extends BaseQuickAdapter<ChatMessageData, BaseViewHolder> {
    private Context mContext;
    WebinarInfo webinarInfo;

    public QAAdapter(Context context, WebinarInfo webinarInfo) {
        super(R.layout.item_qa_list);
        mContext = context;
        this.webinarInfo = webinarInfo;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, ChatMessageData messageData) {
        ChatServer.ChatInfo chatInfo = messageData.chatInfo;
        String showRoleText;
        String roleColor = "#26FB2626";
        String roleBgColor = "#FB2626";
        ChatServer.ChatInfo.QuestionData questionData = chatInfo.questionData;
        SpannableStringBuilder builderQ = new SpannableStringBuilder();
        SpannableStringBuilder builderA = new SpannableStringBuilder();
        RequestOptions requestOptions = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.mipmap.icon_avatar);
        ImageView iv_avatar = viewHolder.getView(R.id.iv_avatar);
        ImageView iv_avatar_re = viewHolder.getView(R.id.iv_avatar_re);
        if (questionData.answer != null && !TextUtils.isEmpty(questionData.answer.content)) {
            switch (questionData.answer.roleName) {
                case "1":
                    showRoleText = "主持人";
                    roleBgColor = "#26FB2626";
                    roleColor = "#FB2626";
                    break;
                case "3":
                    showRoleText = "助理";
                    roleBgColor = "#260A7FF5";
                    roleColor = "#0A7FF5";
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
            String name = CommonUtil.getLimitString(questionData.answer.nick_name, 8) + " ";
            SpannableStringBuilder ssb = new SpannableStringBuilder(name);
            if (!TextUtils.isEmpty(showRoleText)) {
                ssb.append(showRoleText);
                ssb.setSpan(new RadiusBackgroundSpan(Color.parseColor(roleBgColor), 18, Color.parseColor(roleColor)), name.length(), ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new AbsoluteSizeSpan(11, true), name.length(), ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            viewHolder.setText(R.id.tv_name, ssb);
            viewHolder.getView(R.id.cl_re).setVisibility(View.VISIBLE);
            viewHolder.setText(R.id.tv_name_re, CommonUtil.getLimitString(questionData.nick_name, 8));
            Glide.with(mContext).load(questionData.answer.avatar).apply(requestOptions).into(iv_avatar);
            Glide.with(mContext).load(questionData.avatar).apply(requestOptions).into(iv_avatar_re);
            viewHolder.setText(R.id.tv_time, CommonUtil.converChatTime(questionData.answer.created_time));
            builderQ.append("提问 ").append(questionData.content);
            builderA.append("回答 ").append(questionData.answer.content);
            builderQ.setSpan(new ForegroundColorSpan(Color.parseColor("#D67900")), 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            builderA.setSpan(new ForegroundColorSpan(Color.parseColor("#D67900")), 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            viewHolder.setText(R.id.tv_q, builderA);
            viewHolder.setText(R.id.tv_a, builderQ);
        } else {
            viewHolder.setText(R.id.tv_name, CommonUtil.getLimitString(questionData.nick_name, 8));
            Glide.with(mContext).load(questionData.avatar).apply(requestOptions).into(iv_avatar);
            viewHolder.setText(R.id.tv_time, CommonUtil.converChatTime(questionData.created_time));
            viewHolder.setText(R.id.tv_q, questionData.content);
            viewHolder.getView(R.id.cl_re).setVisibility(View.GONE);
        }
    }
} 