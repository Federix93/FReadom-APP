package com.example.android.lab1.utils.firebaseutils;

import android.arch.lifecycle.LiveData;
import android.os.Handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.ListenerRegistration;

public class FirebaseQueryLiveDataRealtimeDB extends LiveData<DataSnapshot> {

    private boolean listenerRemovePending = false;
    private final Handler handler = new Handler();
    private final Query mQueryRealtime;
    private final ListenerFirebase mListener = new ListenerFirebase();

    public FirebaseQueryLiveDataRealtimeDB(DatabaseReference ref){
        mQueryRealtime = ref;
    }

    public FirebaseQueryLiveDataRealtimeDB(Query query){
        mQueryRealtime = query;
    }

    private final Runnable removeListener = new Runnable() {
        @Override
        public void run() {
            mQueryRealtime.removeEventListener(mListener);
            listenerRemovePending = false;
        }
    };

    @Override
    protected void onActive() {
        super.onActive();
        if (listenerRemovePending) {
            handler.removeCallbacks(removeListener);
        }
        else {
            mQueryRealtime.addValueEventListener(mListener);
        }
        listenerRemovePending = false;

    }

    @Override
    protected void onInactive() {
        super.onInactive();
        handler.postDelayed(removeListener, 2000);
        listenerRemovePending = true;
    }

    public class ListenerFirebase implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            setValue(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    }
}
