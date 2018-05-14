package com.example.android.lab1.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.ui.BookDetailsActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class RecyclerBorrowedBooksAdapter extends RecyclerView.Adapter<RecyclerBorrowedBooksAdapter.BorrowedBooksViewHolder> {

    private List<Book> mBookList;

    public RecyclerBorrowedBooksAdapter(List<Book> bookList){
        mBookList = bookList;
    }

    @NonNull
    @Override
    public BorrowedBooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_book_item_profile, parent, false);
        return new BorrowedBooksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowedBooksViewHolder holder, int position) {
        holder.bind(mBookList.get(position));
    }

    @Override
    public int getItemCount() {
        if(mBookList != null)
            return mBookList.size();
        else
            return 0;
    }

    public class BorrowedBooksViewHolder extends RecyclerView.ViewHolder{

        TextView mBookTitle;
        TextView mBookEditor;
        TextView mBookCity;
        ImageView mBookThumbnail;

        public BorrowedBooksViewHolder(View itemView) {
            super(itemView);
            mBookTitle = itemView.findViewById(R.id.rv_book_title);
            mBookEditor = itemView.findViewById(R.id.rv_book_editor);
            mBookCity = itemView.findViewById(R.id.rv_book_city);
            mBookThumbnail = itemView.findViewById(R.id.rv_book_thumbnail);
        }

        public void bind(Book book){

            mBookTitle.setText(book.getTitle());
            mBookEditor.setText(book.getPublisher());
            mBookCity.setText(book.getAddress());
            if (book.getWebThumbnail() != null) {
                Glide.with(itemView.getContext()).load(book.getWebThumbnail()).into(mBookThumbnail);
            } else if (book.getUserBookPhotosStoragePath() != null && book.getUserBookPhotosStoragePath().size() > 0) {
                StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUserBookPhotosStoragePath().get(0));
                Glide.with(itemView.getContext()).load(storage).into(mBookThumbnail);
            } else
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_no_book_photo)).into(mBookThumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }
    }
}
