package com.vhall.uilibs;


import com.vhall.push.VHLivePushFormat;

import java.io.Serializable;

/**
 * 直播参数类
 */
public class Param implements Serializable {

    //发直播相关
    public String broId = "";
    public String broToken = "";
    public int pixel_type = VHLivePushFormat.PUSH_MODE_HD;
    public int videoBitrate = 500;
    public int videoFrameRate = 20;
    public int screenOri = VHLivePushFormat.SCREEN_ORI_PORTRAIT;
    //看直播相关
    public String watchId = "";
    public String key = "";
    public int bufferSecond = 6;

    //互动相关
//    public int interactive_definition = VHILSS.SD;
}
