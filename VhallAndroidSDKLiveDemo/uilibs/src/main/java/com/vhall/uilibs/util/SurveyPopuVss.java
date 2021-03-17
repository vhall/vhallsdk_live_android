package com.vhall.uilibs.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vhall.uilibs.R;


/**
 * Created by huanan on 2017/3/6.
 */
public class SurveyPopuVss extends PopupWindow {
    private TextView tv_title;
    private SurveyView wvSurvey;
    private ProgressBar progressBar;
    private SurveyView.EventListener eventListener;

    public SurveyPopuVss(Context context) {
        super(context);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        setBackgroundDrawable(dw);
        setFocusable(true);
        View root = View.inflate(context, R.layout.survey_layout, null);
        setContentView(root);
        tv_title = root.findViewById(R.id.tv_title);
        wvSurvey = root.findViewById(R.id.wv_survey);
        progressBar = root.findViewById(R.id.progress_bar);
        wvSurvey.setVisibility(View.VISIBLE);
        wvSurvey.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
                super.onProgressChanged(view, progress);
            }
        });
    }


    public void setListener(SurveyView.EventListener listener) {
        this.eventListener = listener;
        if (wvSurvey != null) {
            wvSurvey.setEventListener(eventListener);
        }
    }

    public void loadView(String url, String title) {
        tv_title.setText(title);
        wvSurvey.loadUrl(url);
        Log.e("hkl",url);
    }
}
