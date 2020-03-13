package com.vhall.uilibs.watch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vhall.document.DocumentView;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.R;

import static com.vhall.ops.VHOPS.TYPE_SWITCHOFF;

/**
 * Created by zwp on 2019/7/4
 */
public class DocumentFragmentVss extends Fragment implements WatchContract.DocumentViewVss {

    private static final String TAG = "DocumentFragmentVss";
    private RelativeLayout rlContainer;
    private DocumentView tempView = null;
    private String switchType = "";

    public static DocumentFragment newInstance() {
        DocumentFragment articleFragment = new DocumentFragment();
        return articleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.document_fragment_vss, null);
        rlContainer = view.findViewById(R.id.rl_doc_container);
        if (!TextUtils.isEmpty(switchType)) {
            if (rlContainer != null) {
                if (switchType.equals(TYPE_SWITCHOFF)) {
                    rlContainer.setVisibility(View.GONE);
                } else {
                    rlContainer.setVisibility(View.VISIBLE);
                }
            }
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (tempView != null) {
            refreshView(tempView);
        }
        super.onActivityCreated(savedInstanceState);

    }


    @Override
    public void refreshView(DocumentView view) {
        /**
         * 文档缩放功能不完善，暂不建议使用
         * view.getSettings().setBuiltInZoomControls(true);
         * view.getSettings().setSupportZoom(true);
         */
        if (rlContainer != null) {
            rlContainer.removeAllViews();
            rlContainer.addView(view);
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1280*5, ViewGroup.LayoutParams.MATCH_PARENT);
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            params.addRule(RelativeLayout.CENTER_IN_PARENT);
//            view.setLayoutParams(params);
        } else {
            tempView = view;
        }
    }

    @Override
    public void switchType(String type) {
        switchType = type;
        if (rlContainer != null) {
            if (type.equals(TYPE_SWITCHOFF)) {
                rlContainer.setVisibility(View.GONE);
            } else {
                rlContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setPresenter(BasePresenter presenter) {

    }
}
