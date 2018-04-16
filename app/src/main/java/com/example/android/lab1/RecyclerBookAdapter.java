package com.example.android.lab1;

import android.content.Context;
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

public class RecyclerBookAdapter extends RecyclerView.Adapter<RecyclerBookAdapter.BookViewHolder> {

    private Context mContext;
    private int mNumberItems;

    public RecyclerBookAdapter(Context context, int nItems)
    {
        mContext = context;
        mNumberItems = nItems;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_book_item, parent, false);
        BookViewHolder viewHolder = new BookViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final BookViewHolder holder, int position) {
        holder.bind(position);
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */

    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView mTitle, mAuthor;
        ImageView mThumbnail, mOverflow;

        public BookViewHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.title);
            mAuthor = (TextView) itemView.findViewById(R.id.author);
            mThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            mOverflow = (ImageView) itemView.findViewById(R.id.overflow);

            itemView.setOnClickListener(this);
        }

        void bind(int listIndex)
        {
            mTitle.setText("Titolo mediamente lungo");
            mAuthor.setText("Gino Pilotino Il Terzo");
            Glide.with(mContext).load(mContext.getResources().getDrawable(R.drawable.book_placeholder_thumbnail)).into(mThumbnail);
            mOverflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(mOverflow);
                }
            });
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(mContext, "Sono il numer "+getAdapterPosition(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(mContext, "Preview", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_add_wishlist:
                    Toast.makeText(mContext, "Wishlist", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }
}
