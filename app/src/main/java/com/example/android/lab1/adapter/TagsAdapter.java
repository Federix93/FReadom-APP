package com.example.android.lab1.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.android.lab1.R;

import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsViewHolder> {

    public List<String> mTags;

    public TagsAdapter(List<String> tags) {
        mTags = tags;
        if (mTags.isEmpty())
            mTags.add("");
    }

    @NonNull
    @Override
    public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return new TagsViewHolder(layoutInflater.inflate(R.layout.item_load_book_tag,
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {
        if (mTags != null && !mTags.isEmpty() && position < mTags.size()) {
            // is a tag
            holder.setText(mTags.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mTags != null && !mTags.isEmpty() ? mTags.size() : 0;
    }

    protected class TagsViewHolder extends RecyclerView.ViewHolder {
        private EditText mTagEditText;
        private ImageView mClearImageView;

        public TagsViewHolder(View itemView) {
            super(itemView);
            mTagEditText = itemView.findViewById(R.id.tag_edit);
            mTagEditText.addTextChangedListener(new TextWatcher() {

                public boolean mRemove;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mRemove = before > count;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int adapterPosition = getAdapterPosition();
                    if (s.length() > 0) {
                        if (adapterPosition == getItemCount() - 1) {
                            // is the last tag
                            mTags.add("");
                            notifyItemInserted(adapterPosition + 1);
                        }
                        mTags.set(adapterPosition, s.toString());
                    } else if (mRemove) {
                        mTags.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                    }
                }
            });
            mTagEditText.setFilters(new InputFilter[]
                    {
                            new InputFilter() {
                                @Override
                                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                    for (int i = start; i < end; i++) {
                                        if (!Character.isLetter(source.charAt(i)))
                                            return "";
                                    }
                                    return null;
                                }
                            }
                    });

        }

        private void setText(String text) {
            if (mTagEditText != null) {
                mTagEditText.setText(text);
            }
        }
    }
}
