package com.example.android.lab1.ui.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ChatMessageAdapter;
import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.Message;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    Toolbar mToolbar;
    FirebaseDatabase mFirebaseDatabase;

    DatabaseReference mChatsReference;
    DatabaseReference mMessagesReference;
    DatabaseReference mConversationsReference;

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
    ImageView mToolbarProfileImage;
    TextView mToolbarProfileUsername;
    LinearLayout mInputTextLinearLayout;

    ChildEventListener mChildEventListener;
    private ChatMessageAdapter mChatArrayAdapter;

    String dataTitle, dataMessage;
    private final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_PHOTO_PICKER = 2;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Utilities.setupStatusBarColor(this);

        mMessagesRecyclerView = findViewById(R.id.chat_recyclerView);
        mMessageEditText = findViewById(R.id.edittext_chat_message);
        mSendButton = findViewById(R.id.sendButton);
        mPhotoPickerButton = findViewById(R.id.buttonUpload);
        mInputTextLinearLayout = findViewById(R.id.linearLayoutBottomInput);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mChatsReference = mFirebaseDatabase.getReference().child("chats");
        mMessagesReference = mFirebaseDatabase.getReference().child("messages");
        mConversationsReference = mFirebaseDatabase.getReference().child("conversations");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        mChatID = getIntent().getStringExtra("ChatID");
        mUsername = getIntent().getStringExtra("Username");
        mPhotoProfileURL = getIntent().getStringExtra("ImageURL");
        mBookID = getIntent().getStringExtra("BookID");

        mToolbar = findViewById(R.id.toolbar_chat_activity);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mToolbarProfileImage = findViewById(R.id.chat_toolbar_profile_image);
        mToolbarProfileUsername = findViewById(R.id.chat_toolbar_profile_username);

        mToolbarProfileUsername.setText(mUsername);

        Glide.with(this).load(mPhotoProfileURL).apply(RequestOptions
                .bitmapTransform(new CircleCrop()))
                .into(mToolbarProfileImage);

        final List<Message> chatMessages = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mMessagesRecyclerView.setHasFixedSize(true);
        mMessagesRecyclerView.setLayoutManager(layoutManager);
        mMessagesRecyclerView.setNestedScrollingEnabled(false);

        mChatArrayAdapter = new ChatMessageAdapter(this, chatMessages);
        mMessagesRecyclerView.setAdapter(mChatArrayAdapter);

        mChatArrayAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mMessagesRecyclerView.smoothScrollToPosition(mChatArrayAdapter.getItemCount());
            }
        });
        final DatabaseReference dbRef = mMessagesReference.child(mChatID);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 1) {
                    if (mInputTextLinearLayout.getVisibility() == View.GONE)
                        mInputTextLinearLayout.setVisibility(View.VISIBLE);
                    setInputLinearLayout();
                    dbRef.removeEventListener(this);

                } else {
                    mConversationsReference.child(mBookID).child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (mFirebaseAuth.getUid().equals(dataSnapshot.getValue())) {
                                mInputTextLinearLayout.setVisibility(View.GONE);
                            } else{
                                if(mInputTextLinearLayout.getVisibility() == View.GONE)
                                    mInputTextLinearLayout.setVisibility(View.VISIBLE);
                                setInputLinearLayout();
                            }
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
        };
        dbRef.addValueEventListener(valueEventListener);

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
    }

    private void setInputLinearLayout() {
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

                Message chatMessage = new Message(mFirebaseAuth.getUid(), mMessageEditText.getText().toString(),
                        System.currentTimeMillis() / 1000, null);
                mMessagesReference.child(mChatID).push().setValue(chatMessage);
                final String messageWritten = mMessageEditText.getText().toString();
                mChatsReference.child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            if (chat != null) {
                                chat.setTimestamp(System.currentTimeMillis() / 1000);
                                chat.setLastMessage(messageWritten);
                                chat.setIsText("true");
                                if(chat.getSenderUID() == null){
                                    chat.setSenderUID(mFirebaseAuth.getUid());
                                    chat.setCounter(chat.getCounter() + 1);
                                }else{
                                    if(chat.getSenderUID().equals(mFirebaseAuth.getUid())) {
                                        chat.setCounter(chat.getCounter() + 1);
                                    }
                                    else{
                                        chat.setReceiverUID(chat.getSenderUID());
                                        chat.setSenderUID(mFirebaseAuth.getUid());
                                        chat.setCounter(1);
                                    }
                                    chat.setSenderUID(mFirebaseAuth.getUid());
                                }
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
                        Message chatMessage = new Message(mFirebaseAuth.getUid(), null, System.currentTimeMillis() / 1000,
                                downloadUrl.toString());

                        mMessagesReference.child(mChatID).push().setValue(chatMessage);
                        mChatsReference.child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Chat chat = dataSnapshot.getValue(Chat.class);
                                    if (chat != null) {
                                        chat.setTimestamp(System.currentTimeMillis() / 1000);
                                        chat.setLastMessage(getResources().getString(R.string.photo_message_chat));
                                        chat.setSenderUID(mFirebaseAuth.getUid());
                                        chat.setIsText("false");
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
