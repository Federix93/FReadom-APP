package com.example.android.lab1.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.lab1.R;

import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsViewHolder> {

    private List<String> mTags;

    public TagsAdapter(List<String> tags) {
        mTags = tags;
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
            holder.setText(mTags.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mTags != null && !mTags.isEmpty() ? mTags.size() : 0;
    }

    class TagsViewHolder extends RecyclerView.ViewHolder {
        private TextView mTagEditText;
        private ImageView mClearImageView;

        TagsViewHolder(View itemView) {
            super(itemView);
            mTagEditText = itemView.findViewById(R.id.tag_edit);
            mClearImageView = itemView.findViewById(R.id.tag_clear);
        }

        private void setText(String text) {
            if (mTagEditText != null) {
                mTagEditText.setText(text);
                mClearImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition >= 0 &&
                                adapterPosition < mTags.size()) {
                            mTags.remove(adapterPosition);
                            notifyItemRemoved(adapterPosition);
                        }
                    }
                });
            }
        }
    }
}
