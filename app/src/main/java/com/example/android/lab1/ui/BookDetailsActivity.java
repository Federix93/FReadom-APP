package com.example.android.lab1.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.model.BookPhoto;

public class BookDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        RecyclerView recyclerView = findViewById(R.id.rv_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        ImageGalleryAdapter adapter = new ImageGalleryAdapter(this, BookPhoto.getSpacePhotos());
        recyclerView.setAdapter(adapter);
    }

    private class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder> {

        private BookPhoto[] mBookPhoto;
        private Context mContext;

        @Override
        public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View photoView = inflater.inflate(R.layout.book_image_item, parent, false);
            int width = (parent.getMeasuredWidth() / 3) - 8;
            int height = parent.getMeasuredHeight();
            photoView.setLayoutParams(new RecyclerView.LayoutParams(width, height));

            ImageGalleryAdapter.MyViewHolder viewHolder = new ImageGalleryAdapter.MyViewHolder(photoView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ImageGalleryAdapter.MyViewHolder holder, int position) {

            BookPhoto bookPhoto = mBookPhoto[position];
            ImageView imageView = holder.mPhotoImageView;

            Glide.with(mContext)
                    .load(bookPhoto.getUrl())
                    .apply(new RequestOptions().override(400, 400).fitCenter()
                    .placeholder(R.drawable.ic_no_book_photo))
                    .into(imageView);
        }

        public int getItemCount() {
            return (mBookPhoto.length);
        }


        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ImageView mPhotoImageView;

            public MyViewHolder (View itemView) {
                super (itemView);
                mPhotoImageView = itemView.findViewById(R.id.iv_photo);
                itemView.setOnClickListener(this);
            }

            public void onClick (View view){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    BookPhoto bookPhoto = mBookPhoto[position];
                    Intent intent = new Intent(mContext, BookPhotoDetailActivity.class);
                    intent.putExtra(BookPhotoDetailActivity.BOOK_PHOTO, bookPhoto);
                    startActivity(intent);
                }
            }
        }

        public ImageGalleryAdapter (Context context, BookPhoto[] bookPhotos) {
            mContext = context;
            mBookPhoto = bookPhotos;
        }
    }
}
