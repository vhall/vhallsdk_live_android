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

import com.vhall.business.VhallSDK;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.WebinarInfoRemote;
import com.vhall.business.data.source.WebinarInfoDataSource;
import com.vhall.live.R;
import com.vhall.uilibs.Param;

/**
 *  清单文件设置
 * <activity android:name=".webWatch.UserWebViewActivity"
 *             android:configChanges="orientation|screenSize|keyboardHidden"
 *             android:screenOrientation="sensor"
 *             android:hardwareAccelerated="true"/>
 *
 *
 *
 * <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:id="@+id/rl_web_content"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent">
 *
 *     <WebView
 *         android:id="@+id/wv_play"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent" />
 * </RelativeLayout>
 *
 */
public class UserWebViewActivity extends FragmentActivity {

    WebView webView;
    WebSettings webSettings;
    RelativeLayout rlContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_web_activity);

        String url = "https 全地址";

        webView = findViewById(R.id.wv_play);
        webView.requestFocus(View.FOCUS_DOWN);
        rlContent = findViewById(R.id.rl_web_content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webSettings = webView.getSettings();

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setDomStorageEnabled(true);////启用或禁用DOM缓存

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        webSettings.setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        webSettings.setAllowFileAccessFromFileURLs(true);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        //开启JavaScript支持
        webSettings.setJavaScriptEnabled(true);
        // 支持缩放
        webSettings.setSupportZoom(true);


        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
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
