package com.vhall.uilibs.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

/**
 * @author hkl
 * Date: 2020-03-25 14:06
 */
public class HeadsetReceiver extends BroadcastReceiver {

    public NetChangeListener listener;

    public void setListener(NetChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 如果相等的话就说明网络状态发生了变化
        Log.e("intent2", intent.getAction());
        if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            if (listener != null) {
                listener.onChangeListener(HeadsetUtil.BLUETOOTH_ON);
            }
        } else if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            if (listener != null) {
                listener.onChangeListener(HeadsetUtil.BLUETOOTH_OFF);
            }
        } else if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", 0);
            if (listener != null) {
                if (state == 1) {
                    listener.onChangeListener(HeadsetUtil.HEADSET_ON);
                } else {
                    listener.onChangeListener(HeadsetUtil.HEADSET_OFF);
                }
            }
        }else if (intent.getAction().equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);
            if (state == 0) {
                Log.d("bul", "[Bluetooth] State: disconnected");
                if (listener != null) {
                    listener.onChangeListener(HeadsetUtil.HEADSET_ON);
                }
            } else if (state == 2) {
                Log.d("bul", "[Bluetooth] State: connected");
                if (listener != null) {
                    listener.onChangeListener(HeadsetUtil.HEADSET_ON);
                }
            } else {
                Log.d("bul", "[Bluetooth] State: " + state);
            }
        }
    }

    // 自定义接口
    public interface NetChangeListener {
        void onChangeListener(int status);
    }

}
