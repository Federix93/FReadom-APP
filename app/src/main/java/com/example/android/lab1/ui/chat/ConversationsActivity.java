package com.example.android.lab1.ui.chat;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerConversationAdapter;
import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConversationsActivity extends AppCompatActivity {

    Toolbar mToolbar;
    String mBookID;
    private RecyclerView mRecyclerView;
    private List<com.example.android.lab1.model.User> mUserList;
    private List<String> mListChatID;
    private RecyclerConversationAdapter mAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        mUserList = new ArrayList<>();
        mListChatID = new ArrayList<>();
        mBookID = getIntent().getStringExtra("ID_BOOK_SELECTED");
        Utilities.setupStatusBarColor(this);
        mToolbar = findViewById(R.id.toolbar_conversations_activity);
        mToolbar.setTitle(R.string.conversations_title);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = findViewById(R.id.conversation_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        mAdapter = new RecyclerConversationAdapter(mUserList, mListChatID, mBookID);
        mRecyclerView.setAdapter(mAdapter);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);
        if (firebaseAuth != null) {
            DatabaseReference usersReference = firebaseDatabase.getReference().child("users")
                    .child(firebaseAuth.getUid()).child(mBookID);
            usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    final Map<String, String> map = (Map<String, String>) dataSnapshot.child("mapUserIDChatID").getValue();
                    if (map != null) {
                        for (final String uid : map.keySet()) {
                            if(uid.equals("Dummy")){
                                continue;
                            }
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
                            documentReference.get().addOnCompleteListener(ConversationsActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        com.example.android.lab1.model.User user = task.getResult().toObject(com.example.android.lab1.model.User.class);
                                        mUserList.add(user);
                                        mListChatID.add(map.get(uid));
                                        mAdapter.setItems(mUserList, mListChatID);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
