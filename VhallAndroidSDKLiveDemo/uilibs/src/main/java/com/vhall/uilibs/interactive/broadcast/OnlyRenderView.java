package com.vhall.uilibs.interactive.broadcast;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.vhall.business.core.VhallKey;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.bean.StreamData;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.RenViewUtils;
import com.vhall.uilibs.util.UserManger;
import com.vhall.uilibs.util.VhallGlideUtils;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.SurfaceViewRenderer;

/**
 * @author hkl
 * Date: 2021/9/27 12:34 PM
 */
public class OnlyRenderView extends FrameLayout {
    private Context mContext;

    private VHRenderView renderView1;

    public OnlyRenderView(Context context, StreamData stream, String mainId) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.item_rtc_only_view, this);
        initView(context, stream, mainId);
    }

    private void initView(Context context, StreamData streamData, String mainId) {
        mContext = context;
        Stream stream = streamData.getStream();
        String userId = stream.userId;
        String attributes = stream.getAttributes();
        String avatar = "";
        String name = "", role = "2";
        try {
            stream.removeAllRenderView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean hasAudio = 1 == streamData.getVoice();
        boolean hasVideo = 1 == streamData.getCamera();
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
        renderView1 = findViewById(R.id.image1);
        renderView1.init(null, null);
        renderView1.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
        stream.addRenderView(renderView1);
        TextView tvName = findViewById(R.id.tv_name1);
        if (TextUtils.equals(mainId, userId)) {
            findViewById(R.id.star1).setVisibility(VISIBLE);
        } else {
            findViewById(R.id.star1).setVisibility(GONE);
        }
        RenViewUtils.setRole(tvName, role, BaseUtil.getLimitString(name), context);
        ImageView ivAvatar = findViewById(R.id.avatar1);
        if (!hasVideo) {
            ivAvatar.setVisibility(VISIBLE);
            findViewById(R.id.iv_video1).setVisibility(VISIBLE);
            VhallGlideUtils.loadCircleImage(context, UserManger.judgePic(avatar), R.mipmap.ic_avatar, R.mipmap.ic_avatar, ivAvatar);
        } else {
            ivAvatar.setVisibility(GONE);
            findViewById(R.id.iv_video1).setVisibility(GONE);
        }
        if (hasAudio) {
            findViewById(R.id.iv_audio1).setVisibility(GONE);
        } else {
            findViewById(R.id.iv_audio1).setVisibility(VISIBLE);
        }
    }

    public void destroy() {
        if (renderView1 != null) {
            renderView1.release();
        }
    }
}