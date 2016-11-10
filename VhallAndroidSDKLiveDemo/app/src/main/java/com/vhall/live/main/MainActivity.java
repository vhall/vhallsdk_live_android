package com.vhall.live.main;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.vhall.live.BasePresenter;
import com.vhall.live.R;
import com.vhall.live.utils.ActivityUtils;


/**
 * 主界面的Activity
 */
public class MainActivity extends FragmentActivity implements MainContract.ActView {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.mainFrame);
        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    mainFragment, R.id.mainFrame);
        }
        SettingFragment setFragment = (SettingFragment) getSupportFragmentManager().findFragmentById(R.id.setFrame);
        if (setFragment == null) {
            setFragment = SettingFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    setFragment, R.id.setFrame);
        }
        new MainPresenter(mainFragment,setFragment,this);
    }

    @Override
    public void showSetting(boolean show) {
        if (show)
            findViewById(R.id.setFrame).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.setFrame).setVisibility(View.GONE);
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {

    }
}
