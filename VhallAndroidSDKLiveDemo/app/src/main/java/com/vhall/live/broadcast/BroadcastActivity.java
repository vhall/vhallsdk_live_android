package com.vhall.live.broadcast;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.vhall.live.R;
import com.vhall.live.chat.ChatFragment;
import com.vhall.live.data.Param;
import com.vhall.live.utils.ActivityUtils;

/**
 * 发直播界面的Activity
 */
public class BroadcastActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Param param = (Param) getIntent().getSerializableExtra("param");
        if (param != null)
            setRequestedOrientation(param.screenOri);
        setContentView(R.layout.broadcast_activity);
        BroadcastFragment mainFragment = (BroadcastFragment) getSupportFragmentManager().findFragmentById(R.id.broadcastFrame);
        if (mainFragment == null) {
            mainFragment = BroadcastFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    mainFragment, R.id.broadcastFrame);
        }
        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chatFrame);
        if (chatFragment == null) {
            chatFragment = ChatFragment.newInstance(param.BROADCAST, false);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    chatFragment, R.id.chatFrame);
        }

        new BroadcastPresenter(param, mainFragment, chatFragment);
    }
}
