package com.example.android.lab1.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;

import java.util.ArrayList;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosViewHolder> {

    ArrayList<String> mPhotospaths;
    Activity mContainer;

    public PhotosAdapter(Activity c) {
        mPhotospaths = new ArrayList<>();
        mContainer = c;
    }

    @NonNull
    @Override
    public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.add_photo_square, parent, false);
        v.getLayoutParams().width = parent.getWidth() / 3;
        return new PhotosViewHolder(v, (View.OnClickListener) mContainer, (RemovePhotoClickListener) mContainer);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotosViewHolder holder, int position) {
        holder.setPhoto(mPhotospaths.get(position), mContainer.getApplicationContext());
    }

    @Override
    public int getItemCount() {
        return mPhotospaths.size();
    }

    public void addItem(String newPath) {
        mPhotospaths.add(newPath);
        notifyItemInserted(getItemCount() - 1);
    }

    public String removeItem(int position) {
        String removed = mPhotospaths.remove(position);
        notifyItemRemoved(position);
        return removed;
    }

    public ArrayList<String> getModel() {
        return mPhotospaths;
    }
}
