package com.example.android.lab1;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    public static void addUser(FirebaseUser user) {
        Log.d("LULLO", "FIREBASE");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("LULLO", "EMAIL: " + user.isEmailVerified());
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
    }

}
