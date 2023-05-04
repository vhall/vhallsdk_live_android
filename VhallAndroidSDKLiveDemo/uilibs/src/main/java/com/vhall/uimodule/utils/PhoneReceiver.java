package com.vhall.uimodule.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.TelephonyManager;

public class PhoneReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            // 停止音频声音以接听来电
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
        } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            // 恢复音频声音以结束来电
            audioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, false);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }
}
