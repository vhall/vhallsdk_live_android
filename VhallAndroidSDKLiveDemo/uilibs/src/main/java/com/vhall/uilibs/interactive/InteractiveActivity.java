package com.vhall.uilibs.interactive;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.ActivityUtils;

public class InteractiveActivity extends FragmentActivity implements InteractiveContract.InteractiveActView {
    private InteractiveFragment interactiveFragment;
    private InteractiveContract.InteractiveActPresenter mPresenter;
    private Param param;
    AudioManager audioManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.interactive_activity);
        param = (Param) getIntent().getSerializableExtra("param");
        initView();
    }

    private void initView() {
        interactiveFragment = (InteractiveFragment) getSupportFragmentManager().findFragmentById(R.id.contentVideo);
        if (interactiveFragment == null) {
            interactiveFragment = InteractiveFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), interactiveFragment, R.id.contentVideo);
            new InteractivePresenter(this, interactiveFragment , param);
        }
    }

    @Override
    public void setPresenter(InteractiveContract.InteractiveActPresenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void setSpeakerphoneOn(boolean on) {
        if (audioManager == null) {
            audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(on);           //默认为扬声器播放
    }

    @Override
    public Context getContext() {
        return this;
    }

}
