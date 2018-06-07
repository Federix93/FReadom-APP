package com.example.android.lab1.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.R;
import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.Conversation;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.firebase.ui.auth.ui.ProgressDialogHolder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class RecyclerConversationAdapter extends RecyclerView.Adapter<RecyclerConversationAdapter.ConversationViewHolder> {

    private ActivityCallBack mCallback;


    private List<Conversation> mConversations;
    private String mBookID;
    private String mSenderUID;
    private List<String> mUserIDs;
    private int positionsChecked;
    RecyclerView mRecyclerView;
    Toolbar mToolbar;
    private boolean bookIsPresent;
    private int counterSize;
    public RecyclerConversationAdapter(ArrayList<Conversation> conversations,
                                       String bookID,
                                       Toolbar toolbar,
                                       ActivityCallBack callback) {
        this.mConversations = conversations;
        mBookID = bookID;
        positionsChecked = -1;
        mToolbar = toolbar;
        bookIsPresent = false;
        counterSize = 0;
        mCallback = callback;
    }

    public void deleteChat(RecyclerView.OnItemTouchListener listener) {
        String chatId = mConversations.get(positionsChecked).getChatId();
        String otherUserID = mConversations.get(positionsChecked).getUserId();
        if (chatId != null && otherUserID != null) {
            mToolbar.setBackgroundColor(Color.parseColor("#FFFFFF"));
            mToolbar.setTitle(R.string.conversations_title);
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            mToolbar.getMenu().findItem(R.id.delete_chat).setVisible(false);
            mToolbar.setTitle(R.string.conversations_title);
            final ProgressDialogHolder progressDialogHolder = new ProgressDialogHolder(mRecyclerView.getContext());
            progressDialogHolder.showLoadingDialog(R.string.cancellation_in_progress);
            final Conversation removed = mConversations.remove(positionsChecked);
            notifyItemRemoved(positionsChecked);
            mRecyclerView.removeOnItemTouchListener(listener);
            positionsChecked = -1;
            FirebaseDatabase.getInstance().getReference("chats").child(chatId).removeValue();
            FirebaseDatabase.getInstance().getReference("conversations").child(mBookID).child(chatId).removeValue();
            FirebaseDatabase.getInstance().getReference("openedChats").child(mBookID).child(FirebaseAuth.getInstance().getUid()).child(otherUserID).removeValue();
            FirebaseDatabase.getInstance().getReference("messages").child(chatId).removeValue();
            FirebaseFirestore.getInstance().collection("requestsDone").document(otherUserID).collection("books").document(mBookID).delete();
            FirebaseFirestore.getInstance().collection("requestsDone").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    final List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                     bookIsPresent = false;
                    for(DocumentSnapshot d : snapshotList){
                        if(bookIsPresent) {
                            counterSize = 0;
                            bookIsPresent = false;
                            if(progressDialogHolder.isProgressDialogShowing())
                                progressDialogHolder.dismissDialog();
                            break;
                        }
                        d.getReference().collection("books").document(mBookID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {
                                counterSize++;
                                if (snapshot.exists()) {
                                    bookIsPresent = true;
                                }
                                if(counterSize == snapshotList.size()){
                                    if(!bookIsPresent){
                                        FirebaseFirestore.getInstance().collection("requestsReceived")
                                                .document(FirebaseAuth.getInstance().getUid())
                                                .collection("books").document(mBookID).delete();
                                    }
                                    if(mCallback != null) {
                                        if (progressDialogHolder.isProgressDialogShowing())
                                            progressDialogHolder.dismissDialog();
                                        mCallback.removeWithContext(removed.getChatId());

                                        if (mConversations.size() == 0) {
                                            mCallback.doClose();
                                        }
                                    }
                                }
                            }
                        });

                    }
                }
            });

        }
    }


    public interface ActivityCallBack {
        void doClose();

        void removeWithContext(String chatId);
    }

    public void deselectItem() {
        if (positionsChecked != -1) {
            View viewSelected = mRecyclerView.getChildAt(positionsChecked);
            viewSelected.setBackgroundColor(Color.parseColor("#FFFFFF"));
            mToolbar.setBackgroundColor(Color.parseColor("#FFFFFF"));
            mToolbar.setTitle(R.string.conversations_title);
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            mToolbar.getMenu().findItem(R.id.delete_chat).setVisible(false);
            positionsChecked = -1;
        }
    }

    public boolean isSomeItemSelected() {
        if (positionsChecked == -1)
            return false;
        return true;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_conversation_item, parent, false);
        mRecyclerView = (RecyclerView) parent;
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(mConversations.get(position));
    }

    @Override
    public int getItemCount() {
        return mConversations.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        TextView mUserNameTextView;
        ImageView mUserProfileImageView;
        TextView mLastMessageTextView;
        TextView mTimetampTextView;
        TextView mMessageCounterTextView;

        ConversationViewHolder(View itemView) {
            super(itemView);
            mUserNameTextView = itemView.findViewById(R.id.username_conversation_text_view);
            mUserProfileImageView = itemView.findViewById(R.id.profile_conversation_image_view);
            mLastMessageTextView = itemView.findViewById(R.id.lastmessage_conversation_text_view);
            mTimetampTextView = itemView.findViewById(R.id.timestamp_last_message);
            mMessageCounterTextView = itemView.findViewById(R.id.message_counter_text_view);
        }

        void bind(final Conversation conversation) {
            final User user = conversation.getUser();
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

            if (conversation.getChat() != null) {
                Chat chat = conversation.getChat();
                if (chat != null) {
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTimeInMillis(chat.getTimestamp() * 1000);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    mTimetampTextView.setText(dateFormat.format(cal1.getTime()));
                    if (chat.getIsText().equals("true"))
                        mLastMessageTextView.setText(chat.getLastMessage());
                    else
                        mLastMessageTextView.setText(R.string.photo_message_chat);
                    if (chat.getSenderUID() != null && !chat.getSenderUID().equals(FirebaseAuth.getInstance().getUid())) {
                        mSenderUID = chat.getSenderUID();
                        if (chat.getCounter() == 0) {
                            mMessageCounterTextView.setVisibility(View.GONE);
                        } else {
                            if (mMessageCounterTextView.getVisibility() == View.GONE) {
                                mMessageCounterTextView.setVisibility(View.VISIBLE);
                            }
                            mMessageCounterTextView.setText(String.valueOf(chat.getCounter()));
                            mMessageCounterTextView.setBackground(itemView.getResources().getDrawable(R.drawable.rounded_textview));
                        }
                    } else {
                        mMessageCounterTextView.setVisibility(View.GONE);
                    }
                }
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(positionsChecked != -1){
                        View viewSelected = mRecyclerView.getChildAt(positionsChecked);
                        viewSelected.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        mToolbar.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
                        mToolbar.setTitle(R.string.conversations_title);
                        positionsChecked = -1;
                        mToolbar.getMenu().findItem(R.id.delete_chat).setVisible(false);
                        return;
                    }
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("ChatID", conversation.getChatId());
                    intent.putExtra("Username", user.getUsername());
                    intent.putExtra("ImageURL", user.getPhotoURL());
                    intent.putExtra("BookID", mBookID);
                    v.getContext().startActivity(intent);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(positionsChecked != -1) {
                        return false;
                    }
                    positionsChecked = getAdapterPosition();
                    itemView.setBackgroundColor(Color.parseColor("#ededed"));
                    mToolbar.setTitle("Elimina");
                    mToolbar.setBackgroundColor(Color.parseColor("#bdbdbd"));
                    mToolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
                    mToolbar.getMenu().findItem(R.id.delete_chat).setVisible(true);
                    return true;
                }
            });
        }
    }
}