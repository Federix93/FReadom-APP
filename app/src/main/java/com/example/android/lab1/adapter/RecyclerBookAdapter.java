package com.example.android.lab1.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class RecyclerBookAdapter extends RecyclerView.Adapter<RecyclerBookAdapter.BookViewHolder> {


    private List<Book> books;

    public RecyclerBookAdapter(List<Book> books)
    {
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
        holder.bind(books.get(position));
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */

    @Override
    public int getItemCount() {
        if(books == null)
            return 0;
        return books.size();
    }

    public class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView mTitle, mAuthor;
        ImageView mThumbnail, mOverflow;
        StorageReference mStorageReference;

        public BookViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.title);
            mAuthor = itemView.findViewById(R.id.author);
            mThumbnail = itemView.findViewById(R.id.thumbnail);
            mOverflow = itemView.findViewById(R.id.overflow);

            itemView.setOnClickListener(this);
        }

        void bind(Book book)
        {
            mTitle.setText(book.getTitle());
            mAuthor.setText(book.getAuthors());
            /*if (book.getThumbnail() != null) {
                mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(book.getThumbnail());
                Glide.with(itemView.getContext()).load(mStorageReference).into(mThumbnail);
            } else */
            if (book.getUserBookPhotosStoragePath() != null && book.getUserBookPhotosStoragePath().size() > 0) {
                mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUserBookPhotosStoragePath().get(0));
                Glide.with(itemView.getContext()).load(mStorageReference).into(mThumbnail);
            } else
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_no_book_photo)).into(mThumbnail);
            mOverflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(mOverflow);
                }
            });
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(), "Function not implemented", Toast.LENGTH_SHORT).show();
        }
    }


    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.book_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

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

    /*private class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {
        // TODO move this inner class somewhere else
        private ImageView mTarget;

        public DownLoadImageTask(ImageView target) {
            mTarget = target;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap logo = null;
            try {
                InputStream is = new URL(strings[0]).openStream();

                    //decodeStream(InputStream is)
                      //  Decode an input stream into a bitmap.

                logo = BitmapFactory.decodeStream(is);
            } catch (Exception e) { // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }

        protected void onPostExecute(Bitmap result) {
            mTarget.setImageBitmap(result);
        }
    }*/
}
