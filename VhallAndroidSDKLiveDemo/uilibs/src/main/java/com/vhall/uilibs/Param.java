package com.vhall.uilibs;

import com.vhall.vhalllive.CameraFilterView;

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
    public int bufferSecond = 4;

    //用户相关
    public String userVhallId = "";
    public String userCustomId = "";
    public String userName = "";
    public String userAvatar = "";
}
