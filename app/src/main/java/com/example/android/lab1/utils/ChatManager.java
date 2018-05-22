package com.example.android.lab1.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatManager {

    private String mBookID;
    private final String mBookOwnerUserID;
    private Context mContext;
    private String mChatID;
    private User mBookOwnerUserDatabase;
    private boolean isChatAlreadyOpened = true;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private final DatabaseReference chatsReference;
    private final DatabaseReference conversationsReference;
    private final DatabaseReference usersReference;
    private final DatabaseReference openedChatReference;

    public ChatManager(String bookID, final String bookOwnerID, Context context) {
        mBookID = bookID;
        mBookOwnerUserID = bookOwnerID;
        mContext = context;

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        chatsReference = firebaseDatabase.getReference().child("chats");
        conversationsReference = firebaseDatabase.getReference().child("conversations");
        usersReference = firebaseDatabase.getReference().child("users");
        openedChatReference = firebaseDatabase.getReference().child("openedChats");
        linkUsersToChat();
}

    private void linkUsersToChat(){
        if (firebaseAuth != null && firebaseAuth.getUid() != null) {

            openedChatReference.child(mBookID).child(mBookOwnerUserID).child(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot != null && !dataSnapshot.exists()){
                        createChat();
                        getUserOwner();
                    }else{

                        openedChatReference.child(mBookID).child(mBookOwnerUserID).child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                               mChatID = (String) dataSnapshot.getValue();
                               getUserOwner();
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void openChat() {

        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("ChatID", mChatID);
        intent.putExtra("Username", mBookOwnerUserDatabase.getUsername());
        intent.putExtra("ImageURL", mBookOwnerUserDatabase.getPhotoURL());
        intent.putExtra("BookID", mBookID);
        mContext.startActivity(intent);
    }

    private void createChat() {
        String defaultMessage = "HAI RICHIESTO IL LIBRO ALLO STRONZO";
        isChatAlreadyOpened = false;
        Chat chat = new Chat(mBookID, defaultMessage, System.currentTimeMillis() / 1000);
        DatabaseReference newChat = chatsReference.push();
        mChatID = newChat.getKey();
        newChat.setValue(chat);
        conversationsReference.child(mBookID).child(mChatID).setValue(firebaseAuth.getUid());
        openedChatReference.child(mBookID).child(mBookOwnerUserID).child(firebaseAuth.getUid()).setValue(mChatID);
    }

    private void getUserOwner(){
        usersReference.child(mBookOwnerUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mBookOwnerUserDatabase = dataSnapshot.getValue(User.class);
                openChat();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
