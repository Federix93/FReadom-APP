package com.example.android.lab1.utils.firebaseutils;

import android.arch.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import javax.annotation.Nullable;

public class FirebaseDocumentSnapshotLiveDataFirestore extends LiveData<DocumentSnapshot>{

    private final DocumentReference mDocRef;
    private final ListenerFirebase mListener = new ListenerFirebase();
    private ListenerRegistration listenerRegistration;

    public FirebaseDocumentSnapshotLiveDataFirestore(DocumentReference docRef){
        mDocRef = docRef;
    }

    @Override
    protected void onActive() {
        super.onActive();
        listenerRegistration = mDocRef.addSnapshotListener(mListener);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if(listenerRegistration != null)
            listenerRegistration.remove();
    }

    private class ListenerFirebase implements EventListener<DocumentSnapshot>{
        @Override
        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
            setValue(snapshot);
        }
    }
}
