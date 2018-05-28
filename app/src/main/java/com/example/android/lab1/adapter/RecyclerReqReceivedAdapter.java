package com.example.android.lab1.adapter;

import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.ui.chat.ConversationsActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.List;

public class RecyclerReqReceivedAdapter extends RecyclerView.Adapter<RecyclerReqReceivedAdapter.ReqViewHolder> {

    List<Book> mBookList;

    public RecyclerReqReceivedAdapter(List<Book> bookList){
        mBookList = bookList;
    }

    public void setItems(List<Book> bookList){
        mBookList = bookList;
    }

    @NonNull
    @Override
    public ReqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_book_item_req_received, parent, false);
        return new ReqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReqViewHolder holder, int position) {
        holder.bind(mBookList.get(position));
    }

    @Override
    public int getItemCount() {
        return mBookList.size();
    }

    class ReqViewHolder extends RecyclerView.ViewHolder{

        ImageView thumbnailImageView;
        TextView titleTextView;
        TextView openChatsTextView;

        private ReqViewHolder(View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnail);
            titleTextView = itemView.findViewById(R.id.title_book_text);
            openChatsTextView = itemView.findViewById(R.id.open_chats);
        }

        private void bind(final Book book){
            if (book.getWebThumbnail() != null) {
                Glide.with(itemView.getContext()).load(book.getWebThumbnail()).into(thumbnailImageView);
            } else if (book.getUserBookPhotosStoragePath() != null && book.getUserBookPhotosStoragePath().size() > 0) {
                StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUserBookPhotosStoragePath().get(0));
                Glide.with(itemView.getContext()).load(storage).into(thumbnailImageView);
            } else
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_no_book_photo)).into(thumbnailImageView);

            titleTextView.setText(book.getTitle());
            openChatsTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ConversationsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("ID_BOOK_SELECTED", book.getBookID());
                    v.getContext().startActivity(intent);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "You're opening book", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
