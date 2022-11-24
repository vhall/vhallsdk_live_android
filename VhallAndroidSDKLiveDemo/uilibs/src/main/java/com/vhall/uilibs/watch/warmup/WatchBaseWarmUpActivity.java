package com.vhall.uilibs.watch.warmup;

import static com.vhall.uilibs.util.ToastUtil.showToast;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.WebinarInfoDataSource;
import com.vhall.player.vod.VodPlayerView;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.CommonUtil;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.handler.WeakHandler;
import com.vhall.vhss.data.WarmInfoData;

import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class WatchBaseWarmUpActivity extends AppCompatActivity {

    protected Param params;
    protected WebinarInfo webinarInfo;
    protected WarmInfoData warmInfoData;
    protected ImageView iv_host_avatar, iv_close, iv_cover, iv_play;
    protected TextView tv_host_name, tv_time, tv_time2, tv_title, tv_title2, tv_status;
    protected WebView webView;
    protected VodPlayerView vodPlayerView;

    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String text = CommonUtil.converStringTimeToStr(webinarInfo.start_time);
            tv_time.setText(Html.fromHtml(text));
            return false;
        }
    });

    //开播倒计时显示
    private void handleTimer() {
        ScheduledThreadPoolExecutor timeService = new ScheduledThreadPoolExecutor(1);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1);
            }
        };
        timeService.scheduleAtFixedRate(timerTask, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_warm_up);
        params = (Param) getIntent().getSerializableExtra("param");
        initView();
        initData();
    }

    private void initData() {
        VhallSDK.initWatch(params.watchId, "", "", params.key, params.k_id, new WebinarInfoDataSource.LoadWebinarInfoCallback() {
            @Override
            public void onWebinarInfoLoaded(String jsonStr, WebinarInfo data) {
                if (isFinishing()) {
                    return;
                }
                webinarInfo = data;
                dealData(data);
                //预告状态

                VhallSDK.getWarmInfo(params.watchId, new RequestDataCallbackV2<WarmInfoData>() {
                    @Override
                    public void onSuccess(WarmInfoData data) {
                        if (data != null) {
                            warmInfoData = data;
                            initWatchWarmUp();
                        }
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        showToast(errorMsg);
                        //只有消息
                        initWatchWarmUp();
                    }
                });
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                showToast(errorMsg);
                finish();
            }
        });
    }

    private void dealData(WebinarInfo data) {
        String introduction = data.introduction;
        introduction = introduction.replace("<img", "<img style=\"max-width:100%;height:auto\" ");
        webView.loadDataWithBaseURL("", introduction, "text/html", "utf-8", null);
        RequestOptions requestOptions = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.drawable.icon_default_avatar);
        Glide.with(this).load(data.hostAvatar).apply(requestOptions).into(iv_host_avatar);
        Glide.with(this).load(data.img_url).into(iv_cover);
        tv_time2.setText(data.start_time);
        tv_host_name.setText(data.hostName);
        tv_title.setText(BaseUtil.getLimitString(data.subject, 8));
        tv_title2.setText(data.subject);

        tv_time.setVisibility(View.GONE);
        //活动状态 1直播；2预告；3结束；4回放或点播；5录播
        if (data.status == 1) {
            tv_status.setText("直播中");
        } else if (data.status == 2) {
            tv_status.setText("预告");
            handleTimer();
            tv_time.setVisibility(View.VISIBLE);
        } else if (data.status == 3) {
            tv_status.setText("结束");
        } else {
            tv_status.setText("回放");
        }
    }

    private void initView() {
        webView = findViewById(R.id.web);
        vodPlayerView = findViewById(R.id.vodPlayerView);
        iv_host_avatar = findViewById(R.id.iv_host_avatar);
        iv_close = findViewById(R.id.iv_close);
        iv_cover = findViewById(R.id.iv_cover);
        iv_play = findViewById(R.id.iv_play);
        tv_status = findViewById(R.id.tv_status);
        tv_host_name = findViewById(R.id.tv_host_name);
        tv_time = findViewById(R.id.tv_time);
        tv_time2 = findViewById(R.id.tv_time2);
        tv_title = findViewById(R.id.tv_title);
        tv_title2 = findViewById(R.id.tv_title2);
        iv_play.setOnClickListener(v -> {
            playPoint = 0;
            if (warmInfoData != null && !ListUtils.isEmpty(warmInfoData.list) && warmInfoData.list.get(0) != null) {
                startNew(warmInfoData.list.get(0));
                iv_play.setVisibility(View.GONE);
            }
        });
        iv_close.setOnClickListener(v -> finish());
    }

    //当前播放的是第几个视频
    protected int playPoint = 0;

    protected abstract void stop();

    protected abstract void startNew(WarmInfoData.RecordListBean recordBean);

    protected abstract void initWatchWarmUp();
}