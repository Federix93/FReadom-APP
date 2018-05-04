package com.example.android.lab1.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;

import java.util.ArrayList;

public class PhotosAdapter extends BaseAdapter {

    ArrayList<String> mPhotospaths;
    Activity mContainer;

    public PhotosAdapter(Activity c) {
        mPhotospaths = new ArrayList<>();
        mContainer = c;
    }

    @Override
    public int getCount() {
        return mPhotospaths.size() + 1;
    }

    @Override
    public Object getItem(int position) {

        return mPhotospaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContainer.getApplicationContext());
        /*if (position == getCount() - 1) {
            // get add new photo view
            convertView = layoutInflater.inflate(R.layout.add_new_photo_square, null);
            View viewById = convertView.findViewById(R.id.add_new_user_photo);
            if (viewById != null)
                viewById.setOnClickListener((View.OnClickListener) mContainer);
            convertView.setVisibility(View.VISIBLE);
        } else {*/
        // standard gallery item
        if (getCount() > 0) {
            if (convertView == null)
                convertView = layoutInflater.inflate(R.layout.add_photo_square, null);
            ImageView photo = convertView.findViewById(R.id.load_book_user_photo);
            Glide.with(mContainer.getApplicationContext())
                    .load(mPhotospaths.get(position))
                    .into(photo);
            photo.setOnClickListener((View.OnClickListener) mContainer);
            convertView.findViewById(R.id.load_book_remove_user_photo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mContainer instanceof RemovePhotoClickListener) {
                        ((RemovePhotoClickListener) mContainer).removePhoto(position);
                    }
                }
            });
            convertView.setVisibility(View.VISIBLE);
            //}
        }
        return convertView;
    }

    public void addItem(String newPath) {
        mPhotospaths.add(newPath);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mPhotospaths.remove(position);
        notifyDataSetChanged();
    }
}
