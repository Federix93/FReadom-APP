package com.example.android.lab1.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.android.lab1.ui.BookDetailsActivity;
import com.example.android.lab1.ui.chat.ConversationsActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class RecyclerDashboardLibraryAdapter extends RecyclerView.Adapter<RecyclerDashboardLibraryAdapter.MyViewHolder> {

    private List<Book> mBooks;
    private List<String> mBookIds;
    Context mContext;

    public RecyclerDashboardLibraryAdapter (List<Book> books, List<String> mBookIds) {
        this.mBooks = books;
        this.mBookIds = mBookIds;
    }
    public  RecyclerDashboardLibraryAdapter (List<Book> books) {
        this.mBooks = books;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View cardView = inflater.inflate(R.layout.recycler_book_item_dashboard, parent, false);

        return new MyViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(mBooks.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mBookTitle;
        TextView mBookAuthor;
        TextView mBookCity;
        ImageView mBookThumbnail;
        ImageView mEditButton;
        ImageView mDeleteButton;
        ImageView mChatButton;
        TextView mNotification;

        public MyViewHolder (View itemView) {
            super (itemView);
            mBookTitle = itemView.findViewById(R.id.rv_book_dash_title);
            mBookAuthor = itemView.findViewById(R.id.rv_book_dash_author);
            mBookCity = itemView.findViewById(R.id.rv_book_dash_city);
            mBookThumbnail = itemView.findViewById(R.id.rv_book_thumbnail_dash_library);
            mEditButton = itemView.findViewById(R.id.dash_edit_item);
            mDeleteButton = itemView.findViewById(R.id.dash_delete_item);
            mChatButton = itemView.findViewById(R.id.dash_chat_item);
            mNotification = itemView.findViewById(R.id.dash_chat_notifications);
        }
        public void bind(Book book, final int position){

            mBookTitle.setText(book.getTitle());
            mBookTitle.setTextColor(mContext.getResources().getColor(R.color.black));
            mBookAuthor.setText(book.getAuthors());
            if (book.getAddress()==null) {
                mBookCity.setText(mContext.getResources().getString(R.string.position_not_available));
            } else {
                mBookCity.setText(book.getAddress());
            }
            if (book.getWebThumbnail() != null) {
                Glide.with(itemView.getContext()).load(book.getWebThumbnail()).into(mBookThumbnail);
            } else if (book.getUserBookPhotosStoragePath() != null && book.getUserBookPhotosStoragePath().size() > 0) {
                StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUserBookPhotosStoragePath().get(0));
                Glide.with(itemView.getContext()).load(storage).into(mBookThumbnail);
            } else
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_no_book_photo)).into(mBookThumbnail);

            mChatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent intent = new Intent(v.getContext(), BookDetailsActivity.class);
                    Intent intent = new Intent(v.getContext(), ConversationsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("ID_BOOK_SELECTED", mBookIds.get(position));
                    v.getContext().startActivity(intent);
                }
            });

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),"Function not implemented yet", Toast.LENGTH_SHORT).show();
                }
            });

            mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),"Function not implemented yet", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }
}
