package com.example.android.lab1.ui.homepage;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

public class FragmentAdapter extends FragmentPagerAdapter {

    private ArrayList<TabFragment> fragments = new ArrayList<>();
    private TabFragment currentFragment;

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
        fragments.clear();
        fragments.add(TabFragment.newInstance(0));
        fragments.add(TabFragment.newInstance(1));
        fragments.add(TabFragment.newInstance(2));
        fragments.add(TabFragment.newInstance(3));

    }

    @Override
    public TabFragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            currentFragment = ((TabFragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    public TabFragment getCurrentFragment() {
        return currentFragment;
    }
}
