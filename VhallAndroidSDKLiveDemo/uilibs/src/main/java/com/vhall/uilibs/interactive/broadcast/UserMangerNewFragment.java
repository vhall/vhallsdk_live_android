package com.vhall.uilibs.interactive.broadcast;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.flyco.tablayout.SlidingTabLayout;
import com.vhall.uilibs.R;
import com.vhall.vhss.data.WebinarInfoData;


/**
 * 在线列表 踢出列表
 *
 * @author hkl
 */
public class UserMangerNewFragment extends Fragment {

    public static UserMangerNewFragment getInstance(WebinarInfoData roomInfoData, boolean isGuest, boolean canSpeak, boolean canManger) {
        UserMangerNewFragment fragment = new UserMangerNewFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isGuest", isGuest);
        bundle.putBoolean("canSpeak", canSpeak);
        bundle.putBoolean("canManger", canManger);
        bundle.putSerializable("roomInfoData", roomInfoData);
        fragment.setArguments(bundle);
        return fragment;
    }

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private String titles[] = new String[]{" 成员列表", "受限列表"};
    private Fragment[] mFragments = new Fragment[2];
    private UserListNewFragment onlineFragment;
    private UserListNewFragment kickOutFragment;
    private String mainId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_user_manger, null);
        slidingTabLayout = inflate.findViewById(R.id.st_list);
        viewPager = inflate.findViewById(R.id.viewpager);
        viewPager.setAdapter(new UserMangerAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(2);
        onlineFragment = UserListNewFragment.getInstance(UserListNewFragment.TYPE_ONLINE,  (WebinarInfoData) getArguments().getSerializable("roomInfoData"), getArguments().getBoolean("isGuest", false), getArguments().getBoolean("canSpeak", false), getArguments().getBoolean("canManger", true));
        kickOutFragment = UserListNewFragment.getInstance(UserListNewFragment.TYPE_KICK_OUT,  (WebinarInfoData) getArguments().getSerializable("roomInfoData"), getArguments().getBoolean("isGuest", false), getArguments().getBoolean("canSpeak", false), getArguments().getBoolean("canManger", true));
        if (!TextUtils.isEmpty(mainId)) {
            onlineFragment.setMainId(mainId);
        }
        mFragments = new Fragment[]{onlineFragment, kickOutFragment};
        slidingTabLayout.setViewPager(viewPager, titles);
        return inflate;
    }

    public void refreshUserList() {
        if (onlineFragment != null) {
            onlineFragment.refreshUserList();
        }
        if (kickOutFragment != null) {
            kickOutFragment.refreshUserList();
        }
    }

    public void setMainId(String mainId) {
        this.mainId = mainId;
        if (onlineFragment != null) {
            onlineFragment.setMainId(mainId);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initLister();
    }

    private void initLister() {

    }

    class UserMangerAdapter extends FragmentPagerAdapter {

        private UserMangerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
    }
}
