package com.example.android.lab1.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.example.android.lab1.ui.BookDetailsActivity;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.example.android.lab1.utils.FetchAddressIntentService;
import com.firebase.ui.auth.ui.ProgressDialogHolder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class RecyclerFragmentRequestsDoneAdapter extends RecyclerView.Adapter<RecyclerFragmentRequestsDoneAdapter.MyViewHolder>
        implements AddressResultReciever {

    private List<Book> mBookList;
    private List<User> mUsersOwner;
    private List<String> mCities;

    private Context mContext;
    private Activity mContainer;
    private DatabaseReference userReference;
    private FirebaseDatabase firebaseDatabase;

    private GeoCodingTask mCurrentlyExecuting;
    private Location mCurrentlyResolving;
    private Location mResolveLater;
    private AddressReciever mAddressReciever;
    private boolean isLent;
    private boolean bookIsPresent;
    private int counterSize;

    public RecyclerFragmentRequestsDoneAdapter(Activity container, List<Book> listBooks, List<User> users) {
        mContainer = container;
        mBookList = listBooks;
        mUsersOwner = users;
        mCities = new ArrayList<>(mBookList != null?mBookList.size() : 0);
        for (int i = 0; i < mBookList.size(); i++) {
            mCities.add("");
        }
        isLent = false;
        bookIsPresent = false;
        counterSize = 0;
    }

    @NonNull
    @Override
    public RecyclerFragmentRequestsDoneAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View cardView = inflater.inflate(R.layout.recycler_book_item_req_done, parent, false);
        return new MyViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerFragmentRequestsDoneAdapter.MyViewHolder holder, int position) {
        holder.bind(mBookList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mBookList.size();
    }

    public void setItems(List<Book> listBooks, List<User> usersOwner) {
        mBookList = listBooks;
        mUsersOwner = usersOwner;
        mCities = new ArrayList<>(mBookList != null?mBookList.size() : 0);
        for (int i = 0; i < mBookList.size(); i++) {
            mCities.add("");
        }
    }

    @Override
    public void onPositionResolved(String address, boolean isCity, int position) {
        if (position >= 0 && position < mCities.size()) {
            mCities.set(position, address);
            notifyItemChanged(position);
        }
    }

    private void resolveCityLocation(Location location, int position) {
        // This method will change address mResultAddress var
        Intent intent = new Intent(mContainer, FetchAddressIntentService.class);
        if (mAddressReciever == null)
            mAddressReciever = new AddressReciever(new Handler(), this);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mAddressReciever);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location);
        intent.putExtra(FetchAddressIntentService.Constants.RESOLVE_CITY, true);
        intent.putExtra(FetchAddressIntentService.Constants.ADAPTER_POSITION, position);
        mContainer.startService(intent);

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mBookTitle;
        TextView mBookAuthor;
        TextView mBookCity;
        ImageView mBookThumbnail;
        LinearLayout mChatLayout;
        ImageView mUserPhoto;
        ImageView mThreeDotsMenu;

        public MyViewHolder(View itemView) {
            super(itemView);
            mBookTitle = itemView.findViewById(R.id.rv_book_lent_title);
            mBookAuthor = itemView.findViewById(R.id.rv_book_lent_author);
            mBookCity = itemView.findViewById(R.id.rv_book_lent_city);
            mBookThumbnail = itemView.findViewById(R.id.rv_book_thumbnail_lent);
            mChatLayout = itemView.findViewById(R.id.open_chat_books_lent);
            mUserPhoto = itemView.findViewById(R.id.book_owner_profile_picture);
            mThreeDotsMenu = itemView.findViewById(R.id.three_dots_menu);

            firebaseDatabase = FirebaseDatabase.getInstance();
            userReference = firebaseDatabase.getReference().child("users");
        }

        public void bind(final Book book, final int position) {
            mBookTitle.setText(book.getTitle());
            mBookTitle.setTextColor(mContext.getResources().getColor(R.color.black));
            mBookAuthor.setText(book.getAuthors());
            if (book.getGeoPoint() != null) {
                if (mCities.get(position) != null && mCities.get(position).equals("")) {
                    Location location = new Location("location");
                    location.setLongitude(book.getGeoPoint().getLongitude());
                    location.setLatitude(book.getGeoPoint().getLatitude());

                    resolveCityLocation(location, getAdapterPosition());
                }
                else
                    mBookCity.setText(mCities.get(position));

            } else
                mBookCity.setText(mContext.getResources().getString(R.string.position_not_available));
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), BookDetailsActivity.class);
                    intent.putExtra("BookSelected", mBookList.get(position));
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    v.getContext().startActivity(intent);
                }
            });

            mThreeDotsMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // inflate menu
                    PopupMenu popup = new PopupMenu(v.getContext(),v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.three_dot_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
                    popup.show();
                }
            });

        }
    }
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        private int position;
        public MyMenuItemClickListener(int positon) {
            this.position=positon;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {

                case R.id.delete_item:
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(mContext.getResources().getString(R.string.confirm_request_delete));
                    builder.setMessage(mContext.getResources().getString(R.string.confirm_request_delete_message));
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final ProgressDialogHolder progressDialogHolder = new ProgressDialogHolder(mContext);
                            progressDialogHolder.showLoadingDialog(R.string.end_loan_progress);

                            final String ownerId, bookId;
                            bookId = mBookList.get(position).getBookID();
                            ownerId = mBookList.get(position).getUid();
                            FirebaseFirestore reqDoneRef = FirebaseFirestore.getInstance();
                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                            reqDoneRef.collection("requestsDone").document(firebaseAuth.getUid()).collection("books")
                                    .document(bookId).delete();

                            FirebaseFirestore.getInstance().collection("requestsDone").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    final List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                                    bookIsPresent = false;
                                    for(DocumentSnapshot d : snapshotList){
                                        if(bookIsPresent) {
                                            counterSize = 0;
                                            bookIsPresent = false;
                                            break;
                                        }
                                        d.getReference().collection("books").document(bookId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot snapshot) {
                                                counterSize++;
                                                if (snapshot.exists()) {
                                                    bookIsPresent = true;
                                                }
                                                if(counterSize == snapshotList.size()){
                                                    if(!bookIsPresent){
                                                        FirebaseFirestore.getInstance().collection("requestsReceived")
                                                                .document(FirebaseAuth.getInstance().getUid())
                                                                .collection("books").document(bookId).delete();
                                                    }
                                                }
                                            }
                                        });

                                    }
                                }
                            });

                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            final DatabaseReference chatRef = firebaseDatabase.getReference("chats");
                            final DatabaseReference messagesRef = firebaseDatabase.getReference("messages");
                            final DatabaseReference conversationsRef = firebaseDatabase.getReference("conversations");

                            final DatabaseReference openedChats = FirebaseDatabase.getInstance().getReference("openedChats")
                                    .child(bookId)
                                    .child(ownerId)
                                    .child(firebaseAuth.getUid());

                            openedChats.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String chatId = (String) dataSnapshot.getValue();

                                    messagesRef.child(chatId).getRef().removeValue();
                                    conversationsRef.child(bookId).child(chatId).removeValue();
                                    chatRef.child(chatId).removeValue();
                                    openedChats.removeValue();

                                    if(progressDialogHolder.isProgressDialogShowing())
                                        progressDialogHolder.dismissDialog();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create();
                    builder.show();
            }
            return false;
        }
    }

    private class GeoCodingTask extends AsyncTask<LatLng, String, String> {

        private TextView mTarget;

        public GeoCodingTask(TextView target) {
            this.mTarget = target;
        }

        @Override
        protected String doInBackground(LatLng... latLngs) {
            if (latLngs.length > 0) {
                Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                List<android.location.Address> fromLocation = null;
                try {
                    if (!isCancelled())
                        fromLocation = geocoder.getFromLocation(latLngs[0].latitude,
                                latLngs[0].longitude,
                                1);
                    else
                        return null;

                } catch (IOException e) {
                    return null;
                }
                if (fromLocation == null || fromLocation.size() == 0)
                    return null;
                else {
                    return fromLocation.get(0).getLocality();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String city) {
            if (mTarget != null && !this.isCancelled()) {
                if (city == null) {
                    mTarget.setText(R.string.no_address_found);
                } else {
                    mTarget.setText(city);
                    mCurrentlyExecuting = null;
                    mCurrentlyResolving = null;
                    mResolveLater = null;
                }
            }

        }
    }
}
