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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ChatMessageAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BookPhoto;
import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.Message;
import com.example.android.lab1.ui.CalendarActivity;
import com.example.android.lab1.ui.PhotoDetailActivity;
import com.example.android.lab1.ui.homepage.HomePageActivity;
import com.example.android.lab1.ui.listeners.RatingActivityOpener;
import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.NotificationUtilities;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.viewmodel.MessagesViewModel;
import com.example.android.lab1.viewmodel.ViewModelFactory;
import com.firebase.ui.auth.ui.ProgressDialogHolder;
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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ChatActivity extends AppCompatActivity {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_API_KEY = "36664d38d1ffa619b47a8b56069835d1";
    private final static String ALGOLIA_BOOK_INDEX = "books";
    private final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private final int RESULT_LOAD_IMAGE = 1;
    private final int CAPTURE_IMAGE = 0;
    Toolbar mToolbar;
    FirebaseDatabase mFirebaseDatabase;
    MessagesViewModel messagesViewModel = null;
    DatabaseReference mChatsReference;
    DatabaseReference mMessagesReference;
    DatabaseReference mConversationsReference;
    FirebaseAuth mFirebaseAuth;
    FirebaseStorage mFirebaseStorage;
    FirebaseFirestore mFirebaseFirestore;
    StorageReference mChatPhotosStorageReference;
    String mChatID;
    String mUsername;
    String mPhotoProfileURL;
    String mBookID;
    String mOtherPerson;
    String mSenderUID;
    RecyclerView mMessagesRecyclerView;
    EditText mMessageEditText;
    Button mSendButton;
    ImageButton mPhotoPickerButton;
    ImageView mToolbarProfileImage;
    TextView mToolbarProfileUsername;
    TextView mNoMessagesOwnerTextView;
    TextView mNoMessagesReceiverTextView;
    LinearLayout mInputTextLinearLayout;
    AppCompatButton mStartLoanButton;
    AppCompatButton mEndLoanButton;
    LinearLayout mInfoStartLoanLayout;
    LinearLayout mInfoEndLoanLayout;
    TextView mFromText;
    TextView mToText;
    TextView mLoanStartedText;
    TextView mLoanToBeEndedText;
    TextView mAlreadyLentMessage;
    ConstraintLayout mStartLoanLayout;
    ConstraintLayout mEndLoanLayout;
    ConstraintLayout mOtherPersonInfoLayout;
    ConstraintLayout mOtherPersonAlreadyLentLayout;
    String dataTitle, dataMessage;
    String mPhotoPath;
    private ChatMessageAdapter mChatArrayAdapter;
    private AlertDialog.Builder mAlertDialogBuilder = null;
    private File mPhotoFile = null;

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
                                    Log.d("LULLO", "111Send Button is clicked and SenderUID is: " + chat.getSenderUID());
                                    chat.setSenderUID(mFirebaseAuth.getUid());
                                    chat.setCounter(chat.getCounter() + 1);
                                    Log.d("LULLO", "111after send Button counter is: " + chat.getCounter());
                                } else {
                                    if (chat.getSenderUID().equals(mFirebaseAuth.getUid())) {
                                        chat.setCounter(chat.getCounter() + 1);
                                        Log.d("LULLO", "222Send Button is clicked and SenderUID is: " + chat.getSenderUID());
                                    } else {
                                        chat.setReceiverUID(chat.getSenderUID());
                                        chat.setSenderUID(mFirebaseAuth.getUid());
                                        chat.setCounter(1);
                                        Log.d("LULLO", "222after send Button counter is: " + chat.getCounter());
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
        mNoMessagesOwnerTextView = findViewById(R.id.chat_advice_owner_response_text_view);
        mNoMessagesReceiverTextView = findViewById(R.id.chat_wait_owner_response_text_view);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mChatsReference = mFirebaseDatabase.getReference().child("chats");
        mMessagesReference = mFirebaseDatabase.getReference().child("messages");
        mConversationsReference = mFirebaseDatabase.getReference().child("conversations");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");
        mStartLoanButton = findViewById(R.id.start_loan_button);
        mInfoStartLoanLayout = findViewById(R.id.info_button_layout);
        mEndLoanButton = findViewById(R.id.end_loan_button);
        mInfoEndLoanLayout = findViewById(R.id.info_button_layout_end);
        mFromText = findViewById(R.id.from_text);
        mToText = findViewById(R.id.to_text);
        mLoanStartedText = findViewById(R.id.date_from);
        mLoanToBeEndedText = findViewById(R.id.date_to);
        mStartLoanLayout = findViewById(R.id.chat_layout_container_loan_start);
        mEndLoanLayout = findViewById(R.id.chat_layout_container_loan_end);
        mOtherPersonInfoLayout = findViewById(R.id.other_user_informations);
        mOtherPersonAlreadyLentLayout = findViewById(R.id.other_user_info_already_lent);
        mAlreadyLentMessage = findViewById(R.id.already_lent_message);

        mChatID = getIntent().getStringExtra("ChatID");
        mUsername = getIntent().getStringExtra("Username");
        mPhotoProfileURL = getIntent().getStringExtra("ImageURL");
        mBookID = getIntent().getStringExtra("BookID");
        mSenderUID = getIntent().getStringExtra("SenderUID");

        if (getIntent().getBooleanExtra("FromNotification", false)) {
            if (NotificationUtilities.notificationExist(mChatID))
                NotificationUtilities.removeNotification(mChatID, this);
        }

        if (mSenderUID != null && !mSenderUID.equals(mFirebaseAuth.getUid()))
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

        mToolbarProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookPhoto bookPhoto = new BookPhoto(mPhotoProfileURL, "Profile image");
                Intent intent = new Intent(getApplicationContext(), PhotoDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(PhotoDetailActivity.BOOK_PHOTO, bookPhoto);
                startActivity(intent);
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mMessagesRecyclerView.setHasFixedSize(true);
        mMessagesRecyclerView.setLayoutManager(layoutManager);
        mMessagesRecyclerView.setNestedScrollingEnabled(false);

        messagesViewModel = ViewModelProviders.of(this, new ViewModelFactory(mChatID)).get(MessagesViewModel.class);


        final Observer<List<Message>> messageObserver = new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                if (mChatArrayAdapter == null) {
                    mChatArrayAdapter = new ChatMessageAdapter(getApplicationContext(), messages);
                    mMessagesRecyclerView.setAdapter(mChatArrayAdapter);
                } else {
                    mChatArrayAdapter.setItems(messages);
                    mChatArrayAdapter.notifyDataSetChanged();
                }
                Log.d("LULLO", "I'm observing");
                mMessagesRecyclerView.smoothScrollToPosition(mChatArrayAdapter.getItemCount());

            }
        };
        messagesViewModel.getSnapshotLiveData().observe(this, messageObserver);

        if (mChatID != null) {
            final DatabaseReference dbRef = mMessagesReference.child(mChatID);
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() > 1) {
                        if (mInputTextLinearLayout.getVisibility() == GONE) {
                            mInputTextLinearLayout.setVisibility(View.VISIBLE);
                            mNoMessagesReceiverTextView.setVisibility(GONE);
                        } else {
                            mNoMessagesOwnerTextView.setVisibility(GONE);
                        }
                        mConversationsReference.child(mBookID).child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null && dataSnapshot.exists()) {
                                    mOtherPerson = (String) dataSnapshot.getValue();

                                    if (mFirebaseAuth.getUid().equals(mOtherPerson)) {
                                        //SONO L'UTENTE RICHIEDENTE
                                        if (mEndLoanLayout.getVisibility() == VISIBLE)
                                            mEndLoanLayout.setVisibility(GONE);
                                        if (mStartLoanLayout.getVisibility() == VISIBLE)
                                            mStartLoanLayout.setVisibility(GONE);
                                        if (mOtherPersonAlreadyLentLayout.getVisibility() == VISIBLE)
                                            mOtherPersonAlreadyLentLayout.setVisibility(GONE);
                                        if (mOtherPersonInfoLayout.getVisibility() == VISIBLE)
                                            mOtherPersonInfoLayout.setVisibility(GONE);

                                        mFirebaseFirestore.collection("loans").document(mBookID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                                    Book book = documentSnapshot.toObject(Book.class);
                                                    if (book != null && book.getLentTo() != null) {
                                                        if (book.getLentTo().equals(mFirebaseAuth.getUid())) {
                                                            //SONO LA PERSONA RICHIEDENTE CHE HA RICEVUTO IL LIBRO
                                                            if (mOtherPersonInfoLayout.getVisibility() == GONE) {
                                                                String dateFrom = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanStart())).toString();
                                                                String dateTo = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanEnd())).toString();
                                                                mLoanStartedText.setText(String.format(getResources().getString(R.string.loan_started_from), dateFrom));
                                                                mLoanToBeEndedText.setText(String.format(getResources().getString(R.string.loan_to_be_closed), dateTo));
                                                                mOtherPersonInfoLayout.setVisibility(VISIBLE);
                                                            }
                                                        } else {
                                                            //SONO UN RICHIEDENTE AL QUALE IL LIBRO NON E' STATO DATO
                                                            mOtherPersonInfoLayout.setVisibility(GONE);
                                                            mEndLoanLayout.setVisibility(GONE);
                                                            mStartLoanLayout.setVisibility(GONE);
                                                            String dateTo = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanEnd())).toString();
                                                            mAlreadyLentMessage.setText(String.format(getResources().getString(R.string.already_lent_to_another_user_message), dateTo));
                                                            mOtherPersonAlreadyLentLayout.setVisibility(VISIBLE);
                                                            mInputTextLinearLayout.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    } else {
                                        //SONO IL BOOK OWNER
                                        mFirebaseFirestore.collection("loans").document(mBookID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                                    Book book = documentSnapshot.toObject(Book.class);
                                                    if (book != null && book.getLentTo() != null) {
                                                        if (book.getLentTo().equals(mOtherPerson)) {
                                                            mStartLoanLayout.setVisibility(GONE);
                                                            mOtherPersonAlreadyLentLayout.setVisibility(GONE);
                                                            mOtherPersonInfoLayout.setVisibility(GONE);

                                                            String dateFrom = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanStart())).toString();
                                                            String dateTo = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanEnd())).toString();
                                                            mFromText.setText(String.format(getResources().getString(R.string.loan_started_from), dateFrom));
                                                            mToText.setText(String.format(getResources().getString(R.string.loan_to_be_closed_owner), dateTo));
                                                            mEndLoanLayout.setVisibility(View.VISIBLE);
                                                        } else {
                                                            mStartLoanLayout.setVisibility(GONE);
                                                            mEndLoanLayout.setVisibility(GONE);
                                                            mOtherPersonAlreadyLentLayout.setVisibility(GONE);
                                                            mOtherPersonInfoLayout.setVisibility(GONE);
                                                        }
                                                    }
                                                } else {
                                                    mStartLoanLayout.setVisibility(View.VISIBLE);
                                                    if (mEndLoanLayout.getVisibility() == VISIBLE)
                                                        mEndLoanLayout.setVisibility(GONE);
                                                    if (mOtherPersonAlreadyLentLayout.getVisibility() == VISIBLE)
                                                        mOtherPersonAlreadyLentLayout.setVisibility(GONE);
                                                    if (mOtherPersonInfoLayout.getVisibility() == VISIBLE)
                                                        mOtherPersonInfoLayout.setVisibility(GONE);
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

                        setInputLinearLayout();
                        dbRef.removeEventListener(this);

                    } else {
                        mConversationsReference.child(mBookID).child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                mOtherPerson = (String) dataSnapshot.getValue();

                                if (mFirebaseAuth.getUid().equals(mOtherPerson)) {
                                    mFirebaseFirestore.collection("loans").document(mBookID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                                Book book = documentSnapshot.toObject(Book.class);
                                                if (book != null && book.getLentTo() != null) {
                                                    if (book.getLentTo().equals(mFirebaseAuth.getUid())) {
                                                        if (mOtherPersonInfoLayout.getVisibility() == GONE) {
                                                            String dateFrom = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanStart())).toString();
                                                            String dateTo = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanEnd())).toString();
                                                            mLoanStartedText.setText(String.format(getResources().getString(R.string.loan_started_from), dateFrom));
                                                            mLoanToBeEndedText.setText(String.format(getResources().getString(R.string.loan_to_be_closed), dateTo));
                                                            mOtherPersonInfoLayout.setVisibility(VISIBLE);
                                                        }

                                                        if (mEndLoanLayout.getVisibility() == VISIBLE)
                                                            mEndLoanLayout.setVisibility(GONE);
                                                        if (mStartLoanLayout.getVisibility() == VISIBLE)
                                                            mStartLoanLayout.setVisibility(GONE);
                                                        if (mOtherPersonAlreadyLentLayout.getVisibility() == VISIBLE)
                                                            mOtherPersonAlreadyLentLayout.setVisibility(GONE);
                                                    } else {
                                                        if (mOtherPersonAlreadyLentLayout.getVisibility() == GONE) {
                                                            String dateTo = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanEnd())).toString();
                                                            mAlreadyLentMessage.setText(String.format(getResources().getString(R.string.already_lent_to_another_user_message), dateTo));
                                                            mOtherPersonAlreadyLentLayout.setVisibility(VISIBLE);
                                                            mInputTextLinearLayout.setVisibility(View.GONE);
                                                        }

                                                        if (mEndLoanLayout.getVisibility() == VISIBLE)
                                                            mEndLoanLayout.setVisibility(GONE);
                                                        if (mStartLoanLayout.getVisibility() == VISIBLE)
                                                            mStartLoanLayout.setVisibility(GONE);
                                                        if (mOtherPersonInfoLayout.getVisibility() == VISIBLE)
                                                            mOtherPersonInfoLayout.setVisibility(GONE);
                                                    }
                                                }
                                            }
                                        }
                                    });
                                /*if (mEndLoanLayout.getVisibility() == VISIBLE)
                                    mEndLoanLayout.setVisibility(GONE);
                                if (mStartLoanLayout.getVisibility() == VISIBLE)
                                    mStartLoanLayout.setVisibility(GONE);
                                if (mOtherPersonInfoLayout.getVisibility() == VISIBLE)
                                    mOtherPersonInfoLayout.setVisibility(GONE);
                                if (mOtherPersonAlreadyLentLayout.getVisibility() == VISIBLE)
                                    mOtherPersonAlreadyLentLayout.setVisibility(GONE);*/

                                    mInputTextLinearLayout.setVisibility(GONE);
                                    mNoMessagesReceiverTextView.setVisibility(View.VISIBLE);
                                } else {
                                    if (mInputTextLinearLayout.getVisibility() == GONE) {
                                        mInputTextLinearLayout.setVisibility(View.VISIBLE);
                                    }
                                    mNoMessagesOwnerTextView.setVisibility(View.VISIBLE);

                                    mFirebaseFirestore.collection("loans").document(mBookID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                                Book book = documentSnapshot.toObject(Book.class);
                                                if (book != null && book.getLentTo() != null) {
                                                    if (book.getLentTo().equals(mOtherPerson)) {
                                                        //SONO IL PROPRIETARIO E HO APERTO UNA CONVERSAZIONE CON LA PERSONA
                                                        //ALLA QUALE HO PRESTATO IL LIBRO
                                                        mStartLoanLayout.setVisibility(GONE);
                                                        mOtherPersonInfoLayout.setVisibility(GONE);
                                                        mOtherPersonAlreadyLentLayout.setVisibility(GONE);

                                                        String dateFrom = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanStart())).toString();
                                                        String dateTo = DateFormat.format("dd/MM/yyyy", new Date(book.getLoanEnd())).toString();
                                                        mFromText.setText(String.format(getResources().getString(R.string.loan_started_from), dateFrom));
                                                        mToText.setText(String.format(getResources().getString(R.string.loan_to_be_closed_owner), dateTo));
                                                        mEndLoanLayout.setVisibility(View.VISIBLE);
                                                    } else {
                                                        //SONO IL PROPRIETARIO E HO APERTO UNA CONVERSAZIONE CON UN'ALTRA PERSONA DIVERSA DA
                                                        // QUELLA ALLA QUALE HO PRESTATO IL LIBRO
                                                        mStartLoanLayout.setVisibility(GONE);
                                                        mEndLoanLayout.setVisibility(GONE);
                                                        mOtherPersonInfoLayout.setVisibility(GONE);
                                                        mOtherPersonAlreadyLentLayout.setVisibility(GONE);
                                                    }
                                                }

                                            } else {
                                                mStartLoanLayout.setVisibility(View.VISIBLE);
                                                if (mEndLoanLayout.getVisibility() == VISIBLE)
                                                    mEndLoanLayout.setVisibility(GONE);
                                                if (mOtherPersonAlreadyLentLayout.getVisibility() == VISIBLE)
                                                    mOtherPersonAlreadyLentLayout.setVisibility(GONE);
                                                if (mOtherPersonInfoLayout.getVisibility() == VISIBLE)
                                                    mOtherPersonInfoLayout.setVisibility(GONE);
                                            }
                                        }
                                    });
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
        }
        mStartLoanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, CalendarActivity.CHOOSE_DATE);
            }
        });
        mInfoStartLoanLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(v.getContext()).create();
                alertDialog.setTitle(getResources().getString(R.string.loan_button_info_title));
                alertDialog.setMessage(getResources().getString(R.string.start_loan_button_info_text));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
        mEndLoanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(getResources().getString(R.string.confirm_end_title));
                builder.setMessage(getResources().getString(R.string.confirm_end_text));
                builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEndLoanLayout.setVisibility(GONE);
                        final ProgressDialogHolder progressDialogHolder = new ProgressDialogHolder(ChatActivity.this);
                        progressDialogHolder.showLoadingDialog(R.string.end_loan_progress);
                        mConversationsReference.child(mBookID).child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null) {
                                    mOtherPerson = (String) dataSnapshot.getValue();
                                    if (mOtherPerson != null) {
                                        final DocumentReference docHistoryRef = mFirebaseFirestore.collection("history").document();
                                        final String id = docHistoryRef.getId();

                                        final DocumentReference docLoansRef = mFirebaseFirestore.collection("loans").document(mBookID);
                                        mFirebaseFirestore.collection("loans").document(mBookID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                                    Book book = documentSnapshot.toObject(Book.class);
                                                    if (book != null) {
                                                        long time = System.currentTimeMillis();
                                                        book.setLoanEnd(time);
                                                        docHistoryRef.set(book, SetOptions.merge());
                                                        DocumentReference docBookRef = mFirebaseFirestore.collection("books").document(book.getBookID());
                                                        book.setLoanStart(Long.valueOf(-1));
                                                        book.setLoanEnd(Long.valueOf(-1));
                                                        book.setLentTo(null);
                                                        setBookLoanAlgolia(null);
                                                        docBookRef.set(book, SetOptions.merge());
                                                        docLoansRef.delete();
                                                        if (progressDialogHolder.isProgressDialogShowing())
                                                            progressDialogHolder.dismissDialog();
                                                        RatingActivityOpener ratingActivityOpener = new RatingActivityOpener(ChatActivity.this, mFirebaseAuth.getUid(), mOtherPerson, book.getBookID());
                                                        ratingActivityOpener.onClick(v);
                                                    }
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
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create();
                builder.show();
            }
        });
        mInfoEndLoanLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(v.getContext()).create();
                alertDialog.setTitle(getResources().getString(R.string.loan_button_info_title));
                alertDialog.setMessage(getResources().getString(R.string.end_loan_button_info_text));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
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
            case CalendarActivity.CHOOSE_DATE:
                if (resultCode == RESULT_OK && data != null) {
                    final ProgressDialogHolder progressDialogHolder = new ProgressDialogHolder(this);
                    progressDialogHolder.showLoadingDialog(R.string.start_loan_progress);
                    final long firstDate;
                    final long lastDate;
                    if (data.getExtras().getLong(CalendarActivity.FIRST_DATE) != 0 && data.getExtras().getLong(CalendarActivity.LAST_DATE) != 0) {
                        firstDate = data.getExtras().getLong(CalendarActivity.FIRST_DATE);
                        lastDate = data.getExtras().getLong(CalendarActivity.LAST_DATE);

                        mConversationsReference.child(mBookID).child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null) {
                                    mOtherPerson = (String) dataSnapshot.getValue();
                                    if (mOtherPerson != null) {
                                        mFirebaseFirestore.collection("books").document(mBookID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                                    Book book = documentSnapshot.toObject(Book.class);
                                                    if (book != null) {
                                                        book.setLoanStart(firstDate);
                                                        book.setLoanEnd(lastDate);
                                                        book.setLentTo(mOtherPerson);
                                                        setBookLoanAlgolia(book.getLentTo());
                                                        final DocumentReference docLoanRef = mFirebaseFirestore.collection("loans").document(mBookID);
                                                        docLoanRef.set(book, SetOptions.merge());
                                                        final DocumentReference docBookRef = mFirebaseFirestore.collection("books").document(mBookID);
                                                        docBookRef.set(book, SetOptions.merge());
                                                        DocumentReference reqDone = mFirebaseFirestore.collection("requestsDone").document(mOtherPerson).collection("books").document(mBookID);
                                                        reqDone.delete();
                                                        DocumentReference reqReceived = mFirebaseFirestore.collection("requestsReceived").document(book.getUid()).collection("books").document(mBookID);
                                                        reqReceived.delete();
                                                        Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                                                        if (progressDialogHolder.isProgressDialogShowing())
                                                            progressDialogHolder.dismissDialog();
                                                        intent.putExtra("LoanStart", true);
                                                        startActivity(intent);
                                                        finish();
                                                    }
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
                break;
            case Constants.RATING_REQUEST:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                    intent.putExtra("LoanEnd", true);
                    startActivity(intent);
                    this.finish();
                } else if (resultCode == RESULT_CANCELED) {
                    Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                    startActivity(intent);
                    this.finish();
                }
            default:
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

    private void setBookLoanAlgolia(String lentTo) {
        try {
            Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_API_KEY);
            Index books = client.getIndex(ALGOLIA_BOOK_INDEX);
            JSONObject toLoad = new JSONObject()
                    .put("lentTo", lentTo != null ? lentTo : "none");
            books.partialUpdateObjectAsync(toLoad, mBookID, null);
        } catch (JSONException e) {
            e.printStackTrace();
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
