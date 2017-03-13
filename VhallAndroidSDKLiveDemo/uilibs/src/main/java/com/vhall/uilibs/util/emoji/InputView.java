package com.vhall.uilibs.util.emoji;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.vhall.uilibs.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huanan on 2016/3/14.
 */
public class InputView {

    private static final String TAG = "InputView";
    private boolean showEmoji = false;
    private View contentView;
    private ImageView iv_emoji;
    private EditText et_content;
    private TextView tv_send;
    private ViewPager vp_emoji;
    Context context;
    Activity activity;

    private int limitNo = 280;
    private ClickCallback mCallback;

    public void setClickCallback(ClickCallback clickCallback) {
        mCallback = clickCallback;
    }

    public int getLimitNo() {
        return limitNo;
    }

    public void setLimitNo(int limitNo) {
        this.limitNo = limitNo;
        et_content.setFilters(new InputFilter[]{new MyInputFilter(limitNo)});
    }

    public EditText getEt_content() {
        return et_content;
    }

    public void setEt_content(EditText et_content) {
        this.et_content = et_content;
    }

    private int keyboardHeight_portrait = 0;
    private int keyboardHeight_landspace = 0;

    private InputUser user = null;

    public View getContentView() {
        return contentView;
    }

    //发送点击回调
    public interface SendMsgClickListener {
        void onSendClick(String msg, InputUser user);
    }

    SendMsgClickListener onSendClickListener;

    public void setOnSendClickListener(SendMsgClickListener onSendClickListener) {
        this.onSendClickListener = onSendClickListener;
    }

    //键盘高度获取回调
    public interface KeyboardHeightListener {
        void onHeightReceived(int screenOri, int height);
    }

    KeyboardHeightListener onHeightReceivedListener;

    public void setOnHeightReceivedListener(KeyboardHeightListener onHeightReceivedListener) {
        this.onHeightReceivedListener = onHeightReceivedListener;
    }


    public InputView(Context context, int protraitHeight, int landspaceHeight) {
        this.context = context;
        keyboardHeight_portrait = protraitHeight;
        keyboardHeight_landspace = landspaceHeight;
        initView();
        initEmoji();
    }

