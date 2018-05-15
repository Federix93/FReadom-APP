package com.example.android.lab1.adapter;

import android.content.Intent;
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
import com.example.android.lab1.ui.BookDetailsActivity;
import com.example.android.lab1.ui.chat.ChatActivity;

import org.w3c.dom.Text;

import java.util.List;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class RecyclerConversationAdapter extends RecyclerView.Adapter<RecyclerConversationAdapter.ConversationViewHolder> {

    private List<User> mListUsers;
    private List<String> mListChatID;
    private String mBookID;

    public RecyclerConversationAdapter(List<User> listUsers, List<String> listChatID, String bookID){
        mListUsers = listUsers;
        mListChatID = listChatID;
        mBookID = bookID;
    }

    public void setItems(List<User> listUsers, List<String> listChatID){
        mListUsers = listUsers;
        mListChatID = listChatID;
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
        holder.bind(mListUsers.get(position), mListChatID.get(position));
    }

    @Override
    public int getItemCount() {
        return mListUsers.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder{

        TextView mUserNameTextView;
        ImageView mUserProfileImageView;
        TextView mLastMessageTextView;
        TextView mTimetampTextView;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            mUserNameTextView = itemView.findViewById(R.id.username_conversation_text_view);
            mUserProfileImageView = itemView.findViewById(R.id.profile_conversation_image_view);
            mLastMessageTextView = itemView.findViewById(R.id.lastmessage_conversation_text_view);
            mTimetampTextView = itemView.findViewById(R.id.timestamp_last_message);
        }

        public void bind(final User user, final String chatID){
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
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("ChatID", chatID);
                    intent.putExtra("Username", user.getUsername());
                    intent.putExtra("ImageURL", user.getImage());
                    intent.putExtra("BookID", mBookID);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
