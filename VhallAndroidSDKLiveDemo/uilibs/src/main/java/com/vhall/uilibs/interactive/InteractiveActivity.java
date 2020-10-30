package com.vhall.uilibs.interactive;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.widget.Toast;

import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.ActivityUtils;

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
            new InteractivePresenter(this, interactiveFragment, param);
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

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
