package com.example.android.lab1;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    FragmentManager mFt = null;
    android.support.v7.widget.Toolbar mToolbar;

    public DashboardFragment (){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mFt = getChildFragmentManager();

        mToolbar = getActivity().findViewById(R.id.toolbar_main_activity);

        mToolbar.setTitle(R.string.title_dashboard);
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.fragment_dashboard);

        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // Give the TabLayout the ViewPager
        TabLayout tabs = (TabLayout) rootView.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab());
        tabs.addTab(tabs.newTab());
        tabs.addTab(tabs.newTab());
//        tabs.addTab(tabs.newTab().setText("Library"));
//        tabs.addTab(tabs.newTab().setText("Lended"));
//        tabs.addTab(tabs.newTab().setText("Borrowed"));
        tabs.setupWithViewPager(viewPager);

        return rootView;
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(mFt);
        adapter.addFragment(new DashboardFragmentLibraryItem(), "Library");
        adapter.addFragment(new DashboardFragmentLendedItem(), "Lended");
        adapter.addFragment(new DashboardFragmentBorrowedItem(), "Borrowed");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


}
