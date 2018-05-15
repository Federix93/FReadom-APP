package com.example.android.lab1.ui.chat;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.MessageListAdapter;
import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    FirebaseDatabase mFirebaseDatabase;

    DatabaseReference mChatsReference;
    //DatabaseReference mConversationsReference;
    DatabaseReference mMessagesReference;
    //DatabaseReference mUsersReference;

    FirebaseAuth mFirebaseAuth;
    FirebaseStorage mFirebaseStorage;
    StorageReference mChatPhotosStorageReference;

    String mChatID;
    String mUsername;
    String mPhotoProfileURL;
    String mBookID;

    RecyclerView mMessagesRecyclerView;
    EditText mMessageEditText;
    Button mSendButton;
    ImageButton mPhotoPickerButton;

    ChildEventListener mChildEventListener;
    private MessageListAdapter mChatArrayAdapter;

    String dataTitle, dataMessage;
    private final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_PHOTO_PICKER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mMessagesRecyclerView = findViewById(R.id.chat_recyclerView);
        mMessageEditText = findViewById(R.id.edittext_chat_message);
        mSendButton = findViewById(R.id.sendButton);
        mPhotoPickerButton = findViewById(R.id.buttonUpload);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mChatsReference = mFirebaseDatabase.getReference().child("chats");
        mMessagesReference = mFirebaseDatabase.getReference().child("messages");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        mChatID = getIntent().getStringExtra("ChatID");
        mPhotoProfileURL = getIntent().getStringExtra("ImageURL");
        mBookID = getIntent().getStringExtra("BookID");

        final List<Message> chatMessages = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mMessagesRecyclerView.setHasFixedSize(true);
        mMessagesRecyclerView.setLayoutManager(layoutManager);
        mMessagesRecyclerView.setNestedScrollingEnabled(false);

        mChatArrayAdapter = new MessageListAdapter(this, chatMessages);
        mMessagesRecyclerView.setAdapter(mChatArrayAdapter);

        mChatArrayAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mMessagesRecyclerView.smoothScrollToPosition(mChatArrayAdapter.getItemCount());
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsername = mFirebaseAuth.getCurrentUser().getDisplayName();
                Message chatMessage = new Message(mUsername, mFirebaseAuth.getUid(), mMessageEditText.getText().toString(), mPhotoProfileURL,
                        System.currentTimeMillis() / 1000, null);
                mMessagesReference.child(mChatID).push().setValue(chatMessage);
                mChatsReference.child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            if (chat != null) {
                                chat.setTimestamp(System.currentTimeMillis() / 1000);
                                chat.setLastMessage(mMessageEditText.getText().toString());
                            }
                            mChatsReference.child(mChatID).setValue(chat);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                // Clear input box
                mMessageEditText.setText("");
            }
        });

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message chatMessage = dataSnapshot.getValue(Message.class);
                mChatArrayAdapter.addMessage(chatMessage);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mMessagesReference.child(mChatID).addChildEventListener(mChildEventListener);

        /*if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                if (key.equals("username")) {
                    dataTitle = (String) getIntent().getExtras().get(key);
                }
                if (key.equals("textMessage")) {
                    dataMessage = (String) getIntent().getExtras().get(key);
                }
            }

            showAlertDialog();

        }*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
                //Upload file to  Firebase Storage
                photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Message chatMessage = new Message(mUsername, mFirebaseAuth.getUid(), null, mPhotoProfileURL, System.currentTimeMillis() / 1000,
                                downloadUrl.toString());
                        mMessagesReference.child(mChatID).push().setValue(chatMessage);
                        mChatsReference.child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Chat chat = dataSnapshot.getValue(Chat.class);
                                    if (chat != null) {
                                        chat.setTimestamp(System.currentTimeMillis() / 1000);
                                        chat.setLastMessage(downloadUrl.toString());
                                    }
                                    mChatsReference.child(mChatID).setValue(chat);
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
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message");
        builder.setMessage("title: " + dataTitle + "\n" + "message: " + dataMessage);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    public void subscribeToTopic(View view) {
        FirebaseMessaging.getInstance().subscribeToTopic("notifications");
        Toast.makeText(this, "Subscribed to Topic: Notifications", Toast.LENGTH_SHORT).show();
    }
}
