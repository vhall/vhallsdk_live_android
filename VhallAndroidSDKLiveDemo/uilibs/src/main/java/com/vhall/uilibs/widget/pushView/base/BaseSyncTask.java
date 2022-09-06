package com.vhall.uilibs.widget.pushView.base;

/**
 * @author hkl
 * enterData 进入推屏的数据类
 */
public abstract class BaseSyncTask implements SyncTask {

    private Object enterData;

    public Object getEnterData() {
        return enterData;
    }

    public void setEnterData(Object enterData) {
        this.enterData = enterData;
    }
}
