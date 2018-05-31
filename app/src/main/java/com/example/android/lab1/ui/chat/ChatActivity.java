package com.example.android.lab1.ui.chat;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.example.android.lab1.ui.CalendarActivity;
import com.example.android.lab1.ui.profile.EditProfileActivity;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.viewmodel.ChatViewModel;
import com.example.android.lab1.viewmodel.MessagesViewModel;
import com.example.android.lab1.viewmodel.ViewModelFactory;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.android.lab1.utils.Constants.CAPTURE_IMAGE;
import static com.example.android.lab1.utils.Constants.RESULT_LOAD_IMAGE;

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
    TextView mNoMessagesOwnerTextView;
    TextView mNoMessagesReceiverTextView;
    LinearLayout mInputTextLinearLayout;

    AppCompatButton mStartLoan;
    LinearLayout mInfoLayout;

    ChildEventListener mChildEventListener;
    private ChatMessageAdapter mChatArrayAdapter;

    String dataTitle, dataMessage;
    private final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private final int RESULT_LOAD_IMAGE = 1;
    private final int CAPTURE_IMAGE = 0;

    private AlertDialog.Builder mAlertDialogBuilder = null;
    private File mPhotoFile = null;
    String mPhotoPath;
    private boolean isObservable = true;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Utilities.setupStatusBarColor(this);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mMessagesRecyclerView = findViewById(R.id.chat_recyclerView);
        mMessageEditText = findViewById(R.id.edittext_chat_message);
        mSendButton = findViewById(R.id.sendButton);
        mPhotoPickerButton = findViewById(R.id.buttonUpload);
        mInputTextLinearLayout = findViewById(R.id.linearLayoutBottomInput);
        mNoMessagesOwnerTextView = findViewById(R.id.chat_advice_owner_response_text_view);
        mNoMessagesReceiverTextView = findViewById(R.id.chat_wait_owner_response_text_view);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mChatsReference = mFirebaseDatabase.getReference().child("chats");
        mMessagesReference = mFirebaseDatabase.getReference().child("messages");
        mConversationsReference = mFirebaseDatabase.getReference().child("conversations");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");
        mStartLoan = findViewById(R.id.start_loan_button);
        mInfoLayout = findViewById(R.id.info_button_layout);

        mChatID = getIntent().getStringExtra("ChatID");
        mUsername = getIntent().getStringExtra("Username");
        mPhotoProfileURL = getIntent().getStringExtra("ImageURL");
        mBookID = getIntent().getStringExtra("BookID");
        String senderUID = getIntent().getStringExtra("SenderUID");

        if (senderUID != null && !senderUID.equals(mFirebaseAuth.getUid()))
            mChatsReference.child(mChatID).child("counter").setValue(0);

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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mMessagesRecyclerView.setHasFixedSize(true);
        mMessagesRecyclerView.setLayoutManager(layoutManager);
        mMessagesRecyclerView.setNestedScrollingEnabled(false);

        final ChatViewModel chatViewModel = ViewModelProviders.of(this, new ViewModelFactory(mChatID)).get(ChatViewModel.class);
        final MessagesViewModel messagesViewModel = ViewModelProviders.of(this, new ViewModelFactory(mChatID)).get(MessagesViewModel.class);

        final Observer<Message> chatObserver = new Observer<Message>() {
            @Override
            public void onChanged(@Nullable Message message) {
                Log.d("LULLO", "uuuuuuuuuuuuuuuuuu");
                if (mChatArrayAdapter != null && isObservable) {
                    Log.d("LULLO", "10000000000000");
                    mChatArrayAdapter.addMessage(message);
                    mChatArrayAdapter.notifyDataSetChanged();
                    mMessagesRecyclerView.smoothScrollToPosition(mChatArrayAdapter.getItemCount());
                }else{
                    isObservable = true;
                }
            }
        };

        final Observer<List<Message>> messageObserver = new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                Log.d("LULLO", "888888888888888");
                if (mChatArrayAdapter == null) {
                    Log.d("LULLO", "999999999999999999999");
                    mChatArrayAdapter = new ChatMessageAdapter(getApplicationContext(), messages);
                    mMessagesRecyclerView.setAdapter(mChatArrayAdapter);
                }
                mMessagesRecyclerView.smoothScrollToPosition(mChatArrayAdapter.getItemCount());
                messagesViewModel.getSnapshotLiveData().removeObserver(this);
                isObservable = false;
                chatViewModel.getSnapshotLiveData().observe(ChatActivity.this, chatObserver);
            }
        };
        messagesViewModel.getSnapshotLiveData().observe(this, messageObserver);



        final DatabaseReference dbRef = mMessagesReference.child(mChatID);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 1) {
                    if (mInputTextLinearLayout.getVisibility() == View.GONE) {
                        mInputTextLinearLayout.setVisibility(View.VISIBLE);
                        mNoMessagesReceiverTextView.setVisibility(View.GONE);
                    } else {
                        mNoMessagesOwnerTextView.setVisibility(View.GONE);
                    }
                    setInputLinearLayout();
                    dbRef.removeEventListener(this);

                } else {
                    mConversationsReference.child(mBookID).child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (mFirebaseAuth.getUid().equals(dataSnapshot.getValue())) {
                                mInputTextLinearLayout.setVisibility(View.GONE);
                                mNoMessagesReceiverTextView.setVisibility(View.VISIBLE);
                            } else {
                                if (mInputTextLinearLayout.getVisibility() == View.GONE) {
                                    mInputTextLinearLayout.setVisibility(View.VISIBLE);
                                }
                                mNoMessagesOwnerTextView.setVisibility(View.VISIBLE);
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


        /*mChildEventListener = new ChildEventListener() {
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
        */
        mStartLoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Boolean startLoan = true;
                intent.putExtra("start_loan", startLoan);
                startActivity(intent);
            }
        });
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
                                if (chat.getSenderUID() == null) {
                                    chat.setSenderUID(mFirebaseAuth.getUid());
                                    chat.setCounter(chat.getCounter() + 1);
                                } else {
                                    if (chat.getSenderUID().equals(mFirebaseAuth.getUid())) {
                                        chat.setCounter(chat.getCounter() + 1);
                                    } else {
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
                final CharSequence[] items = {getString(R.string.camera_option_dialog), getString(R.string.gallery_option_dialog)};
                mAlertDialogBuilder = new AlertDialog.Builder(view.getContext());
                mAlertDialogBuilder.setNegativeButton(R.string.negative_button_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                mAlertDialogBuilder.setTitle(R.string.upload_photo).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case CAPTURE_IMAGE:
                                if (!Utilities.checkPermissionActivity(ChatActivity.this,
                                        Manifest.permission.CAMERA)) {
                                    Utilities.askPermissionActivity(ChatActivity.this,
                                            Manifest.permission.CAMERA, CAPTURE_IMAGE);
                                } else {
                                    takePicture();
                                }
                                break;
                            case RESULT_LOAD_IMAGE:
                                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                galleryIntent.setType("image/*");
                                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                                break;
                            default:
                                dialogInterface.cancel();
                        }
                    }
                }).show();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAPTURE_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                }
        }
    }


    public File saveThumbnail() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private void takePicture() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                mPhotoFile = saveThumbnail();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mPhotoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.android.fileprovider",
                        mPhotoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
            startActivityForResult(cameraIntent, CAPTURE_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri photoUri = null;
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    mPhotoPath = data.getData().toString();
                    photoUri = data.getData();
                }
                break;
            case CAPTURE_IMAGE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mPhotoPath = mPhotoFile.getAbsolutePath();
                    photoUri = Uri.fromFile(mPhotoFile.getAbsoluteFile());
                }
                break;
        }

        if (photoUri != null) {
            StorageReference photoRef = mChatPhotosStorageReference.child(photoUri.getLastPathSegment());
            //Upload file to  Firebase Storage
            byte[] compressedImage = null;
            compressedImage = Utilities.compressPhoto(mPhotoPath,
                    getContentResolver(),
                    getApplicationContext());

            photoRef.putBytes(compressedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                    chat.setLastMessage(downloadUrl.toString());
                                    chat.setIsText("false");
                                    if (chat.getSenderUID() == null) {
                                        chat.setSenderUID(mFirebaseAuth.getUid());
                                        chat.setCounter(chat.getCounter() + 1);
                                    } else {
                                        if (chat.getSenderUID().equals(mFirebaseAuth.getUid())) {
                                            chat.setCounter(chat.getCounter() + 1);
                                        } else {
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
                }
            });
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

    @Override
    protected void onResume() {
        super.onResume();
        CurrentOpenChat.setOpenChatID(mChatID);
    }

    @Override
    protected void onStop() {
        super.onStop();
        CurrentOpenChat.setOpenChatID(null);
    }
}
