package com.vhall.uilibs.watch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vhall.business.ChatServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.R;
import com.vhall.uilibs.chat.ChatContract;
import com.vhall.uilibs.util.CommonUtil;
import com.vhall.uilibs.util.emoji.EmojiUtils;
import com.vhall.vhss.data.RecordChaptersData;


/**
 * 章节打点
 */
public class ChapterFragment extends Fragment implements WatchContract.ChapterView {
    public static ChapterFragment newInstance() {
        ChapterFragment chapterFragment = new ChapterFragment();
        return chapterFragment;
    }

    private RecyclerView recyclerView;
    private MyAdapter adapter = new MyAdapter();

    private WatchContract.ChapterPresenter mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_chapter, null);
        recyclerView = inflate.findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
            mPresenter.setSeekToChapter((int) (adapter.getData().get(i).created_at * 1000));
        });
        return inflate;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setPresenter(WatchContract.ChapterPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void loadData(WebinarInfo info) {
        VhallSDK.getRecordChaptersList(info.record_id, new RequestDataCallbackV2<RecordChaptersData>() {
            @Override
            public void onSuccess(RecordChaptersData data) {
                adapter.setNewData(data.list);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }

    class MyAdapter extends BaseQuickAdapter<RecordChaptersData.ListBean, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.chapter_item);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder viewHolder, RecordChaptersData.ListBean data) {
            viewHolder.setText(R.id.tv_title, data.title);
            viewHolder.setText(R.id.tv_time, CommonUtil.converLongTimeToStr((long) data.created_at * 1000));
        }

    }

}
