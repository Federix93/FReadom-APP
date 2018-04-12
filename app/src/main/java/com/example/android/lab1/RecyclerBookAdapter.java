package com.example.android.lab1;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class RecyclerBookAdapter extends RecyclerView.Adapter<RecyclerBookAdapter.BookViewHolder> {

    private int mNumberItems;

    public RecyclerBookAdapter(int nItems)
    {
        mNumberItems = nItems;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recycler_book_item, parent, false);
        BookViewHolder viewHolder = new BookViewHolder(view);

        viewHolder.mTitle.setText("Testo iniziale");

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;

        public BookViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.book_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Sono il numero "+getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        void bind(int listIndex)
        {
            mTitle.setText("Position "+listIndex);
        }

    }
}
