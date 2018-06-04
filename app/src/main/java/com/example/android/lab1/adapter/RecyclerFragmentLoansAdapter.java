package com.example.android.lab1.adapter;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.AddressReciever;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.example.android.lab1.utils.FetchAddressIntentService;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.security.acl.Owner;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class RecyclerFragmentLentAdapter extends RecyclerView.Adapter<RecyclerFragmentLentAdapter.MyViewHolder> {

    private List<Book> mBookList;
    private List<User> mUsersOwner;

    private Context mContext;
    private DatabaseReference userReference;
    private FirebaseDatabase firebaseDatabase;

    boolean isLent = false;

    public RecyclerFragmentLentAdapter(List<Book> listBooks, List<User> users) {
        mBookList = listBooks;
        mUsersOwner = users;
    }

    public RecyclerFragmentLentAdapter(List<Book> listBooks, List<User> users, boolean lent) {
        mBookList = listBooks;
        mUsersOwner = users;
        this.isLent = lent;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View cardView = inflater.inflate(R.layout.recycler_book_item_lent, parent, false);
        return new MyViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(mBookList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mBookList.size();
    }

    public void setItems(List<Book> listBooks, List<User> usersOwner) {
        mBookList = listBooks;
        mUsersOwner = usersOwner;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mBookTitle;
        TextView mBookAuthor;
        ImageView mBookThumbnail;
        LinearLayout mChatLayout;
        ImageView mUserPhoto;
        TextView mLentFromDate;
        TextView mLentToDate;

        public MyViewHolder(View itemView) {
            super(itemView);
            mBookTitle = itemView.findViewById(R.id.rv_book_lent_title);
            //mBookAuthor = itemView.findViewById(R.id.rv_book_lent_author);
            mBookThumbnail = itemView.findViewById(R.id.rv_book_thumbnail_lent);
            mChatLayout = itemView.findViewById(R.id.open_chat_books_lent);
            mUserPhoto = itemView.findViewById(R.id.book_owner_profile_picture);
            mLentFromDate = itemView.findViewById(R.id.lent_from_date);
            mLentToDate = itemView.findViewById(R.id.lent_to_date);

            firebaseDatabase = FirebaseDatabase.getInstance();
            userReference = firebaseDatabase.getReference().child("users");
        }

        public void bind(final Book book, final int position) {
            mBookTitle.setText(book.getTitle());
            mBookTitle.setTextColor(mContext.getResources().getColor(R.color.black));
            //mBookAuthor.setText(book.getAuthors());
//            if (book.getGeoPoint() != null) {
//                Location location = new Location("location");
//                location.setLongitude(book.getGeoPoint().getLongitude());
//                location.setLatitude(book.getGeoPoint().getLatitude());
//
//                resolveCityLocation(location);
//            } else
//                mBookCity.setText(mContext.getResources().getString(R.string.position_not_available));
            if (book.getWebThumbnail() != null) {
                Glide.with(itemView.getContext()).load(book.getWebThumbnail()).into(mBookThumbnail);
            } else if (book.getUserBookPhotosStoragePath() != null && book.getUserBookPhotosStoragePath().size() > 0) {
                StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUserBookPhotosStoragePath().get(0));
                Glide.with(itemView.getContext()).load(storage).into(mBookThumbnail);
            } else
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_no_book_photo)).into(mBookThumbnail);
            if (mUsersOwner.get(position).getPhotoURL() != null) {
                Glide.with(mContext).load(mUsersOwner.get(position).getPhotoURL())
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mUserPhoto);
            }

            String dateFrom = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanStart())).toString();
            String dateTo = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanEnd())).toString();
            mLentFromDate.setText(String.format(mContext.getResources().getString(R.string.from_date), dateFrom));
            mLentToDate.setText(String.format(mContext.getResources().getString(R.string.to_date), dateTo));

            if (isLent) {
                mChatLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("openedChats")
                                .child(book.getBookID())
                                .child(FirebaseAuth.getInstance().getUid())
                                .child(book.getLentTo());
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String chatID = (String) dataSnapshot.getValue();
                                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                Log.d("VINCI", "onDataChange:  username " + mUsersOwner.get(position).getUsername());
                                Log.d("VINCI", "onDataChange:  chatid " + chatID);
                                Log.d("VINCI", "onDataChange:  imageurl " + mUsersOwner.get(position).getPhotoURL());
                                Log.d("VINCI", "onDataChange:  BOOKID " + mBookList.get(position).getBookID());


                                intent.putExtra("ChatID", chatID);
                                intent.putExtra("Username", mUsersOwner.get(position).getUsername());
                                intent.putExtra("ImageURL", mUsersOwner.get(position).getPhotoURL());
                                intent.putExtra("BookID", mBookList.get(position).getBookID());
                                v.getContext().startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            } else {
                mChatLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("openedChats")
                                .child(book.getBookID())
                                .child(book.getUid())
                                .child(FirebaseAuth.getInstance().getUid());
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String chatID = (String) dataSnapshot.getValue();
                                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.putExtra("ChatID", chatID);
                                intent.putExtra("Username", mUsersOwner.get(position).getUsername());
                                intent.putExtra("ImageURL", mUsersOwner.get(position).getPhotoURL());
                                intent.putExtra("BookID", mBookList.get(position).getBookID());
                                v.getContext().startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        }

    }
}
