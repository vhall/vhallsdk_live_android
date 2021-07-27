package com.vhall.uilibs.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.core.VhallKey;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.bean.StreamData;
import com.vhall.uilibs.widget.MyImageSpan;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.SurfaceViewRenderer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hkl
 * Date: 2020-06-12 15:26
 */
public class RenViewUtils {

    public static List<View> getViews(List<StreamData> streams, String mainId, Context context) {
        List<View> views = new ArrayList<>();
        if (ListUtils.isEmpty(streams)) {
            return null;
        } else if (streams.size() == 1) {
            Stream stream = streams.get(0).getStream();
            String userId = stream.userId;
            String attributes = stream.getAttributes();
            String avatar = "";
            String name = "", role = "2";
            if(stream.hasVideo()){
                stream.removeAllRenderView();
            }

            boolean hasAudio = 1 == streams.get(0).getVoice();
            boolean hasVideo = 1 == streams.get(0).getCamera();
            if (!TextUtils.isEmpty(attributes)) {
                try {
                    JSONObject a = new JSONObject(attributes);
                    if (a != null) {
                        name = a.optString(VhallKey.KEY_NICK_NAME);
                        role = a.optString(VhallKey.KEY_ROLE);
                        avatar = a.optString(VhallKey.KEY_AVATAR);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            View view = LayoutInflater.from(context).inflate(R.layout.item_rtc_only_view, null);
            VHRenderView renderView1 = view.findViewById(R.id.image1);
            renderView1.init(null, null);
            renderView1.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
            stream.addRenderView(renderView1);
            TextView tvName = view.findViewById(R.id.tv_name1);
            if (TextUtils.equals(mainId, userId)) {
                view.findViewById(R.id.star1).setVisibility(View.VISIBLE);
            }
            setRole(tvName, role, BaseUtil.getLimitString(name), context);
            ImageView ivAvatar = view.findViewById(R.id.avatar1);
            if (!hasVideo) {
                ivAvatar.setVisibility(View.VISIBLE);
                view.findViewById(R.id.iv_video1).setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(UserManger.judgePic(avatar))
                        .apply(new RequestOptions()
                                .placeholder(R.mipmap.ic_avatar)
                                .error(R.mipmap.ic_avatar))
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(ivAvatar);
            } else {
                ivAvatar.setVisibility(View.GONE);
                view.findViewById(R.id.iv_video1).setVisibility(View.GONE);
            }
            if (hasAudio) {
                view.findViewById(R.id.iv_audio1).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.iv_audio1).setVisibility(View.VISIBLE);
            }
            views.add(view);
            return views;
        } else {
            View view1 = null;
            View view2 = null;
            for (int i = 0; i < streams.size(); i++) {
                Stream stream = streams.get(i).getStream();
                String userId = stream.userId;
                String avatar = "";
                String attributes = stream.getAttributes();
                stream.removeAllRenderView();
                boolean hasAudio = 1 == streams.get(i).getVoice();
                boolean hasVideo = 1 == streams.get(i).getCamera();

                String name = "", role = "2";
                if (!TextUtils.isEmpty(attributes)) {
                    try {
                        JSONObject a = new JSONObject(attributes);
                        if (a != null) {
                            name = a.optString(VhallKey.KEY_NICK_NAME);
                            role = a.optString(VhallKey.KEY_ROLE);
                            avatar = a.optString(VhallKey.KEY_AVATAR);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                stream.removeAllRenderView();
                switch (i) {
                    case 0:
                        view1 = LayoutInflater.from(context).inflate(R.layout.item_rtc_view, null);
                        VHRenderView renderView1 = view1.findViewById(R.id.image1);
                        renderView1.init(null, null);
                        renderView1.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                        stream.addRenderView(renderView1);
                        TextView tvName = view1.findViewById(R.id.tv_name1);
                        if (TextUtils.equals(mainId, userId)) {
                            view1.findViewById(R.id.star1).setVisibility(View.VISIBLE);
                        }

                        ImageView ivAvatar = view1.findViewById(R.id.avatar1);
                        if (!hasVideo) {
                            ivAvatar.setVisibility(View.VISIBLE);
                            view1.findViewById(R.id.iv_video1).setVisibility(View.VISIBLE);
                            Glide.with(context)
                                    .load(UserManger.judgePic(avatar))
                                    .apply(new RequestOptions()
                                            .placeholder(R.mipmap.ic_avatar)
                                            .error(R.mipmap.ic_avatar))
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(ivAvatar);
                        } else {
                            ivAvatar.setVisibility(View.GONE);
                            view1.findViewById(R.id.iv_video1).setVisibility(View.GONE);
                        }
                        if (hasAudio) {
                            view1.findViewById(R.id.iv_audio1).setVisibility(View.GONE);
                        } else {
                            view1.findViewById(R.id.iv_audio1).setVisibility(View.VISIBLE);
                        }

                        setRole(tvName, role, BaseUtil.getLimitString(name), context);
                        break;
                    case 1:
                        VHRenderView renderView2 = view1.findViewById(R.id.image2);
                        renderView2.setVisibility(View.VISIBLE);
                        renderView2.init(null, null);
                        renderView2.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                        stream.addRenderView(renderView2);
                        view1.findViewById(R.id.bg2).setVisibility(View.VISIBLE);
                        TextView tvName2 = view1.findViewById(R.id.tv_name2);
                        if (TextUtils.equals(mainId, userId)) {
                            view1.findViewById(R.id.star2).setVisibility(View.VISIBLE);
                        }

                        ImageView ivAvatar2 = view1.findViewById(R.id.avatar2);
                        if (!hasVideo) {
                            ivAvatar2.setVisibility(View.VISIBLE);
                            view1.findViewById(R.id.iv_video2).setVisibility(View.VISIBLE);
                            Glide.with(context)
                                    .load(UserManger.judgePic(avatar))
                                    .apply(new RequestOptions()
                                            .placeholder(R.mipmap.ic_avatar)
                                            .error(R.mipmap.ic_avatar))
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(ivAvatar2);
                        } else {
                            ivAvatar2.setVisibility(View.GONE);
                            view1.findViewById(R.id.iv_video2).setVisibility(View.GONE);
                        }
                        if (hasAudio) {
                            view1.findViewById(R.id.iv_audio2).setVisibility(View.GONE);
                        } else {
                            view1.findViewById(R.id.iv_audio2).setVisibility(View.VISIBLE);
                        }

                        setRole(tvName2, role, BaseUtil.getLimitString(name), context);
                        ;
                        break;
                    case 2:
                        VHRenderView renderView3 = view1.findViewById(R.id.image3);
                        view1.findViewById(R.id.bg3).setVisibility(View.VISIBLE);
                        renderView3.setVisibility(View.VISIBLE);
                        renderView3.init(null, null);
                        renderView3.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                        stream.addRenderView(renderView3);
                        TextView tvName3 = view1.findViewById(R.id.tv_name3);
                        if (TextUtils.equals(mainId, userId)) {
                            view1.findViewById(R.id.star3).setVisibility(View.VISIBLE);
                        }
                        setRole(tvName3, role, BaseUtil.getLimitString(name), context);
                        ;

                        ImageView ivAvatar3 = view1.findViewById(R.id.avatar3);
                        if (!hasVideo) {
                            ivAvatar3.setVisibility(View.VISIBLE);
                            view1.findViewById(R.id.iv_video3).setVisibility(View.VISIBLE);
                            Glide.with(context)
                                    .load(UserManger.judgePic(avatar))
                                    .apply(new RequestOptions()
                                            .placeholder(R.mipmap.ic_avatar)
                                            .error(R.mipmap.ic_avatar))
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(ivAvatar3);
                        } else {
                            ivAvatar3.setVisibility(View.GONE);
                            view1.findViewById(R.id.iv_video3).setVisibility(View.GONE);
                        }
                        if (hasAudio) {
                            view1.findViewById(R.id.iv_audio3).setVisibility(View.GONE);
                        } else {
                            view1.findViewById(R.id.iv_audio3).setVisibility(View.VISIBLE);
                        }

                        break;
                    case 3:
                        VHRenderView renderView4 = view1.findViewById(R.id.image4);
                        view1.findViewById(R.id.bg4).setVisibility(View.VISIBLE);
                        renderView4.setVisibility(View.VISIBLE);
                        renderView4.init(null, null);
                        renderView4.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                        stream.addRenderView(renderView4);

                        TextView tvName4 = view1.findViewById(R.id.tv_name4);
                        if (TextUtils.equals(mainId, userId)) {
                            view1.findViewById(R.id.star4).setVisibility(View.VISIBLE);
                        }
                        setRole(tvName4, role, BaseUtil.getLimitString(name), context);
                        ;

                        ImageView ivAvatar4 = view1.findViewById(R.id.avatar4);
                        if (!hasVideo) {
                            ivAvatar4.setVisibility(View.VISIBLE);
                            view1.findViewById(R.id.iv_video4).setVisibility(View.VISIBLE);
                            Glide.with(context)
                                    .load(UserManger.judgePic(avatar))
                                    .apply(new RequestOptions()
                                            .placeholder(R.mipmap.ic_avatar)
                                            .error(R.mipmap.ic_avatar))
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(ivAvatar4);
                        } else {
                            ivAvatar4.setVisibility(View.GONE);
                            view1.findViewById(R.id.iv_video4).setVisibility(View.GONE);
                        }
                        if (hasAudio) {
                            view1.findViewById(R.id.iv_audio4).setVisibility(View.GONE);
                        } else {
                            view1.findViewById(R.id.iv_audio4).setVisibility(View.VISIBLE);
                        }

                        break;
                    case 4:
                        view2 = LayoutInflater.from(context).inflate(R.layout.item_rtc_view, null);
                        VHRenderView renderView5 = view2.findViewById(R.id.image1);
                        renderView5.init(null, null);
                        renderView5.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                        stream.addRenderView(renderView5);

                        TextView tvName21 = view2.findViewById(R.id.tv_name1);
                        if (TextUtils.equals(mainId, userId)) {
                            view2.findViewById(R.id.star1).setVisibility(View.VISIBLE);
                        }

                        ImageView ivAvatar21 = view2.findViewById(R.id.avatar1);
                        if (!hasVideo) {
                            ivAvatar21.setVisibility(View.VISIBLE);
                            view2.findViewById(R.id.iv_video1).setVisibility(View.VISIBLE);
                            Glide.with(context)
                                    .load(UserManger.judgePic(avatar))
                                    .apply(new RequestOptions()
                                            .placeholder(R.mipmap.ic_avatar)
                                            .error(R.mipmap.ic_avatar))
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(ivAvatar21);
                        } else {
                            ivAvatar21.setVisibility(View.GONE);
                            view2.findViewById(R.id.iv_video1).setVisibility(View.GONE);
                        }
                        if (hasAudio) {
                            view2.findViewById(R.id.iv_audio1).setVisibility(View.GONE);
                        } else {
                            view2.findViewById(R.id.iv_audio1).setVisibility(View.VISIBLE);
                        }

                        setRole(tvName21, role, BaseUtil.getLimitString(name), context);
                        ;
                        break;
                    case 5:
                        if (view2 == null) {
                            break;
                        }
                        VHRenderView renderView6 = view2.findViewById(R.id.image2);
                        view2.findViewById(R.id.bg2).setVisibility(View.VISIBLE);
                        renderView6.setVisibility(View.VISIBLE);
                        renderView6.init(null, null);
                        renderView6.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                        stream.addRenderView(renderView6);

                        TextView tvName22 = view2.findViewById(R.id.tv_name2);
                        if (TextUtils.equals(mainId, userId)) {
                            view2.findViewById(R.id.star2).setVisibility(View.VISIBLE);
                        }

                        ImageView ivAvatar22 = view2.findViewById(R.id.avatar2);
                        if (!hasVideo) {
                            ivAvatar22.setVisibility(View.VISIBLE);
                            view2.findViewById(R.id.iv_video2).setVisibility(View.VISIBLE);
                            Glide.with(context)
                                    .load(UserManger.judgePic(avatar))
                                    .apply(new RequestOptions()
                                            .placeholder(R.mipmap.ic_avatar)
                                            .error(R.mipmap.ic_avatar))
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(ivAvatar22);
                        } else {
                            ivAvatar22.setVisibility(View.GONE);
                            view2.findViewById(R.id.iv_video2).setVisibility(View.GONE);
                        }
                        if (hasAudio) {
                            view2.findViewById(R.id.iv_audio2).setVisibility(View.GONE);
                        } else {
                            view2.findViewById(R.id.iv_audio2).setVisibility(View.VISIBLE);
                        }

                        setRole(tvName22, role, BaseUtil.getLimitString(name), context);
                        ;
                        break;
                    default:
                        break;
                }
            }
            if (view1 != null) {
                views.add(view1);
            }
            if (view2 != null) {
                views.add(view2);
            }
            return views;
        }
    }

    private static void setRole(TextView tvName, String role, String name, Context context) {
        MyImageSpan imageSpan;
        SpannableStringBuilder builder = new SpannableStringBuilder("  " + name);
        switch (role) {
            case "1":
                imageSpan = new MyImageSpan(context, R.drawable.icon_chat_main);
                builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                tvName.setVisibility(View.VISIBLE);
                tvName.setText(builder);
                break;
            case "2":
                tvName.setVisibility(View.VISIBLE);
                tvName.setText(name);
                break;
            case "3":
                imageSpan = new MyImageSpan(context, R.drawable.icon_chat_assistan);
                builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                tvName.setVisibility(View.VISIBLE);
                tvName.setText(builder);
                break;
            case "4":
                imageSpan = new MyImageSpan(context, R.drawable.icon_chat_guest);
                builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                tvName.setVisibility(View.VISIBLE);
                tvName.setText(builder);
                break;
            default:
                break;
        }
    }
}
