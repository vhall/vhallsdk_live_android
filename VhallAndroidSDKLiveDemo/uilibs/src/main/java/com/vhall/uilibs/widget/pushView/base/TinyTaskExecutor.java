package com.vhall.uilibs.widget.pushView.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhot
 */
public class TinyTaskExecutor {

    private volatile static TinyTaskExecutor sTinyTaskExecutor;

    private ExecutorService mExecutor;
    private volatile Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private static HashMap<Callable, Runnable> sDelayTasks = new HashMap<>();

    private static List futureList = new ArrayList<>();

    public static final int PRIORITY_HIGH = Process.THREAD_PRIORITY_DEFAULT;
    public static final int PRIORITY_NORMAL = Process.THREAD_PRIORITY_BACKGROUND;
    public static final int PRIORITY_LOWEST = Process.THREAD_PRIORITY_LOWEST;

    public static TinyTaskExecutor getInstance() {
        if (sTinyTaskExecutor == null) {
            synchronized (TinyTaskExecutor.class) {
                sTinyTaskExecutor = new TinyTaskExecutor();
            }
        }
        return sTinyTaskExecutor;
    }

    public TinyTaskExecutor() {
//        mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        mExecutor = new TaskThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors(),
                Long.MAX_VALUE, /* timeout */
                TimeUnit.NANOSECONDS,
                new PriorityBlockingQueue<Runnable>(),
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    private static ExecutorService getExecutor() {
        return getInstance().mExecutor;
    }

    public static Handler getMainThreadHandler() {
        return getInstance().mMainThreadHandler;
    }

    /**
     * 添加Task
     *
     * @param callable
     */
    public static void execute(TaskCallable callable) {
        execute(callable, 0);
    }

    public static void execute(final Callable callable, long delayMillisecond) {
        if (callable == null) {
            return;
        }
        if (delayMillisecond < 0) {
            return;
        }

        if (!getExecutor().isShutdown()) {
            if (delayMillisecond > 0) {
                Runnable delayRunnable = new Runnable() {
                    @Override
                    public void run() {
                        synchronized (sDelayTasks) {
                            sDelayTasks.remove(callable);
                        }
                        realExecute(callable);
                    }
                };

                synchronized (sDelayTasks) {
                    sDelayTasks.put(callable, delayRunnable);
                }
                getMainThreadHandler().postDelayed(delayRunnable, delayMillisecond);
            } else {
                realExecute(callable);
            }
        }
    }

    /**
     * realExecute
     *
     * @param callable
     */
    private static void realExecute(Callable callable) {
//        ComparableFutureTask futureTask = new ComparableFutureTask(callable);
        Future future = getExecutor().submit(callable);
        futureList.add(future);
        System.out.println("[TinyTaskExecutor] realExecute");
    }

    /**
     * 移除线程
     *
     * @param callable
     */
    public static void removeTask(final Callable callable) {
        if (callable == null) {
            return;
        }

        Runnable delayRunnable;
        synchronized (sDelayTasks) {
            delayRunnable = sDelayTasks.remove(callable);
        }

        if (delayRunnable != null) {
            getMainThreadHandler().removeCallbacks(delayRunnable);
        }

    }

    /**
     * 检查，会堵塞主线程。
     */
    public static void check() {
        for (Iterator it = futureList.iterator(); it.hasNext(); ) {
            FutureTask ft = (FutureTask) it.next();
            if (!ft.isDone()) {
                try {
                    //if use get(), you will block main thread util the sub thread finished, unless you need the result of sub thread.
                    System.out.println("[TinyTaskExecutor] the check result is: " + ft.get());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                it.remove();
            }
        }
    }

    /**
     * post主线程
     *
     * @param task
     */
    public static void postToMainThread(final Runnable task) {
        postToMainThread(task, 0);
    }


    public static void postToMainThread(final Runnable task, long delayMillis) {
        if (task == null) {
            return;
        }

        getMainThreadHandler().postDelayed(task, delayMillis);
    }

    /**
     * 移除一个线程
     *
     * @param task
     */
    public static void removeMainThreadRunnable(Runnable task) {
        if (task == null) {
            return;
        }

        getMainThreadHandler().removeCallbacks(task);
    }

}
