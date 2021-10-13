package com.vhall.uilibs.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
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
import com.vhall.uilibs.interactive.broadcast.MoreRenderView;
import com.vhall.uilibs.interactive.broadcast.OnlyRenderView;
import com.vhall.uilibs.widget.MyImageSpan;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

import org.eclipse.jetty.util.ajax.JSON;
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
    private static List<MoreRenderView> renderViews = new ArrayList<>();

    public static List<View> getViews(List<StreamData> streams, String mainId, Context context) {
        List<View> views = new ArrayList<>();
        if (ListUtils.isEmpty(streams)) {
            return null;
        } else if (streams.size() == 1) {
            if (streams.get(0) != null && streams.get(0).getStream() != null) {
                OnlyRenderView renderView = new OnlyRenderView(context, streams.get(0), mainId);
                views.add(renderView);
            }
            return views;
        } else {
            /**
             * 默认只添加4个页面 支持 1v15
             */
            if (ListUtils.isEmpty(renderViews) || renderViews.size() < 3) {
                renderViews.clear();
                for (int i = 0; i < 4; i++) {
                    MoreRenderView view = new MoreRenderView(context);
                    renderViews.add(view);
                }
            }
            /**
             * 防止 互动流多余16个 现在用不到
             */
            if (streams.size() > 16) {
                MoreRenderView view5 = new MoreRenderView(context);
                renderViews.add(view5);
                MoreRenderView view6 = new MoreRenderView(context);
                renderViews.add(view6);
            }
            /**
             * 当前总共有几个view 从1开始
             */
            int viewAllNumber = streams.size() <= 4 ? 1 : (streams.size() % 4 == 0 ? streams.size() / 4 : streams.size() / 4 + 1);

            for (int i = 0; i < viewAllNumber; i++) {
                views.add(renderViews.get(i));
            }
            hintOther(streams.size());
            for (int i = 0; i < streams.size(); i++) {
                Stream stream = streams.get(i).getStream();
                String userId = stream.userId;
                String avatar = "";
                String attributes = stream.getAttributes();
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
                /**
                 * 当前的view 在一页4个中的第几个
                 */
                int viewLocation = i % 4;
                /**
                 *当前的view 在第几页 从0开始
                 */
                int viewNumber = i < 4 ? 0 : i / 4;
                MoreRenderView nowView = (MoreRenderView) views.get(viewNumber);
                switch (viewLocation) {
                    case 0:
                        VHRenderView renderView1 = nowView.renderView1;
                        if (renderView1.getStream() != null) {
                            renderView1.getStream().removeAllRenderView();
                        }
                        renderView1.setVisibility(View.VISIBLE);
                        stream.addRenderView(renderView1);
                        TextView tvName = nowView.findViewById(R.id.tv_name1);
                        if (TextUtils.equals(mainId, userId)) {
                            nowView.findViewById(R.id.star1).setVisibility(View.VISIBLE);
                        }

                        ImageView ivAvatar = nowView.findViewById(R.id.avatar1);
                        if (!hasVideo) {
                            ivAvatar.setVisibility(View.VISIBLE);
                            nowView.findViewById(R.id.iv_video1).setVisibility(View.VISIBLE);
                            VhallGlideUtils.loadCircleImage(context, UserManger.judgePic(avatar), R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivAvatar);
                        } else {
                            ivAvatar.setVisibility(View.GONE);
                            nowView.findViewById(R.id.iv_video1).setVisibility(View.GONE);
                        }
                        if (hasAudio) {
                            nowView.findViewById(R.id.iv_audio1).setVisibility(View.GONE);
                        } else {
                            nowView.findViewById(R.id.iv_audio1).setVisibility(View.VISIBLE);
                        }

                        setRole(tvName, role, BaseUtil.getLimitString(name), context);
                        break;
                    case 1:
                        VHRenderView renderView2 = nowView.renderView2;
                        if (renderView2.getStream() != null) {
                            renderView2.getStream().removeAllRenderView();
                        }
                        renderView2.setVisibility(View.VISIBLE);
                        stream.addRenderView(renderView2);
                        nowView.findViewById(R.id.bg2).setVisibility(View.VISIBLE);
                        TextView tvName2 = nowView.findViewById(R.id.tv_name2);
                        if (TextUtils.equals(mainId, userId)) {
                            nowView.findViewById(R.id.star2).setVisibility(View.VISIBLE);
                        }

                        ImageView ivAvatar2 = nowView.findViewById(R.id.avatar2);
                        if (!hasVideo) {
                            ivAvatar2.setVisibility(View.VISIBLE);
                            nowView.findViewById(R.id.iv_video2).setVisibility(View.VISIBLE);
                            VhallGlideUtils.loadCircleImage(context, UserManger.judgePic(avatar), R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivAvatar2);

                        } else {
                            ivAvatar2.setVisibility(View.GONE);
                            nowView.findViewById(R.id.iv_video2).setVisibility(View.GONE);
                        }
                        if (hasAudio) {
                            nowView.findViewById(R.id.iv_audio2).setVisibility(View.GONE);
                        } else {
                            nowView.findViewById(R.id.iv_audio2).setVisibility(View.VISIBLE);
                        }

                        setRole(tvName2, role, BaseUtil.getLimitString(name), context);
                        break;
                    case 2:
                        VHRenderView renderView3 = nowView.renderView3;
                        if (renderView3.getStream() != null) {
                            renderView3.getStream().removeAllRenderView();
                        }
                        nowView.findViewById(R.id.bg3).setVisibility(View.VISIBLE);
                        renderView3.setVisibility(View.VISIBLE);
                        stream.addRenderView(renderView3);
                        TextView tvName3 = nowView.findViewById(R.id.tv_name3);
                        if (TextUtils.equals(mainId, userId)) {
                            nowView.findViewById(R.id.star3).setVisibility(View.VISIBLE);
                        }
                        setRole(tvName3, role, BaseUtil.getLimitString(name), context);
                        ImageView ivAvatar3 = nowView.findViewById(R.id.avatar3);
                        if (!hasVideo) {
                            ivAvatar3.setVisibility(View.VISIBLE);
                            nowView.findViewById(R.id.iv_video3).setVisibility(View.VISIBLE);
                            VhallGlideUtils.loadCircleImage(context, UserManger.judgePic(avatar), R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivAvatar3);

                        } else {
                            ivAvatar3.setVisibility(View.GONE);
                            nowView.findViewById(R.id.iv_video3).setVisibility(View.GONE);
                        }
                        if (hasAudio) {
                            nowView.findViewById(R.id.iv_audio3).setVisibility(View.GONE);
                        } else {
                            nowView.findViewById(R.id.iv_audio3).setVisibility(View.VISIBLE);
                        }

                        break;
                    case 3:
                        VHRenderView renderView4 = nowView.renderView4;
                        if (renderView4.getStream() != null) {
                            renderView4.getStream().removeAllRenderView();
                        }
                        renderView4.setVisibility(View.VISIBLE);
                        stream.addRenderView(renderView4);
                        TextView tvName4 = nowView.findViewById(R.id.tv_name4);
                        nowView.findViewById(R.id.bg4).setVisibility(View.VISIBLE);
                        if (TextUtils.equals(mainId, userId)) {
                            nowView.findViewById(R.id.star4).setVisibility(View.VISIBLE);
                        }
                        setRole(tvName4, role, BaseUtil.getLimitString(name), context);

                        ImageView ivAvatar4 = nowView.findViewById(R.id.avatar4);
                        if (!hasVideo) {
                            ivAvatar4.setVisibility(View.VISIBLE);
                            nowView.findViewById(R.id.iv_video4).setVisibility(View.VISIBLE);
                            VhallGlideUtils.loadCircleImage(context, UserManger.judgePic(avatar), R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivAvatar4);
                        } else {
                            ivAvatar4.setVisibility(View.GONE);
                            nowView.findViewById(R.id.iv_video4).setVisibility(View.GONE);
                        }
                        if (hasAudio) {
                            nowView.findViewById(R.id.iv_audio4).setVisibility(View.GONE);
                        } else {
                            nowView.findViewById(R.id.iv_audio4).setVisibility(View.VISIBLE);
                        }
                        break;
                    default:
                        break;
                }
            }
            return views;
        }
    }


    public static void setRole(TextView tvName, String role, String name, Context context) {
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

    public static void destroy() {
        if (!ListUtils.isEmpty(renderViews))
            for (MoreRenderView renderView : renderViews) {
                if (renderView != null) {
                    renderView.destroy();
                }
            }
        renderViews.clear();
    }

    /**
     * 复用窗口
     *
     * @param size 当前总共窗口的个数
     *             a页数
     *             b当前页显示的个数
     */
    private static void hintOther(int size) {
        int a = size / 4;
        int b = size % 4;
        if (!ListUtils.isEmpty(renderViews) && renderViews.size() > a && renderViews.get(a) != null) {
            renderViews.get(a).setShow(b);
        }
    }
}
