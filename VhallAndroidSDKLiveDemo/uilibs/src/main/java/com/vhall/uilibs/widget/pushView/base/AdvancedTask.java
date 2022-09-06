package com.vhall.uilibs.widget.pushView.base;

/**
 * @author hkl
 */
public abstract class AdvancedTask<T> extends TaskCallable {

    public AdvancedTask() {
    }

    public AdvancedTask(int priority) {
        super(priority);
    }

    public AdvancedTask(int priority, String taskName) {
        super(priority, taskName);
    }

    public abstract T doInBackground();

    public abstract void onSuccess(T t);

    public abstract void onFail(Throwable throwable);

    @Override
    public T call() throws Exception {
        try {
            final T t = doInBackground();
            TinyTaskExecutor.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(t);
                }
            });
            return t;
        } catch (final Throwable throwable) {
            TinyTaskExecutor.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    onFail(throwable);
                }
            });
        } finally {
            return null;
        }
    }
}
