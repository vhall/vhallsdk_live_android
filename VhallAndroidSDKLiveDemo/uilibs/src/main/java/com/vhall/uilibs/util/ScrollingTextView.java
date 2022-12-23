package com.vhall.uilibs.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.vhall.business.VhallSDK;
import com.vhall.uilibs.util.handler.WeakHandler;
import com.vhall.vhss.data.ScrollInfoData;


/**
 * Created by hkl
 */
public class ScrollingTextView extends SurfaceView implements SurfaceHolder.Callback {
    public Context mContext;

    private float mTextSize = 48; //字体大小

    private String mTextColor = "#000000"; //字体的颜色

    private boolean mIsRepeat = false;//是否重复滚动

    private int mStartPoint = 1;// 开始滚动的位置  0是从最左面开始    1是从最末尾开始

    private int mDirection = 0;//滚动方向 0 向左滚动   1向右滚动


    private SurfaceHolder holder;

    private TextPaint mTextPaint;

    private ScrollingTextViewThread mThread;

    private String margueeString;

    private int textWidth = 0, textHeight = 0;

    private int ShadowColor = Color.BLACK;

    public int currentX = 0;// 当前x的位置

    public int sepX = 10;//每一步滚动的距离

    private int mSpeed = 130;//滚动速度

    private int interval = 0;//每次滚动时间间隔 s

    private int alpha = 0;//设置的透明度 0-255

    public static final int ROLL_OVER = 100;

