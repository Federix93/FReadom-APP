package com.example.android.lab1.ui;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ImageGalleryAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BookPhoto;
import com.example.android.lab1.model.Condition;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.listeners.OnPhotoClickListener;
import com.example.android.lab1.ui.listeners.OnPhotoClickListenerDefaultImpl;
import com.example.android.lab1.ui.profile.GlobalShowProfileActivity;
import com.example.android.lab1.utils.ChatManager;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.viewmodel.OpenedChatViewModel;
import com.example.android.lab1.viewmodel.UserViewModel;
import com.example.android.lab1.viewmodel.ViewModelFactory;
import com.firebase.ui.auth.ui.ProgressDialogHolder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.view.View.GONE;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class BookDetailsActivity extends AppCompatActivity {

    ConstraintLayout mProfileConstraintLayout;
    LinearLayout mBookDescriptionLayout;
    LinearLayout mFavoriteContainer;
    LinearLayout mShareContainer;
    RelativeLayout mGalleryLayout;
    Toolbar mToolbar;
    TextView mBookTitleTextView;
    TextView mAuthorTextView;
    TextView mEditorTextView;
    TextView mPublicationDateTextView;
    TextView mGalleryTextView;
    ImageView mBookThumbnailImageView;
    TextView mUsernameTextView;
    TextView mRatingTextView;
    ImageView mUserImageView;
    ImageView mStarImageView;
    AppCompatButton mPreviewButton;
    AppCompatButton mBookButton;
    ImageView mShareImageView;
    ImageView mFavoritesImageView;
    TextView mFavoriteText;
    TextView mBookDetailCondition;
    TextView mBookDescription;
    ImageView mBookDetailConditionColor;
    FirebaseFirestore mFirebaseFirestore;
    FirebaseAuth mFirebaseAuth;
    RecyclerView mGalleryRecyclerView;
    TextView mBookPosition;
    View mSeparatorDescriptionView;
    boolean isFromLink = false;

    private User mUser;
    private Book mBook;
    private ListenerRegistration mListenerRegistration;

    private GeoCodingTask mCurrentlyExecuting;
    private Location mCurrentlyResolving;
    private Location mResolveLater;

    private Activity mSelf;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        mBookTitleTextView = findViewById(R.id.book_title);
        mAuthorTextView = findViewById(R.id.book_author);
        mEditorTextView = findViewById(R.id.book_editor);
        mPublicationDateTextView = findViewById(R.id.book_publication_date);
        mBookThumbnailImageView = findViewById(R.id.book_thumbnail);
        mUsernameTextView = findViewById(R.id.name_book_owner);
        mRatingTextView = findViewById(R.id.rating_book_owner);
        mUserImageView = findViewById(R.id.profile_image_book_detail);
        mStarImageView = findViewById(R.id.star_book_owner);
        mPreviewButton = findViewById(R.id.preview_button);
        mBookButton = findViewById(R.id.book_button);
        mShareImageView = findViewById(R.id.share_icon);
        mFavoritesImageView = findViewById(R.id.add_to_favorite);
        mBookDetailCondition = findViewById(R.id.book_detail_conditions);
        mBookDetailConditionColor = findViewById(R.id.book_detail_conditions_color);
        mGalleryTextView = findViewById(R.id.gallery_book_detail);
        mBookDescription = findViewById(R.id.book_description);
        mBookDescriptionLayout = findViewById(R.id.book_description_container);
        mGalleryLayout = findViewById(R.id.relative_gallery_layout);
        mFavoriteContainer = findViewById(R.id.add_to_favorite_container);
        mFavoriteText = findViewById(R.id.add_to_favorite_text);
        mBookPosition = findViewById(R.id.book_position);
        mSeparatorDescriptionView = findViewById(R.id.separator_view_description);
        mShareContainer = findViewById(R.id.share_container);

        mSelf = this;

        handleIntent(getIntent());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utilities.setupStatusBarColor(this);
        }

        mToolbar = findViewById(R.id.toolbar_book_detail);

        mToolbar.setTitle(R.string.book_detail_title);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mProfileConstraintLayout = findViewById(R.id.profile_detail);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mGalleryRecyclerView = findViewById(R.id.rv_images);
        mGalleryRecyclerView.setHasFixedSize(true);
        mGalleryRecyclerView.setLayoutManager(layoutManager);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();


    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            isFromLink = true;
            String bookID = appLinkData.getLastPathSegment();
            FirebaseFirestore.getInstance().collection("books").document(bookID).get().addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    mBook = snapshot.toObject(Book.class);
                    if (mBook != null) {
                        if (!mBook.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                            updateFavoriteButton();
                            if (mBook.getLentTo() != null) {
                                mBookButton.setText(getResources().getString(R.string.book_not_available));
                                mBookButton.setEnabled(false);
                            } else {
                                checkIfChatExists();
                            }
                        }else{
                            Intent intent = new Intent(BookDetailsActivity.this, BookDetailsLibraryActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra("BookSelected", mBook);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Si è verificato un errore sconosciuto", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        } else {
            if (getIntent().getExtras() != null) {
                mBook = getIntent().getExtras().getParcelable("BookSelected");
                if (mBook != null) {
                    if (mBook.getLentTo() != null) {
                        mBookButton.setText(getResources().getString(R.string.book_not_available));
                        mBookButton.setEnabled(false);
                    } else {
                        checkIfChatExists();
                    }
                }
            } else {
                Toast.makeText(this, "Si è verificato un errore sconosciuto", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!isFromLink)
            updateFavoriteButton();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mListenerRegistration != null)
            mListenerRegistration.remove();
    }

    private void checkIfChatExists() {

        if(FirebaseAuth.getInstance().getUid() != null) {
            OpenedChatViewModel openedChatViewModel = ViewModelProviders.of(BookDetailsActivity.this, new ViewModelFactory(mBook.getBookID(), mBook.getUid())).get(OpenedChatViewModel.class);
            openedChatViewModel.getSnapshotLiveData().observe(BookDetailsActivity.this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (!aBoolean) {
                        mBookButton.setText(getResources().getString(R.string.book_request));
                    } else {
                        mBookButton.setText(getResources().getString(R.string.open_chat));
                    }
                    updateUI();
                }
            });
        }
        else{
            updateUI();
        }
    }

    private void updateFavoriteButton() {
        if (mFirebaseAuth.getUid() != null && mBook != null) {
            DocumentReference doc = mFirebaseFirestore.collection("favorites").document(mFirebaseAuth.getUid()).collection("books").document(mBook.getBookID());
            mListenerRegistration = doc.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (snapshot != null) {
                        if (snapshot.exists()) {
                            mFavoriteText.setText(getResources().getString(R.string.remove_from_favorites));
                            String uri = "@drawable/ic_favorite_orange_24dp";  // where myresource (without the extension) is the file
                            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                            Drawable res = getResources().getDrawable(imageResource);
                            mFavoritesImageView.setImageDrawable(res);
                        } else {
                            String uri = "@drawable/ic_favorite_border_orange_24dp";  // where myresource (without the extension) is the file
                            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                            Drawable res = getResources().getDrawable(imageResource);
                            mFavoritesImageView.setImageDrawable(res);
                            mFavoriteText.setText(getResources().getString(R.string.add_to_favorite));
                        }
                    }
                }
            });
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
        if (mBook.getDescription() != null) {
            mBookDescription.setText(mBook.getDescription());
        } else {
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
        if (!String.valueOf(mBook.getPublishYear()).isEmpty())
            mPublicationDateTextView.setText(String.valueOf(mBook.getPublishYear()));
        else
            mPublicationDateTextView.setText(getResources().getString(R.string.date_not_available));
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

        // storage photos
        if (mGalleryRecyclerView != null) {
            if (mBook.getUserBookPhotosStoragePath() != null && mBook.getUserBookPhotosStoragePath().size() > 0) {
                BookPhoto[] bookPhotos = new BookPhoto[mBook.getUserBookPhotosStoragePath().size()];
                for (int i = 0; i < mBook.getUserBookPhotosStoragePath().size(); i++) {
                    bookPhotos[i] = new BookPhoto(mBook.getUserBookPhotosStoragePath().get(i), mBook.getTitle());
                }
                OnPhotoClickListener photoClickListener = new OnPhotoClickListenerDefaultImpl(BookDetailsActivity.this,
                        mBook.getUserBookPhotosStoragePath(),
                        mBook.getTitle());
                mGalleryRecyclerView.setAdapter(new ImageGalleryAdapter(bookPhotos,
                        getApplicationContext(),
                        photoClickListener));
            } else {
                mGalleryLayout.setVisibility(GONE);
            }
        }
        if (mBook.getUid() != null) {
            UserViewModel userViewModel = ViewModelProviders.of(this, new ViewModelFactory(mBook.getUid())).get(UserViewModel.class);
            userViewModel.getSnapshotLiveData().observe(this, new Observer<User>() {
                @Override
                public void onChanged(@Nullable User user) {
                    mUser = user;
                    if (mUser != null) {
                        if (mUser.getImage() == null) {
                            Glide.with(getApplicationContext()).load(R.mipmap.profile_picture)
                                    .apply(bitmapTransform(new CircleCrop()))
                                    .into(mUserImageView);
                        } else {
                            Glide.with(getApplicationContext()).load(mUser.getImage())
                                    .apply(bitmapTransform(new CircleCrop()))
                                    .into(mUserImageView);
                        }
                        if (mUser.getUsername() != null) {
                            mUsernameTextView.setText(mUser.getUsername());
                        }
                        if (Float.compare(mUser.getRating(), 0) != 0) {
                            mRatingTextView.setText(String.format(Locale.getDefault(),
                                    "%.1f", mUser.getRating()));
                            Glide.with(BookDetailsActivity.this).load(R.drawable.ic_star_orange_24dp).into(mStarImageView);
                        } else {
                            mRatingTextView.setText(getResources().getString(R.string.not_rated_yet));
                            Glide.with(BookDetailsActivity.this)
                                    .load(R.drawable.ic_star_empty_24dp)
                                    .into(mStarImageView);
                        }
                        setupOnClickListeners();
                    } else {
                        Toast.makeText(getApplicationContext(), "Si è verificato un errore sconosciuto", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }
    }

    private void setupOnClickListeners() {

        if (mBook != null && mBook.getInfoLink() != null) {
            mPreviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    builder.setToolbarColor(v.getContext()
                            .getResources()
                            .getColor(R.color.colorPrimary));
                    customTabsIntent.launchUrl(v.getContext(), Uri.parse(mBook.getInfoLink()));
                }
            });
        } else {
            mPreviewButton.setEnabled(false);
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.book_detail_linear_layout_container),
                    R.string.preview_not_available, Snackbar.LENGTH_LONG).setDuration(3000);
        }

        //if (mBook.getDescription() != null) {
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
        /*} else {
            mBookDescriptionLayout.setVisibility(GONE);
        }*/

        mBookButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mFirebaseAuth.getUid() != null) {
                    final ProgressDialogHolder progressDialogHolder = new ProgressDialogHolder(v.getContext());
                    progressDialogHolder.showLoadingDialog(R.string.fui_progress_dialog_loading);
                    DocumentReference documentReference = mFirebaseFirestore.collection("books").document(mBook.getBookID());
                    documentReference.get().addOnCompleteListener(BookDetailsActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                mBook = task.getResult().toObject(Book.class);
                                if(mBook != null) {
                                    if (mBook.getLentTo() == null) {
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("FieldFilled", true);
                                        mFirebaseFirestore.collection("requestsDone").document(mFirebaseAuth.getUid()).set(data);
                                        DocumentReference reqDocRef = mFirebaseFirestore.collection("requestsDone").document(mFirebaseAuth.getUid()).collection("books").document(mBook.getBookID());
                                        reqDocRef.set(mBook, SetOptions.merge());
                                        final DocumentReference reqReceivedDocRef = mFirebaseFirestore.collection("requestsReceived").document(mBook.getUid()).collection("books").document(mBook.getBookID());
                                        reqReceivedDocRef.set(mBook, SetOptions.merge());
                                        new ChatManager(mBook.getBookID(), mBook.getUid(), mBook.getTitle(), getApplicationContext());
                                    } else {
                                        if (progressDialogHolder.isProgressDialogShowing())
                                            progressDialogHolder.dismissDialog();
                                        Snackbar.make(v, getResources().getString(R.string.already_lent), Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                                if (progressDialogHolder.isProgressDialogShowing())
                                    progressDialogHolder.dismissDialog();
                            } else {
                                if (progressDialogHolder.isProgressDialogShowing())
                                    progressDialogHolder.dismissDialog();
                                Snackbar.make(v, "Error", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });


                } else {
                    Intent intent = new Intent(getApplicationContext(), SignInPostponedActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }
        });
        mProfileConstraintLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GlobalShowProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("UserObject", mUser);
                intent.putExtra("UserID", mBook.getUid());
                startActivity(intent);
            }
        });

        mShareContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, Uri.parse("http://freadom.com/book/" + mBook.getBookID()).toString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        mFavoriteContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFirebaseAuth.getUid() != null) {
                    final DocumentReference documentReference = mFirebaseFirestore.collection("favorites").document(mFirebaseAuth.getUid()).collection("books").document(mBook.getBookID());
                    documentReference.get().addOnSuccessListener(BookDetailsActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {
                            if (snapshot != null) {
                                if (snapshot.exists()) {
                                    documentReference.delete();
                                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.book_detail_linear_layout_container),
                                            R.string.book_removed, Snackbar.LENGTH_LONG).setDuration(4000);
                                    mySnackbar.setAction(R.string.go_favorite, new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getApplicationContext(), FavoriteBooksActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            startActivity(intent);
                                        }
                                    });
                                    mySnackbar.show();
                                } else {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("FieldFilled", true);
                                    mFirebaseFirestore.collection("favorites").document(mFirebaseAuth.getUid()).set(data);
                                    addBookToFavorite();
                                }
                            } else {
                                Map<String, Object> data = new HashMap<>();
                                data.put("FieldFilled", true);
                                mFirebaseFirestore.collection("favorites").document(mFirebaseAuth.getUid()).set(data);
                                addBookToFavorite();
                            }
                        }
                    });
                } else {
                    Intent intent = new Intent(getApplicationContext(), SignInPostponedActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }
        });
    }

    private void addBookToFavorite() {
        final DocumentReference reqDocRef = mFirebaseFirestore.collection("favorites").document(mFirebaseAuth.getUid()).collection("books").document(mBook.getBookID());
        reqDocRef.set(mBook, SetOptions.merge());

        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.book_detail_linear_layout_container),
                R.string.book_added, Snackbar.LENGTH_LONG).setDuration(3000);
        mySnackbar.setAction(R.string.go_favorite, new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FavoriteBooksActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        mySnackbar.show();
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
