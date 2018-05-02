package com.example.android.lab1.model;

import android.content.Context;

import com.example.android.lab1.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GenreBook {

    private static List<String> mList;

    public static void setGenres(ArrayList<String> pList)
    {
        mList = pList;
    }

    public static List<String> getList(Context context){

        mList = Arrays.asList(context.getResources().getStringArray(R.array.genre));
        return mList;
    }
}
