package com.example.android.lab1.adapter;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.android.lab1.R;

import java.util.ArrayList;

public class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.AuthorViewHolder> {

    private ArrayList<String> mAuthors;
    private LayoutInflater mLayoutInflater;
    private boolean mEditable;

    public AuthorAdapter(LayoutInflater layoutInflater, ArrayList<String> authors) {
        this.mLayoutInflater = layoutInflater;
        this.mAuthors = authors;
        this.mEditable = false;
    }

    @NonNull
    @Override
    public AuthorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = mLayoutInflater.inflate(R.layout.author_item, parent, false);
        return new AuthorViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorViewHolder holder, int position) {
        holder.setAuthor(mAuthors != null && position < mAuthors.size() ? mAuthors.get(position) : null);
    }

    public boolean isEditable() {
        return mEditable;
    }

    public void setEditable(boolean editable) {
        this.mEditable = editable;
        if (mEditable)
            addAuthor(null);
    }

    @Override
    public int getItemCount() {
        return mAuthors != null ? mAuthors.size() : 0;
    }

    private void addAuthor(String o) {
        if (mAuthors == null)
            mAuthors = new ArrayList<>();
        mAuthors.add(o);
        notifyDataSetChanged();
    }

    private void removeAuthor(int adapterPosition) {
        mAuthors.remove(adapterPosition);
        notifyDataSetChanged();
    }

    public ArrayList<String> getAuthors() {
        return mAuthors;
    }

    public class AuthorViewHolder extends RecyclerView.ViewHolder {

        private ImageView mRemoveAuthorButton;
        private EditText mAuthorEditText;

        public AuthorViewHolder(View itemView) {
            super(itemView);
            TextInputLayout authorTextInputLayout = itemView.findViewById(R.id.author_edit);
            mAuthorEditText = authorTextInputLayout.getEditText();
            mAuthorEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (getAdapterPosition() >= 0) {
                        mAuthors.set(getAdapterPosition(), s.toString());
                    }
                }
            });
            if (mEditable) {
                mAuthorEditText.setEnabled(true);
                mRemoveAuthorButton = itemView.findViewById(R.id.remove_author_button);
                mRemoveAuthorButton.setVisibility(View.VISIBLE);
            }
        }

        public void setAuthor(String author) {
            mAuthorEditText.setText(author);
            if (mEditable) {
                if (getAdapterPosition() > 0) {
                    mRemoveAuthorButton.setImageResource(R.drawable.ic_remove_black_24dp);
                    mRemoveAuthorButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeAuthor(getAdapterPosition());
                        }
                    });
                } else {
                    mRemoveAuthorButton.setImageResource(R.drawable.ic_add_black_24dp);
                    mRemoveAuthorButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getItemCount() < 3)
                                addAuthor(null);
                        }
                    });
                }
            }
        }
    }
}
