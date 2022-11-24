package com.vhall.uilibs.watch.minimalist;

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
import org.vhwebrtc.SurfaceViewRenderer;

/**
 * @author hkl
 * 显示互动流的小窗口
 * Date: 2021/9/27 12:34 PM
 */
public class MiniRenderView extends FrameLayout {
    private Context mContext;
    private Stream stream;
    private VHRenderView renderView1;

    public MiniRenderView(Context context, StreamData streamData) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.item_rtc_mini_view, this);
        stream = streamData.getStream();
        initView(context, streamData);
    }


    public Stream getStream() {
        return stream;
    }

    private void initView(Context context, StreamData streamData) {
        mContext = context;
        stream = streamData.getStream();
        String userId = stream.userId;
        String attributes = stream.getAttributes();
        String avatar = "";
        String name = "", role = "2";
        try {
            stream.removeAllRenderView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(attributes)) {
            try {
                JSONObject a = new JSONObject(attributes);
                name = a.optString(VhallKey.KEY_NICK_NAME);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        renderView1 = findViewById(R.id.image1);
        renderView1.init(null, null);
        renderView1.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        stream.addRenderView(renderView1);
        TextView tvName = findViewById(R.id.tv_name1);
        tvName.setText(BaseUtil.getLimitString(name));

    }

    public void destroy() {
        if (renderView1 != null) {
            renderView1.release();
        }
    }
}