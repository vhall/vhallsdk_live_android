package com.vhall.uilibs.interactive;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.widget.Toast;

import com.vhall.business.VhallSDK;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.InteractiveDataSource;
import com.vhall.business.data.source.WebinarInfoRepository;
import com.vhall.business.data.source.remote.WebinarInfoRemoteDataSource;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.ActivityUtils;

import vhall.com.vss.module.room.VssRoomManger;

/**
 * @author other
 */
public class InteractiveActivity extends FragmentActivity implements InteractiveContract.InteractiveActView {
    private InteractiveFragment interactiveFragment;
    private InteractiveContract.InteractiveActPresenter mPresenter;
    private Param param;

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
            if (VssRoomManger.enter) {
                WebinarInfoRepository repository = WebinarInfoRepository.getInstance(WebinarInfoRemoteDataSource.getInstance());
                repository.getInteractiveInfo(param.webinar_id, VhallSDK.getUserNickname(), "", param.key, VhallSDK.getUserId(), new InteractiveDataSource.InteractiveCallback() {
                    @Override
                    public void onSuccess(Object something) {
                        WebinarInfo webinarInfo = (WebinarInfo) something;
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        showToast(errorMsg);
                    }
                });
                new InteractivePresenterVss(this, interactiveFragment, param);
            } else {
                new InteractivePresenter(this, interactiveFragment, param);
            }
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), interactiveFragment, R.id.contentVideo);
        }
    }

    @Override
    public void setPresenter(InteractiveContract.InteractiveActPresenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
