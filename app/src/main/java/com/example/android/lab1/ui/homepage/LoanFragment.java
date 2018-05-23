package com.example.android.lab1.ui.homepage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ViewPagerAdapter;

public class LoanFragment extends Fragment {

    FragmentManager mFt = null;
    android.support.v7.widget.Toolbar mToolbar;

    public LoanFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_loans_and_requests, container, false);

        mFt = getChildFragmentManager();

        mToolbar = getActivity().findViewById(R.id.toolbar_home_page_activity);

        mToolbar.setTitle(R.string.title_loans);
        mToolbar.getMenu().clear();

        ViewPager viewPager = rootView.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // Give the TabLayout the ViewPager
        TabLayout tabs = rootView.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab());
        tabs.addTab(tabs.newTab());

        tabs.setupWithViewPager(viewPager);

        return rootView;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(mFt);
        adapter.addFragment(new LentFragmentItem(), getResources().getString(R.string.loan_your_item));
        adapter.addFragment(new BorrowedFragmentItem(), getResources().getString(R.string.loan_other_book_item));
        viewPager.setAdapter(adapter);
    }


}
