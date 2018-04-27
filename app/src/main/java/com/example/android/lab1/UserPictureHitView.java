package com.example.android.lab1;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import com.algolia.instantsearch.ui.views.AlgoliaHitView;
import com.bumptech.glide.Glide;
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
        mContext = context;
        thisObject = this;
    }

    @Override
    public void onUpdateView(JSONObject result) {

        String userID = result.optString("uid");


        if(!userID.equals(""))
        {
            FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference mDocRef = mFirebaseFirestore.collection("users").document(userID);
            mDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User mUser = document.toObject(User.class);
                            Glide.with(getContext()).load(mUser.getImage()).into(thisObject);
                        } else {
                            Log.d("ER", "No such document");
                        }
                    } else {
                        Log.d("ER", "get failed with ", task.getException());
                    }
                }
            });
            mDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                }
            });
        }
        else
        {
            Glide.with(getContext()).load(getResources().getDrawable(R.drawable.ic_no_book_photo)).into(this);
        }
    }
}
