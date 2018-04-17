package com.example.android.lab1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

public class ImagePagerAdapter extends PagerAdapter
{
    Context mContext;
    LayoutInflater mInflater;
    LinearLayout mDotsContainer;
    ArrayList<String> mPhotosPath;

    public ImagePagerAdapter(Context context, ArrayList<String> imagesPath, LinearLayout dotsContainer)
    {
        mContext = context;
        mPhotosPath = imagesPath;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDotsContainer = dotsContainer;
    }

    @Override
    public int getCount() {
        return mPhotosPath != null? mPhotosPath.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final View itemView = mInflater.inflate(R.layout.gallery_pager_item, container, false);
        final ImageView imageView = itemView.findViewById(R.id.user_book_gallery_photo);

        if (mPhotosPath.size() > 0) {
            Glide.with(mContext)
                    .load(mPhotosPath.get(position))
                    .apply(new RequestOptions().centerCrop())
                    .into(imageView);

            /*TextView[] dots = new TextView[mPhotosPath.size()];
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            Log.d("DEBUBDOTS", "instantiateItem: instantiang view n. " + Integer.toString(position));
            mDotsContainer.removeAllViews();

            for (int i = 0; i < dots.length; i++) {
                dots[i] = new TextView(mContext);
                dots[i].setText(".");
                dots[i].setTextSize(45);
                dots[i].setTypeface(null, Typeface.BOLD);
                dots[i].setLayoutParams(params);
                //if (i == position)
                  //  dots[i].setTextColor(Color.WHITE);
                //else
                    dots[i].setTextColor(Color.GRAY);
                mDotsContainer.addView(dots[i]);
            }*/
        }
        else
        {
            // set it to a place holder

        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }

}
