package com.example.android.lab1.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BookPhoto;
import com.example.android.lab1.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class BookDetailsActivity extends AppCompatActivity {

    ImageView mImageView;
    ConstraintLayout mProfileConstraintLayout;
    Toolbar mToolbar;
    TextView mBookTitleTextView;
    TextView mAuthorTextView;
    TextView mEditorTextView;
    TextView mPublicationDateTextView;
    ImageView mBookThumbnailImageView;

    private String mBookId;

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

        mBookId = getIntent().getStringExtra("ID_BOOK_SELECTED");

        Window window = getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.background_app));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();

            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        mToolbar = findViewById(R.id.toolbar_book_detail);

        mToolbar.setTitle(R.string.app_name);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mImageView = findViewById(R.id.profile_image_book_detail);

        mProfileConstraintLayout = findViewById(R.id.profile_detail);

        mProfileConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GlobalShowProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        Glide.with(this).load(R.mipmap.profile_picture)
                .apply(bitmapTransform(new CircleCrop()))
                .into(mImageView);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        RecyclerView recyclerView = findViewById(R.id.rv_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        ImageGalleryAdapter adapter = new ImageGalleryAdapter(this, BookPhoto.getSpacePhotos());
        recyclerView.setAdapter(adapter);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);
        final DocumentReference docRef = firebaseFirestore.collection("books").document(mBookId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Book book = task.getResult().toObject(Book.class);
                    updateUI(book);
                }
            }
        });
    }


    private class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder> {

        private BookPhoto[] mBookPhoto;
        private Context mContext;

        @Override
        public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View photoView = inflater.inflate(R.layout.book_image_item, parent, false);
            int width = (parent.getMeasuredWidth() / 3) - 8;
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

            public MyViewHolder (View itemView) {
                super (itemView);
                mPhotoImageView = itemView.findViewById(R.id.iv_photo);
                itemView.setOnClickListener(this);
            }

            public void onClick (View view){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    BookPhoto bookPhoto = mBookPhoto[position];
                    Intent intent = new Intent(mContext, BookPhotoDetailActivity.class);
                    intent.putExtra(BookPhotoDetailActivity.BOOK_PHOTO, bookPhoto);
                    startActivity(intent);
                }
            }
        }

        public ImageGalleryAdapter (Context context, BookPhoto[] bookPhotos) {
            mContext = context;
            mBookPhoto = bookPhotos;
        }
    }

    private void updateUI(Book book) {
        if(book.getTitle() != null)
            mBookTitleTextView.setText(book.getTitle());
        if(book.getAuthor() != null)
            mAuthorTextView.setText(book.getAuthor());
        if(book.getPublisher() != null)
            mEditorTextView.setText(book.getPublisher());
        if(String.valueOf(book.getPublishYear()) != null)
            mPublicationDateTextView.setText(String.valueOf(book.getPublishYear()));
        if(book.getThumbnail() != null)
            Glide.with(this).load(book.getThumbnail())
                    .apply(new RequestOptions().centerCrop())
                    .into(mBookThumbnailImageView);
    }
}
