package com.example.android.lab1.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class RecyclerYourLibraryAdapter extends RecyclerView.Adapter<RecyclerYourLibraryAdapter.MyViewHolder> {

    private List<Book> mBooks;
    private List<String> mBookIds;
    Context mContext;

    public RecyclerYourLibraryAdapter(List<Book> books, List<String> mBookIds) {
        this.mBooks = books;
        this.mBookIds = mBookIds;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View cardView = inflater.inflate(R.layout.recycler_book_item_library, parent, false);

        return new MyViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(mBooks.get(position));
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mBookTitle;
        TextView mBookAuthor;
        ImageView mBookThumbnail;


        public MyViewHolder (View itemView) {
            super (itemView);
            mBookTitle = itemView.findViewById(R.id.library_book_title);
            mBookAuthor = itemView.findViewById(R.id.library_book_author);
            mBookThumbnail = itemView.findViewById(R.id.library_book_thumbnail);
        }
        @SuppressLint("CheckResult")
        public void bind(Book book){
            mBookTitle.setText(book.getTitle());
            mBookAuthor.setText(book.getAuthors());

            if (book.getWebThumbnail() != null) {
                Glide.with(itemView.getContext()).asBitmap()
                        .load(book.getWebThumbnail())
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate())
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                Palette palette = Palette.from(resource).generate();
                                int defaultColor = 0xFF3333;
                                int color = palette.getDarkMutedColor(defaultColor);
                                itemView.setBackgroundColor(color);
                                return false;
                            }
                        })
                        .into(mBookThumbnail);
            } else if (book.getUserBookPhotosStoragePath() != null && book.getUserBookPhotosStoragePath().size() > 0) {
                StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUserBookPhotosStoragePath().get(0));
                Glide.with(itemView.getContext()).load(storage).into(mBookThumbnail);
            } else
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_no_book_photo)).into(mBookThumbnail);
        }

    }
}
