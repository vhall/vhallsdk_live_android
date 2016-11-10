package com.vhall.live.watch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.WatchLive;
import com.vhall.business.widget.ContainerLayout;
import com.vhall.live.R;
import com.vhall.live.VhallApplication;
import com.vhall.live.utils.VhallUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 观看直播的Fragment
 */
public class WatchLiveFragment extends Fragment implements WatchContract.LiveView, View.OnClickListener {

    private WatchContract.LivePresenter mPresenter;

    private ImageView clickStart;
    private ImageView clickOrientation;
    private RadioButton radioButtonShowSD;
    private RadioButton radioButtonShowHD;
    private RadioButton radioButtonShowUHD;
    private RadioGroup radioChoose;
    private TextView fragmentDownloadSpeed;
    private ProgressDialog mProcessDialog;
    private ContainerLayout mContainerLayout;
    private Button btn_change_scaletype;
    private Button btnClickShowAudio;
    private Button btnClickShowVideo;
    private Dialog alertDialog;
    private String lotteryStr;
    /***
     * 控制显示Toast
     */
    public static boolean isShowToast = true;

    public static WatchLiveFragment newInstance() {
        return new WatchLiveFragment();
    }

    @Override
    public void setPresenter(WatchContract.LivePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.watch_live_fragment, container, false);
        initView(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().setResult(Activity.RESULT_OK);
        mPresenter.stopWatch();
    }

    private void initView(View root) {
        clickStart = (ImageView) root.findViewById(R.id.click_rtmp_watch);
        clickStart.setOnClickListener(this);
        clickOrientation = (ImageView) root.findViewById(R.id.click_rtmp_orientation);
        clickOrientation.setOnClickListener(this);
        radioChoose = (RadioGroup) root.findViewById(R.id.radio_choose);
        radioChoose.setOnCheckedChangeListener(checkListener);
        btnClickShowAudio = (Button) root.findViewById(R.id.btn_click_show_aduio);
        btnClickShowAudio.setOnClickListener(this);
        btnClickShowVideo = (Button) root.findViewById(R.id.btn_click_show_video);
        btnClickShowVideo.setOnClickListener(this);
        radioButtonShowSD = (RadioButton) root.findViewById(R.id.radio_btn_sd);
        radioButtonShowHD = (RadioButton) root.findViewById(R.id.radio_btn_hd);
        radioButtonShowUHD = (RadioButton) root.findViewById(R.id.radio_btn_uhd);
        mContainerLayout = (ContainerLayout) root.findViewById(R.id.rl_container);
        fragmentDownloadSpeed = (TextView) root.findViewById(R.id.fragment_download_speed);

        btn_change_scaletype = (Button) root.findViewById(R.id.btn_change_scaletype);
        btn_change_scaletype.setOnClickListener(this);
        /** 初始化Dialog*/
        mProcessDialog = new ProgressDialog(this.getActivity());
        mProcessDialog.setCancelable(true);
        mProcessDialog.setCanceledOnTouchOutside(false);
        mPresenter.start();
    }

    @Override
    public Activity getmActivity() {
        return this.getActivity();
    }

    @Override
    public ContainerLayout getWatchLayout() {
        return mContainerLayout;
    }

    @Override
    public void setPlayPicture(boolean state) {
        if (state) {
            clickStart.setBackgroundResource(R.drawable.pause);
        } else {
            clickStart.setBackgroundResource(R.drawable.play);
        }
    }

    @Override
    public void setDownSpeed(String text) {
        fragmentDownloadSpeed.setText(text);
    }

    @Override
    public void showLoading(boolean isShow) {
        if (isShow) {
            mProcessDialog.show();
        } else {
            mProcessDialog.dismiss();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.click_rtmp_watch:
                mPresenter.onWatchBtnClick();
                break;
            case R.id.click_rtmp_orientation:
                mPresenter.changeOriention();
                break;
            case R.id.btn_change_scaletype:
                mPresenter.setScaleType();
                break;
            case R.id.btn_click_show_video:
                mPresenter.onSwitchPixel(WatchLive.DPI_DEFAULT);
                break;
            case R.id.btn_click_show_aduio:
                if (btnClickShowAudio.getVisibility() == View.VISIBLE)
                    mPresenter.onSwitchPixel(WatchLive.DPI_AUDIO);
                break;
            default:
                break;
        }
    }

