package com.vhall.uilibs.watch.warmup;

import static com.vhall.business.ErrorCode.ERROR_LOGIN_MORE;
import static com.vhall.uilibs.util.ToastUtil.showToast;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.vhall.business.MessageServer;
import com.vhall.business.module.warmup.WatchWarmUp;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.watch.minimalist.MiniBaseCallBack;
import com.vhall.vhss.data.WarmInfoData;

public class WatchWarmUpActivity extends WatchBaseWarmUpActivity {

    private WatchWarmUp watchWarmUp;
    //循环播放
    private boolean loopPlay = false;
    private VHPlayerListener listener = new VHPlayerListener() {
        @Override
        public void onStateChanged(Constants.State state) {
            Log.e("vhall_", state.toString());
            switch (state) {
                case START:
                    playPoint++;
                    break;
                case BUFFER:
                    break;
                case END:
                    //列表循环播放
                    if (warmInfoData != null && !ListUtils.isEmpty(warmInfoData.list)) {
                        if (playPoint == warmInfoData.list.size()) {
                            if (loopPlay)
                                playPoint = 0;
                            else {
                                iv_play.setVisibility(View.VISIBLE);
                                return;
                            }
                        }
                        startNew(warmInfoData.list.get(playPoint));
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case ERROR_LOGIN_MORE://被其他人踢出
                    break;
            }
        }

        @Override
        public void onError(int i, int i1, String s) {
            showToast(s);
        }
    };


    @Override
    public void stop() {
        if (watchWarmUp != null) {
            watchWarmUp.stop();
        }
    }

    @Override
    public void startNew(WarmInfoData.RecordListBean recordBean) {
        if (watchWarmUp != null) {
            watchWarmUp.start(recordBean);
        }
    }

    @Override
    public void initWatchWarmUp() {
        if (watchWarmUp == null) {
            watchWarmUp = new WatchWarmUp.Builder()
                    .context(this)
                    .messageCallback(new MiniBaseCallBack.SimpleMessageEventCallback() {
                        @Override
                        public void onEvent(MessageServer.MsgInfo messageInfo) {
                            super.onEvent(messageInfo);
                            if (messageInfo.event == MessageServer.EVENT_RESTART) {
                                showToast("直播开始");
                                tv_status.setText("直播中");
                                tv_time.setVisibility(View.GONE);
                                //根据自己的业务逻辑增加跳转
                            } else if (messageInfo.event == MessageServer.EVENT_OVER) {
                                showToast("直播结束");
                                tv_status.setText("结束");
                                //结束直接跳到回放 回放基本或生成失败
                            }
                        }
                    })
                    .callback(listener)
                    .vodPlayView(vodPlayerView).build();
        }
        watchWarmUp.setWebinarInfo(webinarInfo);
        watchWarmUp.setScaleType(Constants.VideoMode.DRAW_MODE_ASPECTFILL);
        if (warmInfoData != null) {
            if (!TextUtils.isEmpty(warmInfoData.img_url))
                Glide.with(this).load(warmInfoData.img_url).into(iv_cover);
            loopPlay = TextUtils.equals("2", warmInfoData.player_type);
        }
        if (warmInfoData == null || ListUtils.isEmpty(warmInfoData.list)) {
            iv_play.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (watchWarmUp != null) {
            watchWarmUp.destroy();
        }
    }
}