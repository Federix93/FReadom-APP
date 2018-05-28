package com.example.android.lab1.utils.firebaseutils;

import android.arch.lifecycle.LiveData;
import android.os.Handler;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import javax.annotation.Nullable;

public class FirebaseDocumentSnapshotLiveDataFirestore extends LiveData<DocumentSnapshot> {

    private final DocumentReference mDocRef;
    private boolean listenerRemovePending = false;
    private final Handler handler = new Handler();
    private final ListenerFirebase mListener = new ListenerFirebase();
    private ListenerRegistration listenerRegistration;

    public FirebaseDocumentSnapshotLiveDataFirestore(DocumentReference docRef) {
        mDocRef = docRef;
    }

    private final Runnable removeListener = new Runnable() {
        @Override
        public void run() {
            if (listenerRegistration != null)
                listenerRegistration.remove();
            listenerRemovePending = false;
        }
    };

    @Override
    protected void onActive() {
        super.onActive();
        if (listenerRemovePending) {
            handler.removeCallbacks(removeListener);
        } else {
            listenerRegistration = mDocRef.addSnapshotListener(mListener);
        }
        listenerRemovePending = false;
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        handler.postDelayed(removeListener, 2000);
        listenerRemovePending = true;
    }

    private class ListenerFirebase implements EventListener<DocumentSnapshot> {
        @Override
        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
            setValue(snapshot);
        }
    }
}
