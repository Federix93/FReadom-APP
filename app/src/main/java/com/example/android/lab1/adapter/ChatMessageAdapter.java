package com.example.android.lab1.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.lab1.R;
import com.example.android.lab1.model.BookPhoto;
import com.example.android.lab1.model.chatmodels.Message;
import com.example.android.lab1.ui.BookPhotoDetailActivity;
import com.example.android.lab1.ui.PhotoDetailActivity;
import com.example.android.lab1.utils.glideimageloader.GlideApp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    Context mContext;
    private List<Message> mMessageList;

    public ChatMessageAdapter(Context context, List<Message> messages) {
        mContext = context;
        mMessageList = messages;
    }
    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_chat_item_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_chat_item_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }
    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = mMessageList.get(position);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth != null && firebaseAuth.getUid() != null) {
            if (message.getSenderId().equals(firebaseAuth.getUid())) {
                // If the current user is the sender of the message
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        } else {
            return 0;
        }
    }

    public void addMessage(Message chatMessage) {
        mMessageList.add(chatMessage);
        notifyItemInserted(mMessageList.size());
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView messageText;
        TextView timeText;
        ImageView imageSent;
        TextView timeImageSent;
        ConstraintLayout constraintLayoutMessage;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            imageSent = itemView.findViewById(R.id.image_uploaded_sent);
            timeImageSent = itemView.findViewById(R.id.time_image_upload);
            constraintLayoutMessage = itemView.findViewById(R.id.constraint_layout_sent);

            imageSent.setOnClickListener(this);
        }

        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                BookPhoto bookPhoto = new BookPhoto(mMessageList.get(position).getPhotoURL(), "chat photo");
                Intent intent = new Intent(mContext, PhotoDetailActivity.class);
                intent.putExtra(PhotoDetailActivity.BOOK_PHOTO, bookPhoto);
                mContext.startActivity(intent);
            }
        }

        void bind(Message message) {

            if (message.getPhotoURL() != null) {
                constraintLayoutMessage.setVisibility(View.GONE);
                timeImageSent.setVisibility(View.VISIBLE);

                imageSent.setVisibility(View.VISIBLE);
                GlideApp.with(imageSent.getContext()).load(message.getPhotoURL()).into(imageSent);

                Calendar cal1 = Calendar.getInstance();
                cal1.setTimeInMillis(message.getTimestamp()*1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                timeImageSent.setText(dateFormat.format(cal1.getTime()));
            } else {
                constraintLayoutMessage.setVisibility(View.VISIBLE);
                timeImageSent.setVisibility(View.GONE);
                imageSent.setVisibility(View.GONE);

                messageText.setText(message.getTextMessage());

                Calendar cal1 = Calendar.getInstance();
                cal1.setTimeInMillis(message.getTimestamp()*1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                timeText.setText(dateFormat.format(cal1.getTime()));
            }
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView messageText;
        TextView timeText;
        ImageView imageReceived;
        TextView timeImageReceived;
        ConstraintLayout constraintLayoutMessage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            imageReceived = itemView.findViewById(R.id.image_uploaded_received);
            timeImageReceived = itemView.findViewById(R.id.time_image_upload);
            constraintLayoutMessage = itemView.findViewById(R.id.constraint_layout_received);

            imageReceived.setOnClickListener(this);
        }

        void bind(Message message) {

            if (message.getPhotoURL() != null) {
                constraintLayoutMessage.setVisibility(View.GONE);
                timeImageReceived.setVisibility(View.VISIBLE);
                imageReceived.setVisibility(View.VISIBLE);

                GlideApp.with(imageReceived.getContext()).load(message.getPhotoURL()).into(imageReceived);

                Calendar cal1 = Calendar.getInstance();
                cal1.setTimeInMillis(message.getTimestamp()*1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                timeImageReceived.setText(dateFormat.format(cal1.getTime()));
            } else {
                constraintLayoutMessage.setVisibility(View.VISIBLE);
                timeImageReceived.setVisibility(View.GONE);
                imageReceived.setVisibility(View.GONE);

                messageText.setText(message.getTextMessage());

                Calendar cal1 = Calendar.getInstance();
                cal1.setTimeInMillis(message.getTimestamp()*1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                timeText.setText(dateFormat.format(cal1.getTime()));
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                BookPhoto bookPhoto = new BookPhoto(mMessageList.get(position).getPhotoURL(), "chat photo");
                Intent intent = new Intent(mContext, PhotoDetailActivity.class);
                intent.putExtra(PhotoDetailActivity.BOOK_PHOTO, bookPhoto);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mContext.startActivity(intent);

            }
        }
    }
}