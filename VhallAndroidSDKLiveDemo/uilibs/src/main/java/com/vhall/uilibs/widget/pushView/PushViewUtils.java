package com.vhall.uilibs.widget.pushView;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.vhall.uilibs.R;
import com.vhall.uilibs.widget.pushView.base.BaseSyncTask;
import com.vhall.uilibs.widget.pushView.base.EnterSyncExecutor;


/**
 * @author hkl
 * Date: 2020-02-25 17:07
 */
public class PushViewUtils {

    private View enterView;
    private Handler handlerEnter = new Handler();
    private Runnable runnableEnter;
    private Context mContext;
    private DismissCallBack dismissCallBack;
    private ShowCallBack showCallBack;
    private long enterExecuteTime = 1000;
    private long lastEnterExecuteTime = 5000;
    private EnterSyncExecutor enterSyncExecutor;

    public void setDismissCallBack(DismissCallBack dismissCallBack) {
        this.dismissCallBack = dismissCallBack;
    }

    public void setShowCallBack(ShowCallBack showCallBack) {
        this.showCallBack = showCallBack;
    }

    public PushViewUtils(Context context) {
        mContext = context;
        enterSyncExecutor = new EnterSyncExecutor();
    }

    public PushViewUtils(Context context, long enterExecuteTime, long lastEnterExecuteTime) {
        mContext = context;
        this.enterExecuteTime = enterExecuteTime;
        // this.enterSyncExecutor = enterSyncExecutor;
        this.lastEnterExecuteTime = lastEnterExecuteTime;
    }

    /**
     * 显示推荐进入方法，每有通知，调用一次即可
     */
    public void intoShowEnter(View enterView, final Object data) {
        this.enterView = enterView;
        if (enterSyncExecutor.isLastTaskRunning()) {
            enterSyncExecutor.setLastTaskRunning(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handlerEnter.removeCallbacks(runnableEnter);
                    dismissEnter();
                    addEnterToQueue(data);
                }
            }, 1000);
        } else {
            addEnterToQueue(data);
        }
    }

    /**
     * 显示进入方法，每有通知，调用一次即可
     * showTime ms
     */
    public void intoShowEnter(View enterView, Object data, long showTime) {
        lastEnterExecuteTime = showTime;
        intoShowEnter(enterView, data);
    }


    /**
     * 添加推荐进入线程到队列中
     */
    private void addEnterToQueue(final Object data) {
        final BaseSyncTask task = new BaseSyncTask() {
            @Override
            public void doTask(boolean isLastTask, Object enterTxt) {
                if (isLastTask) {
                    showEnter(lastEnterExecuteTime, data);
                } else {
                    showEnter(enterExecuteTime, data);
                }
            }
        };
        task.setEnterData(data);
        enterSyncExecutor.enqueue(task);
    }

    private void showEnter(final long showTime, final Object data) {
        if (enterView == null) {
            return;
        }
        enterView.setVisibility(View.VISIBLE);
        if (showCallBack != null) {
            showCallBack.viewShow(enterView, data);
        }
        enterView.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.translate_user_enter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                handlerEnter.postDelayed(runnableEnter = new Runnable() {
                    @Override
                    public void run() {
                        dismissEnter();
                    }
                }, showTime);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        enterView.startAnimation(animation);
    }

    /**
     * 动画隐藏条目进入
     */
    private void dismissEnter() {
        if (enterView == null) {
            return;
        } else if (enterView.getVisibility() == View.GONE) {
            return;
        }
        enterView.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.translate_user_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (enterSyncExecutor.isLastTaskRunning()) {
                    enterView.setVisibility(View.GONE);
                } else {
                    enterView.setVisibility(View.INVISIBLE);
                }
                enterView.clearAnimation();
                enterSyncExecutor.finish();
                if (dismissCallBack != null) {
                    dismissCallBack.viewDismiss();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        enterView.startAnimation(animation);
    }

    public void dismissTask() {
        handlerEnter.removeCallbacks(runnableEnter);
        dismissEnter();
        enterView.setVisibility(View.GONE);
    }

    public void onDestroy() {
        enterSyncExecutor.clear();
    }

    public interface DismissCallBack {
        void viewDismiss();
    }

    public interface ShowCallBack {
        void viewShow(View view, Object showData);
    }
}
