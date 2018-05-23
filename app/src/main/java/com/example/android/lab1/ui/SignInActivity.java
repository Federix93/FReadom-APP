package com.example.android.lab1.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.homepage.HomePageActivity;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.utils.NetworkConnectionReceiver;
import com.example.android.lab1.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ui.ProgressDialogHolder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 10;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_API_KEY = "36664d38d1ffa619b47a8b56069835d1";
    private final static String ALGOLIA_USER_INDEX = "users";
    private static Index algoliaIndex;

    ImageView mLogoImageView;
    ConstraintLayout mRootConstraintLayout;
    Button mLoginButton;
    Button mWithoutLoginButton;
    private User mUser;

    private NetworkConnectionReceiver mNetworkConnectionBroadcastReceiver;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mNetworkConnectionBroadcastReceiver = new NetworkConnectionReceiver(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        if(user != null){
            openHomePageActivity();
        }

        mLogoImageView = findViewById(R.id.logo);
        mRootConstraintLayout = findViewById(R.id.root);
        mLoginButton = findViewById(R.id.open_firebase_ui_button);
        mWithoutLoginButton = findViewById(R.id.open_without_login_button);

        Utilities.setupStatusBarColor(this);

        Glide.with(this).load(R.drawable.background).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mRootConstraintLayout.setBackground(resource);
            }
        });

        Glide.with(this)
                .load(R.drawable.bookique).apply(new RequestOptions().transforms(new CenterCrop()))
                .transition(new DrawableTransitionOptions().transition(R.anim.button_animation))
                .into(mLogoImageView);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFirebaseUI();
            }
        });

        mWithoutLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openHomePageActivity();
                Toast.makeText(getApplicationContext(), "Function not yet implemented", Toast.LENGTH_SHORT).show();
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
                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    if (firebaseUser.getEmail() != null) {
                        final DocumentReference documentReference = mFirebaseFirestore.collection("users").document(firebaseUser.getUid());
                        mUser = User.getInstance();
                        if (firebaseUser.getPhotoUrl() != null)
                            mUser.setImage(firebaseUser.getPhotoUrl().toString());
                        else
                            mUser.setImage(null);
                        mUser.setUsername(firebaseUser.getDisplayName());
                        mUser.setPhone(firebaseUser.getPhoneNumber());
                        mUser.setEmail(firebaseUser.getEmail());
                        mUser.setRating(0.0f);
                        final ProgressDialogHolder progressDialogHolder = new ProgressDialogHolder(this);
                        progressDialogHolder.showLoadingDialog(R.string.fui_progress_dialog_signing_in);
                        documentReference.set(mUser).addOnSuccessListener(this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference usersReference = firebaseDatabase.getReference().child("users");
                                if (mFirebaseAuth != null && mFirebaseAuth.getUid() != null) {
                                    String userID = mFirebaseAuth.getUid();
                                    setupAlgolia();
                                    loadUserOnAlgolia(userID);
                                    com.example.android.lab1.model.chatmodels.User user =
                                            new com.example.android.lab1.model.chatmodels.User(mUser.getUsername(), mUser.getImage(), FirebaseInstanceId.getInstance().getToken());
                                    usersReference.child(mFirebaseAuth.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                if (progressDialogHolder.isProgressDialogShowing())
                                                    progressDialogHolder.dismissDialog();

                                                    openHomePageActivity();

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
                }
            }
        }
    }

    private boolean isEmailAndPasswordProvider() {
        for (UserInfo userInfo : mFirebaseAuth.getCurrentUser().getProviderData()) {
            if (userInfo.getProviderId().equals("password")) {
                return true;
            }
        }
        return false;
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
                            .setLogo(R.drawable.bookique)
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
    }
}
