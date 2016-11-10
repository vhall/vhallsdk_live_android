package com.vhall.live.main;

import android.text.TextUtils;

import com.vhall.live.VhallApplication;
import com.vhall.live.data.Param;

/**
 * 主界面的Presenter
 */
public class MainPresenter implements MainContract.Presenter {
    private MainContract.MainView mView;
    private MainContract.ActView mActView;
    private MainContract.SetView mSetView;

    public MainPresenter(MainContract.MainView view, MainContract.SetView setView, MainContract.ActView actView) {
        mView = view;
        mSetView = setView;
        mActView = actView;

        mView.setPresenter(this);
        mSetView.setPresenter(this);
    }

    @Override
    public boolean checkInput(boolean isStart) {
        if (TextUtils.isEmpty(mSetView.getID())) {
            mView.showErrorMsg("请填写ID");
            return false;
        }

        if (mSetView.getBufferSecond() <= 0) {
            mView.showErrorMsg("缓冲时间必须大于0");
            return false;
        }
        if (isStart) {
            if (TextUtils.isEmpty(mSetView.getToken())) {
                mView.showErrorMsg("请填写token");
                return false;
            }
            if (mSetView.getvideoBitrate() <= 0) {
                mView.showErrorMsg("码率必须大于0");
                return false;
            }
            if (mSetView.getFrameRate() <= 0) {
                mView.showErrorMsg("帧率必须大于0");
                return false;
            }
        }
        return true;
    }

    @Override
    public void startBroadcast(int ori) {
        if (!checkInput(true))
            return;
        mView.skipBroadcast(getParam(ori));
    }

    @Override
    public void startWatch(int watch_type) {
        if (!checkInput(false))
            return;
        Param param = getParam(0);
        param.watch_type = watch_type;
        mView.skipWatch(param);
    }

    @Override
    public void loginBtnClickEvent() {
        if (TextUtils.isEmpty(VhallApplication.user_vhall_id)) {
            mView.skipLogin();
        } else {
            VhallApplication.user_vhall_id = "";
            mView.setLoginBtnText("登录");
        }
    }

    private Param getParam(int ori) {
        Param params = new Param();
        params.id = mSetView.getID();
        params.token = mSetView.getToken();
        params.videoBitrate = mSetView.getvideoBitrate();
        params.frameRate = mSetView.getFrameRate();
        params.bufferSecond = mSetView.getBufferSecond();
        params.k = mSetView.getK();
        params.pixel_type = mSetView.getdpi();
        params.screenOri = ori;
        params.record_id = mSetView.getRecord();
        return params;
    }

    @Override
    public void start() {
        if (!TextUtils.isEmpty(VhallApplication.user_vhall_id)) {
            mView.setLoginBtnText("退出");
        } else {
            mView.setLoginBtnText("登录");
        }
    }

    @Override
    public void showSetting(boolean show) {
        mActView.showSetting(show);
    }
}
