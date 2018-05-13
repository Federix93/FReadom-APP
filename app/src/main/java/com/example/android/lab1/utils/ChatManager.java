package com.example.android.lab1.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.Conversation;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.ui.BookDetailsActivity;
import com.example.android.lab1.ui.ChatActivity;
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

public class ChatManager {

    public static void createChat(final String receiverUserID, final String bookReferenceID, final Context context) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        final DatabaseReference chatsReference = firebaseDatabase.getReference().child("chats");
        final DatabaseReference usersReference = firebaseDatabase.getReference().child("users");
        final DatabaseReference conversationsReference = firebaseDatabase.getReference().child("conversations");
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth != null && firebaseAuth.getUid() != null) {
            DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getUid());
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {

                    final com.example.android.lab1.model.User userAuth = snapshot.toObject(com.example.android.lab1.model.User.class);
                    usersReference.child(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                User user = new User();
                                user.setUsername(userAuth.getUsername());
                                user.setPhotoURL(userAuth.getImage());
                                usersReference.child(firebaseAuth.getUid()).setValue(user);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    Chat chat = new Chat();
                    chat.setLastMessage("");
                    chat.setTimestamp(System.currentTimeMillis() / 1000);
                    chat.setBookID(bookReferenceID);
                    DatabaseReference newChat = chatsReference.push();
                    String keyChat = newChat.getKey();
                    newChat.setValue(chat);

                    Conversation conversation = new Conversation();
                    conversation.setUserID1(firebaseAuth.getUid());
                    conversation.setUserID2(receiverUserID);
                    conversationsReference.child(keyChat).setValue(conversation);
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("ChatID", keyChat);
                    intent.putExtra("Username", userAuth.getUsername());
                    intent.putExtra("ImageURL", userAuth.getImage());
                    context.startActivity(intent);
                }
            });
        }
    }
}
