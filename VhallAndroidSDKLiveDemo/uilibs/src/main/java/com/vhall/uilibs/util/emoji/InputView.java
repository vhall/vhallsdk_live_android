package com.vhall.uilibs.util.emoji;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.vhall.business.utils.LogManager;
import com.vhall.uilibs.R;
import com.vhall.uilibs.widget.HeightProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huanan on 2016/3/14.
 */
public class InputView implements View.OnClickListener {

    private static final String TAG = "InputView";
    private boolean showEmoji = false;
    private View contentView;
    private ImageView iv_emoji;
    private EditText et_content;
    private TextView tv_send;
    private ViewPager vp_emoji;
    private View bg;
    Context context;
    Activity activity;
    private boolean hasVirtual = false; // 是否有虚拟按键
    private int virtualHeight = 0;  // 虚拟按键的高度
    private int keyboardHeight = 0;

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
        hasVirtual = KeyBoardManager.hasVirtualButton(context);
        if (hasVirtual) {
            virtualHeight = KeyBoardManager.getVirtualButtonHeight(context);
        }
    }

    @Override
    public void onClick(View v) {
        if(v == bg){
            dismiss();
        }else if(v == iv_emoji){
            showEmoji = !showEmoji;
            show(showEmoji, null);
        }else if(v == et_content){
            showEmoji = false;
            show(showEmoji, null);
        }else if(v == tv_send){
            if (onSendClickListener != null) {
                String msg = et_content.getText().toString();
                if (msg.contains("@") && msg.contains(":")) {
                    msg = msg.substring(msg.indexOf(":") + 1);
                }
                if (user != null) {
                    String text = String.format("@%s:",user.userName);
                    msg = text + msg;
                }
                onSendClickListener.onSendClick(msg, user);
                et_content.setText("");
                dismiss();
            }
        }
    }

    public void initView() {
        contentView = View.inflate(context, R.layout.emoji_inputview_layout, null);
        iv_emoji = contentView.findViewById(R.id.iv_emoji);
        et_content = (EditText) contentView.findViewById(R.id.et_content);
        tv_send = (TextView) contentView.findViewById(R.id.tv_send);
        vp_emoji = (ViewPager) contentView.findViewById(R.id.vp_emoji);
        contentView.setVisibility(View.GONE);
        bg = (View) contentView.findViewById(R.id.view_bg);


        bg.setOnClickListener(this);
        iv_emoji.setOnClickListener(this);
        et_content.setOnClickListener(this);
        tv_send.setOnClickListener(this);
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

    private HeightProvider heightProvider;
    public void add2Window(Activity activity) {
        this.activity = activity;
        FrameLayout layout = (FrameLayout) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        layout.addView(contentView, params);
        observeSoftKeyboard(activity);
        if(heightProvider == null){
            heightProvider = new HeightProvider(activity).init();
            heightProvider.setHeightListener(new HeightProvider.HeightListener() {
                @Override
                public void onHeightChanged(int height) {
                    onKeyBoardChanged(height);
                }
            });
        }
    }

    void onKeyBoardChanged(int height){
        if(height > 0){
            KeyBoardManager.setKeyboardHeight(context,height);
            keyboardHeight_portrait = height;
            keyboardHeight_landspace = height;
            if(!showEmoji){
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                params.setMargins(0, 0, 0, height);
            }
        }else {
            if(!showEmoji){
                dismiss();
            }
        }
    }

    public void show(boolean isShowEmoji, InputUser user) {
        this.user = user;
        if (user != null) {
            et_content.setText(String.format("@%s:",user.userName));
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
        if (keyboardHeight_portrait > 0) {
            vp_emoji.getLayoutParams().height = this.keyboardHeight_portrait;
        }
        contentView.setVisibility(View.VISIBLE);

        //延时0.2秒显示表情
        contentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                params.setMargins(0, 0, 0, 0);
                contentView.setLayoutParams(params);
                iv_emoji.setImageResource(R.mipmap.inputview_icon_keyboard);
                vp_emoji.setVisibility(View.VISIBLE);
            }
        },200);
    }

    private void showKeyboard() {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            if (keyboardHeight_portrait == 0) {
//                int height = Screen.getScreenHeight(activity);
                params.setMargins(0, 0, 0, 800);
                contentView.setLayoutParams(params);
                KeyBoardManager.openKeyboard(et_content, activity);
                contentView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        params.setMargins(0, 0, 0, keyboardHeight_portrait);
                        contentView.setLayoutParams(params);
                        iv_emoji.setImageResource(R.mipmap.inputview_icon_emoji);
                        vp_emoji.setVisibility(View.GONE);
                        contentView.setVisibility(View.VISIBLE);
                    }
                }, 300);
                return;
            }
            params.setMargins(0, 0, 0, keyboardHeight_portrait);
        } else {
            if (keyboardHeight_landspace != 0) {
                params.setMargins(0, 0, 0, keyboardHeight_landspace);
            }
        }
        if (contentView.getVisibility() == View.VISIBLE) {
            contentView.setLayoutParams(params);
            iv_emoji.setImageResource(R.mipmap.inputview_icon_emoji);
            vp_emoji.setVisibility(View.GONE);
            contentView.setVisibility(View.VISIBLE);
            KeyBoardManager.openKeyboard(et_content, activity);
            KeyBoardManager.openKeyboard(et_content, activity);//保留
        } else {
            contentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    contentView.setLayoutParams(params);
                    iv_emoji.setImageResource(R.mipmap.inputview_icon_emoji);
                    vp_emoji.setVisibility(View.GONE);
                    contentView.setVisibility(View.VISIBLE);
                    KeyBoardManager.openKeyboard(et_content, activity);
                }
            }, 50);
        }
    }

    public void dismiss() {
        if (contentView.getVisibility() == View.GONE) {
            return;
        }
        KeyBoardManager.closeKeyboard(et_content, activity);
        showEmoji = false;
        //延时0.2秒显示表情
        contentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                contentView.setLayoutParams(params);
                iv_emoji.setImageResource(R.mipmap.inputview_icon_emoji);
                vp_emoji.setVisibility(View.GONE);
                contentView.setVisibility(View.GONE);
            }
        },200);

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
                /** 是否存在虚拟键盘*/
                if (hasVirtual && virtualHeight > 0) {
                    keyboardHeight = height - displayHeight - rect.top - virtualHeight;
                } else {
                    keyboardHeight = height - displayHeight - rect.top;
                }
                if (previousKeyboardHeight != keyboardHeight) {
                    boolean hide = (double) displayHeight / height > 0.8;
                    if (hide && !showEmoji) {
                        dismiss();
                    }
                    if (!hide) {
                        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            keyboardHeight_portrait = keyboardHeight;
                            if (onHeightReceivedListener != null) {
                                onHeightReceivedListener.onHeightReceived(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, keyboardHeight_portrait);
                            }
                            params.setMargins(0, 0, 0, keyboardHeight);
                        } else if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                            keyboardHeight_landspace = keyboardHeight;
                            if (onHeightReceivedListener != null) {
                                onHeightReceivedListener.onHeightReceived(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, keyboardHeight_landspace);
                            }
                            params.setMargins(0, 0, 0, keyboardHeight);
                        }
                        contentView.setLayoutParams(params);
                    }
                }
                previousKeyboardHeight = keyboardHeight;
            }
        });
    }

    public interface ClickCallback {
        void onEmojiClick();
    }

    public void destroyed(){
        if(heightProvider != null){
            heightProvider.destroyed();
        }
    }
}
