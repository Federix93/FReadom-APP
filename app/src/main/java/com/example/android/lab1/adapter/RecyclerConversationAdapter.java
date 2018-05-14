package com.example.android.lab1.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.R;
import com.example.android.lab1.model.User;

import java.util.List;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class RecyclerConversationAdapter extends RecyclerView.Adapter<RecyclerConversationAdapter.ConversationViewHolder> {

    public List<User> mListUsers;

    public RecyclerConversationAdapter(List<User> listUsers){
        mListUsers = listUsers;
    }

    public void setItems(List<User> listUsers){
        mListUsers = listUsers;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_conversation_item, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(mListUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mListUsers.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder{

        TextView mUserNameTextView;
        ImageView mUserProfileImageView;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            mUserNameTextView = itemView.findViewById(R.id.username_conversation_text_view);
            mUserProfileImageView = itemView.findViewById(R.id.profile_conversation_image_view);
        }

        public void bind(User user){
            Log.d("LULLO", "USERNAME" + user.getUsername());
            mUserNameTextView.setText(user.getUsername());
            //if (user.getImage() == null) {
                Glide.with(itemView.getContext()).load(R.mipmap.profile_picture)
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mUserProfileImageView);
            /*} else {
                Glide.with(itemView.getContext()).load(user.getImage())
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mUserProfileImageView);
            }*/
        }
    }
}
