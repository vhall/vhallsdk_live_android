package com.vhall.uimodule.utils;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.vhall.uimodule.R;

/**
 * Activity的工具类
 */
public class ActivityUtils {

    public static void addFragmentToActivity(FragmentManager fragmentManager,
                                             Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commitAllowingStateLoss();
    }

    public static void changeFragmentToActivity(FragmentManager fragmentManager,
                                                Fragment showFragment, Fragment hintFragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (hintFragment == null) {
            if (!showFragment.isAdded()) {
                transaction.add(frameId, showFragment).commit();
            } else {
                transaction.show(showFragment).commit();
            }
        } else if (!showFragment.isAdded()) {
            transaction.hide(hintFragment).add(frameId, showFragment).commit();
        } else {
            transaction.hide(hintFragment).show(showFragment).commit();
        }
    }

    public static void remove(FragmentManager fragmentManager,
                              Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragment);
        transaction.commitAllowingStateLoss();
    }

    public static void show(FragmentManager fragmentManager,
                              Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();
    }

    public static void hideAddFragmentToActivity(FragmentManager fragmentManager,
                                                 Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.hide(fragment);
        transaction.commitAllowingStateLoss();
    }
}
