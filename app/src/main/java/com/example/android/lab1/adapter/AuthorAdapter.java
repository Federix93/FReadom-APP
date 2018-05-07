package com.example.android.lab1.adapter;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.android.lab1.R;

import java.util.ArrayList;

public class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.AuthorViewHolder> {

    private final ArrayList<String> mAuthors;
    private final LayoutInflater mLayoutInflater;

    public AuthorAdapter(LayoutInflater layoutInflater, ArrayList<String> authors) {
        this.mLayoutInflater = layoutInflater;
        this.mAuthors = authors;
    }

    @NonNull
    @Override
    public AuthorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = mLayoutInflater.inflate(R.layout.author_edit_text, parent, false);
        return new AuthorViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorViewHolder holder, int position) {
        holder.setAuthor(mAuthors.get(position));
    }

    @Override
    public int getItemCount() {
        return mAuthors != null ? mAuthors.size() : 0;
    }

    public class AuthorViewHolder extends RecyclerView.ViewHolder {

        private EditText mAuthorEditText;

        public AuthorViewHolder(View itemView) {
            super(itemView);
            TextInputLayout authorTextInputLayout = itemView.findViewById(R.id.author_edit);
            mAuthorEditText = authorTextInputLayout.getEditText();
        }

        public void setAuthor(String author) {
            mAuthorEditText.setText(author);
        }
    }
}
