package com.example.android.lab1;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
    private Toolbar mToolbar;
    private FloatingActionButton mFAB;

    public HomeFragment()
    {

    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        mToolbar = getActivity().findViewById(R.id.toolbar_main_activity);
        mToolbar.setTitle("Lab 2");
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.fragment_home);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int clickedId = item.getItemId();
                switch (clickedId)
                {
                    case R.id.action_search:
                        Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.action_filter:
                        Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
                        break;

                }
                return true;
            }
        });

        mFAB = getActivity().findViewById(R.id.fab_add_book);
        mFAB.setVisibility(View.VISIBLE);

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
