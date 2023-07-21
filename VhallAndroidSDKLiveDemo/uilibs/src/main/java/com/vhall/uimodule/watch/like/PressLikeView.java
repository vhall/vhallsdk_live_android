package com.vhall.uimodule.watch.like;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;


import com.vhall.uimodule.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 自定义点赞弹出View
 */
public class PressLikeView extends ViewGroup {

    //图片路径集合，放入之后可以随机抽取显示
    private List<Integer> imageResources;
    //插值器
    private List<Interpolator> inters;
    //随机数取值器
    private Random random;
    //默认图片大小
    private int defaultSize = 80;//弹出的图片尺寸大小

    public PressLikeView(Context context) {
        super(context);
    }

    public PressLikeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    //初始化数据
    private void initData() {
        random = new Random();
        imageResources = new ArrayList<>();
        imageResources.add(R.mipmap.icon_live_heart1);
        imageResources.add(R.mipmap.icon_live_heart6);
        imageResources.add(R.mipmap.icon_live_heart2);
        imageResources.add(R.mipmap.icon_live_heart7);
        imageResources.add(R.mipmap.icon_live_heart3);
        imageResources.add(R.mipmap.icon_live_heart4);
        imageResources.add(R.mipmap.icon_live_heart5);
        imageResources.add(R.mipmap.icon_live_heart8);
        inters = new ArrayList<>();
        //添加4中插值器
        inters.add(new LinearInterpolator());
        inters.add(new AccelerateInterpolator());
        inters.add(new AccelerateDecelerateInterpolator());
        inters.add(new DecelerateInterpolator());
    }

    public PressLikeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public void setImageResources(List<Integer> imageUrls) {
        imageResources.clear();
        this.imageResources = imageUrls;
    }

    /**
     * 暴露设置弹出图片大小size的方法
     *
     * @param defaultSize
     */
    public void setDefaultSize(int defaultSize) {
        this.defaultSize = defaultSize;
    }

    private int mLikeNum = 0;

    public void show(int num) {
        mLikeNum = Math.min(num, 10);
        handler.postDelayed(runnable, 300);
        setVisibility(VISIBLE);
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mLikeNum <= 0) {
                handler.removeCallbacks(runnable);
            } else {
                showResource();
                handler.postDelayed(this, 200);
            }
            --mLikeNum;
        }
    };

    public void release() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
    }

    //使用自定义的图片
    public void showResource() {
        ImageView view = new ImageView(getContext());
        view.setLayoutParams(new LayoutParams(defaultSize, defaultSize));//设置大小
        int imageUrl = R.mipmap.icon_live_heart1;
        //如果未传递url 并且初始化的图片集合有数据，随机抽取一个图片加载到view中,
        if (imageResources != null && imageResources.size() > 0) {
            imageUrl = imageResources.get(random.nextInt(imageResources.size()));
        }
        view.setImageResource(imageUrl);
        addView(view);//添加到容器中
        view.layout(getWidth() / 2 - defaultSize, (int) (getHeight() - defaultSize * 1.5), getWidth() / 2, (int) (getHeight() - 0.5 * defaultSize));//计算位置
        startAnim(view);//开始动画
    }

    /**
     * 开始弹出动画
     *
     * @param view
     */
    private void startAnim(final ImageView view) {
        AnimatorSet animatorSet = new AnimatorSet();
        //淡入动画
        ValueAnimator inAnim = ValueAnimator.ofFloat(0.5f, 1f);
        inAnim.setDuration(500);
        inAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setAlpha(value);
                view.setScaleX(value);
                view.setY(value);
            }
        });
        //淡出动画
        ValueAnimator outAnim = ValueAnimator.ofFloat(1, 0);
        outAnim.setDuration(1200);
        outAnim.setStartDelay(1200);//延迟启动，保证图片飞到一大半再淡出
        outAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setAlpha((float) animation.getAnimatedValue());
            }
        });
        //位移动画
        ValueAnimator transAnim = ValueAnimator.ofObject(new BezierValue(), new Point(getWidth() / 2 - defaultSize / 6, getHeight()), new Point(new Random().nextInt(getWidth()), 0));
        transAnim.setDuration(3000);
        transAnim.setInterpolator(inters.get(random.nextInt(inters.size())));//随机设置插值器
        transAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                view.setX(point.x);
                view.setY(point.y);
            }
        });
        //组合动画
        //三个动画同时执行
        animatorSet.playTogether(inAnim,outAnim,transAnim);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                removeView(view);//动画结束移除ImageView
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    //自定义插值器-贝塞尔曲线
    static class BezierValue implements TypeEvaluator<Point> {
        private final Random random = new Random();
        private int ctrlPX1, ctrlPX2, ctrlPY1, ctrlPY2;
        private boolean isInit;//只需要初始化一次

        @Override
        public Point evaluate(float fraction, Point startValue, Point endValue) {
            Point point = new Point();
            point.x = (int) cubicPointX(fraction, startValue.x, endValue.x);
            point.y = (int) cubicPointY(fraction, startValue.y, endValue.y);
            return point;
        }

        //贝塞尔计算x
        private double cubicPointX(float fraction, int start, int end) {
            if (!isInit) {
                //初始化控制点y左边
                ctrlPY1 = random.nextInt(start + end / 2);
                ctrlPY2 = random.nextInt(start + end / 2) + (start + end / 2);
                //初始化控制点x坐标
                if (random.nextBoolean()) {//先左后右
                    ctrlPX1 = (int) (random.nextInt(start) - start / 4f);//减去start/4 是为了运动曲线更明显
                    ctrlPX2 = (int) (random.nextInt(start) + start * 1.25f);//start是宽度的一半，为了保证后面往右运动，应该是随机数加上start。现在乘1.25是为了让曲线更明显
                } else {//先右后左
                    ctrlPX1 = (int) (random.nextInt(start) + start * 1.25f);
                    ctrlPX2 = (int) (random.nextInt(start) - start / 4f);
                }
                isInit = true;
            }
            return start * Math.pow((1 - fraction), 3) + 3 * ctrlPX1 * fraction * Math.pow((1 - fraction), 2)
                    + 3 * ctrlPX2 * Math.pow(fraction, 2) * (1 - fraction) + end * Math.pow(fraction, 3);
        }

        //贝塞尔计算y
        private double cubicPointY(float fraction, int start, int end) {
            return start * Math.pow((1 - fraction), 3) + 3 * ctrlPY1 * fraction * Math.pow((1 - fraction), 2)
                    + 3 * ctrlPY2 * Math.pow(fraction, 2) * (1 - fraction) + end * Math.pow(fraction, 3);
        }
    }
}