package com.vhall.uimodule.module.chat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vhall.uimodule.R;

import java.util.List;

public class ChatImagesView extends RecyclerView {

    public interface OnItemClickListener {
        void onItemClick(int position, List<String> urls);
    }

    private List<String> urls;
    private OnItemClickListener listener;

    public ChatImagesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setUrls(List<String> urls) {
        if (urls == null) {
            return;
        }

        this.urls = urls;
        int spanCount = Math.min(urls.size(), 4);
        setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        setAdapter(new ImageAdapter());
    }

    private class ImageAdapter extends Adapter<ImageHolder> {

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.chat_image_item, parent, false);
            return new ImageHolder(view);
        }

        @SuppressLint("RecyclerView")
        @Override
        public void onBindViewHolder(ImageHolder holder, int position) {
            Glide.with(getContext()).
                    load(urls.get(position))
                    .into(holder.imageView);
            holder.imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(position, urls);
                    }

                    showAsDialog(urls.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }
    }

    private void showAsDialog(String imgUrl) {
        final ImgItemDialog imgDialog = new ImgItemDialog(getContext(), imgUrl);
        imgDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        imgDialog.show();
    }

    private class ImageHolder extends ViewHolder {

        ImageView imageView;

        public ImageHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.chat_image);
        }
    }

    private class ImgItemDialog extends Dialog {

        private ImageView mItemView;
        private String mImgUrl;

        public ImgItemDialog(@NonNull Context context, String imgUrl) {
            super(context);
            mImgUrl = imgUrl;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            View contentView = View.inflate(getContext(), R.layout.layout_img_gallery, null);
            setContentView(contentView);

            setCancelable(true);
            contentView.findViewById(R.id.gallery_root).setOnClickListener(v -> dismiss());

            mItemView = contentView.findViewById(R.id.gallery_item);
            Glide.with(getContext()).load(mImgUrl).into(mItemView);
            mItemView.setOnClickListener(v -> dismiss());
        }

        @Override
        public void show() {
            super.show();

            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.width= WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height= WindowManager.LayoutParams.MATCH_PARENT;
            getWindow().setAttributes(layoutParams);
        }
    }
}
