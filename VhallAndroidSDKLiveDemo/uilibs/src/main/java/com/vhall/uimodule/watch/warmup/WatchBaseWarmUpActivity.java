package com.vhall.uimodule.watch.warmup;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.player.vod.VodPlayerView;
import com.vhall.uimodule.R;
import com.vhall.uimodule.base.IBase;
import com.vhall.uimodule.utils.CommonUtil;
import com.vhall.uimodule.utils.ListUtils;
import com.vhall.uimodule.utils.WeakHandler;
import com.vhall.vhss.data.WarmInfoData;

import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public abstract class WatchBaseWarmUpActivity extends AppCompatActivity implements IBase {

    protected WebinarInfo webinarInfo;
    protected WarmInfoData warmInfoData;
    protected ImageView iv_host_avatar, iv_close, iv_cover, iv_play;
    protected TextView tv_host_name, tv_time, tv_time2, tv_title, tv_title2, tv_status;
    protected WebView webView;
    protected VodPlayerView vodPlayerView;
    protected Context mContext;

    public static void startActivityForResult(Activity context, WebinarInfo info) {
        Intent intent = new Intent(context, WatchWarmUpActivity.class);
        intent.putExtra("info", info);
        context.startActivityForResult(intent, WatchWarmUpActivity.CODE_REQUEST);
    }

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
        mContext = this;
        initView();
        initData();
    }

    private void initData() {
        webinarInfo = (WebinarInfo) getIntent().getSerializableExtra("info");
        if (webinarInfo == null) {
            finish();
        }
        dealData(webinarInfo);
        //预告状态
        VhallSDK.getWarmInfo(webinarInfo.webinar_id, new RequestDataCallbackV2<WarmInfoData>() {
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

    private void dealData(WebinarInfo data) {
        String introduction = data.introduction;

        if (null == introduction || introduction.isEmpty() || TextUtils.equals("<p></p>", introduction)) {
            findViewById(R.id.web_empty).setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        } else {
            String wordBreakConfig = "<body style=\"word-wrap:break-word;\"> </body>";
            introduction = wordBreakConfig + introduction;
            introduction = introduction.replace("<img", "<img style=\"max-width:100%;height:auto\" ");
            webView.loadDataWithBaseURL("", introduction, "text/html", "utf-8", null);
        }

        RequestOptions requestOptions = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.mipmap.icon_avatar);
        Glide.with(this).load(data.hostAvatar).apply(requestOptions).into(iv_host_avatar);

        Glide.with(this).load(data.img_url).into(iv_cover);
        tv_time2.setText(data.start_time);
        tv_host_name.setText(CommonUtil.getLimitString(data.hostName, 8));
        tv_title.setText(CommonUtil.getLimitString(data.subject, 8));
        tv_title2.setText(CommonUtil.getLimitString(data.subject, 8));

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