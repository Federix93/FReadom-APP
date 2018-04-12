package com.example.android.lab1;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment {

    private static final int numColumns = 3;

    private int numberOfItems;
    private RecyclerView mRecyclerView;
    private RecyclerBookAdapter mAdapter;
    private FragmentActivity mParent;

    public HomeFragment()
    {

    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mParent = getActivity();
        numberOfItems = 2000;

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = rootView.findViewById(R.id.recycler_books);

        GridLayoutManager layoutManager = new GridLayoutManager(mParent, numColumns);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerBookAdapter(numberOfItems);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }


}
