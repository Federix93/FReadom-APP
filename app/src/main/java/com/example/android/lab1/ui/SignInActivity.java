package com.example.android.lab1.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.android.lab1.FirebaseManager;
import com.example.android.lab1.GlideApp;
import com.example.android.lab1.utils.NetworkConnectionReceiver;
import com.example.android.lab1.R;
import com.example.android.lab1.ui.homepage.HomePageActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 10;
    private FirebaseAuth mFirebaseAuth;

    ImageView mLogoImageView;
    ConstraintLayout mRootConstraintLayout;
    Button mLoginButton;
    Button mWithoutLoginButton;

    private NetworkConnectionReceiver mNetworkConnectionBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mNetworkConnectionBroadcastReceiver = new NetworkConnectionReceiver(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        if(user != null){
            openHomePageActivity();
        }

        mLogoImageView = findViewById(R.id.logo);
        mRootConstraintLayout = findViewById(R.id.root);
        mLoginButton = findViewById(R.id.open_firebase_ui_button);
        mWithoutLoginButton = findViewById(R.id.open_without_login_button);

        GlideApp.with(this).load(R.drawable.background).diskCacheStrategy(DiskCacheStrategy.DATA).into(new SimpleTarget<Drawable>() {
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
                openHomePageActivity();
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
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                if (user != null) {
                    FirebaseManager.addUser(user);
                    openHomePageActivity();
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
