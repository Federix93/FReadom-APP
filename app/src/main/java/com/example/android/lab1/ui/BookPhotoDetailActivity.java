package com.example.android.lab1.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;
import com.example.android.lab1.utils.Utilities;

import java.util.ArrayList;

public class BookPhotoDetailActivity extends AppCompatActivity {

    public static final String BOOK_PHOTO = "BOOK_PHOTO_DETAIL";
    Toolbar mToolbar;
    private ViewPager mImageGallery;
    private RecyclerView mCountImagesDots;
    private Integer mSelected;
    private ArrayList<String> mUrls;

    public void setSelected(Integer mSelected) {
        this.mSelected = mSelected;
        mCountImagesDots.getAdapter().notifyItemRangeChanged(0, mUrls.size());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_full_detail);

        Utilities.setupStatusBarColor(this);

        mImageGallery = findViewById(R.id.image_container);
        mCountImagesDots = findViewById(R.id.image_dots);
        mToolbar = findViewById(R.id.toolbar_library_book_detail);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent() == null ||
                !getIntent().hasExtra(Keys.URLS.toString()) ||
                getIntent().getStringArrayListExtra(Keys.URLS.toString()) == null ||
                getIntent().getStringArrayListExtra(Keys.URLS.toString()).isEmpty()) {
            finish();
            return;
        }

        String title = getIntent().hasExtra(Keys.BOOK_TITLE.toString()) ?
                getIntent().getStringExtra(Keys.BOOK_TITLE.toString()) : getString(R.string.photo_detail);
        mUrls = getIntent().getStringArrayListExtra(Keys.URLS.toString());
        Integer initialPhoto = getIntent().getIntExtra(Keys.SELECTED.toString(), 0);
        mToolbar.setTitle(title);

        mImageGallery.setAdapter(new ImagePager());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mCountImagesDots.setAdapter(new DotsAdapter());
        mCountImagesDots.setLayoutManager(linearLayoutManager);
        mCountImagesDots.setHasFixedSize(true);
        mImageGallery.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (initialPhoto >= 0 && initialPhoto <= mUrls.size()) {
            mImageGallery.setCurrentItem(initialPhoto);
            setSelected(initialPhoto);
        }
    }

    public enum Keys {
        BOOK_TITLE, URLS, SELECTED
    }

    private class DotsAdapter extends RecyclerView.Adapter<DotsAdapter.DotsHolder> {
        public DotsAdapter() {
            notifyItemRangeInserted(0, mUrls.size());
        }

        @Override
        public DotsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = (LayoutInflater)
                    parent.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            return new DotsHolder(layoutInflater.inflate(R.layout.item_dot,
                    parent,
                    false));
        }

        @Override
        public void onBindViewHolder(@NonNull DotsHolder holder, int position) {
            holder.update(position == mSelected);
        }

        @Override
        public int getItemCount() {
            return mUrls.size();
        }

        class DotsHolder extends RecyclerView.ViewHolder {
            TextView mDot;

            public DotsHolder(View itemView) {
                super(itemView);
                mDot = (TextView) itemView;
            }

            public void update(boolean selected) {
                int color = getResources().getColor(selected ? R.color.white : R.color.gray_active_icon);
                mDot.setTextColor(color);
            }
        }
    }

    private class ImagePager extends PagerAdapter {

        @Override
        public int getCount() {
            return mUrls.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) container.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ImageView currentPhoto = (ImageView) layoutInflater.inflate(R.layout.item_photo, container, false);
            Glide.with(container.getContext())
                    .load(mUrls.get(position))
                    .into(currentPhoto);

            container.addView(currentPhoto);
            return currentPhoto;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((ImageView) object);
        }
    }
}
