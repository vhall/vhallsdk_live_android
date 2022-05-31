package com.vhall.uilibs.broadcast;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.vhall.beautifykit.control.FaceBeautyControlView;
import com.vhall.business.data.WebinarInfo;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.beautysource.FaceBeautyDataFactory;
import com.vhall.uilibs.chat.PushChatFragment;
import com.vhall.uilibs.util.ActivityUtils;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.uilibs.util.emoji.InputView;
import com.vhall.uilibs.util.emoji.KeyBoardManager;

/**
 * 发直播界面的Activity
 */
public class BroadcastActivity extends FragmentActivity implements BroadcastContract.BroadcastView {
    InputView inputView;
    PushChatFragment chatFragment;
    private boolean noDelay = false;
    private boolean isDirector = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        WebinarInfo webinarInfo = (WebinarInfo) getIntent().getSerializableExtra("webinarInfo");
        Param param = (Param) getIntent().getSerializableExtra("param");
        if (param.screenOri == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        noDelay = param.noDelay;
        isDirector = param.isDirector;
        setContentView(R.layout.broadcast_activity);

        if (inputView == null) {
            inputView = new InputView(this, KeyBoardManager.getKeyboardHeight(this), KeyBoardManager.getKeyboardHeightLandspace(this));
            inputView.add2Window(this);
            inputView.setClickCallback(new InputView.ClickCallback() {
                @Override
                public void onEmojiClick() {

                }
            });
            inputView.setOnSendClickListener(new InputView.SendMsgClickListener() {
                @Override
                public void onSendClick(String msg, InputUser user) {
                    if (chatFragment != null) {
                        chatFragment.performSend(msg, PushChatFragment.CHAT_EVENT_CHAT);
                    }
                }
            });
            inputView.setOnHeightReceivedListener(new InputView.KeyboardHeightListener() {
                @Override
                public void onHeightReceived(int screenOri, int height) {
                    if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        KeyBoardManager.setKeyboardHeight(BroadcastActivity.this, height);
                    } else {
                        KeyBoardManager.setKeyboardHeightLandspace(BroadcastActivity.this, height);
                    }
                }
            });
        }
        chatFragment = (PushChatFragment) getSupportFragmentManager().findFragmentById(R.id.chatFrame);
        if (chatFragment == null) {
            chatFragment = PushChatFragment.newInstance(VhallUtil.BROADCAST, false);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    chatFragment, R.id.chatFrame);
        }
        if (isDirector) {
            DirectorFragment mainFragment = (DirectorFragment) getSupportFragmentManager().findFragmentById(R.id.broadcastFrame);
            if (mainFragment == null) {
                mainFragment = DirectorFragment.newInstance();
                ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                        mainFragment, R.id.broadcastFrame);
            }
            new DirectorPresenter(param, webinarInfo, this,mainFragment, chatFragment);
        } else if (noDelay) {
            BroadcastNoDelayFragment mainFragment = (BroadcastNoDelayFragment) getSupportFragmentManager().findFragmentById(R.id.broadcastFrame);
            if (mainFragment == null) {
                mainFragment = BroadcastNoDelayFragment.newInstance(param.screenOri);
                ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                        mainFragment, R.id.broadcastFrame);
            }
            mainFragment.setIFaceBeautySwitch(new IFaceBeautySwitch() {
                @Override
                public void changeVisibility() {
                    BroadcastActivity.this.changeVisibility();
                }
            });
            new BroadcastNoDelayPresenter(param, webinarInfo, this, mainFragment, chatFragment);
        } else {
            BroadcastFragment mainFragment = (BroadcastFragment) getSupportFragmentManager().findFragmentById(R.id.broadcastFrame);
            if (mainFragment == null) {
                mainFragment = BroadcastFragment.newInstance(param.screenOri);
                ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                        mainFragment, R.id.broadcastFrame);
            }
            new BroadcastPresenter(param, webinarInfo, this, mainFragment, chatFragment);
            mainFragment.setIFaceBeautySwitch(new IFaceBeautySwitch() {
                @Override
                public void changeVisibility() {
                    BroadcastActivity.this.changeVisibility();
                }
            });
        }
        initBeautifyData(param.screenOri);
    }


    @Override
    public void showChatView(boolean isShowEmoji, InputUser user, int contentLengthLimit) {
        if (contentLengthLimit > 0) {
            inputView.setLimitNo(contentLengthLimit);
        }
        inputView.show(isShowEmoji, user);
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void setPresenter(BroadcastContract.Presenter presenter) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        inputView.dismiss();
        Log.e("onConfigurationChanged", "onCreate");
    }

    @Override
    public void onBackPressed() {
        inputView.dismiss();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (inputView != null) {
            inputView.destroyed();
        }
        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        if (null != inputView) {
            inputView.dismiss();
        }
        super.onUserLeaveHint();
    }

    // 高级美颜相关
    private FaceBeautyControlView mFaceBeautyControlView;
    private FaceBeautyDataFactory mFaceBeautyDataFactory;

    private void changeVisibility() {
        //新的美颜
        if (mFaceBeautyControlView.getVisibility() == View.VISIBLE) {
            mFaceBeautyControlView.setVisibility(View.GONE);
        } else {
            mFaceBeautyControlView.setVisibility(View.VISIBLE);
        }
    }

    private void initBeautifyData(int orientation) {
        mFaceBeautyDataFactory = new FaceBeautyDataFactory(getActivity());
        mFaceBeautyControlView = findViewById(R.id.faceBeautyControlView);
        mFaceBeautyControlView.setMainTabVisibility(false, true, true, false);
        mFaceBeautyControlView.setSelectLineVisible();
        mFaceBeautyControlView.changeOrientation(orientation);
        mFaceBeautyControlView.bindDataFactory(mFaceBeautyDataFactory);
    }
}
