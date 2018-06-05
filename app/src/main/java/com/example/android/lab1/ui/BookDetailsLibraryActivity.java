package com.example.android.lab1.ui;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ImageGalleryAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BookPhoto;
import com.example.android.lab1.model.Condition;
import com.example.android.lab1.ui.listeners.OnPhotoClickListener;
import com.example.android.lab1.ui.listeners.OnPhotoClickListenerDefaultImpl;
import com.example.android.lab1.utils.Utilities;
import com.firebase.ui.auth.ui.ProgressDialogHolder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;

public class BookDetailsLibraryActivity extends AppCompatActivity {

    private Book mBook;

    LinearLayout mBookDescriptionLayout;
    RelativeLayout mGalleryLayout;

    TextView mBookTitleTextView;
    TextView mAuthorTextView;
    TextView mEditorTextView;
    TextView mPublicationDateTextView;
    TextView mGalleryTextView;
    ImageView mBookThumbnailImageView;
    TextView mBookDescription;
    ImageView mBookDetailConditionColor;
    TextView mBookDetailCondition;
    TextView mBookPosition;
    View mSeparatorDescriptionView;
    View mSeparatorGlobalProfileView;
    View mSeparatorShareView;

    AppCompatButton mEditButton;
    AppCompatButton mDeleteButton;
    android.support.v7.widget.Toolbar mToolbar;

    RecyclerView mGalleryRecyclerView;

    private GeoCodingTask mCurrentlyExecuting;
    private Location mCurrentlyResolving;
    private Location mResolveLater;

