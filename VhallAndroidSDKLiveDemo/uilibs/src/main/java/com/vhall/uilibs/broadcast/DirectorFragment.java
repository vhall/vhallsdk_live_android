package com.vhall.uilibs.broadcast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.vhall.net.NetBroadcastReceiver;
import com.vhall.net.NetUtil;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.RtcInternal;
import com.vhall.uilibs.util.ToastUtil;
import com.vhall.uilibs.widget.DirectorErrorView;

/**
 *
 */
public class DirectorFragment extends Fragment implements BroadcastContract.DirectorView {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private RelativeLayout mContainerLayout;
    private DirectorErrorView directorErrorView;
    private Button btnPublish, mBackBtn;
    private NetUtil netUtil;

    private BroadcastContract.IDirectorPresenter mPresenter;

    public static DirectorFragment newInstance() {
        return new DirectorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflate = inflater.inflate(R.layout.fragment_director, container, false);
        mContainerLayout = inflate.findViewById(R.id.rl_container);
        directorErrorView = inflate.findViewById(R.id.director_error_view);
        btnPublish = inflate.findViewById(R.id.btn_publish);
        mBackBtn = inflate.findViewById(R.id.btn_back);
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("2".equals(director_stream_status)) {
                    ToastUtil.showToast("当前直播间没有流，不能开始直播");
                } else
                    mPresenter.onstartBtnClick();
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter.getWebinarStatus())
                    showEndLiveDialog();
                else
                    getActivity().finish();
            }
        });
        monitorNetWork();
        mPresenter.init();

        return inflate;
    }

    private void monitorNetWork() {
        netUtil = new NetUtil(getContext(), new NetBroadcastReceiver.NetChangeListener() {
            @Override
            public void onChangeListener(int status) {
                if (RtcInternal.isNetworkConnected(getContext())) {
                    onResume();
                } else {
                    ToastUtil.showToast("当前网络异常");
                }
            }
        });
    }

    private String director_stream_status = "2";

    //断流
    @Override
    public void setDirectorError(String type) {
        director_stream_status = type;
        if (directorErrorView == null) {
            return;
        }
        //2 没有流 1 有流
        if ("2".equals(type)) {
            if (mPresenter.getWebinarStatus())
                directorErrorView.call("", DirectorErrorView.TIME_START);
            else
                directorErrorView.call("", DirectorErrorView.NO_STREAM);
            directorErrorView.setVisibility(View.VISIBLE);
        } else if ("1".equals(type)) {
            directorErrorView.call("", DirectorErrorView.TIME_END);
            directorErrorView.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPresenter.start();
                }
            }, 500);
        }
    }

    @Override
    public void setStartBtnImage(boolean start) {
        if (start) {
            btnPublish.setVisibility(View.GONE);
        }
    }

    @Override
    public void setPresenter(BroadcastContract.IDirectorPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public RelativeLayout getWatchLayout() {
        return mContainerLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter != null)
            mPresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPresenter != null)
            mPresenter.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null)
            mPresenter.onDestroy();
        if (netUtil != null)
            netUtil.release();
        if (directorErrorView!=null){
            directorErrorView.release();
        }
    }

    public void showEndLiveDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("提示")
                .setMessage("您是否要结束直播？")
                .setPositiveButton("结束", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        alertDialog.show();
    }
}