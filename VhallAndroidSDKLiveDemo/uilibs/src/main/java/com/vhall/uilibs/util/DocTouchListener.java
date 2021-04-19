package com.vhall.uilibs.util;


import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author hkl
 * Date: 2020-05-28 16:11
 */

public class DocTouchListener implements View.OnTouchListener {
    private boolean isCanTouch = true;
    private int point_num = 0;//当前触摸的点数
    public static final float SCALE_MAX = 8.0f; //最大的缩放比例
    private static final float SCALE_MIN = 1.0f;

    private double oldDist = 0;
    private double moveDist = 0;
    //针对控件的坐标系，即控件左上角为原点
    private double moveX = 0;
    private double moveY = 0;

    private double downX = 0;
    private double downY = 0;
    //针对屏幕的坐标系，即屏幕左上角为原点
    private double moveRawX = 0;
    private double moveRawY = 0;


    public void setIsCanTouch(boolean canTouch) {
        isCanTouch = canTouch;
    }

    private float scale = 1;

    public float getScale() {
        return scale;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isCanTouch) {
            return false;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                point_num = 1;
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                point_num = 0;
                downX = 0;
                downY = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (point_num == 1) {
                    //只有一个手指的时候才有移动的操作
                    float lessX = (float) (downX - event.getX());
                    float lessY = (float) (downY - event.getY());
                    moveX = event.getX();
                    moveY = event.getY();
                    moveRawX = event.getRawX();
                    moveRawY = event.getRawY();
                    setSelfPivot(v, lessX, lessY);
                    //setPivot(getPivotX() + lessX, getPivotY() + lessY);
                } else if (point_num == 2) {
                    //只有2个手指的时候才有放大缩小的操作
                    moveDist = spacing(event);
                    double space = moveDist - oldDist;
                    scale = (float) (v.getScaleX() + space / v.getWidth());
                    if (scale > SCALE_MIN && scale < SCALE_MAX) {
                        setScale(v, scale);
                    } else if (scale < SCALE_MIN) {
                        setScale(v, SCALE_MIN);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);//两点按下时的距离
                point_num += 1;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                point_num -= 1;
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 触摸使用的移动事件
     *
     * @param lessX
     * @param lessY
     */
    private void setSelfPivot(View view, float lessX, float lessY) {
        float setPivotX = 0;
        float setPivotY = 0;
        setPivotX = view.getPivotX() + lessX;
        setPivotY = view.getPivotY() + lessY;
        Log.e("lawwingLog", "setPivotX:" + setPivotX + "  setPivotY:" + setPivotY
                + "  getWidth:" + view.getWidth() + "  getHeight:" + view.getHeight());
        if (setPivotX < 0 && setPivotY < 0) {
            setPivotX = 0;
            setPivotY = 0;
        } else if (setPivotX > 0 && setPivotY < 0) {
            setPivotY = 0;
            if (setPivotX > view.getWidth()) {
                setPivotX = view.getWidth();
            }
        } else if (setPivotX < 0 && setPivotY > 0) {
            setPivotX = 0;
            if (setPivotY > view.getHeight()) {
                setPivotY = view.getHeight();
            }
        } else {
            if (setPivotX > view.getWidth()) {
                setPivotX = view.getWidth();
            }
            if (setPivotY > view.getHeight()) {
                setPivotY = view.getHeight();
            }
        }
        setPivot(view, setPivotX, setPivotY);
    }

    /**
     * 计算两个点的距离
     *
     * @param event
     * @return
     */
    private double spacing(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    /**
     * 平移画面，当画面的宽或高大于屏幕宽高时，调用此方法进行平移
     *
     * @param x
     * @param y
     */
    public void setPivot(View view, float x, float y) {
        view.setPivotX(x);
        view.setPivotY(y);
    }

    /**
     * 设置放大缩小
     *
     * @param scale
     */
    public void setScale(View view, float scale) {
        view.setScaleX(scale);
        view.setScaleY(scale);
    }

    /**
     * 初始化比例，也就是原始比例
     */
    public void setInitScale(View view) {
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
        setPivot(view, view.getWidth() / 2, view.getHeight() / 2);
    }
}
