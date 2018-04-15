package com.example.android.lab1;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    public static void addUser(FirebaseUser user) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        boolean hasAccessInPasswordMode = true;
        boolean isPasswordMode = false;
        for (UserInfo userInfo : user.getProviderData()) {
            if (userInfo.getProviderId().equals("password")) {
                isPasswordMode = true;
                if (!user.isEmailVerified()) {
                    hasAccessInPasswordMode = false;
                }
            }
        }

        if (isPasswordMode && !hasAccessInPasswordMode)
            return;
        else {
            if (user.getEmail() != null) {

                final DocumentReference documentReference = db.collection("users").document(user.getUid());
                Map<String, Object> userToAddInFirebase = new HashMap<>();
                userToAddInFirebase.put(User.Utils.USERNAME_KEY, user.getDisplayName());
                userToAddInFirebase.put(User.Utils.EMAIL_KEY, user.getEmail());
                if (user.getPhotoUrl() != null)
                    userToAddInFirebase.put(User.Utils.PICTURE_KEY, user.getPhotoUrl().toString());
                else
                    userToAddInFirebase.put(User.Utils.PICTURE_KEY, null);
                userToAddInFirebase.put(User.Utils.PHONE_KEY, user.getPhoneNumber());
                userToAddInFirebase.put(User.Utils.POSITION_KEY, null);
                userToAddInFirebase.put(User.Utils.SHORTBIO_KEY, null);
                documentReference.set(userToAddInFirebase).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("LULLO", "Document created");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("LULLO", "Faileure " + e.getMessage());
                    }
                });
            }
        }
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

}