    public void initView() {
        contentView = View.inflate(context, R.layout.emoji_inputview_layout, null);
        iv_emoji = (ImageView) contentView.findViewById(R.id.iv_emoji);
        et_content = (EditText) contentView.findViewById(R.id.et_content);
        tv_send = (TextView) contentView.findViewById(R.id.tv_send);
        vp_emoji = (ViewPager) contentView.findViewById(R.id.vp_emoji);
        contentView.setVisibility(View.GONE);
        iv_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmoji = !showEmoji;
                show(showEmoji, null);

            }
        });
        et_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmoji = false;
                show(showEmoji, null);
            }
        });
        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSendClickListener != null) {
                    String msg = et_content.getText().toString();
                    if (msg.contains("@") && msg.contains(":")) {
                        msg = msg.substring(msg.indexOf(":")+1);
                    }
                    if (user!=null) {
                        String text = "@" + user.userName + ":";
                        msg = text+msg;
                    }
                    onSendClickListener.onSendClick(msg, user);
                    et_content.setText("");
                    dismiss();
                }
            }
        });
        et_content.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_SEND
                        || (arg2 != null && arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    tv_send.performClick();
                    return true;
                }
                return false;
            }
        });
        et_content.setHint("我来说两句");
    }

    public void initEmoji() {
        List emoji = EmojiUtils.getExpressionRes(90);
        List<View> views = new ArrayList<View>();
        for (int i = 1; i <= 5; i++) {// 20*5
            View view = EmojiUtils.getGridChildView(context, i, emoji, et_content);
            views.add(view);
        }
        vp_emoji.setAdapter(new EmojiPagerAdapter(views));
    }

    public void add2Window(Activity activity) {
        this.activity = activity;
        FrameLayout layout = (FrameLayout) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        layout.addView(contentView, params);
        observeSoftKeyboard(activity);
    }

    public void show(boolean isShowEmoji, InputUser user) {
        this.user = user;
        if (user!=null) {
            String text = "@" + user.userName + ":";
            et_content.setText(text);
            et_content.setSelection(et_content.getText().length());
        }
        et_content.requestFocus();
        showEmoji = isShowEmoji;
        if (isShowEmoji) {
            showEmoji();
        } else {
            showKeyboard();
        }
    }

    private void showEmoji() {
        if (null != mCallback) {
            mCallback.onEmojiClick();
        }
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        KeyBoardManager.closeKeyboard(et_content, activity);
        contentView.setVisibility(View.VISIBLE);
        new Handler() {//延时0.2秒显示表情
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                params.setMargins(0, 0, 0, 0);
                contentView.setLayoutParams(params);
                iv_emoji.setImageResource(R.mipmap.inputview_icon_keyboard);
                vp_emoji.setVisibility(View.VISIBLE);
            }
        }.sendEmptyMessageDelayed(1, 200);
    }

    private void showKeyboard() {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            if (keyboardHeight_portrait == 0) {
//                int height = Screen.getScreenHeight(activity);
                params.setMargins(0, 0, 0, 800);
                contentView.setLayoutParams(params);
                KeyBoardManager.openKeyboard(et_content, activity);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        params.setMargins(0, 0, 0, keyboardHeight_portrait);
                        contentView.setLayoutParams(params);
                        iv_emoji.setImageResource(R.mipmap.inputview_icon_emoji);
                        vp_emoji.setVisibility(View.GONE);
                        contentView.setVisibility(View.VISIBLE);
                    }
                },300);
                return;
            }
            params.setMargins(0, 0, 0, keyboardHeight_portrait);
        } else {
            if (keyboardHeight_landspace == 0) {
                keyboardHeight_landspace = 840;
            }
            params.setMargins(0, 0, 0, keyboardHeight_landspace);

        }
        if (contentView.getVisibility() == View.VISIBLE) {
            contentView.setLayoutParams(params);
            iv_emoji.setImageResource(R.mipmap.inputview_icon_emoji);
            vp_emoji.setVisibility(View.GONE);
            contentView.setVisibility(View.VISIBLE);
            KeyBoardManager.openKeyboard(et_content, activity);
            KeyBoardManager.openKeyboard(et_content, activity);//保留
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    contentView.setLayoutParams(params);
                    iv_emoji.setImageResource(R.mipmap.inputview_icon_emoji);
                    vp_emoji.setVisibility(View.GONE);
                    contentView.setVisibility(View.VISIBLE);
                    KeyBoardManager.openKeyboard(et_content, activity);
                }
            },300);
        }


    }

    public void dismiss() {
        if (contentView.getVisibility() == View.GONE)
            return;
        KeyBoardManager.closeKeyboard(et_content, activity);
        showEmoji = false;
        new Handler() {//延时0.2秒显示表情
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                contentView.setLayoutParams(params);
                iv_emoji.setImageResource(R.mipmap.inputview_icon_emoji);
                vp_emoji.setVisibility(View.GONE);
                contentView.setVisibility(View.GONE);
            }
        }.sendEmptyMessageDelayed(1, 200);


    }

    public void observeSoftKeyboard(final Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int previousKeyboardHeight = -1;

            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int displayHeight = rect.bottom - rect.top;
                int height = decorView.getHeight();
                int keyboardHeight = height - displayHeight - rect.top;
                if (previousKeyboardHeight != keyboardHeight) {
                    boolean hide = (double) displayHeight / height > 0.8;
                    if (hide && !showEmoji) {
                        dismiss();
                    }
                    if (!hide) {
                        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                        if ( activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            keyboardHeight_portrait = keyboardHeight;
                            if (onHeightReceivedListener != null)
                                onHeightReceivedListener.onHeightReceived(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, keyboardHeight_portrait);
                            params.setMargins(0,0,0,keyboardHeight);
                        } else if ( activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                            keyboardHeight_landspace = keyboardHeight;
                            if (onHeightReceivedListener != null)
                                onHeightReceivedListener.onHeightReceived(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, keyboardHeight_landspace);
                            params.setMargins(0,0,0,keyboardHeight);
                        }
                        contentView.setLayoutParams(params);
                    }
                }
                previousKeyboardHeight = keyboardHeight;
            }
        });
    }

    public  interface ClickCallback{
        void onEmojiClick();
    }
}
