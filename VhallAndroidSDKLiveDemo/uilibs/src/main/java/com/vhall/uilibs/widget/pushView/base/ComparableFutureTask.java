package com.vhall.uilibs.widget.pushView.base;

import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author zhot
 */
public class ComparableFutureTask extends FutureTask implements Comparable<ComparableFutureTask> {

    private Callable callable;

    public Callable getCallable() {
        return callable;
    }

    @Override
    public int compareTo(ComparableFutureTask second) {
        // 时间越小越优先
        if (((TaskCallable) getCallable()).getPriority() < ((TaskCallable) second.getCallable()).getPriority()) {
            return -1;
        } else if (((TaskCallable) getCallable()).getPriority() > ((TaskCallable) second.getCallable()).getPriority()) {
            return 1;
        } else {
            return 0;
        }
    }

    public ComparableFutureTask(@NonNull Callable callable) {
        super(callable);
        this.callable = callable;
    }
}
