package com.vhall.uilibs.interactive.doc;

/**
 * @author hkl
 * Date: 2019-11-18 15:17
 */
public interface IDocViewLister {
    void onError(int error, String msg);

    void setVisibility(int visibility, int rootVisibility);
}
