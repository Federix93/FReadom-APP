package com.example.android.lab1.utils;

import android.content.Context;
import android.content.Intent;

import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.Conversation;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChatManager {

    private String mBookID;
    private final String mBookOwnerUserID;
    private com.example.android.lab1.model.User mUserLogged;
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private String mChatID;

    private User mUserLoggedDatabase;
    private User mBookOwnerUserDatabase;

    private final DatabaseReference chatsReference;
    private final DatabaseReference conversationsReference;
    private final DatabaseReference usersReference;

    public ChatManager(String bookID, final String bookOwnerID, Context context) {
        mBookID = bookID;
        mBookOwnerUserID = bookOwnerID;
        mContext = context;

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        chatsReference = firebaseDatabase.getReference().child("chats");
        conversationsReference = firebaseDatabase.getReference().child("conversations");
        usersReference = firebaseDatabase.getReference().child("users");

        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth != null && firebaseAuth.getUid() != null) {
            DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getUid());
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    mUserLogged = snapshot.toObject(com.example.android.lab1.model.User.class);
                    usersReference.child(firebaseAuth.getUid()).child(mBookID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                Map<String, String> map = new HashMap<>();
                                map.put("Dummy", "");
                                mUserLoggedDatabase = new User(mUserLogged.getUsername(),
                                        mUserLogged.getImage(), map);
                                usersReference.child(firebaseAuth.getUid()).child(mBookID).setValue(mUserLoggedDatabase, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if(databaseError == null) {
                                            DocumentReference documentReference = firebaseFirestore.collection("users").document(mBookOwnerUserID);
                                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot snapshot) {
                                                    final com.example.android.lab1.model.User userOwnerFromDB = snapshot.toObject(com.example.android.lab1.model.User.class);
                                                    usersReference.child(mBookOwnerUserID).child(mBookID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (!dataSnapshot.exists()) {
                                                                Map<String, String> map = new HashMap<>();
                                                                map.put("Dummy", "");
                                                                mBookOwnerUserDatabase = new User(userOwnerFromDB.getUsername(),
                                                                        userOwnerFromDB.getImage(), map);
                                                                usersReference.child(mBookOwnerUserID).child(mBookID).setValue(mBookOwnerUserDatabase, new DatabaseReference.CompletionListener() {
                                                                    @Override
                                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                        if (databaseError == null) {
                                                                            checkChatExists();
                                                                        }
                                                                    }
                                                                });
                                                            }else{
                                                                checkChatExists();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                });
                            }else{
                                DocumentReference documentReference = firebaseFirestore.collection("users").document(mBookOwnerUserID);
                                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot snapshot) {
                                        final com.example.android.lab1.model.User userOwnerFromDB = snapshot.toObject(com.example.android.lab1.model.User.class);
                                        usersReference.child(mBookOwnerUserID).child(mBookID).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.exists()) {
                                                    Map<String, String> map = new HashMap<>();
                                                    map.put("Dummy", "");
                                                    mBookOwnerUserDatabase = new User(userOwnerFromDB.getUsername(),
                                                            userOwnerFromDB.getImage(), map);
                                                    usersReference.child(mBookOwnerUserID).child(mBookID).setValue(mBookOwnerUserDatabase, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if (databaseError == null) {
                                                                checkChatExists();
                                                            }
                                                        }
                                                    });
                                                }else{
                                                    checkChatExists();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                });
                            }
                        }

                    @Override
                    public void onCancelled (DatabaseError databaseError){

                    }
                });
            }
        });
    }

}

    public void checkChatExists() {
        usersReference.child(firebaseAuth.getUid()).child(mBookID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Map<String, String> map = (Map<String, String>) dataSnapshot.child("mapUserIDChatID").getValue();
                if (dataSnapshot.exists()) {
                    if (user != null && map != null && map.get(mBookOwnerUserID) != null) {
                        mChatID = map.get(mBookOwnerUserID);
                        openChat();
                    }else if(map != null && map.get(mBookOwnerUserID) == null){
                        createChat();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void openChat() {
        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("ChatID", mChatID);
        intent.putExtra("Username", mUserLogged.getUsername());
        intent.putExtra("ImageURL", mUserLogged.getImage());
        intent.putExtra("BookID", mBookID);
        mContext.startActivity(intent);
    }

    public void createChat() {

        Chat chat = new Chat(mBookID, "", System.currentTimeMillis() / 1000);
        DatabaseReference newChat = chatsReference.push();
        mChatID = newChat.getKey();
        newChat.setValue(chat);
        Conversation conversation = new Conversation(firebaseAuth.getUid(), mBookOwnerUserID);
        conversationsReference.child(mChatID).setValue(conversation);

        mUserLoggedDatabase.setMapUserIDChatID(mBookOwnerUserID, mChatID);
        mBookOwnerUserDatabase.setMapUserIDChatID(firebaseAuth.getUid(), mChatID);

        usersReference.child(firebaseAuth.getUid()).child(mBookID).setValue(mUserLoggedDatabase);
        usersReference.child(mBookOwnerUserID).child(mBookID).setValue(mBookOwnerUserDatabase);
        openChat();
    }
}