    private Activity mSelf;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail_library);

        if (getIntent().getExtras() != null) {
            mBook = getIntent().getExtras().getParcelable("BookSelected");
        } else {
            Toast.makeText(this, "Si è verificato un errore sconosciuto", Toast.LENGTH_SHORT).show();
            finish();
        }

        mBookTitleTextView = findViewById(R.id.library_book_title);
        mAuthorTextView = findViewById(R.id.library_book_author);
        mEditorTextView = findViewById(R.id.library_book_editor);
        mPublicationDateTextView = findViewById(R.id.library_book_publication_date);
        mBookThumbnailImageView = findViewById(R.id.library_book_thumbnail);
        mEditButton = findViewById(R.id.library_edit_button);
        mDeleteButton = findViewById(R.id.library_delete_button);
        mBookDetailCondition = findViewById(R.id.library_book_detail_conditions);
        mBookDetailConditionColor = findViewById(R.id.library_book_detail_conditions_color);
        mGalleryTextView = findViewById(R.id.library_gallery_book_detail);
        mBookDescription = findViewById(R.id.library_book_description);
        mBookDescriptionLayout = findViewById(R.id.library_book_description_container);
        mGalleryLayout = findViewById(R.id.relative_library_gallery_layout);
        mBookPosition = findViewById(R.id.library_book_position);
        mSeparatorDescriptionView = findViewById(R.id.separator_desciption_library);
        mSelf = this;

        mToolbar = findViewById(R.id.toolbar_library_book_detail);

        mToolbar.setTitle(R.string.book_detail_title);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mGalleryRecyclerView = findViewById(R.id.rv_images);
        mGalleryRecyclerView.setHasFixedSize(true);
        mGalleryRecyclerView.setLayoutManager(layoutManager);

        Utilities.setupStatusBarColor(this);

        if (mBook != null) {
            updateUI();
        }
    }

    private void updateUI() {
        if (mBook.getTitle() != null)
            mBookTitleTextView.setText(mBook.getTitle());
        else
            mBookTitleTextView.setText(getResources().getString(R.string.title_not_available));
        if (mBook.getAuthors() != null)
            mAuthorTextView.setText(mBook.getAuthors());
        else
            mAuthorTextView.setText(getResources().getString(R.string.author_not_available));
        if (mBook.getPublisher() != null)
            mEditorTextView.setText(mBook.getPublisher());
        else
            mEditorTextView.setText(getResources().getString(R.string.editor_not_available));
        if (!String.valueOf(mBook.getPublishYear()).isEmpty())
            mPublicationDateTextView.setText(String.valueOf(mBook.getPublishYear()));
        else
            mPublicationDateTextView.setText(getResources().getString(R.string.date_not_available));
        if (mBook.getDescription() != null)
            mBookDescription.setText(mBook.getDescription());
        else {
            mSeparatorDescriptionView.setVisibility(GONE);
            mBookDescriptionLayout.setVisibility(GONE);
        }
        if (mBook.getGeoPoint() != null) {
            Location location = new Location("location");
            location.setLongitude(mBook.getGeoPoint().getLongitude());
            location.setLatitude(mBook.getGeoPoint().getLatitude());

            resolveCityLocation(location);
        } else
            mBookPosition.setText(getResources().getString(R.string.position_not_available));
        if (!String.valueOf(mBook.getCondition()).isEmpty()) {
            mBookDetailCondition.setText(String.format(getResources().getString(R.string.condition), Condition.getCondition(getApplicationContext(), mBook.getCondition())));
            mBookDetailConditionColor.setColorFilter(Condition.getConditionColor(getApplicationContext(), mBook.getCondition()));
        }
        if (mBook.getWebThumbnail() != null)
            Glide.with(this).load(mBook.getWebThumbnail())
                    .apply(new RequestOptions().centerCrop())
                    .into(mBookThumbnailImageView);
        else
            Glide.with(this).load(R.drawable.book_thumbnail_placeholder)
                    .apply(new RequestOptions().centerCrop())
                    .into(mBookThumbnailImageView);


        if (mGalleryRecyclerView != null) {
            if (mBook.getUserBookPhotosStoragePath() != null && mBook.getUserBookPhotosStoragePath().size() > 0) {
                BookPhoto[] bookPhotos = new BookPhoto[mBook.getUserBookPhotosStoragePath().size()];
                for (int i = 0; i < mBook.getUserBookPhotosStoragePath().size(); i++) {
                    bookPhotos[i] = new BookPhoto(mBook.getUserBookPhotosStoragePath().get(i), mBook.getTitle());
                }
                OnPhotoClickListener onPhotoClickListener = new OnPhotoClickListenerDefaultImpl(BookDetailsLibraryActivity.this,
                        mBook.getUserBookPhotosStoragePath(),
                        mBook.getTitle());
                mGalleryRecyclerView.setAdapter(new ImageGalleryAdapter(bookPhotos,
                        getApplicationContext(),
                        onPhotoClickListener));
            } else {
                mGalleryLayout.setVisibility(GONE);
            }
        }
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BookDetailsLibraryActivity.this,
                        LoadBookActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra(LoadBookActivity.BOOK, mBook);
                startActivity(i);
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (FirebaseAuth.getInstance() != null) {
                    final ProgressDialogHolder progressDialogHolder = new ProgressDialogHolder(v.getContext());
                    progressDialogHolder.showLoadingDialog(R.string.deleting_book_dialog);
                    final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("books").document(mBook.getBookID());
                    documentReference.get().addOnCompleteListener(BookDetailsLibraryActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                final Book book = task.getResult().toObject(Book.class);
                                if (book != null) {
                                    if (book.getLentTo() == null) {
                                        documentReference.delete();
                                        DocumentReference reqReceived = FirebaseFirestore.getInstance().collection("requestsReceived")
                                                .document(FirebaseAuth.getInstance().getUid())
                                                .collection("books")
                                                .document(book.getBookID());
                                        reqReceived.delete();
                                        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("requestsDone");
                                        collectionReference.get().addOnCompleteListener(BookDetailsLibraryActivity.this, new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot d : task.getResult().getDocuments()) {
                                                        d.getReference().collection("books").document(book.getBookID()).delete();
                                                    }
                                                }
                                            }
                                        });
                                        FirebaseDatabase.getInstance().getReference("conversations").child(book.getBookID()).removeValue();
                                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("openedChats").child(book.getBookID())
                                                .child(FirebaseAuth.getInstance().getUid());
                                        final List<String> chatIDs = new ArrayList<>();
                                        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                                    Log.d("LULLO", "ChatID: " + d.getValue());
                                                    chatIDs.add((String) d.getValue());
                                                }
                                                for (String s : chatIDs) {
                                                    FirebaseDatabase.getInstance().getReference("chats").child(s).removeValue();
                                                    Log.d("LULLO", "ChatID: " + s);
                                                }
                                                if (progressDialogHolder.isProgressDialogShowing())
                                                    progressDialogHolder.dismissDialog();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        Snackbar.make(v, "Il libro è attualmente in prestito. Impossibile eliminare il libro", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                if (progressDialogHolder.isProgressDialogShowing())
                                    progressDialogHolder.dismissDialog();
                                Toast.makeText(getApplicationContext(), "Qualcosa è andato storto", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        mBookDescriptionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TextDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("BookDescription", mBook.getDescription());
                intent.putExtra("Title", mBook.getTitle());
                startActivity(intent);
            }
        });

    }

    private void resolveCityLocation(Location location) {
        // This method will change address mResultAddress var
        mCurrentlyResolving = location;
        mCurrentlyExecuting = new GeoCodingTask(mBookPosition);
        mCurrentlyExecuting.execute(new LatLng(location.getLatitude(),
                location.getLongitude()));
    }

    private class GeoCodingTask extends AsyncTask<LatLng, String, String> {

        private TextView mTarget;

        public GeoCodingTask(TextView target) {
            this.mTarget = target;
        }

        @Override
        protected String doInBackground(LatLng... latLngs) {
            if (latLngs.length > 0) {
                Geocoder geocoder = new Geocoder(mSelf, Locale.getDefault());
                List<Address> fromLocation = null;
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
