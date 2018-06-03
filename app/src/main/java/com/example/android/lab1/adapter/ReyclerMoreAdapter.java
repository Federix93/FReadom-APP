package com.example.android.lab1.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.ui.BookDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ReyclerMoreAdapter extends RecyclerView.Adapter<ReyclerMoreAdapter.BookViewHolder> {

    private List<Book> books;

    public ReyclerMoreAdapter(List<Book> books) {
        if (this.books == null) {
            this.books = new ArrayList<>();
        }
        for (Book b : books) {
            if (!b.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                this.books.add(b);
            }
        }

    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_book_item_more, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BookViewHolder holder, int position) {
        holder.bind(books.get(position));

    }

    @Override
    public int getItemCount() {
        if (books == null)
            return 0;
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {

        TextView mTitle, mAuthor;
        ImageView mThumbnail;
        StorageReference mStorageReference;

        BookViewHolder(final View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mAuthor = itemView.findViewById(R.id.author);
            mThumbnail = itemView.findViewById(R.id.thumbnail);

        }

        void bind(final Book book) {

            mTitle.setText(book.getTitle());
            mAuthor.setText(book.getAuthors());
            if (book.getWebThumbnail() != null) {
                Glide.with(itemView.getContext()).load(book.getWebThumbnail()).into(mThumbnail);
            } else if (book.getUserBookPhotosStoragePath() != null && !book.getUserBookPhotosStoragePath().isEmpty()) {
                mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUserBookPhotosStoragePath().get(0));
                Glide.with(itemView.getContext()).load(mStorageReference).into(mThumbnail);
            } else
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_no_book_photo)).into(mThumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), BookDetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("BookSelected", book);
                    v.getContext().startActivity(intent);
                }
            });

        }

    }





    public void addAll(List<Book> newBooks) {
        int initialSize = books.size();
        books.addAll(newBooks);
        notifyItemRangeInserted(initialSize, books.size());
    }
}