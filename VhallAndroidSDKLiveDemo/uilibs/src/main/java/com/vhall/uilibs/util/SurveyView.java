package com.vhall.uilibs.util;

import android.content.Context;
import android.net.http.SslError;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zwp on 2019/7/5
 */
public class SurveyView extends WebView {
    private static final String TAG = "SurveyView";
    public static final int EVENT_PAGE_LOADED = 1;//页面加载完成
    public static final int EVENT_JS_BACK = 2;//JS 回调
    public static final int EVENT_JS_FINASH = 3;//JS 回调
    private WebSettings mSettings;
    private static final String JSNAME = "messageHandlers";

    private static final String JSNAME1 = "android";
    private Context context;

    private EventListener eventListener;

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }


    public interface EventListener {
        void onEvent(int eventCode, String eventMsg);
    }


    public SurveyView(Context context) {
        super(context);
        this.context = context;
        initConfig();
    }

    public SurveyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initConfig();

    }

    public SurveyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initConfig();
    }

    private void initConfig() {
        mSettings = getSettings();
        mSettings.setJavaScriptEnabled(true);
        addJavascriptInterface(new JS2Android(), JSNAME);
        addJavascriptInterface(new JS2Android(), JSNAME1);
        setWebViewClient(new MyWebViewClient());
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
                            if (eventListener != null) {
                                eventListener.onEvent(EVENT_JS_BACK, data);
                            }
                        } else if ("submit".equals(event)) {
                            if (200 == result.optInt("code")) {
                                Toast.makeText(context, "提交成功", Toast.LENGTH_SHORT).show();
                                if (eventListener != null) {
                                    eventListener.onEvent(EVENT_JS_BACK, data);
                                }
                            } else {
                                Toast.makeText(context, result.optString("msg"), Toast.LENGTH_SHORT).show();
                            }
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

            if (eventListener != null) {
                eventListener.onEvent(EVENT_PAGE_LOADED, "on page finished");
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }


    @Override
    public void destroy() {
        eventListener = null;
        super.destroy();
    }
}
