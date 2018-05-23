package com.example.android.lab1.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BookPhoto;
import com.example.android.lab1.model.BooksBorrowed;
import com.example.android.lab1.model.Condition;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.profile.GlobalShowProfileActivity;
import com.example.android.lab1.utils.ChatManager;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.ListenerRegistration;

import static android.view.View.GONE;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class BookDetailsActivity extends AppCompatActivity {

    ConstraintLayout mProfileConstraintLayout;
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
    TextView mBookDetailCondition;
    TextView mBookDescription;
    ImageView mBookDetailConditionColor;
    FirebaseFirestore mFirebaseFirestore;
    FirebaseAuth mFirebaseAuth;

    private String mBookId;
    private User mUser;
    private ListenerRegistration mListenerRegistration;
    private Book mBook;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        mBookTitleTextView = findViewById(R.id.book_title_detail_activity);
        mAuthorTextView = findViewById(R.id.author);
        mEditorTextView = findViewById(R.id.editor);
        mPublicationDateTextView = findViewById(R.id.publication_date);
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

        mBookId = getIntent().getStringExtra("ID_BOOK_SELECTED");

        Utilities.setupStatusBarColor(this);
        //setupOnClickListeners();

        mToolbar = findViewById(R.id.toolbar_book_detail);

        mToolbar.setTitle(R.string.app_name);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mProfileConstraintLayout = findViewById(R.id.profile_detail);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.rv_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mFirebaseFirestore.setFirestoreSettings(settings);
        final DocumentReference docRef = mFirebaseFirestore.collection("books").document(mBookId);
        mListenerRegistration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                //Log.d("DEBUGPERSISTANCE", "onComplete: " + documentSnapshot.getMetadata()
                //        .isFromCache());
                mBook = documentSnapshot.toObject(Book.class);
                updateUI(mBook);
                if (mListenerRegistration != null)
                    mListenerRegistration.remove();
            }
        });

        mBookDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TextDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("BookDescription", "The book description goes on the back cover (for paperbacks) or the inside flap copy (for hard copies) and right below the price (on Amazon). It’s crucial that this short paragraph be right. There are so many examples of how book descriptions led to huge changes in sales, it’s incredible authors don’t spend more time getting it right. One of our favorite stories is Mark Edwards’ book, Killing Cupid.");
                intent.putExtra("Title", mBook.getTitle());
                startActivity(intent);
            }
        });


                /*.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Book book = task.getResult().toObject(Book.class);

                    updateUI(book);
                }
            }
        });*/
    }

    private void updateUI(final Book book) {
        if (book.getTitle() != null)
            mBookTitleTextView.setText(book.getTitle());
        else
            mBookTitleTextView.setText(getResources().getString(R.string.title_not_available));
        if (book.getAuthors() != null)
            mAuthorTextView.setText(book.getAuthors());
        else
            mAuthorTextView.setText(getResources().getString(R.string.author_not_available));
        if (book.getPublisher() != null)
            mEditorTextView.setText(book.getPublisher());
        else
            mEditorTextView.setText(getResources().getString(R.string.editor_not_available));
        if (!String.valueOf(book.getPublishYear()).isEmpty())
            mPublicationDateTextView.setText(String.valueOf(book.getPublishYear()));
        else
            mPublicationDateTextView.setText(getResources().getString(R.string.date_not_available));
        if (!String.valueOf(book.getCondition()).isEmpty()) {
            mBookDetailCondition.setText(String.format(getResources().getString(R.string.condition), Condition.getCondition(getApplicationContext(), book.getCondition())));
            mBookDetailConditionColor.setColorFilter(Condition.getConditionColor(getApplicationContext(), book.getCondition()));
        }
        if (book.getWebThumbnail() != null)
            Glide.with(this).load(book.getWebThumbnail())
                    .apply(new RequestOptions().centerCrop())
                    .into(mBookThumbnailImageView);
        else
            Glide.with(this).load(R.drawable.book_thumbnail_placeholder)
                    .apply(new RequestOptions().centerCrop())
                    .into(mBookThumbnailImageView);
        setupOnClickListeners(book);

        // storage photos
        RecyclerView recyclerView = findViewById(R.id.rv_images);
        if (recyclerView != null) {
            if (book.getUserBookPhotosStoragePath() != null && book.getUserBookPhotosStoragePath().size() > 0) {
                BookPhoto[] bookPhotos = new BookPhoto[book.getUserBookPhotosStoragePath().size()];
                for (int i = 0; i < book.getUserBookPhotosStoragePath().size(); i++) {
                    bookPhotos[i] = new BookPhoto(book.getUserBookPhotosStoragePath().get(i), book.getTitle());
                }
                recyclerView.setAdapter(new ImageGalleryAdapter(bookPhotos, getApplicationContext()));
            } else {
                recyclerView.setVisibility(GONE);
                mGalleryTextView.setVisibility(GONE);
            }
        }
        if (book.getUid() != null) {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            firebaseFirestore.setFirestoreSettings(settings);
            final DocumentReference docRef = firebaseFirestore.collection("users").document(book.getUid());
            docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                    if (snapshot.exists()) {
                        mUser = snapshot.toObject(User.class);
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
                            if (String.valueOf(mUser.getRating()) != null) {
                                mRatingTextView.setText(String.valueOf(mUser.getRating()));
                            }
                        }
                    }
                }
            });

            mProfileConstraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), GlobalShowProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("UserObject", mUser);
                    intent.putExtra("UserID", book.getUid());
                    startActivity(intent);
                }
            });
        }
    }

    private void setupOnClickListeners(final Book book) {

        if (book != null && book.getInfoLink() != null) {
            mPreviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    builder.setToolbarColor(v.getContext()
                            .getResources()
                            .getColor(R.color.colorPrimary));
                    customTabsIntent.launchUrl(v.getContext(), Uri.parse(book.getInfoLink()));
                }
            });
        } else
            mPreviewButton.setEnabled(false);

        mBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DocumentReference docRef;
                if(mFirebaseAuth.getUid() != null) {
                    docRef = mFirebaseFirestore.collection("borrowedBooks").document(mFirebaseAuth.getUid());
                    docRef.get().addOnCompleteListener(BookDetailsActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                BooksBorrowed booksBorrowed = documentSnapshot.toObject(BooksBorrowed.class);
                                if(booksBorrowed != null) {
                                    if(!booksBorrowed.getBooksID().contains(mBookId)) {
                                        booksBorrowed.getBooksID().add(mBookId);
                                        docRef.set(booksBorrowed);
                                    }
                                }
                                else{
                                    List<String> list = new ArrayList<>();
                                    list.add(mBookId);
                                    booksBorrowed = new BooksBorrowed(list);
                                    docRef.set(booksBorrowed);
                                }
                                new ChatManager(mBookId, mBook.getUid(), getApplicationContext());
                            }
                        }
                    });
                }
                else{
                    Snackbar.make(v, "Devi essere loggato", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        mShareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Function not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        mFavoritesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Function not implemented", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder> {

        private BookPhoto[] mBookPhoto;
        private Context mContext;

        public ImageGalleryAdapter(BookPhoto[] mBookPhoto, Context mContext) {
            this.mBookPhoto = mBookPhoto;
            this.mContext = mContext;
        }

        @Override
        public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View photoView = inflater.inflate(R.layout.book_detail_gallery_item, parent, false);
            int width = (parent.getMeasuredWidth() / 3) - 16;
            int height = parent.getMeasuredHeight();
            photoView.setLayoutParams(new RecyclerView.LayoutParams(width, height));

            ImageGalleryAdapter.MyViewHolder viewHolder = new ImageGalleryAdapter.MyViewHolder(photoView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ImageGalleryAdapter.MyViewHolder holder, int position) {

            BookPhoto bookPhoto = mBookPhoto[position];
            ImageView imageView = holder.mPhotoImageView;

            Glide.with(mContext)
                    .load(bookPhoto.getUrl())
                    .apply(new RequestOptions().override(400, 400).fitCenter()
                            .placeholder(R.drawable.ic_no_book_photo))
                    .into(imageView);
        }

        public int getItemCount() {
            return (mBookPhoto.length);
        }


        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ImageView mPhotoImageView;

            public MyViewHolder(View itemView) {
                super(itemView);
                mPhotoImageView = itemView.findViewById(R.id.iv_photo);
                itemView.setOnClickListener(this);
            }

            public void onClick(View view) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    BookPhoto bookPhoto = mBookPhoto[position];
                    Intent intent = new Intent(mContext, BookPhotoDetailActivity.class);
                    intent.putExtra(BookPhotoDetailActivity.BOOK_PHOTO, bookPhoto);
                    startActivity(intent);
                }
            }
        }
    }
}
