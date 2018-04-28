package com.example.android.lab1.model;

import android.content.Context;
import android.support.v7.widget.AppCompatRatingBar;
import android.util.AttributeSet;

import com.algolia.instantsearch.ui.views.AlgoliaHitView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

public class UserRatingHitView extends AppCompatRatingBar implements AlgoliaHitView {


    public UserRatingHitView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
                    setRating(mUser.getRating());
                }
            });
        }

    }
}
