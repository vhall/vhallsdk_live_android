package com.vhall.uilibs.watch;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.VhallSDK;
import com.vhall.uilibs.util.ActivityUtils;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.MarqueeView;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.chat.ChatFragment;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.uilibs.util.emoji.InputView;
import com.vhall.uilibs.util.emoji.KeyBoardManager;


/**
 * 观看页的Activity
 */
public class WatchActivity extends FragmentActivity implements WatchContract.WatchView {
    private FrameLayout contentDoc, contentDetail, contentChat, contentQuestion;
    private RadioGroup radio_tabs;
    private RadioButton chatRadioButton, questionRadioButton;
    private LinearLayout ll_detail;
    private LinearLayout mLayoutOnlyNotice;
    private ImageView image_line_show;
    private ImageView mImageSignClose;
    private MarqueeView mMarqueeNotice;
    private TextView mTextSignInContent;
    private Param param;
    private String mSignId;
    private MyCount myCount;
    private int type;
    public WatchPlaybackFragment playbackFragment;
    public WatchLiveFragment liveFragment;
    private AlertDialog alertDialog;
    public ChatFragment chatFragment;
    public ChatFragment questionFragment ;
    public String  mNoticeContentStr ;
    InputView inputView;
    public int chatEvent = ChatFragment.CHAT_EVENT_CHAT;
    private boolean isClickNoticeClose = false;  //是否点击公告关闭按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.watch_activity);
        param = (Param) getIntent().getSerializableExtra("param");
        type = getIntent().getIntExtra("type", VhallUtil.WATCH_LIVE);
        liveFragment = (WatchLiveFragment) getSupportFragmentManager().findFragmentById(R.id.contentVideo);
        playbackFragment = (WatchPlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.contentVideo);
        chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.contentChat);
        DocumentFragment docFragment = (DocumentFragment) getSupportFragmentManager().findFragmentById(R.id.contentDoc);
        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentDetail);
        questionFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.contentQuestion);
        initView();
        if (chatFragment == null) {
            chatFragment = ChatFragment.newInstance(type, false);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    chatFragment, R.id.contentChat);
        }
        if (docFragment == null) {
            docFragment = DocumentFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    docFragment, R.id.contentDoc);
        }

        if (questionFragment == null && type == VhallUtil.WATCH_LIVE) {
            questionFragment = ChatFragment.newInstance(type, true);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    questionFragment, R.id.contentQuestion);
        }

        if (detailFragment == null) {
            detailFragment = DetailFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    detailFragment, R.id.contentDetail);
        }

        if (liveFragment == null && type == VhallUtil.WATCH_LIVE) {
            liveFragment = WatchLiveFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    liveFragment, R.id.contentVideo);
            new WatchLivePresenter(liveFragment, docFragment, chatFragment, questionFragment, this, param);
        }

        if (playbackFragment == null && type == VhallUtil.WATCH_PLAYBACK) {
            playbackFragment = WatchPlaybackFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    playbackFragment, R.id.contentVideo);
            new WatchPlaybackPresenter(playbackFragment, docFragment, chatFragment, this, param);
        }

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

    private void initView() {
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
                if (chatFragment != null && chatEvent == ChatFragment.CHAT_EVENT_CHAT){
                    chatFragment.performSend(msg , chatEvent);
                } else if (questionFragment != null && chatEvent == ChatFragment.CHAT_EVENT_QUESTION) {
                    questionFragment.performSend(msg , chatEvent);
                }
            }
        });
        inputView.setOnHeightReceivedListener(new InputView.KeyboardHeightListener() {
            @Override
            public void onHeightReceived(int screenOri, int height) {
                if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    KeyBoardManager.setKeyboardHeight(WatchActivity.this, height);
//                    app.getParameterManager().setKeyboardHeightProtrait(height);
                } else {
                    KeyBoardManager.setKeyboardHeightLandspace(WatchActivity.this, height);
//                    app.getParameterManager().setKeyboardHeightLandSpace(height);
                }
            }
        });
        ll_detail = (LinearLayout) this.findViewById(R.id.ll_detail);
        contentDoc = (FrameLayout) findViewById(R.id.contentDoc);
        contentDetail = (FrameLayout) findViewById(R.id.contentDetail);
        contentChat = (FrameLayout) findViewById(R.id.contentChat);
        contentQuestion = (FrameLayout) findViewById(R.id.contentQuestion);
        chatRadioButton = (RadioButton) findViewById(R.id.rb_chat);
        questionRadioButton = (RadioButton) findViewById(R.id.rb_question);
        image_line_show = (ImageView) findViewById(R.id.image_line_show);
        mLayoutOnlyNotice = (LinearLayout) findViewById(R.id.layout_only_notice);

        mMarqueeNotice = (MarqueeView) findViewById(R.id.mMarqueeView);
        if (type == VhallUtil.WATCH_LIVE) {
            questionRadioButton.setVisibility(View.VISIBLE);
            contentChat.setVisibility(View.VISIBLE);
            image_line_show.setVisibility(View.VISIBLE);
            chatRadioButton.setText("聊天");
        }
        if (type == VhallUtil.WATCH_PLAYBACK) {
            chatRadioButton.setText("评论");
            contentChat.setVisibility(View.VISIBLE);
        }
        radio_tabs = (RadioGroup) findViewById(R.id.radio_tabs);
        radio_tabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_chat) {
                    chatEvent = ChatFragment.CHAT_EVENT_CHAT;
                    contentChat.setVisibility(View.VISIBLE);
                    contentDoc.setVisibility(View.GONE);
                    contentDetail.setVisibility(View.GONE);
                    contentQuestion.setVisibility(View.GONE);
                } else if (checkedId == R.id.rb_doc) {
                    contentDoc.setVisibility(View.VISIBLE);
                    contentChat.setVisibility(View.GONE);
                    contentDetail.setVisibility(View.GONE);
                    contentQuestion.setVisibility(View.GONE);
                } else if (checkedId == R.id.rb_question) {
                    chatEvent = ChatFragment.CHAT_EVENT_QUESTION;
                    contentDoc.setVisibility(View.GONE);
                    contentDetail.setVisibility(View.GONE);
                    contentQuestion.setVisibility(View.VISIBLE);
                    contentChat.setVisibility(View.GONE);
                } else if (checkedId == R.id.rb_detail) {
                    contentDoc.setVisibility(View.GONE);
                    contentChat.setVisibility(View.GONE);
                    contentQuestion.setVisibility(View.GONE);
                    contentDetail.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void showChatView(boolean isShowEmoji, InputUser user, int contentLengthLimit) {
        if (contentLengthLimit > 0)
            inputView.setLimitNo(contentLengthLimit);
        inputView.show(isShowEmoji, user);
    }

    @Override
    public void setShowDetail(boolean isShow) {
        if (isShow) {
            ll_detail.setVisibility(View.VISIBLE);
            if (!isClickNoticeClose) {
                if (!TextUtils.isEmpty(mNoticeContentStr)) {
                    mLayoutOnlyNotice.setVisibility(View.VISIBLE);
                    mMarqueeNotice.setVisibility(View.VISIBLE);
                    mMarqueeNotice.setText(mNoticeContentStr);
                    mMarqueeNotice.startScroll();
                }
            }
        } else {
            ll_detail.setVisibility(View.GONE);
            mMarqueeNotice.setVisibility(View.GONE);
            mMarqueeNotice.stopScroll();
            mLayoutOnlyNotice.setVisibility(View.GONE);
        }
    }

    @Override
    public void showNotice(String content) {
        this.mNoticeContentStr = content;
        isClickNoticeClose = false;
        if (!TextUtils.isEmpty(content) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutOnlyNotice.setVisibility(View.VISIBLE);
            mMarqueeNotice.setVisibility(View.VISIBLE);
            mMarqueeNotice.setText(content);
            mMarqueeNotice.startScroll();
        }
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

    @Override
    public void showSingIn(String signId, final int startTime) {
        this.mSignId = signId;
        LayoutInflater inflater = getLayoutInflater();
        View dialog = inflater.inflate(R.layout.alert_dialog_show_signin, (ViewGroup) findViewById(R.id.dialog));
        mImageSignClose = (ImageView) dialog.findViewById(R.id.image_signin_close);
        mImageSignClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        mTextSignInContent = (TextView) dialog.findViewById(R.id.tv_signin_content);
        alertDialog = new AlertDialog.Builder(this).setView(dialog).create();
        alertDialog.setCanceledOnTouchOutside(false); //点击外部不消失
        alertDialog.show();
        myCount = new MyCount(startTime * 1000, 100);
        myCount.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
    }

    public void performOnclickSignIn(View view) {
        VhallSDK.getInstance().performSignIn(param.watchId, param.userVhallId, param.userName, mSignId, new VhallSDK.RequestCallback() {
            @Override
            public void onSuccess() {
                close();
                Toast.makeText(WatchActivity.this, "签到成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Toast.makeText(WatchActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void performMarqueeClose(View view) {
        if (mLayoutOnlyNotice.getVisibility() == View.VISIBLE) {
            mLayoutOnlyNotice.setVisibility(View.GONE);
            isClickNoticeClose = true;
        }
    }

    @Override
    public void setPresenter(BasePresenter presenter) {
    }

    /* 定义一个倒计时的内部类 */
    class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            Toast.makeText(WatchActivity.this, "签到已结束", Toast.LENGTH_SHORT).show();
            close();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mTextSignInContent.setText("您有" + millisUntilFinished / 1000 + "秒的时间进行签到");
        }
    }

    public void close() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (myCount != null) {
            myCount.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mNoticeContentStr) && mMarqueeNotice != null) {
            mMarqueeNotice.setText(mNoticeContentStr);
            mMarqueeNotice.startScroll();
        }
    }


}
