package com.example.android.lab1;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment {

    private int mNumColumns;

    private int mNumberOfItems;
    private View mRootView;
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

        if (container.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mNumColumns = 2;
        } else {
            mNumColumns = 3;
        }

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
        mNumberOfItems = 200;

        mRootView = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = mRootView.findViewById(R.id.recycler_books);

        GridLayoutManager layoutManager = new GridLayoutManager(mParent, mNumColumns);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new RecyclerBookAdapter(mRootView.getContext(), mNumberOfItems);
        mRecyclerView.setAdapter(mAdapter);

        return mRootView;
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


}
