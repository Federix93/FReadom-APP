package com.example.android.lab1.utils.firebaseutils;

import android.arch.lifecycle.LiveData;
import android.os.Handler;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseChildLiveDataRealtimeDB extends LiveData<DataSnapshot> {

    private boolean listenerRemovePending = false;
    private final Handler handler = new Handler();
    private final Query mQueryRealtime;
    private final ListenerFirebase mListener = new ListenerFirebase();

    public FirebaseChildLiveDataRealtimeDB(DatabaseReference ref){
        mQueryRealtime = ref;
    }

    public FirebaseChildLiveDataRealtimeDB(Query query){
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
            mQueryRealtime.addChildEventListener(mListener);
        }
        listenerRemovePending = false;

    }

    @Override
    protected void onInactive() {
        super.onInactive();
        handler.postDelayed(removeListener, 2000);
        listenerRemovePending = true;
    }

    public class ListenerFirebase implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            setValue(dataSnapshot);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
