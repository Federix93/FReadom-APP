package com.example.android.lab1.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.android.lab1.R;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.homepage.HomePageActivity;
import com.example.android.lab1.utils.NetworkConnectionReceiver;
import com.example.android.lab1.utils.Utilities;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ui.ProgressDialogHolder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 10;
    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_API_KEY = "36664d38d1ffa619b47a8b56069835d1";
    private final static String ALGOLIA_USER_INDEX = "users";
    private static Index algoliaIndex;
    ImageView mLogoImageView;
    ConstraintLayout mRootConstraintLayout;
    Button mLoginButton;
    Button mWithoutLoginButton;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private User mUser;

    private NetworkConnectionReceiver mNetworkConnectionBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mNetworkConnectionBroadcastReceiver = new NetworkConnectionReceiver(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        createNotificationChannel();

        if (user != null) {
            openHomePageActivity();
        }

        mLogoImageView = findViewById(R.id.logo);
        mRootConstraintLayout = findViewById(R.id.root);
        mLoginButton = findViewById(R.id.open_firebase_ui_button);
        mWithoutLoginButton = findViewById(R.id.open_without_login_button);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utilities.setupStatusBarColor(this);
        }

        Glide.with(this).load(R.drawable.liberty_statue_background).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mRootConstraintLayout.setBackground(resource);
            }
        });

        Glide.with(this)
                .load(R.drawable.logo).apply(new RequestOptions().transforms(new CircleCrop()))
                .transition(new DrawableTransitionOptions().transition(R.anim.button_animation))
                .into(mLogoImageView);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFirebaseUI();
                mLoginButton.setVisibility(View.GONE);
                mWithoutLoginButton.setVisibility(View.GONE);
            }
        });

        mWithoutLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHomePageActivity();
                //Toast.makeText(getApplicationContext(), "Function not yet implemented", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mNetworkConnectionBroadcastReceiver, filter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkConnectionBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                final FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    if (firebaseUser.getEmail() != null) {
                        final ProgressDialogHolder progressDialogHolder = new ProgressDialogHolder(SignInActivity.this);
                        progressDialogHolder.showLoadingDialog(R.string.fui_progress_dialog_signing_in);
                        final DocumentReference userDocumentReference = mFirebaseFirestore.collection("users").document(firebaseUser.getUid());
                        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<User>() {
                            @Nullable
                            @Override
                            public User apply(@NonNull Transaction transaction) {
                                User user = null;
                                try {
                                    user = transaction.get(userDocumentReference).toObject(User.class);
                                    // set image
                                    if (user == null)
                                        user = new User();
                                    if (user.getImage() == null)
                                        user.setImage(firebaseUser.getPhotoUrl() != null ?
                                                firebaseUser.getPhotoUrl().toString() : null);
                                    if (user.getUsername() == null)
                                        user.setUsername(firebaseUser.getDisplayName());
                                    if (user.getPhone() == null)
                                        user.setPhone(firebaseUser.getPhoneNumber());
                                    if (user.getEmail() == null)
                                        user.setEmail(firebaseUser.getEmail());
                                    if (user.getRating() == null)
                                        user.setRating(0f);
                                    if (user.getNumRatings() < 0)
                                        user.setNumRatings(0);

                                    transaction.set(userDocumentReference, user);

                                } catch (FirebaseFirestoreException e) {
                                    user = null;
                                }
                                return user;
                            }

                        }).addOnSuccessListener(new OnSuccessListener<User>() {
                            @Override
                            public void onSuccess(User userResult) {
                                mUser = userResult;
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference usersReference = firebaseDatabase.getReference().child("users");
                                if (mFirebaseAuth != null && mFirebaseAuth.getUid() != null
                                        && mUser != null) {
                                    String userID = mFirebaseAuth.getUid();
                                    setupAlgolia();
                                    loadUserOnAlgolia(userID);
                                    com.example.android.lab1.model.chatmodels.User user =
                                            new com.example.android.lab1.model.chatmodels.User(mUser.getUsername(), mUser.getImage(), FirebaseInstanceId.getInstance().getToken());
                                    usersReference.child(mFirebaseAuth.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                openHomePageActivity();
                                                if (progressDialogHolder.isProgressDialogShowing())
                                                    progressDialogHolder.dismissDialog();
                                            } else {
                                                if (progressDialogHolder.isProgressDialogShowing())
                                                    progressDialogHolder.dismissDialog();
                                                Snackbar.make(mRootConstraintLayout, R.string.error_message, Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });

                    }
                }
            } else {
                if (response == null) {
                    return;
                }
                if (response.getError() != null) {
                    mLoginButton.setVisibility(View.VISIBLE);
                    mWithoutLoginButton.setVisibility(View.VISIBLE);
                }
                mLoginButton.setVisibility(View.VISIBLE);
                mWithoutLoginButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupAlgolia() {
        Client algoliaClient = new Client(ALGOLIA_APP_ID, ALGOLIA_API_KEY);
        algoliaIndex = algoliaClient.getIndex(ALGOLIA_USER_INDEX);
    }

    private void loadUserOnAlgolia(String userID) {
        try {

            JSONObject algoliaUser = new JSONObject();

            if (mUser.getImage() != null)
                algoliaUser.put("image", mUser.getImage());
            algoliaUser.put("rating", mUser.getRating());

            algoliaIndex.addObjectAsync(algoliaUser, userID,
                    null);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openHomePageActivity() {
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void openFirebaseUI() {

        List<AuthUI.IdpConfig> providers = null;
        PackageManager pkManager = this.getPackageManager();
        try {
            PackageInfo pkgInfo = pkManager.getPackageInfo("com.twitter.android", 0);
            String getPkgInfo = pkgInfo.toString();

            if (!getPkgInfo.equals("com.twitter.android")) {
                providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.FacebookBuilder().build(),
                        new AuthUI.IdpConfig.TwitterBuilder().build());

            }
        } catch (PackageManager.NameNotFoundException e) {
            providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build());
        }

        if (providers != null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setTheme(R.style.LoginTheme)
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);

            //.setLogo(R.drawable.bookique)
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence requestName = getString(R.string.new_request_channel_title);
            String requestDescription = getString(R.string.new_request_channel_description);
            int requestImportance = NotificationManager.IMPORTANCE_DEFAULT;

            CharSequence messageName = getString(R.string.new_message_channel_title);
            String messageDescription = getString(R.string.new_message_channel_description);
            int messageImportance = NotificationManager.IMPORTANCE_DEFAULT;

            CharSequence loanName = getString(R.string.loan_channel_title);
            String loanDescription = getString(R.string.loan_channel_description);
            int loanImportance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel requestChannel = new NotificationChannel(Utilities.BOOK_REQUEST_CHANNEL_ID, requestName, requestImportance);
            requestChannel.setDescription(requestDescription);

            NotificationChannel messageChannel = new NotificationChannel(Utilities.NEW_MESSAGE_CHANNEL_ID, messageName, messageImportance);
            messageChannel.setDescription(messageDescription);

            NotificationChannel loanChannel = new NotificationChannel(Utilities.LOAN_CHANNEL_ID, loanName, loanImportance);
            messageChannel.setDescription(loanDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(requestChannel);
            notificationManager.createNotificationChannel(messageChannel);
            notificationManager.createNotificationChannel(loanChannel);
        }
    }
}
