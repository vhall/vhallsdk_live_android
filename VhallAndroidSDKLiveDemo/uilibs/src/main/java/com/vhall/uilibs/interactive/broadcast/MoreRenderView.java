package com.vhall.uilibs.interactive.broadcast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.vhall.uilibs.R;
import com.vhall.vhallrtc.client.VHRenderView;

import org.webrtc.SurfaceViewRenderer;


/**
 * @author hkl
 * Date: 2021/9/27 12:34 PM
 */
public class MoreRenderView extends FrameLayout {

    public VHRenderView renderView1;
    public VHRenderView renderView2;
    public VHRenderView renderView3;
    public VHRenderView renderView4;

    public MoreRenderView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.item_rtc_view, this);
        initView();
    }

    private void initView() {
        renderView1 = findViewById(R.id.image1);
        renderView1.init(null, null);
        renderView1.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);

        renderView2 = findViewById(R.id.image2);
        renderView2.init(null, null);
        renderView2.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);

        renderView3 = findViewById(R.id.image3);
        renderView3.init(null, null);
        renderView3.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);

        renderView4 = findViewById(R.id.image4);
        renderView4.init(null, null);
        renderView4.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
    }

    public void destroy() {
        if (renderView1 != null) {
            renderView1.release();
        }
        if (renderView2 != null) {
            renderView2.release();
        }
        if (renderView3 != null) {
            renderView3.release();
        }
        if (renderView4 != null) {
            renderView4.release();
        }
    }

    /**
     * @param num 显示的个数 最少一个
     */
    public void setShow(int num) {
        if (num < 4) {
            renderView4.setVisibility(View.GONE);
            findViewById(R.id.avatar4).setVisibility(GONE);
            findViewById(R.id.image4).setVisibility(GONE);
            findViewById(R.id.iv_audio4).setVisibility(GONE);
            findViewById(R.id.star4).setVisibility(GONE);
            findViewById(R.id.iv_video4).setVisibility(GONE);
            findViewById(R.id.tv_tag4).setVisibility(GONE);
            findViewById(R.id.tv_name4).setVisibility(GONE);
        }
        if (num < 3) {
            renderView3.setVisibility(View.GONE);
            findViewById(R.id.avatar3).setVisibility(GONE);
            findViewById(R.id.image3).setVisibility(GONE);
            findViewById(R.id.iv_audio3).setVisibility(GONE);
            findViewById(R.id.star3).setVisibility(GONE);
            findViewById(R.id.iv_video3).setVisibility(GONE);
            findViewById(R.id.tv_tag3).setVisibility(GONE);
            findViewById(R.id.tv_name3).setVisibility(GONE);
        }
        if (num < 2) {
            renderView2.setVisibility(View.GONE);
            findViewById(R.id.avatar2).setVisibility(GONE);
            findViewById(R.id.image2).setVisibility(GONE);
            findViewById(R.id.iv_audio2).setVisibility(GONE);
            findViewById(R.id.star2).setVisibility(GONE);
            findViewById(R.id.iv_video2).setVisibility(GONE);
            findViewById(R.id.tv_tag2).setVisibility(GONE);
            findViewById(R.id.tv_name2).setVisibility(GONE);
        }
    }
}