   private WeakHandler mHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == ROLL_OVER) {
                stopScroll();
                if (mOnMargueeListener != null) {
                    mOnMargueeListener.onRollOver();
                }
            }
            return false;
        }
    });

    public ScrollingTextView(Context context) {
        this(context, null);
    }

    public ScrollingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {

        holder = this.getHolder();
        holder.addCallback(this);
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        setZOrderOnTop(true);//使surfaceview放到最顶层
        getHolder().setFormat(PixelFormat.TRANSLUCENT);//使窗口支持透明度
    }

    public void setText(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            measurementsText(msg);
        }
    }

    private ScrollInfoData scrollingInfo;

    public void setScrollingInfo(ScrollInfoData scrollingInfo) {
        if (scrollingInfo == null) {
            setVisibility(GONE);
        } else {
            this.scrollingInfo = scrollingInfo;
            if (scrollingInfo.scrolling_open == 0) {
                setVisibility(GONE);
                return;
            }
            if (!TextUtils.isEmpty(scrollingInfo.getColor())) {
                setTextColor(scrollingInfo.getColor());
            }
            if (scrollingInfo.getSize() > 0) {
                setTextSize(DensityUtils.dpToPx(scrollingInfo.getSize()));
            }
            switch (scrollingInfo.getSpeed()) {
                case ScrollInfoData.SPEED_HIGHT:
                    sepX = 18;
//                    setSpeed(20);
                    setSpeed(65);
                    break;
                case ScrollInfoData.SPEED_MIDDLE:
                    sepX = 6;
                    setSpeed(125);
//                    setSpeed(100);
                    break;
                case ScrollInfoData.SPEED_LOW:
                    sepX = 2;
                    setSpeed(250);
//                    setSpeed(700);
                    break;
                default:
                    break;
            }
            interval = scrollingInfo.getInterval();
            //定义的透明度百分比 0-100
            int mAlpha;
            if (scrollingInfo.getAlpha() > 100) {
                mAlpha = 100;
            } else if (scrollingInfo.getAlpha() <= 0) {
                mAlpha = 0;
            } else {
                mAlpha = scrollingInfo.getAlpha();
            }
            alpha = (int) (2.55 * mAlpha);

            if (scrollingInfo.getText_type() == 2) {
                setText(scrollingInfo.getText() + " - " + VhallSDK.getUserId() + " - " + VhallSDK.getUserNickname());
            } else {
                setText(scrollingInfo.getText());
            }

            if (scrollingInfo.getScrolling_open() != 0) {
                startScroll();
            }
        }
    }

    public void setTextSize(float mTextSize) {
        this.mTextSize = mTextSize;
    }

    public void setTextColor(String mTextColor) {
        this.mTextColor = mTextColor;
    }

    public void setSpeed(int mSpeed) {
        this.mSpeed = mSpeed;
    }

    private int frequency = 1;

    protected void measurementsText(String msg) {
        margueeString = msg;
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(Color.parseColor(mTextColor));
        mTextPaint.setStrokeWidth(0.1f);
        // mTextPaint.setFakeBoldText(true);//加粗
        // 设定阴影(柔边, X 轴位移, Y 轴位移, 阴影颜色)
//        mTextPaint.setShadowLayer(5, 3, 3, ShadowColor);
        textWidth = (int) mTextPaint.measureText(margueeString);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        textHeight = (int) fontMetrics.bottom;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();

        frequency = (width + textWidth) / mSpeed;
        if (mStartPoint == 0)
            currentX = 0;
        else
            currentX = width - getPaddingLeft() - getPaddingRight();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mThread != null)
            mThread.isRun = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mThread != null)
            mThread.isRun = false;
    }

    /**
     * 开始滚动
     */
    public void startScroll() {
        if (mThread != null && mThread.isRun) {
            return;
        }
        mThread = new ScrollingTextViewThread(holder);//创建一个绘图线程
        mThread.start();
    }

    /**
     * 停止滚动
     */
    public void stopScroll() {
        if (mThread != null) {
            mThread.isRun = false;
            mThread.interrupt();
        }
        if (mHandler != null) {
            mHandler.removeMessages(ROLL_OVER);
            mThread = null;
        }
    }

    /**
     * 线程
     */
    class ScrollingTextViewThread extends Thread {

        private SurfaceHolder holder;

        public boolean isRun;//是否在运行


        public ScrollingTextViewThread(SurfaceHolder holder) {
            this.holder = holder;
            isRun = true;
        }

        public void onDraw() {

            try {
                synchronized (holder) {
                    if (TextUtils.isEmpty(margueeString)) {
                        Thread.sleep(500);//睡眠时间为1秒
                        return;
                    }
                    Canvas canvas = holder.lockCanvas();
                    int paddingLeft = getPaddingLeft();
                    int paddingTop = getPaddingTop();
                    int paddingRight = getPaddingRight();
                    int paddingBottom = getPaddingBottom();

                    int contentWidth = getWidth() - paddingLeft - paddingRight;
                    int contentHeight = getHeight() - paddingTop - paddingBottom;

                    int centeYLine = paddingTop + contentHeight / 2;//中心线

                    if (mDirection == 0) {//向左滚动
                        if (currentX <= -textWidth) {
                            if (!mIsRepeat) {//如果是不重复滚动
                                mHandler.sendEmptyMessage(ROLL_OVER);
                                holder.unlockCanvasAndPost(canvas);//结束锁定画图，并提交改变。
                                return;
                            }
                            if (currentX != 0) {
                                Thread.sleep(interval * 1000);
                            }
                            currentX = contentWidth;
                        } else {
                            currentX -= sepX;
                        }
                    } else {//  向右滚动
                        if (currentX >= contentWidth) {
                            if (!mIsRepeat) {//如果是不重复滚动
                                mHandler.sendEmptyMessage(ROLL_OVER);
                                holder.unlockCanvasAndPost(canvas);//结束锁定画图，并提交改变。
                                return;
                            }
                            if (currentX != 0) {
                                Thread.sleep(interval * 1000);
                            }
                            currentX = -textWidth;
                        } else {
                            currentX += sepX;
                        }
                    }

                    if (canvas != null) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色
                        mTextPaint.setAlpha(alpha);
                        canvas.drawText(margueeString, currentX, centeYLine + DensityUtils.dpToPxInt(getContext(), textHeight) / 2, mTextPaint);
                        holder.unlockCanvasAndPost(canvas);//结束锁定画图，并提交改变。
                    }

                    int a = textWidth / margueeString.trim().length();
                    int b = a / sepX;
                    int c = mSpeed / b == 0 ? 1 : mSpeed / b;

                    Thread.sleep(frequency);//睡眠时间为移动的频率
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            while (isRun) {
                onDraw();
            }
        }

    }

    public void reset() {
        int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        if (mStartPoint == 0)
            currentX = 0;
        else
            currentX = contentWidth;
    }

    /**
     * 滚动回调
     */
    public interface OnMargueeListener {
        void onRollOver();//滚动完毕
    }

    OnMargueeListener mOnMargueeListener;

    public void setOnMargueeListener(OnMargueeListener mOnMargueeListener) {
        this.mOnMargueeListener = mOnMargueeListener;
    }
}
