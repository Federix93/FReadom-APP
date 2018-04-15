package com.example.android.lab1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.firebase.ui.auth.ui.ProgressDialogHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class MainActivity extends AppCompatActivity {

    ImageView mToolbarIcon;
    TextInputLayout mUsernameTextInputLayout;
    TextInputLayout mEmailTextInputLayout;
    TextInputLayout mPhoneTextInputLayout;
    TextInputLayout mAddressTextInputLayout;
    TextInputLayout mShortBioTextInputLayout;
    ImageView mCircleImageView;
    Toolbar mToolbar;
    private FirebaseAuth mFirebaseAuth;

    public static final String ADDRESS_KEY = "ADDRESS";
    private static final int RC_SIGN_IN = 10;
    private Map<String, Object> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        if(mFirebaseAuth.getCurrentUser() == null){
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        mUsernameTextInputLayout = findViewById(R.id.username_text);

        mPhoneTextInputLayout = findViewById(R.id.phone_text);

        mEmailTextInputLayout = findViewById(R.id.email_text);

        mAddressTextInputLayout = findViewById(R.id.address_text_view);

        mCircleImageView = findViewById(R.id.profile_image);

        mShortBioTextInputLayout = findViewById(R.id.bio_text_edit);

        mToolbarIcon = findViewById(R.id.icon_toolbar);

        mToolbar = findViewById(R.id.toolbar_main_activity);
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);

        final GUIManager gui = new GUIManager(this);
        gui.execute();

        mToolbarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                if (mFirebaseAuth.getCurrentUser() != null)
                    if(data.get(User.Utils.POSITION_KEY) != null)
                        intent.putExtra(ADDRESS_KEY, data.get(User.Utils.POSITION_KEY).toString());
                startActivity(intent);
            }
        });
    }

    private class GUIManager extends AsyncTask<Void, Void, Void>{

        ProgressDialog progressDialog;
        Context mContext;

        public GUIManager(Context pContext){
            mContext = pContext;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            if(mFirebaseAuth.getCurrentUser() != null) {
                DocumentReference docRef = db.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                data = document.getData();
                                if(data == null)
                                    return;
                                if (mUsernameTextInputLayout.getEditText() != null) {
                                    if (data.get(User.Utils.USERNAME_KEY) != null)
                                        mUsernameTextInputLayout.getEditText().setText(data.get(User.Utils.USERNAME_KEY).toString());
                                    mUsernameTextInputLayout.getEditText().setKeyListener(null);
                                    mUsernameTextInputLayout.getEditText().setEnabled(false);
                                }
                                if (mEmailTextInputLayout.getEditText() != null) {
                                    if (data.get(User.Utils.EMAIL_KEY) != null)
                                        mEmailTextInputLayout.getEditText().setText(data.get(User.Utils.EMAIL_KEY).toString());
                                    mEmailTextInputLayout.getEditText().setKeyListener(null);
                                    mEmailTextInputLayout.getEditText().setEnabled(false);
                                }
                                if (mPhoneTextInputLayout.getEditText() != null) {
                                    if (data.get(User.Utils.PHONE_KEY) != null)
                                        mPhoneTextInputLayout.getEditText().setText(data.get(User.Utils.PHONE_KEY).toString());
                                    mPhoneTextInputLayout.getEditText().setKeyListener(null);
                                    mPhoneTextInputLayout.getEditText().setEnabled(false);
                                }

                                if (mShortBioTextInputLayout.getEditText() != null) {
                                    if (data.get(User.Utils.SHORTBIO_KEY) != null)
                                        mShortBioTextInputLayout.getEditText().setText(data.get(User.Utils.SHORTBIO_KEY).toString());
                                    mShortBioTextInputLayout.getEditText().setKeyListener(null);
                                    mShortBioTextInputLayout.getEditText().setEnabled(false);
                                }
                                if(data.get(User.Utils.PICTURE_KEY) != null) {
                                    Glide.with(getApplicationContext()).load(data.get(User.Utils.PICTURE_KEY))
                                            .apply(bitmapTransform(new CircleCrop()))
                                            .into(mCircleImageView);
                                }else {
                                    Glide.with(getApplicationContext()).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                                            .apply(bitmapTransform(new CircleCrop()))
                                            .into(mCircleImageView);
                                }
                                if (mAddressTextInputLayout.getEditText() != null) {
                                    if (data.get(User.Utils.POSITION_KEY) != null)
                                        mAddressTextInputLayout.getEditText().setText(data.get(User.Utils.POSITION_KEY).toString());
                                    mAddressTextInputLayout.getEditText().setKeyListener(null);
                                    mAddressTextInputLayout.getEditText().setEnabled(false);
                                }
                            }
                        }
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }
}
