package com.example.android.lab1.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.Review;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.HistoryActivity;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.example.android.lab1.ui.listeners.RatingActivityOpener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class RecyclerHistoryAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_BOOK_LENT = 1;
    private static final int VIEW_TYPE_BOOK_BORROWED = 2;

    Activity mContext;
    List<Book> mBooks;

    public RecyclerHistoryAdapter(Activity contex, List<Book> books) {
        this.mContext = contex;
        this.mBooks = books;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);

        if (viewType == VIEW_TYPE_BOOK_BORROWED){
            return new BorrowedViewHolder(view);
        } else if (viewType == VIEW_TYPE_BOOK_LENT) {
            return new LentViewHolder(view);
        } else
            return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Book book = mBooks.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_BOOK_BORROWED:
                ((BorrowedViewHolder) holder).bind(book);
                break;
            case VIEW_TYPE_BOOK_LENT:
                ((LentViewHolder) holder).bind(book);
        }
    }


    @Override
    public int getItemCount() {
        return mBooks.size();
    }
    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Book book = mBooks.get(position);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth != null && firebaseAuth.getUid() != null) {
            if (book.getUid().equals(firebaseAuth.getUid())) {
                return VIEW_TYPE_BOOK_BORROWED;
            } else {
                return VIEW_TYPE_BOOK_LENT;
            }
        } else {
            return 0;
        }
    }

    public void setItems(List<Book> booksLent) {
        this.mBooks = booksLent;
    }

    private class BorrowedViewHolder extends RecyclerView.ViewHolder {
        TextView mHistoryVerb;
        ImageView mHistoryArrow;
        TextView mHistoryBookTitle;
        ImageView mHistoryBookThumbnail;
        TextView mHistoryToUser;
        TextView mHistoryUser;
        ImageView mHistoryUserImage;
        TextView mHistoryLeaveRating;
        TextView mHistoryDateFrom;
        TextView mHistoryDateTo;

        private BorrowedViewHolder(View itemView) {
            super(itemView);
            mHistoryVerb = itemView.findViewById(R.id.history_verb);
            mHistoryArrow = itemView.findViewById(R.id.history_arrow);
            mHistoryBookTitle = itemView.findViewById(R.id.history_title);
            mHistoryBookThumbnail = itemView.findViewById(R.id.history_thumbnail);
            mHistoryToUser = itemView.findViewById(R.id.history_to_user);
            mHistoryUser = itemView.findViewById(R.id.history_username);
            mHistoryUserImage = itemView.findViewById(R.id.history_user_image);
            mHistoryLeaveRating = itemView.findViewById(R.id.history_leave_rating);
            mHistoryDateFrom = itemView.findViewById(R.id.history_from_date);
            mHistoryDateTo = itemView.findViewById(R.id.history_to_date);
        }

        void bind (final Book book) {
            mHistoryVerb.setText(R.string.you_borrowed);
            //mHistoryBookTitle.setText(book.getTitle());
            String uri = "@drawable/ic_fast_forward_orange_24dp";  // where myresource (without the extension) is the file
            int imageResource = mContext.getResources().getIdentifier(uri, null, mContext.getPackageName());
            Drawable res = mContext.getResources().getDrawable(imageResource);
            mHistoryArrow.setImageDrawable(res);
            if (book.getWebThumbnail() != null) {
                if (!((Activity)mContext).isFinishing())
                    Glide.with(mContext).load(book.getWebThumbnail())
                            .apply(new RequestOptions().centerCrop())
                            .into(mHistoryBookThumbnail);
            }

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("users").document(book.getLentTo()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        mHistoryUser.setText(user.getUsername());
                        if (user.getImage() != null) {
                            if (!((Activity)mContext).isFinishing())
                                Glide.with(mContext).load(user.getImage())
                                        .apply(bitmapTransform(new CircleCrop()))
                                        .into(mHistoryUserImage);
                        }
                    }
                }
            });
            String dateFrom = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanStart())).toString();
            String dateTo = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanEnd())).toString();
            mHistoryDateFrom.setText(String.format(mContext.getResources().getString(R.string.from_date), dateFrom));
            mHistoryDateTo.setText(String.format(mContext.getResources().getString(R.string.to_date), dateTo));

            Query query = firebaseFirestore.collection("users").document(book.getLentTo())
                    .collection("ratings").whereEqualTo("bookId", book.getBookID());
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (!task.getResult().isEmpty()) {
                        List<Review> reviews = task.getResult().toObjects(Review.class);
                        for (Review r : reviews) {
                            if (r.getReviewerId().equals(FirebaseAuth.getInstance().getUid())) {
                                mHistoryLeaveRating.setText(mContext.getResources().getString(R.string.review_published));
                            } else {
                                mHistoryLeaveRating.setText(mContext.getResources().getString(R.string.review_exchange));
                                mHistoryLeaveRating.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        RatingActivityOpener ratingActivityOpener = new RatingActivityOpener(mContext, FirebaseAuth.getInstance().getUid(), book.getLentTo() , book.getBookID());
                                        ratingActivityOpener.onClick(v);
                                    }
                                });
                            }
                        }
                    } else {
                        mHistoryLeaveRating.setText(mContext.getResources().getString(R.string.review_exchange));
                        mHistoryLeaveRating.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                RatingActivityOpener ratingActivityOpener = new RatingActivityOpener(mContext, FirebaseAuth.getInstance().getUid(), book.getLentTo() , book.getBookID());
                                ratingActivityOpener.onClick(v);
                            }
                        });
                    }
                }
            });
        }
    }

    private class LentViewHolder extends RecyclerView.ViewHolder {
        TextView mHistoryVerb;
        ImageView mHistoryArrow;
        TextView mHistoryBookTitle;
        ImageView mHistoryBookThumbnail;
        TextView mHistoryToUser;
        TextView mHistoryUser;
        ImageView mHistoryUserImage;
        TextView mHistoryLeaveRating;
        TextView mHistoryDateFrom;
        TextView mHistoryDateTo;
        private LentViewHolder(View itemView) {
            super(itemView);
            mHistoryVerb = itemView.findViewById(R.id.history_verb);
            mHistoryArrow = itemView.findViewById(R.id.history_arrow);
            mHistoryBookTitle = itemView.findViewById(R.id.history_title);
            mHistoryBookThumbnail = itemView.findViewById(R.id.history_thumbnail);
            mHistoryToUser = itemView.findViewById(R.id.history_to_user);
            mHistoryUser = itemView.findViewById(R.id.history_username);
            mHistoryUserImage = itemView.findViewById(R.id.history_user_image);
            mHistoryLeaveRating = itemView.findViewById(R.id.history_leave_rating);
            mHistoryDateFrom = itemView.findViewById(R.id.history_from_date);
            mHistoryDateTo = itemView.findViewById(R.id.history_to_date);
        }

        void bind(final Book book) {
            mHistoryVerb.setText(R.string.you_lent);
            //mHistoryBookTitle.setText(book.getTitle());
            String uri = "@drawable/ic_fast_rewind_green_24dp";  // where myresource (without the extension) is the file
            int imageResource = mContext.getResources().getIdentifier(uri, null, mContext.getPackageName());
            Drawable res = mContext.getResources().getDrawable(imageResource);
            mHistoryArrow.setImageDrawable(res);
            if (book.getWebThumbnail() != null) {
                if (!((Activity)mContext).isFinishing())
                    Glide.with(mContext).load(book.getWebThumbnail())
                            .apply(new RequestOptions().centerCrop())
                            .into(mHistoryBookThumbnail);
            }

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("users").document(book.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        mHistoryUser.setText(user.getUsername());
                        if (user.getImage() != null) {
                            if (!((Activity)mContext).isFinishing())
                                Glide.with(mContext).load(user.getImage())
                                        .apply(bitmapTransform(new CircleCrop()))
                                        .into(mHistoryUserImage);
                        }
                    }
                }
            });
            String dateFrom = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanStart())).toString();
            String dateTo = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanEnd())).toString();
            mHistoryDateFrom.setText(String.format(mContext.getResources().getString(R.string.from_date), dateFrom));
            mHistoryDateTo.setText(String.format(mContext.getResources().getString(R.string.to_date), dateTo));

            Query query = firebaseFirestore.collection("users").document(book.getUid())
                    .collection("ratings").whereEqualTo("bookId", book.getBookID());
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (!task.getResult().isEmpty()) {
                        List<Review> reviews = task.getResult().toObjects(Review.class);
                        for (Review r : reviews) {
                            if (r.getReviewerId().equals(FirebaseAuth.getInstance().getUid())) {
                                mHistoryLeaveRating.setText(mContext.getResources().getString(R.string.review_published));
                            } else {
                                mHistoryLeaveRating.setText(mContext.getResources().getString(R.string.review_exchange));
                                mHistoryLeaveRating.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        RatingActivityOpener ratingActivityOpener = new RatingActivityOpener(mContext, FirebaseAuth.getInstance().getUid(), book.getUid() , book.getBookID());
                                        ratingActivityOpener.onClick(v);
                                    }
                                });
                            }
                        }
                    } else {
                        mHistoryLeaveRating.setText(mContext.getResources().getString(R.string.review_exchange));
                        mHistoryLeaveRating.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                RatingActivityOpener ratingActivityOpener = new RatingActivityOpener(mContext, FirebaseAuth.getInstance().getUid(), book.getUid() , book.getBookID());
                                ratingActivityOpener.onClick(v);
                            }
                        });
                    }
                }
            });
        }
    }
}
