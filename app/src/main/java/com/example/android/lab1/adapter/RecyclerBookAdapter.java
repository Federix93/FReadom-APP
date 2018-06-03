package com.example.android.lab1.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.ui.BookDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class RecyclerBookAdapter extends RecyclerView.Adapter<RecyclerBookAdapter.BookViewHolder> {

    private List<Book> books;
    private Integer mAnimationDuration;

    public RecyclerBookAdapter(List<Book> books) {
        if (this.books == null) {
            this.books = new ArrayList<>();
        }
        for (Book b : books) {
            if (!b.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                this.books.add(b);
            }
        }

    }

    public void updateItems(List<Book> books) {
        this.books = books;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_book_item, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BookViewHolder holder, int position) {
        /*if (mFilteredIds != null && mFilteredIds.contains(IDs.get(position)))
            holder.layoutHide();
        else */
        holder.bind(books.get(position));
        setFadeAnimation(holder.mRootView);

    }

    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        if (mAnimationDuration == null)
            mAnimationDuration = view.getContext().getResources().getInteger(R.integer.homepage_recycler_view_animation_duration);
        anim.setDuration(mAnimationDuration);
        view.startAnimation(anim);
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */

    @Override
    public int getItemCount() {
        if (books == null)
            return 0;
        return books.size();
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {

        View mRootView;
        TextView mTitle, mAuthor;
        ImageView mThumbnail;
        StorageReference mStorageReference;
        private ViewGroup.LayoutParams mOldParams;

        public BookViewHolder(final View itemView) {
            super(itemView);
            mRootView = itemView;
            mTitle = itemView.findViewById(R.id.title);
            mAuthor = itemView.findViewById(R.id.author);
            mThumbnail = itemView.findViewById(R.id.thumbnail);
            //mOverflow = itemView.findViewById(R.id.overflow);

        }

        void bind(final Book book) {
           /* mRootView.setVisibility(View.VISIBLE);
            if (mOldParams != null) mRootView.setLayoutParams(mOldParams); */
            mTitle.setText(book.getTitle());
            mAuthor.setText(book.getAuthors());
            if (book.getWebThumbnail() != null) {
                Glide.with(itemView.getContext()).load(book.getWebThumbnail()).into(mThumbnail);
            } else if (book.getUserBookPhotosStoragePath() != null && !book.getUserBookPhotosStoragePath().isEmpty()) {
                mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUserBookPhotosStoragePath().get(0));
                Glide.with(itemView.getContext()).load(mStorageReference).into(mThumbnail);
            } else
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_no_book_photo)).into(mThumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), BookDetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("BookSelected", book);
                    v.getContext().startActivity(intent);
                }
            });

            /*mOverflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(mOverflow);
                }
            });*/
        }

        /*private void layoutHide() {
            mOldParams = mRootView.getLayoutParams();
            mRootView.setVisibility(View.GONE);
            mRootView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        } */
    }


    /*private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.book_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }*/

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_view_preview:
                    return true;
                case R.id.action_add_wishlist:
                    return true;
                default:
            }
            return false;
        }
    }
}