package com.example.android.lab1.model;

import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.example.android.lab1.R;
import com.google.android.gms.internal.zzfgs;
import com.google.firebase.firestore.Blob;

import java.util.HashMap;
import java.util.Map;

public abstract class Condition {

    private static String BAD = "BAD";
    private static final String DECENT = "DECENT";
    private static final String GOOD = "GOOD";
    private static final String GREAT = "GREAT";
    private static final String NEW = "NEW";

    public static final Map<Integer, String> mMapcondition = new HashMap<Integer, String>();;

    static{
        mMapcondition.put(1, BAD);
        mMapcondition.put(2, DECENT);
        mMapcondition.put(3, GOOD);
        mMapcondition.put(4, GREAT);
        mMapcondition.put(5, NEW);
    }

    /*public enum Status
    {
        BAD, DECENT, GOOD, GREAT, NEW
    }

    private Status status;

    public String getStatus()
    {
        return status.toString();
    }

    public void setStatus(Status s)
    {
        this.status = s;
    }

    public static Integer getResource(Status s)
    {
        switch (s)
        {
            case BAD:
                return R.string.bad;
            case DECENT:
                return R.string.decent;
            case GOOD:
                return R.string.good;
            case GREAT:
                return R.string.great;
            case NEW:
                return R.string.neww;
        }
        return null;
    }*/
}
