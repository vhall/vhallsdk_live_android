package com.vhall.uilibs.widget.pushView.base;


import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 进入直播间队列
 * 进入控制
 * @author hkl
 */
public class EnterSyncExecutor {

    private volatile static EnterSyncExecutor sEnterSyncExecutor;

    public ArrayDeque<BaseSyncTask> pendingQueue = new ArrayDeque<>();
    private BaseSyncTask currentTask;
    //记录队列task是否正在执行
    private boolean lastTaskRunning = false;

    //记录执行开始时间,默认-1
    private long lastTaskStartTime = -1L;

    private final AtomicInteger count = new AtomicInteger(1);

    private void coreExecute() {
        currentTask = pendingQueue.poll();
        if (currentTask != null) {
            //记录本次线程时间
            lastTaskStartTime = System.currentTimeMillis();
            TinyTaskExecutor.execute(new AdvancedTask() {
                @Override
                public Object doInBackground() {
                    return null;
                }

                @Override
                public void onSuccess(Object o) {
                    if (pendingQueue.size() == 0) {
                        lastTaskRunning = true;
                    }
                    if (null != currentTask) {
                        currentTask.doTask(pendingQueue.size() == 0, currentTask.getEnterData());
                    }
                }

                @Override
                public void onFail(Throwable throwable) {

                }
            });
        }
    }

    public void enqueue(final BaseSyncTask task) {
        pendingQueue.offer(task);
        if (currentTask == null) {
            coreExecute();
        }
    }

    public boolean isLastTaskRunning() {
        return lastTaskRunning;
    }

    public void setLastTaskRunning(boolean lastTaskRunning) {
        this.lastTaskRunning = lastTaskRunning;
    }

    public void finish() {
        if (lastTaskRunning) {
            lastTaskRunning = false;
        }
        coreExecute();
    }

    public void clear() {
        pendingQueue.clear();
        finish();
    }
}
