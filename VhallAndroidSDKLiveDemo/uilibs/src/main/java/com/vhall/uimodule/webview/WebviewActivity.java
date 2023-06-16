package com.vhall.uimodule.webview;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vhall.uimodule.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class WebviewActivity extends AppCompatActivity {
    WebSettings webSettings;
    private WebView webView;
    private EditText web_site;

    public static void startActivity(Context context, String url) {
        Intent intent = new Intent(context, WebviewActivity.class);
        intent.putExtra("defaulturl", url);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        webView = findViewById(R.id.webview);
        web_site = findViewById(R.id.web_site);
        webView.requestFocus(View.FOCUS_DOWN);

//        getWindow.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
//                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getPushPermission(100, this);
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
        webSettings.setAllowContentAccess(true);////启用或禁用DOM缓存
        webSettings.setAllowFileAccess(true);////启用或禁用DOM缓存
        webSettings.setAllowFileAccessFromFileURLs(false);////启用或禁用DOM缓存
        webSettings.setAllowUniversalAccessFromFileURLs(false);////启用或禁用DOM缓存

        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        webView.setWebContentsDebuggingEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest webResourceRequest) {
                FileInputStream input;

                String url = webResourceRequest.getUrl().toString();

                String key = "http://androidimg";
                /*如果请求包含约定的字段 说明是要拿本地的图片*/
                if (url.contains(key)) {
                    String imgPath = url.replace(key, "");
                    try {
                        /*重新构造WebResourceResponse  将数据已流的方式传入*/
                        input = new FileInputStream(new File(imgPath.trim()));
                        WebResourceResponse response = new WebResourceResponse("image/jpg", "UTF-8", input);

                        /*返回WebResourceResponse*/
                        return response;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();

                    }

                }
                return super.shouldInterceptRequest(webView, webResourceRequest);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                super.onReceivedSslError(view, handler, error);
                handler.proceed();//忽略证书的错误继续Load页面内容，不会显示空白页面
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }


        });

        webView.setWebChromeClient(new WebChromeClient() {
            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;


            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
                request.getOrigin();
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
//                rlContent.addView(view);
                mCustomViewCallback = callback;
//                webView.setVisibility(View.GONE);
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
//                rlContent.removeView(mCustomView);
                mCustomViewCallback.onCustomViewHidden();
                mCustomView = null;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        });
        initJSInterface();
        loadLocalHtml(getIntent().getStringExtra("defaulturl"));
    }

    @SuppressLint("JavascriptInterface")
    private void initJSInterface() {
        if (null != webView) {
            webView.addJavascriptInterface(this, "vhandroid");
        }
    }

    private void loadLocalHtml(String url) {
        if (null != webView) {
            url = (url!=null)?url:"https://live.vhall.com/v3/lives/embedclient/watch/927829294";
            web_site.setText(url);
            webView.loadUrl(url);
        }
    }

    @JavascriptInterface
    public void methodName() {
        showJSIncoming("JS called Java method: methodName()");
    }

    @JavascriptInterface
    public void methodNameWithArgs(String arg1, String arg2) {
        showJSIncoming("JS called Java method: methodNameWithArgs(arg1, arg2) \n" + "arg1 = " + arg1 + "\n" + "arg2 = " + arg2);
    }

    private void showJSIncoming(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.tv_display)).setText(msg);
            }
        });
    }

    public void load(View view) {
        String url = web_site.getText().toString();
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "请输入正确的url地址", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!url.contains("://")) {
            url = "https://" + url;
            web_site.setText(url);
        }

        webView.loadUrl(url);
        hideInput();
    }

    public void clear(View view) {
        web_site.setText("");
        hideInput();
    }

    //调用JS方法
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void executeJS(String code) {
        String url = "javascript:" + code;
        webView.evaluateJavascript(url, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {

            }
        });
    }

    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }


    private long lastTime;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public static boolean getPushPermission(int requestCode, AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (activity.checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        activity.requestPermissions(new String[]{CAMERA, RECORD_AUDIO, MODIFY_AUDIO_SETTINGS}, requestCode);
        return false;
    }

}