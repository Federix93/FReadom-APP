package com.example.android.lab1.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ProfileBookAdapter extends RecyclerView.Adapter<ProfileBookAdapter.MyViewHolder> {

    private List<Book> mBooks;
    Context mContext;

    public ProfileBookAdapter (List<Book> books) {
        this.mBooks = books;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View cardView = inflater.inflate(R.layout.recycler_book_item_profile, parent, false);

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
        TextView mBookEditor;
        TextView mBookCity;
        ImageView mBookThumbnail;

        public MyViewHolder (View itemView) {
            super (itemView);
            mBookTitle = itemView.findViewById(R.id.rv_book_title);
            mBookEditor = itemView.findViewById(R.id.rv_book_editor);
            mBookCity = itemView.findViewById(R.id.rv_book_city);
            mBookThumbnail = itemView.findViewById(R.id.rv_book_thumbnail);
        }
        public void bind(Book book){

            mBookTitle.setText(book.getTitle());
            mBookTitle.setTextColor(mContext.getResources().getColor(R.color.black));
            mBookEditor.setText(book.getPublisher());
            mBookCity.setText(book.getAddress());
            if (book.getWebThumbnail() != null) {
                Glide.with(itemView.getContext()).load(book.getWebThumbnail()).into(mBookThumbnail);
            } else if (book.getUserBookPhotosStoragePath() != null && book.getUserBookPhotosStoragePath().size() > 0) {
                StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUserBookPhotosStoragePath().get(0));
                Glide.with(itemView.getContext()).load(storage).into(mBookThumbnail);
            } else
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_no_book_photo)).into(mBookThumbnail);

        }

    }
}
