package com.vhall.live.webWatch;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.WebinarInfoRemote;
import com.vhall.business.data.source.WebinarInfoDataSource;
import com.vhall.live.R;
import com.vhall.uilibs.Param;

/**
 * Created by zwp on 2020-03-11
 */
public class WebViewActivity extends FragmentActivity {

    WebView webView;
    WebSettings webSettings;
    RelativeLayout rlContent;
    private Param param;
    private String baseUrl = "https://e.vhall.com/webinar/inituser/";
    //https://live.vhall.com/room/embedclient/854954136?
    //<iframe allow="camera *;microphone *;" allowfullscreen="true" border="0" src="https://live.vhall.com/webinar/inituser/263823730" width="800" height="600"></iframe>
    private String roomId = "";
    private boolean inject = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_web_activity);
        param = (Param) getIntent().getSerializableExtra("param");
        roomId = param.watchId;
        webView = findViewById(R.id.wv_play);
        webView.requestFocus(View.FOCUS_DOWN);
        rlContent = findViewById(R.id.rl_web_content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setDomStorageEnabled(true);////启用或禁用DOM缓存

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        String url = getIntent().getStringExtra("url");
        if (TextUtils.isEmpty(url)) {
            WebinarInfoRemote.getInstance().getWatchWebinarInfo(roomId, "","", "", "", "", "", new WebinarInfoDataSource.LoadWebinarInfoCallback() {
                @Override
                public void onWebinarInfoLoaded(String jsonStr, WebinarInfo webinarInfo) {
                    baseUrl = "https://t-webinar.e.vhall.com/v3/lives/watch/";
                    baseUrl = baseUrl + roomId;
                    webView.loadUrl(baseUrl);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    baseUrl = baseUrl + roomId;

                    webView.loadUrl(baseUrl);
                }
            });
        } else {
            webView.loadUrl(url);
        }


        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                StringBuilder builder = new StringBuilder();
//                builder.append("function remainTime(){  \n" +
//                        "    setTimeout(\"remainTime()\",10000);\n" +
//                        "var f = document.activeElement == document.getElementsByTagName('textarea')[0]; \n");
//                builder.append("alert(f);}");
//                builder.append("remainTime();  ");
//                view.loadUrl("javascript:"+builder.toString());
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            AlertDialog alertDialog = null;
            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;


            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                //设置用户麦克风和摄像头权限，没有这个上不了麦
                request.grant(request.getResources());
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                //实现播放器全屏响应
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                mCustomView = view;
                rlContent.addView(view);
                mCustomViewCallback = callback;
                webView.setVisibility(View.GONE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                //实现播放器退出全屏响应
                webView.setVisibility(View.VISIBLE);
                webView.requestFocus(View.FOCUS_DOWN);
                if (mCustomView == null) {
                    return;
                }
                mCustomView.setVisibility(View.GONE);
                rlContent.removeView(mCustomView);
                mCustomViewCallback.onCustomViewHidden();
                mCustomView = null;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                alertDialog = new AlertDialog.Builder(webView.getContext()).create();
                alertDialog.show();
                webView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.cancel();
                    }
                }, 50);
            }

        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
        }
    }
}
