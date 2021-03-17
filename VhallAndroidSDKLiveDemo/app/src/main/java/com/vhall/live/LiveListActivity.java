package com.vhall.live;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.vhall.live.data.LiveListInfo;
import com.vhall.live.http.VHAPI;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.watch.VWatchActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * 主界面的Activity
 */
public class LiveListActivity extends Activity {

    private RecyclerView liveListView;
    private List<LiveListInfo.DataBean.ListsBean> items = new ArrayList<>();
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar liveListProgressBar;
//    private UserInfo userInfo = null;;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_list_activity);

        liveListProgressBar = findViewById(R.id.live_list_progress_bar);

        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(this::getLiveList);

        liveListView = findViewById(R.id.live_list);
        refreshLayout.setRefreshing(true);
        getLiveList();
    }

    private void getLiveList() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("auth_type", "");
        hashMap.put("account", "");
        hashMap.put("password", "");
        VHAPI.post(hashMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zhx", "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) return;

                LiveListInfo liveListInfo = JSON.parseObject(response.body().string(), LiveListInfo.class);
                if (liveListInfo == null || liveListInfo.data == null) {
                    return;
                }

                items = liveListInfo.data.lists;

                runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);
                    liveListView.setLayoutManager(new LinearLayoutManager(LiveListActivity.this));
                    liveListView.setAdapter(new LiveListAdapter());
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        liveListProgressBar.setVisibility(View.GONE);
    }

    private class LiveListAdapter extends RecyclerView.Adapter<LiveListViewHolder> {

        @NonNull
        @Override
        public LiveListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(LiveListActivity.this).inflate(R.layout.live_list_item, parent, false);
            return new LiveListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LiveListViewHolder holder, int position) {
            String currentState = "";
            if (items.get(position).status == 1) {
                currentState = "直播中";
            } else if (items.get(position).status == 2) {
                currentState = "预约中";
            } else {
                currentState = "已结束";
            }
            holder.live_list_state.setText(currentState);
            holder.live_list_subject.setText(items.get(position).subject);

            setBitmap(50, R.drawable.list_bg, holder.live_list_background);
            holder.live_list_background.setOnClickListener(v -> {
                if (liveListProgressBar.getVisibility() == View.VISIBLE) {
                    return;
                }
                liveListProgressBar.setVisibility(View.VISIBLE);

                Param param = VhallApplication.param;
                Intent intent = new Intent(LiveListActivity.this, VWatchActivity.class);
                param.watchId = items.get(position).webinar_id + "";
                intent.putExtra("param", param);
                intent.putExtra("type", VhallUtil.WATCH_LIVE);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }
    }

    public void setBitmap(int corners, int imageUrl, ImageView target) {
        RoundedCorners roundedCorners = new RoundedCorners(corners);
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);

        Glide.with(this).asBitmap().load(imageUrl).apply(options).into(target);
    }

    private static class LiveListViewHolder extends RecyclerView.ViewHolder {

        TextView live_list_state;
        TextView live_list_subject;
        ImageView live_list_background;

        public LiveListViewHolder(@NonNull View itemView) {
            super(itemView);
            live_list_state = itemView.findViewById(R.id.live_state);
            live_list_subject = itemView.findViewById(R.id.subject);
            live_list_background = itemView.findViewById(R.id.image_bg);
        }
    }

}
