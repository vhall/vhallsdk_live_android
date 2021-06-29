package com.vhall.live.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vhall.live.R;
import com.vhall.uilibs.util.DensityUtils;

public class LiveSelectMenuWidget extends FrameLayout implements View.OnClickListener {

    private LinearLayout container;
    public LiveSelectMenuWidget(@NonNull Context context) {
        super(context);
        init();
    }

    public LiveSelectMenuWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveSelectMenuWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.live_select_menu,this);
        this.container = findViewById(R.id.container);
        setVisibility(GONE);
        setOnClickListener(this::onClick);
    }

    public void showMenus(String ...menuNames){
        container.removeAllViews();
        for (String menu:menuNames){
            TextView menuView = new TextView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dpToPxInt(50));
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.setMargins(0,0,0, DensityUtils.dpToPxInt(1));
            menuView.setLayoutParams(layoutParams);
            menuView.setTextColor(Color.WHITE);
            menuView.setGravity(Gravity.CENTER);
//            menuView.setTextSize(DensityUtils.spToPxInt(18));
            menuView.setBackgroundColor(Color.RED);
            menuView.setText(menu);
            menuView.setOnClickListener(this::onClick);
            container.addView(menuView,layoutParams);
        }
        setVisibility(VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if(v instanceof TextView){
            if(mListener != null){
                mListener.onClick(((TextView) v).getText().toString());
            }
        }
        setVisibility(GONE);
    }

    public void setListener(OnMenuListener mListener) {
        this.mListener = mListener;
    }

    private OnMenuListener mListener;

    public interface OnMenuListener{
        void onClick(String name);
    }
}
