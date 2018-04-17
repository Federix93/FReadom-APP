package com.example.android.lab1.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.example.android.lab1.R;

public class Condition
{
    public enum Status
    {
        BAD, DECENT, GOOD, GREAT, NEW
    }

    private Status status;

    public Condition(Status s)
    {
        setStatus(s);
    }

    public Condition(String s)
    {
        setStatus(Status.valueOf(s));
    }

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
    }
}
