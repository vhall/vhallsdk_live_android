package com.vhall.uimodule.module.interactive;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.uimodule.R;
import com.vhall.uimodule.utils.CommonUtil;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

import org.vhwebrtc.SurfaceViewRenderer;

/**
 * @author hkl
 * 显示互动流的小窗口
 * Date: 2021/9/27 12:34 PM
 */
public class MiniRenderView extends FrameLayout {
    private Context mContext;
    public Stream stream;
    public VHRenderView renderView1;
    public StreamData streamData;
    private ImageView ivAvatar;

    public void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.item_rtc_mini_view, this);
        mContext = context;
    }

    public MiniRenderView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public MiniRenderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MiniRenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setStream(StreamData streamData) {
        this.streamData = streamData;
        stream = streamData.getStream();
        String avatar = streamData.getAvatar();
        String name = streamData.getName();
        try {
            stream.removeAllRenderView();
        } catch (Exception e) {
            e.printStackTrace();
        }

        renderView1 = findViewById(R.id.render_view);
        if (renderView1.getTag() == null || !renderView1.getTag().equals(renderView1.hashCode())) {
            renderView1.init(null, null);
            renderView1.setTag(renderView1.hashCode());
            renderView1.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        }
        stream.addRenderView(renderView1);
        ivAvatar = findViewById(R.id.iv_avatar);
        RequestOptions requestOptions = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.mipmap.icon_avatar);
        Glide.with(mContext).load(avatar).apply(requestOptions).into(ivAvatar);
        TextView tvName = findViewById(R.id.tv_name);
        tvName.setText(CommonUtil.getLimitString(name, 8));
        // 1 false 关
        ivAvatarVisibility(streamData.getCamera() == 0);
    }

    public void ivAvatarVisibility(boolean show) {
        if (show) {
            ivAvatar.setVisibility(VISIBLE);
        } else {
            ivAvatar.setVisibility(GONE);
        }
    }

    public void destroy() {
        if (renderView1 != null) {
            renderView1.release();
        }

        if (null != stream) {
            stream.removeAllRenderView();
        }
    }
}