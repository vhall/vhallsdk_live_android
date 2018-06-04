//package com.vhall.uilibs.util;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//import android.widget.PopupWindow;
//import com.vhall.uilibs.R;
//import com.vhall.business_support.dlna.DeviceDisplay;
//
//import org.fourthline.cling.model.meta.Device;
//
///**
// * Created by huanan on 2017/9/1.
// * 紧投屏使用
// */
//public class DevicePopu extends PopupWindow {
//    private Context context;
//    private ListView mListView;
//    private ArrayAdapter<DeviceDisplay> listAdapter;
//
//    public DevicePopu(Context context) {
//        super(context);
//        this.context = context;
//        ColorDrawable dw = new ColorDrawable(Color.WHITE);
//        setBackgroundDrawable(dw);
//        setFocusable(true);
//        View root = View.inflate(context, R.layout.device_layout, null);
//        setContentView(root);
//        mListView = (ListView) root.findViewById(R.id.lv_device);
//        listAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
//        mListView.setAdapter(listAdapter);
//    }
//
//    public void deviceAdded(final Device device) {
//        DeviceDisplay d = new DeviceDisplay(device);
//        int position = listAdapter.getPosition(d);
//        if (position >= 0) {
//            // Device already in the list, re-set new value at same position
//            listAdapter.remove(d);
//            listAdapter.insert(d, position);
//        } else {
//            listAdapter.add(d);
//        }
//    }
//
//    public void deviceRemoved(final Device device) {
//        listAdapter.remove(new DeviceDisplay(device));
//    }
//
//    public void clear() {
//        listAdapter.clear();
//    }
//
//    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
//        mListView.setOnItemClickListener(onItemClickListener);
//    }
//}
