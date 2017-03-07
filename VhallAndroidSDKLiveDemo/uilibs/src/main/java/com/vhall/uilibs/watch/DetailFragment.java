package com.vhall.uilibs.watch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.R;


/**
 * 详情页的Fragment
 */
public class DetailFragment extends Fragment implements WatchContract.DetailView {
    public static DetailFragment newInstance() {
        DetailFragment articleFragment = new DetailFragment();
        return articleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detail_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setPresenter(BasePresenter presenter) {
    }
}
