package com.vhall.uimodule.module.chapters;

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
import com.vhall.uimodule.module.chat.ChatMessageData;
import com.vhall.uimodule.utils.CommonUtil;
import com.vhall.uimodule.widget.RadiusBackgroundSpan;
import com.vhall.vhss.data.RecordChaptersData;

/**
 * @author hkl
 * Date: 2022/7/21 18:01
 */
public class ChapterAdapter extends BaseQuickAdapter<RecordChaptersData.ListBean, BaseViewHolder> {
    private Context mContext;
    WebinarInfo webinarInfo;

    public ChapterAdapter(Context context, WebinarInfo webinarInfo) {
        super(R.layout.item_chapter);
        mContext = context;
        this.webinarInfo = webinarInfo;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, RecordChaptersData.ListBean chapterData) {
        viewHolder.setText(R.id.tv_idx, viewHolder.getAdapterPosition()+1+".");
        viewHolder.setText(R.id.tv_title, chapterData.title);
        viewHolder.setText(R.id.tv_time, getTime(chapterData.created_at));
    }

    protected String getTime(double created_at) {
        int h = (int) (created_at/3600);
        int m = (int) (created_at-h*3600)/60;
        int s = (int) (created_at%60);
        return String.format("%02d:%02d:%02d", h,m,s);
    }
} 