package com.vhall.uimodule.watch.survey;

import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.vhall.uimodule.R;
import com.vhall.uimodule.base.BaseBottomDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class SurveyWebView extends BaseBottomDialog {
    private static final String TAG = "SurveyView";
    private WebSettings mSettings;
    private static final String JSNAME = "messageHandlers";
    private static final String JSNAME1 = "android";
    private WebView webView;
    private String url;


    public SurveyWebView( Context mContext, String url) {
        super(mContext);
        this.url = url;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_survey_webview);
        webView = findViewById(R.id.web_view);
        initWebConfig();
    }
    private void initWebConfig() {
        mSettings = webView.getSettings();
        mSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JS2Android(), JSNAME);
        webView.addJavascriptInterface(new JS2Android(), JSNAME1);


        //增加下面配置 跳转生效
        mSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        mSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        mSettings.setDomStorageEnabled(true);////启用或禁用DOM缓存

        mSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mSettings.setDefaultTextEncodingName("UTF-8");
        mSettings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        mSettings.setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        mSettings.setAllowFileAccessFromFileURLs(true);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        mSettings.setAllowUniversalAccessFromFileURLs(true);
        // 支持缩放
        mSettings.setSupportZoom(true);
        webView.setWebViewClient(new MyWebViewClient());
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            mSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.loadUrl(url);
        Log.e("vhall_", url);
    }

    class JS2Android {
        @JavascriptInterface
        public void onWebEvent(String data) {
            Log.e(TAG, "onWebEvent:" + data);
            if (!TextUtils.isEmpty(data)) {
                try {
                    JSONObject result = new JSONObject(data);
                    String event = result.optString("event");
                    if (!TextUtils.isEmpty(event)) {
                        if ("close".equals(event)) {
                            dismiss();
                        } else if ("submit".equals(event)) {
                            if (200 == result.optInt("code")) {
                               showToast("提交成功");
                            } else {
                                showToast(result.optString("msg"));
                            }
                            dismiss();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            /*
            //js 注入隐藏问卷头部图片
            String js = "var script = document.querySelector('.header').style.backgroundImage='url()';";
            js += "var child=document.querySelector('.header').style.minHeight='30px';";
            loadUrl("javascript:" + js);*/
        }
    }
}
