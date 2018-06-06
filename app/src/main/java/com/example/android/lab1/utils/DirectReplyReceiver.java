package com.example.android.lab1.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DirectReplyReceiver extends BroadcastReceiver {

    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    DatabaseReference messagesReference;
    DatabaseReference chatsReference;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.hasExtra("notificationID"))
        {
            if(NotificationUtilities.notificationExist(intent.getStringExtra("notificationID")))
                NotificationUtilities.removeNotification(intent.getStringExtra("notificationID"), context, false);
        }

        if(intent.hasExtra("summaryID"))
        {
            NotificationUtilities.deleteAll();
        }

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if (remoteInput != null) {

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseDatabase = FirebaseDatabase.getInstance();
            messagesReference = firebaseDatabase.getReference().child("messages");
            chatsReference = firebaseDatabase.getReference().child("chats");


            CharSequence newMessage = remoteInput.getCharSequence(FirebaseNotificationService.MESSAGE_REPLY_KEY);
            final String messageWritten = newMessage.toString();
            final String chatID = intent.getStringExtra("notificationID");


            Message chatMessage = new Message(firebaseAuth.getUid(), newMessage.toString(),
                    System.currentTimeMillis() / 1000, null);
            messagesReference.child(chatID).push().setValue(chatMessage);

            chatsReference.child(chatID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        if (chat != null) {
                            chat.setTimestamp(System.currentTimeMillis() / 1000);
                            chat.setLastMessage(messageWritten);
                            chat.setIsText("true");
                            if (chat.getSenderUID() == null) {
                                chat.setSenderUID(firebaseAuth.getUid());
                                chat.setCounter(chat.getCounter() + 1);
                            } else {
                                if (chat.getSenderUID().equals(firebaseAuth.getUid())) {
                                    chat.setCounter(chat.getCounter() + 1);
                                } else {
                                    chat.setReceiverUID(chat.getSenderUID());
                                    chat.setSenderUID(firebaseAuth.getUid());
                                    chat.setCounter(1);
                                }
                                chat.setSenderUID(firebaseAuth.getUid());
                            }
                        }
                        chatsReference.child(chatID).setValue(chat);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(chatID.hashCode());

        }


    }
}
