package com.vhall.uilibs.interactive.broadcast.config;

import com.vhall.business_interactive.InterActive;

public class RtcConfig {
    static InterActive interActive;
    public static void setInteractive(InterActive interactive){
        RtcConfig.interActive = interactive;
    }

    public static InterActive getInterActive(){
        return interActive;
    }
}
