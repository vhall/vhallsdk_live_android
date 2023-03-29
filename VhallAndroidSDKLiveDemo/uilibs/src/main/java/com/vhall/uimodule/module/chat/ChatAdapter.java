package com.vhall.uimodule.module.chat;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.ChatServer;
import com.vhall.business.ErrorCode;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.survey.SurveyServer;
import com.vhall.business.utils.SurveyInternal;
import com.vhall.uimodule.R;
import com.vhall.uimodule.module.lottery.LotteryListDialog;
import com.vhall.uimodule.module.survey.SurveyWebView;
import com.vhall.uimodule.module.watch.WatchLiveActivity;
import com.vhall.uimodule.utils.CommonUtil;
import com.vhall.uimodule.utils.ToastUtils;
import com.vhall.uimodule.utils.emoji.EmojiUtils;
import com.vhall.uimodule.widget.RadiusBackgroundSpan;
import com.vhall.uimodule.widget.VhClickSpan;
import com.vhall.vhss.data.SurveyInfoData;

import org.json.JSONException;

import java.util.List;

/**
 * @author hkl
 * Date: 2022/7/21 18:01
 */
public class ChatAdapter extends BaseQuickAdapter<ChatMessageData, BaseViewHolder> {
    private Context mContext;
    WebinarInfo webinarInfo;
    private SurveyServer mSurveyServer;
    private FragmentActivity activity;
    public ChatAdapter(Context context, WebinarInfo webinarInfo, FragmentActivity activity) {
        super(R.layout.item_chat_list);
        mContext = context;
        this.webinarInfo = webinarInfo;
        this.activity = activity;

        try {
            mSurveyServer = new SurveyServer.Builder()
                    .webinarInfo(webinarInfo)
                    .build();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, ChatMessageData messageData) {
        ChatServer.ChatInfo chatInfo = messageData.chatInfo;
        MessageServer.MsgInfo msgInfo = messageData.msgInfo;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String showRoleText;
        String roleColor = "#26FB2626";
        String roleBgColor = "#FB2626";

        switch (msgInfo == null ? chatInfo.roleName : msgInfo.role) {
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
        if (msgInfo != null) {
            viewHolder.getView(R.id.group_chat).setVisibility(View.GONE);
            viewHolder.getView(R.id.group_msg).setVisibility(View.VISIBLE);
            ImageView iv_msg = viewHolder.getView(R.id.iv_msg);
            TextView tv_msg = viewHolder.getView(R.id.tv_msg);
            String name;
            iv_msg.setVisibility(View.GONE);
            name = CommonUtil.getLimitString(msgInfo.nick_name, 8) + " ";
            SpannableStringBuilder ssb = new SpannableStringBuilder(showRoleText);
            if (!TextUtils.isEmpty(showRoleText)) {
                ssb.setSpan(new RadiusBackgroundSpan(Color.parseColor(roleBgColor), 18, Color.parseColor(roleColor)), 0, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new AbsoluteSizeSpan(11, true), 0, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            switch (msgInfo.event) {
                case MessageServer.EVENT_GIFT_SEND_SUCCESS:
                    viewHolder.setText(R.id.tv_name, CommonUtil.getLimitString(msgInfo.giftInfoData.gift_user_nickname, 8));
                    iv_msg.setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(msgInfo.giftInfoData.gift_image_url).into(iv_msg);
                    name = " " + CommonUtil.getLimitString(msgInfo.giftInfoData.gift_user_nickname, 8) + " ";
                    builder.append(name);
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#262626")), 0, name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    builder.append("送出一个 ");
                    builder.append(msgInfo.giftInfoData.gift_name);
                    break;
                case MessageServer.EVENT_SURVEY:
                    builder.append(name);
                    builder.append(" ");
                    builder.append(ssb);
                    String sp = " 推送了" + msgInfo.survey_name + " 点击查看";
                    builder.append(sp);
                    VhClickSpan span = new VhClickSpan(mContext.getResources().getColor(R.color.color_3562FA), false) {
                        @Override
                        public void onClick(View widget) {
                            mSurveyServer.getHistorySurveyList(new RequestDataCallback() {
                                @Override
                                public void onSuccess(Object result) {
                                    if (null != result) {
                                        List<SurveyInfoData> dataList = (List<SurveyInfoData>) result;
                                        if (!dataList.isEmpty()) {
                                            for (SurveyInfoData surveyItem : dataList) {
                                                if (msgInfo.id.equals(surveyItem.question_id)) {
                                                    if ("0".equals(surveyItem.is_answered)) {
                                                        new SurveyWebView(mContext, SurveyInternal.createSurveyUrl(webinarInfo, msgInfo.id)).show();
                                                    } else {
                                                        ToastUtils.Companion.showToast("已参与");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onError(int errorCode, String errorMsg) {
                                    if(errorCode == ErrorCode.ERROR_NO_SUPPORT)
                                        new SurveyWebView(mContext, SurveyInternal.createSurveyUrl(webinarInfo, msgInfo.id)).show();
                                }
                            });
                        }
                    };
                    builder.setSpan(span, name.length() + showRoleText.length() + sp.length() - 4, name.length() + showRoleText.length() + sp.length() + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    break;

                case MessageServer.EVENT_SIGNIN:
                    builder.append(name);
                    builder.append(" ");
                    builder.append(ssb);
                    builder.append(" 发起了签到");
                    break;
                case MessageServer.EVENT_QUESTION:
                    builder.append(name)
                            .append(" ")
                            .append(ssb)
                            .append(msgInfo.status == 0 ? " 关闭" : " 开启")
                            .append("了问答");
                    break;
                case MessageServer.EVENT_START_LOTTERY://开始抽奖
                    builder.append("抽奖正在进行中");
                    break;
                case MessageServer.EVENT_END_LOTTERY://结束抽奖
                    String str = "抽奖已结束，查看中奖名单";
                    builder.append(str);
                    VhClickSpan lotteryspan = new VhClickSpan(mContext.getResources().getColor(R.color.color_3562FA), false) {
                        @Override
                        public void onClick(View widget) {
                            new LotteryListDialog(mContext, webinarInfo,msgInfo.lotteryInfo.lottery_id,activity).show();
                        }
                    };
                    builder.setSpan(lotteryspan, str.length() - 6, str.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    break;
                default:
                    break;
            }
            tv_msg.setMovementMethod(LinkMovementMethod.getInstance());//不设置点击会失效
            tv_msg.setText(builder);
        } else {
            viewHolder.getView(R.id.group_chat).setVisibility(View.VISIBLE);
            viewHolder.getView(R.id.group_msg).setVisibility(View.GONE);
            String name = CommonUtil.getLimitString(chatInfo.user_name, 8)+" ";
            SpannableStringBuilder ssb = new SpannableStringBuilder(name);
            if (!TextUtils.isEmpty(showRoleText)) {
                ssb.append(showRoleText);
                ssb.setSpan(new RadiusBackgroundSpan(Color.parseColor(roleBgColor), 18, Color.parseColor(roleColor)), name.length(), ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new AbsoluteSizeSpan(11, true), name.length(), ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            viewHolder.setText(R.id.tv_name, ssb);
            RequestOptions requestOptions = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.mipmap.icon_avatar);
            ImageView iv_avatar = viewHolder.getView(R.id.iv_avatar);
            ChatImagesView imagesView = viewHolder.getView(R.id.chat_image);
            if(chatInfo.avatar != null && chatInfo.avatar.contains("https"))
                Glide.with(mContext).load(chatInfo.avatar).apply(requestOptions).into(iv_avatar);
            else if(chatInfo.avatar != null && !chatInfo.avatar.contains("https"))
                Glide.with(mContext).load("https:"+chatInfo.avatar).apply(requestOptions).into(iv_avatar);
            viewHolder.setText(R.id.tv_time, CommonUtil.converChatTime(chatInfo.time));
            //处理回复消息
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    if (chatInfo.msgData != null) {
                        if (chatInfo.msgData.imageUrls != null && chatInfo.msgData.imageUrls.size() > 0) {
                            imagesView.setUrls(chatInfo.msgData.imageUrls);
                            imagesView.setVisibility(View.VISIBLE);
                        } else {
                            imagesView.setVisibility(View.GONE);
                        }
                        if (chatInfo.replyMsg != null) {
                            viewHolder.getView(R.id.tv_or_text).setVisibility(View.VISIBLE);
                            String textContent = chatInfo.replyMsg.content == null ? "" : chatInfo.replyMsg.content.textContent;
                            viewHolder.setText(R.id.tv_or_text, CommonUtil.getLimitString(chatInfo.replyMsg.user_name, 8) + " " + EmojiUtils.getEmojiText(mContext, textContent));
                            builder.append("回复 ");
                            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FC9600")), 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        } else {
                            viewHolder.getView(R.id.tv_or_text).setVisibility(View.GONE);
                        }
                        if (TextUtils.isEmpty(chatInfo.msgData.text)) {
                            if (chatInfo.replyMsg != null) {
                                viewHolder.setText(R.id.tv_text, builder);
                                viewHolder.getView(R.id.tv_text).setVisibility(View.VISIBLE);
                            } else {
                                viewHolder.getView(R.id.tv_text).setVisibility(View.GONE);
                            }
                        } else {
                            String textContent = chatInfo.msgData.text;
                            if (VhallSDK.getUserId().equals(chatInfo.msgData.target_id)) {
                                builder.insert(0, "私聊消息---");
                                if (textContent.endsWith("\n")) {
                                    textContent.replace("\n", "");
                                }
                            }
                            viewHolder.getView(R.id.tv_text).setVisibility(View.VISIBLE);
                            builder.append(EmojiUtils.getEmojiText(mContext, textContent));
                            viewHolder.setText(R.id.tv_text, builder);
                        }
                    }
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