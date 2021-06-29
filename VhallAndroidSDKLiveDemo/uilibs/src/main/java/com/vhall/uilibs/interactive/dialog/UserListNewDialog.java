package com.vhall.uilibs.interactive.dialog;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;


import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.broadcast.UserMangerNewFragment;
import com.vhall.uilibs.util.DensityUtils;
import com.vhall.vhss.data.WebinarInfoData;


/**
 * @author hkl
 */
public class UserListNewDialog extends DialogFragment {
    private Context mContext;
    private UserMangerNewFragment instance;
    /**
     * 主讲人id
     */
    private String mainId;

    public static UserListNewDialog getInstance(boolean canManger, boolean canSpeak, boolean isGuest, WebinarInfoData roomInfoData) {
        UserListNewDialog fragment = new UserListNewDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isGuest", isGuest);
        bundle.putBoolean("canSpeak", canSpeak);
        bundle.putBoolean("canManger", canManger);
        bundle.putSerializable("roomInfoData", roomInfoData);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_user_list, null);
        mContext = getContext();
        FrameLayout llRoot = view.findViewById(R.id.fl_root);
        ViewGroup.LayoutParams layoutParams = llRoot.getLayoutParams();
        layoutParams.height = Math.min(DensityUtils.getScreenHeight(mContext), DensityUtils.getScreenWidth(mContext));
        layoutParams.width = Math.min(DensityUtils.getScreenHeight(mContext), DensityUtils.getScreenWidth(mContext));
        llRoot.setLayoutParams(layoutParams);
        init(view);
        return view;
    }

    public void refreshUserList() {
        if (instance != null) {
            instance.refreshUserList();
        }
    }

    public void setMainId(String mainId) {
        this.mainId = mainId;
        if (instance != null) {
            instance.setMainId(mainId);
        }
    }

    private void init(View view) {
        instance = UserMangerNewFragment.getInstance( (WebinarInfoData) getArguments().getSerializable("roomInfoData"),getArguments().getBoolean("isGuest", false),getArguments().getBoolean("canSpeak", false),getArguments().getBoolean("canManger", true));
        if (!TextUtils.isEmpty(mainId)) {
            instance.setMainId(mainId);
        }
        showFragment(instance);
        view.findViewById(R.id.cl_root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fl_root, fragment);
        transaction.commit();
    }


    @Override
    public void dismiss() {
        super.dismiss();
    }
}
