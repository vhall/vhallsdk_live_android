package com.vhall.live.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.live.R;
import com.vhall.live.broadcast.BroadcastActivity;
import com.vhall.live.data.Param;
import com.vhall.live.login.LoginActivity;
import com.vhall.live.watch.WatchActivity;


/**
 * 主界面的Fragment
 */
public class MainFragment extends Fragment implements MainContract.MainView, View.OnClickListener {

    private MainContract.Presenter mPresenter;

    private Button mStartPotrait, mStartLandspace, mWatchRTMP, mWatchHLS;
    private Button btn_login;
    private TextView tv_set;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        mPresenter.start();
    }

    private void initView() {

        tv_set = (TextView) getView().findViewById(R.id.tv_set);
        mStartPotrait = (Button) getView().findViewById(R.id.btn_start_potrait);
        mStartLandspace = (Button) getView().findViewById(R.id.btn_start_landspace);
        mWatchRTMP = (Button) getView().findViewById(R.id.btn_watch_rtmp);
        mWatchHLS = (Button) getView().findViewById(R.id.btn_watch_hls);

        btn_login = (Button) getView().findViewById(R.id.btn_login);

        mStartPotrait.setOnClickListener(this);
        mStartLandspace.setOnClickListener(this);
        mStartLandspace.setOnClickListener(this);
        mWatchRTMP.setOnClickListener(this);
        mWatchHLS.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        tv_set.setOnClickListener(this);

    }

    @Override
    public void skipBroadcast(Param param) {
        Intent intent = new Intent(getActivity(), BroadcastActivity.class);
        intent.putExtra("param", param);
        startActivity(intent);
    }

    @Override
    public void skipWatch(Param param) {
        Intent intent = new Intent(getActivity(), WatchActivity.class);
        intent.putExtra("param", param);
        startActivityForResult(intent, 10);
    }

    @Override
    public void showErrorMsg(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void skipLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivityForResult(intent, 10);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (10 == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                setLoginBtnText("退出");
            }
        }
    }

    @Override
    public void setLoginBtnText(String text) {
        btn_login.setText(text);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_watch_rtmp:
                mPresenter.startWatch(Param.WATCH_LIVE);
                break;
            case R.id.btn_start_potrait:
                mPresenter.startBroadcast(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.btn_start_landspace:
                mPresenter.startBroadcast(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.btn_watch_hls:
                mPresenter.startWatch(Param.WATCH_PLAYBACK);
                break;
            case R.id.btn_login:
                mPresenter.loginBtnClickEvent();
                break;
            case R.id.tv_set:
                mPresenter.showSetting(true);
                break;
            default:
                break;
        }
    }
}
