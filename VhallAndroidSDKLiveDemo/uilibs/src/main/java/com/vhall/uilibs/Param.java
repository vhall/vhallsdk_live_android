package com.vhall.uilibs;


import com.vhall.vhalllive.pushlive.CameraFilterView;

import java.io.Serializable;

/**
 * 直播参数类
 */
public class Param implements Serializable {

    //发直播相关
    public String broId = "";
    public String broToken = "";
    public int pixel_type = CameraFilterView.TYPE_HDPI;
    public int videoBitrate = 500;
    public int videoFrameRate = 20;
    //看直播相关
    public String watchId = "";
    public String key = "";
    public int bufferSecond = 6;

    //互动相关
//    public int interactive_definition = VHILSS.SD;
}
