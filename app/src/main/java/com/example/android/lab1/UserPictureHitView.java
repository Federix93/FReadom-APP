package com.example.android.lab1;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import com.algolia.instantsearch.ui.views.AlgoliaHitView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONObject;


public class UserPictureHitView extends AppCompatImageView implements AlgoliaHitView {

    Context mContext;
    UserPictureHitView thisObject;

    public UserPictureHitView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        thisObject = this;
    }

    @Override
    public void onUpdateView(JSONObject result) {

        String userID = result.optString("uid");


        if(!userID.equals(""))
        {
            FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference mDocRef = mFirebaseFirestore.collection("users").document(userID);
            mDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User mUser = documentSnapshot.toObject(User.class);
                    Glide.with(getContext()).load(mUser.getImage()).apply(RequestOptions.circleCropTransform()).into(thisObject);
                }
            });
        }

    }
}
