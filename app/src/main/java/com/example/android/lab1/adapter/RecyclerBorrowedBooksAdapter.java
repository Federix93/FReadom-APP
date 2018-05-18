package com.example.android.lab1.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class RecyclerBorrowedBooksAdapter extends RecyclerView.Adapter<RecyclerBorrowedBooksAdapter.BorrowedBooksViewHolder> {

    private List<Book> mBookList;
    private List<String> mBookIds;
    private List<String> mChatIds;
    private List<String> mUsersIds;
    User mBookOwner;

    DatabaseReference userReference;
    FirebaseDatabase firebaseDatabase;
    Context mContext;

    public RecyclerBorrowedBooksAdapter(List<Book> bookList, List<String> booksId, List<String> chatIds, List<String> usersIds){
        mBookList = bookList;
        mBookIds = booksId;
        mChatIds = chatIds;
        mUsersIds = usersIds;
    }

    @NonNull
    @Override
    public BorrowedBooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recycler_book_item_dashboard, parent, false);
        return new BorrowedBooksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowedBooksViewHolder holder, int position) {
        holder.bind(mBookList.get(position), position);
    }

    @Override
    public int getItemCount() {
        if(mBookList != null)
            return mBookList.size();
        else
            return 0;
    }

    public void setItems(List<Book> listBooks, List<String> booksID, List<String> chatIDs, List<String> usersIds) {
        mBookList = listBooks;
        mBookIds = booksID;
        mChatIds = chatIDs;
        mUsersIds = usersIds;
    }

    public class BorrowedBooksViewHolder extends RecyclerView.ViewHolder{

        TextView mBookTitle;
        TextView mBookAuthor;
        TextView mBookCity;
        ImageView mBookThumbnail;
        ImageView mChatButton;
        TextView mNotification;
        ImageView mEditButton;
        ImageView mDeleteButton;


        public BorrowedBooksViewHolder(View itemView) {
            super(itemView);
            mBookTitle = itemView.findViewById(R.id.rv_book_dash_title);
            mBookAuthor = itemView.findViewById(R.id.rv_book_dash_author);
            mBookCity = itemView.findViewById(R.id.rv_book_dash_city);
            mBookThumbnail = itemView.findViewById(R.id.rv_book_thumbnail_dash_library);
            mEditButton = itemView.findViewById(R.id.dash_edit_item);
            mDeleteButton = itemView.findViewById(R.id.dash_delete_item);
            mChatButton = itemView.findViewById(R.id.dash_chat_item);
            mNotification = itemView.findViewById(R.id.dash_chat_notifications);
            firebaseDatabase = FirebaseDatabase.getInstance();
            userReference = firebaseDatabase.getReference().child("users");
        }

        public void bind(final Book book, final int position){

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
                public void onClick(final View v) {

                    userReference.child(mUsersIds.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mBookOwner = dataSnapshot.getValue(User.class);

                            Intent intent = new Intent(v.getContext(), ChatActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra("ChatID", mChatIds.get(position));
                            intent.putExtra("Username", mBookOwner.getUsername());
                            intent.putExtra("ImageURL", mBookOwner.getPhotoURL());
                            intent.putExtra("BookID", mBookIds.get(position));
                            v.getContext().startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });

            mDeleteButton.setVisibility(View.GONE);

            mEditButton.setVisibility(View.GONE);
        }
    }
}