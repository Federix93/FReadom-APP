package com.example.android.lab1.utils;

import android.arch.lifecycle.LiveData;
import android.os.Handler;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class FirebaseQueryLiveDataFirestore extends LiveData<QuerySnapshot>{

    private boolean listenerRemovePending = false;
    private final Handler handler = new Handler();
    private final Query mQuery;
    private final ListenerFirebase mListener = new ListenerFirebase();
    private ListenerRegistration listenerRegistration;

    private final Runnable removeListener = new Runnable() {
        @Override
        public void run() {
            if(listenerRegistration != null)
                listenerRegistration.remove();
            listenerRemovePending = false;
        }
    };

    public FirebaseQueryLiveDataFirestore(Query query){
        mQuery = query;
    }

    @Override
    protected void onActive() {
        super.onActive();
        if (listenerRemovePending) {
            handler.removeCallbacks(removeListener);
        }
        else {
            listenerRegistration = mQuery.addSnapshotListener(mListener);
        }
        listenerRemovePending = false;
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        handler.postDelayed(removeListener, 3000);
        listenerRemovePending = true;
    }

    private class ListenerFirebase implements EventListener<QuerySnapshot>{
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            setValue(queryDocumentSnapshots);
        }
    }
}
