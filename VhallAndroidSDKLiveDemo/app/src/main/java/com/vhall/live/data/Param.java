package com.vhall.live.data;

import com.vinny.vinnylive.CameraFilterView;

import java.io.Serializable;

/**
 * 直播参数类
 */
public class Param implements Serializable {
    public static final int HDPI = CameraFilterView.TYPE_HDPI;
    public static final int XHDPI = CameraFilterView.TYPE_XHDPI;

    public static final int BROADCAST = 0x00;
    public static final int WATCH_LIVE = 0x01;
    public static final int WATCH_PLAYBACK = 0x02;

    public String id;
    public String token;
    public int videoBitrate;
    public int frameRate;
    public int bufferSecond;
    public String k;
    public int pixel_type = CameraFilterView.TYPE_HDPI;
    public int screenOri;
    public int watch_type;
    public String record_id; //片段ID 观看回放时使用
}
