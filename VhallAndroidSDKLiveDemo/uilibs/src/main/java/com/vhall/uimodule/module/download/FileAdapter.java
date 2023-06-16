package com.vhall.uimodule.module.download;

import android.content.Context;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uimodule.R;
import com.vhall.vhss.data.FilesData;
import com.vhall.vhss.data.RecordsData;

/**
 * @author hkl
 * Date: 2022/7/21 18:01
 */
public class FileAdapter extends BaseQuickAdapter<FilesData.ListBean, BaseViewHolder> {
    private Context mContext;
    WebinarInfo webinarInfo;

    public FileAdapter(Context context, WebinarInfo webinarInfo) {
        super(R.layout.item_chapter);
        mContext = context;
        this.webinarInfo = webinarInfo;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, FilesData.ListBean chapterData) {
        viewHolder.setText(R.id.tv_idx, "");
        viewHolder.setText(R.id.tv_title, (chapterData.file_name.length()>8?(chapterData.file_name.substring(0,8)+"..."):chapterData.file_name));
        viewHolder.setText(R.id.tv_time, chapterData.file_size+"/"+chapterData.file_ext);
    }
}