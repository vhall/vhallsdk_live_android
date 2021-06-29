package com.vhall.uilibs.util;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * @author hkl
 * Date: 2020-03-25 14:07
 */
public class HeadsetUtil {

    public static final int BLUETOOTH_ON = 2;
    public static final int BLUETOOTH_OFF = 3;

    public static final int HEADSET_ON = 4;
    public static final int HEADSET_OFF = 5;
    public static final int BLUETOOTH_STATUS_ON = 6;
    public static final int BLUETOOTH_STATUS_OFF = 7;
    private static HandlerThread handlerThread;
    private static Handler myHandler;

    private HeadsetReceiver headsetReceiver;
    public static void init(Application application){
        context = application;
    }

    public void open(Context context, HeadsetReceiver.NetChangeListener listener) {
        if (context == null || headsetReceiver != null) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        headsetReceiver = new HeadsetReceiver();
        //注册广播接收
        context.registerReceiver(headsetReceiver, filter);
        if (listener != null) {
            headsetReceiver.setListener(listener);
        }
    }

    public void releaseHeadsetReceiver() {
        stopBluetooth(context);
        Log.e("intent", "releaseHeadsetReceiver");
        Log.e("intent", "  isBluetoothScoOn"+mAudioManager.isBluetoothScoOn());
        if (headsetReceiver == null) {
            return;
        }
        headsetReceiver.setListener(null);
        // TODO: 4/28/21 application 修改
//        MyApplication.context.unregisterReceiver(headsetReceiver);
        context.unregisterReceiver(headsetReceiver);
        headsetReceiver = null;
    }

    public void stopBluetooth(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(mAudioManager.isBluetoothScoOn()){
            Log.e("intent", "setSco");
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
            setSco(mAudioManager, true);
        }
    }

    private AudioManager mAudioManager;
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * 第一次耳机链接 不处理（广播开始会直接又一个耳机的广播 这个时候会和自己本地的判断重复处理）
     */
    private boolean firstHeadSet = true;

    public void registerHeadsetReceiver() {
        firstHeadSet = true;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        open(context, new HeadsetReceiver.NetChangeListener() {
            @Override
            public void onChangeListener(int status) {
                if (status == HeadsetUtil.BLUETOOTH_ON) {
                    if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
                        Log.e("intent32", "isBluetoothScoAvailableOffCall");
                        return;
                    }
                    if (mAudioManager.getMode() == AudioManager.MODE_IN_CALL) {
                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                    }
                    mAudioManager.startBluetoothSco();
                    HeadsetUtil.setSco(mAudioManager);
                } else if (status == HeadsetUtil.BLUETOOTH_OFF) {
                    if (!mAudioManager.isWiredHeadsetOn()) {
                        mAudioManager.setBluetoothScoOn(false);
                        mAudioManager.stopBluetoothSco();
                    }
                } else if (status == HeadsetUtil.HEADSET_OFF) {
                    if (firstHeadSet) {
                        firstHeadSet = false;
                        return;
                    }
                    if (BluetoothProfile.STATE_CONNECTED != mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                        mAudioManager.setBluetoothScoOn(false);
                        mAudioManager.stopBluetoothSco();
                    }
                } else if (status == HeadsetUtil.HEADSET_ON) {
                    if (firstHeadSet) {
                        firstHeadSet = false;
                        return;
                    }
                    if (BluetoothProfile.STATE_CONNECTED != mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                        if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
                            return;
                        }
                        mAudioManager.startBluetoothSco();
                        if (mAudioManager.getMode() == AudioManager.MODE_IN_CALL) {
                            mAudioManager.setMode(AudioManager.MODE_NORMAL);
                        }
                        HeadsetUtil.setSco(mAudioManager);
                    }
                }
            }
        });
    }


    public static void setSco(AudioManager mAudioManager) {
        setSco(mAudioManager, false);
    }
    private static Application context;

    /**
     * 重连次数
     */
    private static int mConnectIndex = 0;
    /**
     * 是否已经 再链接防止频繁注册（因为直播断开问题流程没法正常走完 所以每次 断开的时候 需要重置）
     */
    private static boolean isRegister = false;

    /**
     * @param close 表示此次操作是断开还是链接
     *              断开则只处理 SCO_AUDIO_STATE_DISCONNECTED 和失败
     *              链接则只处理 SCO_AUDIO_STATE_CONNECTED 和失败
     */
    public static void setSco(final AudioManager mAudioManager, final boolean close) {
        mConnectIndex = 0;
//        if (!close && isRegister) {
//            return;
//        }
//        if (!close) {
//            isRegister = true;
//        }
        Log.e("intent", "  isBluetoothScoOn"+mAudioManager.isBluetoothScoOn());
        aaa(mAudioManager,close);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                Log.e("intent", intent.getAction());
                Log.e("intent", "close"+close);
                Log.e("intent", "sco status  " + state);
                BroadcastReceiver receiver = this;
                if (AudioManager.SCO_AUDIO_STATE_DISCONNECTED == state && close) {
                    isRegister = false;
                    mAudioManager.setBluetoothScoOn(false);  //打开SCO
                    Log.e("intent", "sco断开链接成功");
                    context.unregisterReceiver(receiver);  //别遗漏
                    if (myHandler != null) {
                        myHandler.removeCallbacksAndMessages(null);
                        myHandler = null;
                    }
                    return;
                }
                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state && !close) {
                    isRegister = false;
                    mAudioManager.setBluetoothScoOn(true);  //打开SCO
                    Log.e("intent", "sco链接成功");
                    context.unregisterReceiver(receiver);  //别遗漏
                    if (myHandler != null) {
                        myHandler.removeCallbacksAndMessages(null);
                        myHandler = null;
                    }
                }

            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
    }

    private static void aaa(final AudioManager mAudioManager, final boolean close){
        if (handlerThread == null) {
            handlerThread = new HandlerThread("HeadsetUtil");
            handlerThread.start();
        }
        if (myHandler == null) {
            myHandler = new Handler(handlerThread.getLooper());
        }
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //重连5次
                if (mConnectIndex < 10) {
                    if (close) {
                        mAudioManager.stopBluetoothSco();
                        mAudioManager.setBluetoothScoOn(false);
                    } else {
                        mAudioManager.startBluetoothSco();//再次尝试连接
                    }
                } else {
                    if (close) {
                        mAudioManager.stopBluetoothSco();
                        mAudioManager.setBluetoothScoOn(false);
                    } else {
                        mAudioManager.setBluetoothScoOn(true);
                    }
                    myHandler.removeCallbacksAndMessages(null);
                    myHandler = null;
                    isRegister = false;
                }
                mConnectIndex++;
            }
        });
    }

}