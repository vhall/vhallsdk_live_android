package com.vhall.uilibs.watch;

/**
 * @author：jooper Email：jooperge@163.com
 * 描述：文档状态管理
 * [全屏、非全屏]
 * [横屏、竖屏]
 * 修改历史:
 * <p>
 * 创建于： 2022/6/15
 */
public class DocFragState {
    public static final int STATE_NONE = -1;
    public static final int STATE_HOR_FULL = 0;
    public static final int STATE_VER_NONFULL = 1;
    public static final int STATE_VER_FULL = 2;

    public static boolean isStateHorizontal(int state) {
        return state == STATE_HOR_FULL;
    }

    public static boolean isFullScreen(int state) {
        return state == STATE_HOR_FULL || state == STATE_VER_FULL;
    }

    /**
     * 全屏/非全屏状态转换
     *
     * @param state
     * @return
     */
    public static int toggleIfFullState(int state) {
        int resultState;
        switch (state) {
            case STATE_NONE:
            case STATE_VER_NONFULL:
                resultState = STATE_VER_FULL;
                break;
            case STATE_VER_FULL:
            case STATE_HOR_FULL:
            default:
                resultState = STATE_VER_NONFULL;
                break;
        }
        return resultState;
    }

    public static int toggleOriState(int state) {
        int resultState;
        switch (state) {
            case STATE_NONE:
            case STATE_VER_NONFULL:
            case STATE_VER_FULL:
                resultState = STATE_HOR_FULL;
                break;
            case STATE_HOR_FULL:
            default:
                resultState = STATE_VER_FULL;
                break;
        }
        return resultState;
    }
}