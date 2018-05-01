package com.example.android.lab1.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;

import java.util.List;

public class ProfileBookAdapter extends RecyclerView.Adapter<ProfileBookAdapter.MyViewHolder> {

    private Context mContext;
    private List<Book> mBooks;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView mBookTitle;
        public TextView mBookEditor;
        public TextView mBookCity;
        public ImageView mBookThumbnail;
        public TextView mBookReview;

        public MyViewHolder (View itemView) {
            super (itemView);
            mBookTitle = itemView.findViewById(R.id.rv_book_title);
            mBookEditor = itemView.findViewById(R.id.rv_book_editor);
            mBookCity = itemView.findViewById(R.id.rv_book_city);
            mBookThumbnail = itemView.findViewById(R.id.rv_book_thumbnail);
            mBookReview = itemView.findViewById(R.id.rv_book_review);
        }
    }
    public ProfileBookAdapter (Context context, List<Book> books) {
        this.mContext = context;
        this.mBooks = books;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View cardView = inflater.inflate(R.layout.recycler_book_item_profile, parent, false);

        return new MyViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }


}
