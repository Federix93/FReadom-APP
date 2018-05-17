package com.example.android.lab1;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.SignInActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class FirebaseManager {

    public static void addUser(Activity activity, final FirebaseUser firebaseUser) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (firebaseUser.getEmail() != null) {
            final DocumentReference documentReference = db.collection("users").document(firebaseUser.getUid());
            User user = User.getInstance();
            if (firebaseUser.getPhotoUrl() != null)
                user.setImage(firebaseUser.getPhotoUrl().toString());
            else
                user.setImage(null);
            user.setUsername(firebaseUser.getDisplayName());
            user.setPhone(firebaseUser.getPhoneNumber());
            user.setEmail(firebaseUser.getEmail());
            user.setRating(0.0f);
            documentReference.set(user).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        }
    }
    //}

}
