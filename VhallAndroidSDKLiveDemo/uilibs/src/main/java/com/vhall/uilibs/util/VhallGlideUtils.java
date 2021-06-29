package com.vhall.uilibs.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.uilibs.R;

public class VhallGlideUtils {
    public static void loadImage(Context context, String url, int placeId, int errorId, ImageView imageView){
        Glide.with(context).load(url).apply(new RequestOptions()
                .placeholder(placeId)
                .error(errorId)
                .transforms(new CircleCrop()))
                .into(imageView);
    }


    public static void loadCircleImage(Context context,String url,int placeId, int errorId, ImageView imageView){
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeId)
                        .error(errorId))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(imageView);
    }
}
