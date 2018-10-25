package com.vhall.uilibs.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;


import com.vhall.uilibs.R;

/**
 * Created by qing on 2018/1/31.
 */

public class CircleView extends ImageView {

    private Paint mColorPaint;
    private int mBackColor = Color.RED;
    private float mRadius = 150;
    private RectF mRoundRect;
    private int baseLineY;
    private Paint textPaint;
    public int second = 0;
    private Context mContext;

    public CircleView(Context context) {
        this(context, null, 0);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.circleView, defStyleAttr, 0);
        mBackColor = array.getColor(R.styleable.circleView_circleBackColor, mBackColor);
        mRadius = array.getFloat(R.styleable.circleView_circleRadius, mRadius);
        array.recycle();
    }

    private void init() {
        mColorPaint = new Paint();
        mColorPaint.setAntiAlias(true);
        mColorPaint.setColor(Color.WHITE);
        mColorPaint.setStyle(Paint.Style.FILL);


        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(50);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);

    }

    public void setTextAndInvalidate(int timer) {
        this.second = timer;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.vhall_class_open_hand_x2);
//        canvas.drawBitmap(bitmap, new Matrix(), new Paint());

        mColorPaint.setColor(getResources().getColor(R.color.gray_normal));
        onDrawBackAndText();
        canvas.drawRoundRect(mRoundRect, mRadius, mRadius, mColorPaint);

        if (second <= 0) {
            Bitmap mBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_hand)).getBitmap();
            Rect mSrcRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
            Rect mDestRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
            canvas.drawBitmap(mBitmap, mSrcRect, mDestRect, mColorPaint);
        } else {
            canvas.drawText(String.valueOf(second) + "s", mRoundRect.centerX(), baseLineY, textPaint);
        }
    }

    private void onDrawBackAndText() {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
        mRoundRect = new RectF();
        mRoundRect.set(0, 0, getWidth(), getHeight());
        baseLineY = (int) (mRoundRect.centerY() - top / 2 - bottom / 2);//
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        mRadius = size / 2;
    }

}
