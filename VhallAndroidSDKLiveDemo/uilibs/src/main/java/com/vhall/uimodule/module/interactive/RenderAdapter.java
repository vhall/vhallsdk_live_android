package com.vhall.uimodule.module.interactive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.uimodule.R;
import com.vhall.uimodule.utils.DensityUtils;

import java.util.ArrayList;

/**
 * @author hkl
 * Date: 2022/12/22 14:20
 */
public class RenderAdapter extends RecyclerView.Adapter<RenderAdapter.RenderHolder> {

    private String mainId;
    private ArrayList<StreamData> mStreamDataList = new ArrayList<>();

    public RenderAdapter(String mainId) {
        this.mainId = mainId;
    }

    public void setMainId(String mainId) {
        this.mainId = mainId;
        int mainScreenOldIndex = findIndexById(mainId);
        if (mainScreenOldIndex >= 0) {
            StreamData mainScreenStream = mStreamDataList.get(mainScreenOldIndex);

            mStreamDataList.remove(mainScreenOldIndex);
            notifyItemRemoved(mainScreenOldIndex);

            mStreamDataList.add(0, mainScreenStream);
            notifyItemInserted(0);
        }
    }

    private void showAvatar(ImageView avatar, String avatarUrl) {
        RequestOptions requestOptions = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.mipmap.icon_avatar);
        Glide.with(avatar.getContext()).load(avatarUrl).apply(requestOptions)
                .into(avatar);
    }

    public void removeData(StreamData item) {
        int index = findIndexOfData(item);
        if (index >= 0) {
            mStreamDataList.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void addNewData(StreamData item) {
        int index = findIndexOfData(item);
        if (index >= 0) {
            mStreamDataList.remove(index);
            notifyItemRemoved(index);
        }
        if (item.getStreamUserId().equals(mainId)) {
            mStreamDataList.add(0, item);
            notifyItemInserted(0);
        } else {
            mStreamDataList.add(item);
            notifyItemInserted(mStreamDataList.size());
        }
    }

    public void changeItemData(StreamData item) {
        int index = findIndexOfData(item);
        if (index >= 0) {
            mStreamDataList.remove(index);
            mStreamDataList.add(index, item);
            notifyItemChanged(index);
        }
    }

    public void updateCameraStatus(String userId, boolean cameraOff) {
        int index = findIndexById(userId);
        StreamData targetStream = mStreamDataList.get(index);
        targetStream.setCamera(cameraOff ? 0 : 1);
        notifyItemChanged(index);
    }

    private int findIndexById(String mainId) {
        for (int i = 0; i < mStreamDataList.size(); i++) {
            if (mStreamDataList.get(i).getStreamUserId().equals(mainId)) {
                return i;
            }
        }
        return -1;
    }

    private int findIndexOfData(StreamData targetItem) {
        for (int i = 0; i < mStreamDataList.size(); i++) {
            if (mStreamDataList.get(i).getStream().streamId.equals(targetItem.getStream().streamId)) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public RenderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RenderHolder holder = new RenderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_render_view, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RenderHolder holder, int position) {
        StreamData itemStreamData = mStreamDataList.get(position);
        holder.renderView.setStream(itemStreamData);
        holder.renderView.setLayoutParams(new ConstraintLayout.LayoutParams(DensityUtils.getScreenWidth() / 2, DensityUtils.getScreenWidth() * 9 / 32));
        if (itemStreamData.getCamera() == 0) {
            showAvatar(holder.avatar, itemStreamData.getAvatar());
        }
    }

    @Override
    public int getItemCount() {
        return mStreamDataList.size();
    }

//    @Override
//    public void onViewAttachedToWindow(@NonNull RenderHolder holder) {
//        super.onViewAttachedToWindow(holder);
//        holder.renderView.getStream().unmuteVideo(null);
//    }

    public class RenderHolder extends RecyclerView.ViewHolder {

        MiniRenderView renderView;
        ImageView avatar;

        public RenderHolder(View itemView) {
            super(itemView);
            renderView = itemView.findViewById(R.id.render);
            avatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}