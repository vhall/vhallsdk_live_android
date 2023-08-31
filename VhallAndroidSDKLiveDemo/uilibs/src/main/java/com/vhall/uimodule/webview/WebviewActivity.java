package com.vhall.uimodule.webview;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.vhall.logmanager.VLog;
import com.vhall.uimodule.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.Url;

public class WebviewActivity extends AppCompatActivity {
    WebSettings webSettings;
    private WebView webView;
    private EditText web_site;
    private String referer = "https://test02-live.vhall.com";

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webSettings = webView.getSettings();
        //开启JavaScript支持
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setDomStorageEnabled(true);////启用或禁用DOM缓存

        webSettings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        webSettings.setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        webSettings.setAllowFileAccessFromFileURLs(true);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setDefaultTextEncodingName("UTF-8");

        // 支持缩放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if(!url.startsWith("http"))
                {
                    PackageManager packageManager = getPackageManager();
                    VLog.e("========",url);
                    //通过浏览器打开URL
                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
                    try {
                        startActivity(intent);
                        // 可以处理该scheme
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "未安装应用", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
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
        loadUrl(getIntent().getStringExtra("defaulturl"));
    }

    private void loadUrl(String url) {
        loadUrl(url,true);
    }

    private void loadUrl(String url, boolean updateTitle) {
        if (null != webView) {
            if(url == null ||url.length()==0)
                return;
//            url = (url!=null)?url:"https://live.vhall.com/v3/lives/embedclient/watch/927829294";
            if(updateTitle)
                web_site.setText(url);

            if(referer!= null && referer.length()>0){
                Map<String,String> webviewHead =new HashMap<>();
                webviewHead.put("Referer", referer);
                webView.loadUrl(url,webviewHead);
            } else
                webView.loadUrl(url);

        }
    }

    //JS 调用原生方法
    //webView
    @SuppressLint("JavascriptInterface")
    private void initJSInterface() {
        if (null != webView) {
            webView.addJavascriptInterface(this, "vhandroid");
        }
    }
    @JavascriptInterface
    public void methodName() {
        showJSCallbackMsg("JS called Java method: methodName()");
    }
    @JavascriptInterface
    public void methodNameWithArgs(String arg1, String arg2) {
        showJSCallbackMsg("JS called Java method: methodNameWithArgs(arg1, arg2) \n" + "arg1 = " + arg1 + "\n" + "arg2 = " + arg2);
    }
    //原生调用JS方法
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void executeJS(String code) {
        String url = "javascript:" + code;
        webView.evaluateJavascript(url, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            }
        });
    }

    private void showJSCallbackMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.tv_jslog).setVisibility(View.VISIBLE);
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
        loadUrl(url);
        hideInput();
    }

    public void clear(View view) {
        web_site.setText("");
        hideInput();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}