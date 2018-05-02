package com.example.android.lab1.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.lab1.R;
import com.example.android.lab1.model.GenreBook;

public class GenreBooksAdapter extends RecyclerView.Adapter<GenreBooksAdapter.GenreViewHolder> {

    Context mContext;
    public GenreBooksAdapter(Context pContext){
        mContext = pContext;
    }

    @NonNull
    @Override
    public GenreBooksAdapter.GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.genre_book_item, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        holder.mGenreText.setText(GenreBook.getList(holder.itemView.getContext()).get(position));
    }

    @Override
    public int getItemCount() {
        return GenreBook.getList(mContext).size();
    }


    public class GenreViewHolder extends RecyclerView.ViewHolder{

        TextView mGenreText;

        public GenreViewHolder(View itemView) {
            super(itemView);
            mGenreText = itemView.findViewById(R.id.genre_text_view);
        }
    }
}
