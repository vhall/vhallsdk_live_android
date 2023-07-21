package com.vhall.uimodule.watch.chapters;

import android.content.Context;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uimodule.R;
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