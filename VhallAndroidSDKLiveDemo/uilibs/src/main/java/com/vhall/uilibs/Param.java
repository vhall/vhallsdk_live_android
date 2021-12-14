package com.vhall.uilibs;


import com.vhall.push.VHLivePushFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 直播参数类
 */
public class Param implements Serializable {

    //发直播相关
    public String broId = "";
    public String broName = "";
    public String broToken = "5d56d034e933e1826c7c896859019e33";
    public String guestPwd = "123456";//嘉宾口令
    public String guestAvatar = "";//嘉宾头像
    public int pixel_type = VHLivePushFormat.PUSH_MODE_HD;
    public int videoBitrate = 500;
    public int videoFrameRate = 20;
    public int screenOri = VHLivePushFormat.SCREEN_ORI_PORTRAIT;
    //看直播相关
    public String watchId = "";
    public String key = "";
    public int bufferSecond = 6;
    public String vssToken ;
    public String vssRoomId;
    public String webinar_id;
    public String join_id;
    public String noticeContent;
    public int inav_num;
    public List<String> filters=new ArrayList<>();
    //当前选择的是不是无延迟观看（是无延迟互动 上麦后关闭页面）
    public boolean noDelay=false;

    //互动相关
//    public int interactive_definition = VHILSS.SD;
}