    @Override
    public void showToast(String message) {
        if (isShowToast)
            Toast.makeText(VhallApplication.getApp(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 切换分辨率
     *
     * @param map 0 : 无效不可用  1 ：有效可用
     */
    @Override
    public void showRadioButton(HashMap map) {
        if (map == null)
            return;
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            Integer value = (Integer) entry.getValue();
            switch (key) {
                case "Audio":
                    if (value == 1)
                        btnClickShowAudio.setVisibility(View.VISIBLE);
                    else
                        btnClickShowAudio.setVisibility(View.GONE);
                    break;
                case "SD":
                    if (value == 1)
                        radioButtonShowSD.setVisibility(View.VISIBLE);
                    else
                        radioButtonShowSD.setVisibility(View.GONE);
                    break;
                case "HD":
                    if (value == 1)
                        radioButtonShowHD.setVisibility(View.VISIBLE);
                    else
                        radioButtonShowHD.setVisibility(View.GONE);
                    break;
                case "UHD":
                    if (value == 1)
                        radioButtonShowUHD.setVisibility(View.VISIBLE);
                    else
                        radioButtonShowUHD.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public void setScaleButtonText(String text) {
        btn_change_scaletype.setText(text);
    }

    @Override
    public void showDialogStatus(int level, final boolean isLottery, String[] nameList) {
        if (alertDialog != null) {
            alertDialog.cancel();
        }
        switch (level) {
            case 1:
                alertDialog = new AlertDialog.Builder(this.getActivity())
                        .setTitle("vhall抽奖")
                        .setMessage("抽奖中~~~~~")
                        .create();
                alertDialog.show();
                break;
            case 2:
                if (isLottery) {
                    lotteryStr = "恭喜您,中奖了";
                } else
                    lotteryStr = "有点遗憾,这次没中";
                alertDialog = new AlertDialog.Builder(this.getActivity())
                        .setTitle(lotteryStr)
                        .setItems(nameList, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .setPositiveButton("下一步", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isLottery) {
                                    showDialogStatus(3 , isLottery , null);
                                }
                            }
                        }).create();
                alertDialog.show();
                break;
            case 3:
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                View submitView = layoutInflater.inflate(R.layout.submit_lottery, null);
                final EditText submitNickname = (EditText) submitView.findViewById(R.id.lottery_submit_nickname);
                final EditText submitPhone = (EditText) submitView.findViewById(R.id.lottery_submit_phone);

                alertDialog = new AlertDialog.Builder(this.getActivity())
                        .setTitle("请提供您的信息")
                        .setView(submitView)
                        .setPositiveButton("提交领奖信息", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String submitNicknameStr = submitNickname.getText().toString();
                                final String submitPhoneStr = submitPhone.getText().toString();
                                if (!TextUtils.isEmpty(submitNicknameStr) && !TextUtils.isEmpty(submitPhoneStr)) {
                                    if (VhallUtil.IsPhone(submitPhoneStr)) {
                                        mPresenter.submitLotteryInfo(submitNicknameStr, submitPhoneStr);
                                    } else {
                                        showToast("手机号填写错误");
                                        return;
                                    }
                                } else {
                                    showToast("请填写信息");
                                    return;
                                }
                            }
                        }).create();
                alertDialog.show();
                break;
            default:

        }

    }

    @Override
    public int changeOrientation() {
        if (this.getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        return getActivity().getRequestedOrientation();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public RadioGroup.OnCheckedChangeListener checkListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i) {
                case R.id.radio_btn_sd:
                    mPresenter.onSwitchPixel(WatchLive.DPI_SD);
                    break;
                case R.id.radio_btn_hd:
                    mPresenter.onSwitchPixel(WatchLive.DPI_HD);
                    break;
                case R.id.radio_btn_uhd:
                    mPresenter.onSwitchPixel(WatchLive.DPI_UHD);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestory();
    }
}
