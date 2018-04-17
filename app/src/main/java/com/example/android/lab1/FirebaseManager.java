package com.example.android.lab1;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    public static void addUser(final FirebaseUser user) {

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
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if(!documentSnapshot.exists()){
                            User userLocal = User.getInstance();
                            if(user.getPhotoUrl() != null)
                                userLocal.setImage(user.getPhotoUrl().toString());
                            else
                                userLocal.setImage(null);
                            userLocal.setUsername(user.getDisplayName());
                            userLocal.setPhone(user.getPhoneNumber());
                            userLocal.setEmail(user.getEmail());
                            documentReference.set(userLocal).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                });
            }
        }
    }

}
