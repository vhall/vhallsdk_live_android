package com.vhall.uimodule.module.records;

import android.content.Context;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uimodule.R;
import com.vhall.vhss.data.RecordsData;

/**
 * @author hkl
 * Date: 2022/7/21 18:01
 */
public class RecordAdapter extends BaseQuickAdapter<RecordsData.ListBean, BaseViewHolder> {
    private Context mContext;
    WebinarInfo webinarInfo;

    public RecordAdapter(Context context, WebinarInfo webinarInfo) {
        super(R.layout.item_chapter);
        mContext = context;
        this.webinarInfo = webinarInfo;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, RecordsData.ListBean chapterData) {
        viewHolder.setText(R.id.tv_idx, "");
        if(webinarInfo.record_id == null)
            viewHolder.setText(R.id.tv_title, (chapterData.name.length()>8?(chapterData.name.substring(0,8)+"..."):chapterData.name));
        else
            viewHolder.setText(R.id.tv_title, (chapterData.name.length()>8?(chapterData.name.substring(0,8)+"..."):chapterData.name)+(chapterData.record_id == Integer.parseInt(webinarInfo.record_id) ?"(播放中)":""));
        viewHolder.setText(R.id.tv_time, chapterData.duration);
    }
}