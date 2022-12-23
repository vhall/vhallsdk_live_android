package com.vhall.uilibs.widget;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zwp on 2019/7/5
 */
public class ExamWebView extends BaseBottomDialog {
    private static final String TAG = "SurveyView";
    private WebView webView;
    private WebSettings mSettings;
    private static final String JSNAME = "messageHandlers";
    private static final String JSNAME1 = "android";
    private Context context;
    private String url;


    public ExamWebView(Context context, String url) {
        super(context);
        this.context = context;
        this.url = url;
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            mSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.loadUrl(url);
        Log.e("vhall_", url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_exam_webview);
        webView = findViewById(R.id.web_view);
        findViewById(R.id.tv_cancel).setOnClickListener(v -> dismiss());
        initWebConfig();
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
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
