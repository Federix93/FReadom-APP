package com.example.android.lab1.ui.searchbooks;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.algolia.instantsearch.ui.views.AlgoliaHitView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;


public class UserPictureHitView extends AppCompatImageView implements AlgoliaHitView {

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
