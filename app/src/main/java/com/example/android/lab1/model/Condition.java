package com.example.android.lab1.model;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.example.android.lab1.R;
import com.google.android.gms.internal.zzfgs;
import com.google.firebase.firestore.Blob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Condition {


    public static final Map<String, Integer> mMapcondition = new HashMap<String, Integer>();;

    public static ArrayList<String> getConditions(Context pContext)
    {
        ArrayList<String> list = new ArrayList<>();
        list.add(pContext.getResources().getString(R.string.condition_poor));
        list.add(pContext.getResources().getString(R.string.condition_fair));
        list.add(pContext.getResources().getString(R.string.condition_good));
        list.add(pContext.getResources().getString(R.string.condition_near_mint));
        list.add(pContext.getResources().getString(R.string.condition_mint));
        return list;
    }
}
