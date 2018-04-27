package com.example.android.lab1.ui.homepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;

public class DashboardFragmentBorrowedItem extends Fragment {

    public DashboardFragmentBorrowedItem() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_borrowed, container, false);
        return view;
    }
}
