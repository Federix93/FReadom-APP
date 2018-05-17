//package com.example.android.lab1;
//
//import android.app.Activity;
//import android.content.Context;
//import android.support.annotation.NonNull;
//import android.util.Log;
//
//import com.algolia.search.saas.Client;
//import com.algolia.search.saas.Index;
//import com.algolia.search.saas.Request;
//import com.example.android.lab1.model.Book;
//import com.example.android.lab1.model.User;
//import com.example.android.lab1.ui.SignInActivity;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.EventListener;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.FirebaseFirestoreException;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//public class FirebaseManager {
//
//    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
//    private final static String ALGOLIA_API_KEY = "36664d38d1ffa619b47a8b56069835d1";
//    private final static String ALGOLIA_USER_INDEX = "users";
//
//    private static Index algoliaIndex;
//
//    public static void addUser(final FirebaseUser user) {
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        if (user.getEmail() != null) {
//            final DocumentReference documentReference = db.collection("users").document(user.getUid());
//            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                    if (!documentSnapshot.exists()) {
//                        final User userLocal = User.getInstance();
//                        if (user.getPhotoUrl() != null)
//                            userLocal.setImage(user.getPhotoUrl().toString());
//                        else
//                            userLocal.setImage(null);
//                        userLocal.setUsername(user.getDisplayName());
//                        userLocal.setPhone(user.getPhoneNumber());
//                        userLocal.setEmail(user.getEmail());
//                        userLocal.setRating(0.0f);
//                        documentReference.set(userLocal).addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Log.d("LULLO", "Document created");
//                                setupAlgolia();
//                                uploadOnAlgloia(userLocal, documentReference.getId());
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.d("LULLO", "Faileure " + e.getMessage());
//                            }
//                        });
//                    }
//                }
//            });
//        }
//    }
//    //}
//
//    private static void setupAlgolia() {
//        Client algoliaClient = new Client(ALGOLIA_APP_ID, ALGOLIA_API_KEY);
//        algoliaIndex = algoliaClient.getIndex(ALGOLIA_USER_INDEX);
//    }
//
//    private static void uploadOnAlgloia(User userToLoad, String userID) {
//
//        try {
//
//            JSONObject user = new JSONObject();
//
//            if(userToLoad.getImage() != null)
//                user.put("image", userToLoad.getImage());
//            user.put("rating", "0");
//
//            Request gni =
//            algoliaIndex.addObjectAsync(user, userID, null);
//
//            gni.
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//
//
//    }
//
//}
