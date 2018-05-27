package com.example.android.lab1.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ImageGalleryAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BookPhoto;
import com.example.android.lab1.model.Condition;
import com.example.android.lab1.utils.Utilities;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import static android.view.View.GONE;

public class BookDetailsLibraryActivity extends AppCompatActivity{

    private String mBookId;
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

    AppCompatButton mEditButton;
    AppCompatButton mDeleteButton;
    android.support.v7.widget.Toolbar mToolbar;

    RecyclerView mGalleryRecyclerView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail_library);

        mBookId = getIntent().getExtras().getString("bookId");

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

        mToolbar = findViewById(R.id.toolbar_library_book_detail);

        mToolbar.setTitle(R.string.app_name);
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

        if (mBookId != null) {

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("books").document(mBookId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        mBook = documentSnapshot.toObject(Book.class);
                        if (mBook != null) {
                            updateUI (mBook);
                        }
                    }
                }
            });

        }
    }

    private void updateUI(Book book) {
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


        if (mGalleryRecyclerView != null) {
            if (book.getUserBookPhotosStoragePath() != null && book.getUserBookPhotosStoragePath().size() > 0) {
                BookPhoto[] bookPhotos = new BookPhoto[book.getUserBookPhotosStoragePath().size()];
                for (int i = 0; i < book.getUserBookPhotosStoragePath().size(); i++) {
                    bookPhotos[i] = new BookPhoto(book.getUserBookPhotosStoragePath().get(i), book.getTitle());
                }
                mGalleryRecyclerView.setAdapter(new ImageGalleryAdapter(bookPhotos, getApplicationContext()));
            } else {
                mGalleryLayout.setVisibility(GONE);
            }
        }

    }
}
