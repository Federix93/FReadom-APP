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

public class LentBookAdapter extends RecyclerView.Adapter<LentBookAdapter.MyViewHolder>{

    Context mContext;
    private List<Book> mBooks;
    private List<String> mBookIds;

    public LentBookAdapter (List<Book> books, List<String> mBookIds) {
        this.mBooks = books;
        this.mBookIds = mBookIds;
    }

    @NonNull
    @Override
    public LentBookAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View cardView = inflater.inflate(R.layout.recycler_book_item_lent, parent, false);
        return new MyViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull LentBookAdapter.MyViewHolder holder, int position) {
        holder.bind(mBooks.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView mBookTitle;
        TextView mBookAuthor;
        TextView mBookCity;
        ImageView mBookThumbnail;

        public MyViewHolder(View itemView) {
            super(itemView);
            mBookTitle = itemView.findViewById(R.id.rv_book_lent_title);
            mBookAuthor = itemView.findViewById(R.id.rv_book_lent_author);
            mBookCity = itemView.findViewById(R.id.rv_book_lent_city);
            mBookThumbnail = itemView.findViewById(R.id.rv_book_thumbnail_lent);
        }

        public void bind (Book book, int position) {

        }
    }
}
