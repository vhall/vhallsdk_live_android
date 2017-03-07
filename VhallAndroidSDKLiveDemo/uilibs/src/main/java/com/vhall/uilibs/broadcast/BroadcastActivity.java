package com.vhall.uilibs.broadcast;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.vhall.uilibs.util.ActivityUtils;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.chat.ChatFragment;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.uilibs.util.emoji.InputView;
import com.vhall.uilibs.util.emoji.KeyBoardManager;
import com.vhall.uilibs.watch.WatchActivity;


/**
 * 发直播界面的Activity
 */
public class BroadcastActivity extends FragmentActivity implements BroadcastContract.BraodcastView {
    InputView inputView;
    ChatFragment chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Param param = (Param) getIntent().getSerializableExtra("param");
        int screenOri = getIntent().getIntExtra("ori", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (screenOri == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.broadcast_activity);

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
                if (chatFragment != null)
                    chatFragment.performSend(msg , ChatFragment.CHAT_EVENT_CHAT);
            }
        });
        inputView.setOnHeightReceivedListener(new InputView.KeyboardHeightListener() {
            @Override
            public void onHeightReceived(int screenOri, int height) {
                if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    KeyBoardManager.setKeyboardHeight(BroadcastActivity.this, height);
//                    app.getParameterManager().setKeyboardHeightProtrait(height);
                } else {
                    KeyBoardManager.setKeyboardHeightLandspace(BroadcastActivity.this, height);
//                    app.getParameterManager().setKeyboardHeightLandSpace(height);
                }
            }
        });

        BroadcastFragment mainFragment = (BroadcastFragment) getSupportFragmentManager().findFragmentById(R.id.broadcastFrame);
        if (mainFragment == null) {
            mainFragment = BroadcastFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    mainFragment, R.id.broadcastFrame);
        }
        chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chatFrame);
        if (chatFragment == null) {
            chatFragment = ChatFragment.newInstance(VhallUtil.BROADCAST, false);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    chatFragment, R.id.chatFrame);
        }

        new BroadcastPresenter(param, this, mainFragment, chatFragment);
    }

    @Override
    public void showChatView(boolean isShowEmoji, InputUser user, int contentLengthLimit) {
        if (contentLengthLimit > 0)
            inputView.setLimitNo(contentLengthLimit);
        inputView.show(isShowEmoji, user);
    }

    @Override
    public void setPresenter(BroadcastContract.Presenter presenter) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && inputView.getContentView().getVisibility() == View.VISIBLE) {
            boolean isDismiss = isShouldHideInput(inputView.getContentView(), ev);
            if (isDismiss) {
                inputView.dismiss();
                return false;
            } else {
                return super.dispatchTouchEvent(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View view, MotionEvent event) {
        if (view.getVisibility() == View.GONE)
            return false;
        int[] leftTop = {0, 0};
        //获取输入框当前的location位置
        inputView.getContentView().getLocationInWindow(leftTop);
        int left = leftTop[0];
        int top = leftTop[1];
        int bottom = top + inputView.getContentView().getHeight();
        int right = left + inputView.getContentView().getWidth();
        return !(event.getX() > left && event.getX() < right
                && event.getY() > top && event.getY() < bottom);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        inputView.dismiss();
    }

    @Override
    public void onBackPressed() {
        inputView.dismiss();
        super.onBackPressed();
    }

    @Override
    protected void onUserLeaveHint() {
        if (null != inputView) {
            inputView.dismiss();
        }
        super.onUserLeaveHint();
    }
}
