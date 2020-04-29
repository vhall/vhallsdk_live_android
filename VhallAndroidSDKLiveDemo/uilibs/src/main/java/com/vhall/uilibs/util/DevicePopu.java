package com.vhall.uilibs.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business_support.dlna.DMCControl;
import com.vhall.business_support.dlna.DMCControlListener;
import com.vhall.business_support.dlna.DeviceDisplay;

import org.fourthline.cling.model.meta.Device;

import com.vhall.uilibs.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by huanan on 2017/9/1.
 * 仅投屏使用
 */
public class DevicePopu extends PopupWindow {
    private static final String TAG = "DevicePopu";
    private Context context;
    private ListView mListView;
    private ArrayAdapter<DeviceDisplay> listAdapter;
    private CheckBox cbPlayPause;
    private TextView tvCurrentTime, tvDuration,tvNoDevice;
    private SeekBar seekBarTv;
    private DMCControlListener controlListener;
    private DMCControl dmcControl;
    private LinearLayout llControl;
    private Timer timer;


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (dmcControl != null) {
                        dmcControl.getPositionInfo();
                    }
                    break;
            }
            return false;
        }
    });

    public DevicePopu(final Context context) {
        super(context);
        this.context = context;
        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        setBackgroundDrawable(dw);
        setFocusable(true);
        View root = View.inflate(context, R.layout.device_layout, null);
        setContentView(root);
        mListView = (ListView) root.findViewById(R.id.lv_device);
        llControl = root.findViewById(R.id.ll_control);
        listAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        mListView.setAdapter(listAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (llControl.getVisibility() == View.GONE) {
                    llControl.setVisibility(View.VISIBLE);
                }
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(parent, view, position, id);
                }
            }
        });

        cbPlayPause = root.findViewById(R.id.cb_play_pause);
        tvCurrentTime = root.findViewById(R.id.tv_current_time);
        tvDuration = root.findViewById(R.id.tv_duration);
        seekBarTv = root.findViewById(R.id.seekbar_tv);

        tvNoDevice = root.findViewById(R.id.tv_no_device);

        cbPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dmcControl != null) {
                    if (cbPlayPause.isChecked()) {
                        dmcControl.play();
                        handlePosition();
                    } else {
                        dmcControl.pause();
                        timer.cancel();
                        timer = null;
                    }
                }

            }
        });

        seekBarTv.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String tartget = VhallUtil.converLongTimeToStr((long)seekBar.getProgress()*1000);
                dmcControl.seekToPosition(tartget);
            }
        });

        controlListener = new DMCControlListener() {
            @Override
            public void onStart() {
                cbPlayPause.setChecked(true);
            }

            @Override
            public void onPause() {
                cbPlayPause.setChecked(false);
            }

            @Override
            public void onStop() {
                cbPlayPause.setChecked(false);
            }

            @Override
            public void currentPosition(String curTime, String duration) {
                tvDuration.setText(duration);
//                tvCurrentTime.setText(VhallUtil.converLongTimeToStr(position));
                tvCurrentTime.setText(curTime);
                int max = VhallUtil.converTimeStrToSecond(duration);
                seekBarTv.setMax(max);
                int progress = VhallUtil.converTimeStrToSecond(curTime);
                seekBarTv.setProgress(progress);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.e(TAG, "errorCode: " + errorCode + "--errorMsg:" + errorMsg);
                Toast.makeText(context,errorMsg,Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void handlePosition() {
        if (timer != null) {
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 1000);
    }

    public void setDmcControl(DMCControl dmcControl) {
        this.dmcControl = dmcControl;
        if (dmcControl != null) {
            dmcControl.setDMCControlListener(controlListener);
        }
    }

    public void deviceAdded(final Device device) {
        if(tvNoDevice.getVisibility()== View.VISIBLE){
            tvNoDevice.setVisibility(View.GONE);
        }
        DeviceDisplay d = new DeviceDisplay(device);
        int position = listAdapter.getPosition(d);
        if (position >= 0) {
            // Device already in the list, re-set new value at same position
            listAdapter.remove(d);
            listAdapter.insert(d, position);
        } else {
            listAdapter.add(d);
        }

    }

    public void deviceRemoved(final Device device) {
        listAdapter.remove(new DeviceDisplay(device));
    }

    public void clear() {
        listAdapter.clear();
    }

    private AdapterView.OnItemClickListener itemClickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        if(listAdapter.getCount() == 0){
            tvNoDevice.setVisibility(View.VISIBLE);
        }else{
            tvNoDevice.setVisibility(View.GONE);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        llControl.setVisibility(View.GONE);
        if (dmcControl != null) {
            dmcControl.stop();
        }
    }

}
