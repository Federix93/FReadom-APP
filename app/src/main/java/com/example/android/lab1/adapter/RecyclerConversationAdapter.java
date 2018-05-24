package com.example.android.lab1.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.R;
import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class RecyclerConversationAdapter extends RecyclerView.Adapter<RecyclerConversationAdapter.ConversationViewHolder> {

    private List<User> mListUsers;
    private List<String> mListChatID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChatsReference;
    private String mBookID;
    private String mSenderUID;

    public RecyclerConversationAdapter(List<User> listUsers, List<String> listChatID, String bookID){
        mListUsers = listUsers;
        mListChatID = listChatID;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mChatsReference = mFirebaseDatabase.getReference().child("chats");
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
        TextView mMessageCounterTextView;
        private ValueEventListener mChildEventListener;

        ConversationViewHolder(View itemView) {
            super(itemView);
            mUserNameTextView = itemView.findViewById(R.id.username_conversation_text_view);
            mUserProfileImageView = itemView.findViewById(R.id.profile_conversation_image_view);
            mLastMessageTextView = itemView.findViewById(R.id.lastmessage_conversation_text_view);
            mTimetampTextView = itemView.findViewById(R.id.timestamp_last_message);
            mMessageCounterTextView = itemView.findViewById(R.id.message_counter_text_view);
        }

        void bind(final User user, final String chatID){
            mUserNameTextView.setText(user.getUsername());
            if (user.getPhotoURL() == null) {
                Glide.with(itemView.getContext()).load(R.mipmap.profile_picture)
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mUserProfileImageView);
            } else {
                Glide.with(itemView.getContext()).load(user.getPhotoURL())
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mUserProfileImageView);
            }
            mChildEventListener = new ValueEventListener(){

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat != null) {
                        Calendar cal1 = Calendar.getInstance();
                        cal1.setTimeInMillis(chat.getTimestamp() * 1000);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        mTimetampTextView.setText(dateFormat.format(cal1.getTime()));
                        if(chat.getIsText().equals("true"))
                            mLastMessageTextView.setText(chat.getLastMessage());
                        else
                            mLastMessageTextView.setText(R.string.photo_message_chat);
                        if(chat.getSenderUID() != null && !chat.getSenderUID().equals(FirebaseAuth.getInstance().getUid())){
                            mSenderUID = chat.getSenderUID();
                            if(chat.getCounter() == 0) {
                                mMessageCounterTextView.setText("");
                                mMessageCounterTextView.setBackground(null);
                            }
                            else {
                                mMessageCounterTextView.setText(String.valueOf(chat.getCounter()));
                                mMessageCounterTextView.setBackground(itemView.getResources().getDrawable(R.drawable.rounded_textview));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mChatsReference.child(chatID).addValueEventListener(mChildEventListener);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("ChatID", chatID);
                    intent.putExtra("Username", user.getUsername());
                    intent.putExtra("ImageURL", user.getPhotoURL());
                    intent.putExtra("BookID", mBookID);
                    intent.putExtra("SenderUID", mSenderUID);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
