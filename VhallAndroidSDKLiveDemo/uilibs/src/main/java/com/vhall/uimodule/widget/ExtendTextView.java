package com.vhall.uimodule.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by huanan on 2017/3/14.
 */
public class ExtendTextView extends AppCompatTextView {
    private String empty_char = " ";

    public void setDrawableClickListener(DrawableClickListener drawableClickListener) {
        this.drawableClickListener = drawableClickListener;
    }

    private DrawableClickListener drawableClickListener;

    public static final int DRAWABLE_LEFT = 0;
    public static final int DRAWABLE_RIGHT = 2;

    public ExtendTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExtendTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendTextView(Context context) {
        super(context);
    }

    public interface DrawableClickListener {
        void onDrawableClick(int position);
    }

    private CharSequence showTest="";
    @Override
    public void setText(CharSequence text, BufferType type) {
        showTest=text;
        //todo 文字不满一行也可以滚动
        super.setText(showTest, type);
    }

    @Override
    public boolean isFocused() {
        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (drawableClickListener != null) {
                    Drawable drawableRight = getCompoundDrawables()[DRAWABLE_RIGHT];
                    if (drawableRight != null && event.getRawX() >= (getRight() - drawableRight.getBounds().width())) {
                        drawableClickListener.onDrawableClick(DRAWABLE_RIGHT);
                        return false;
                    }
                    Drawable drawableLeft = getCompoundDrawables()[DRAWABLE_LEFT];
                    if (drawableLeft != null && event.getRawX() <= (getLeft() + drawableLeft.getBounds().width())) {
                        drawableClickListener.onDrawableClick(DRAWABLE_LEFT);
                        return false;
                    }
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }
}
