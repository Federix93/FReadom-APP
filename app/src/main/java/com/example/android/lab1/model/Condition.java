package com.example.android.lab1.model;

import android.content.Context;

import com.example.android.lab1.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Condition {

    public static final Map<String, Integer> mMapcondition = new HashMap<String, Integer>();

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
    public static String getCondition (Context pContext, int cond) {
        switch (cond) {
            case 0:
                return pContext.getResources().getString(R.string.condition_poor);
            case 1:
                return pContext.getResources().getString(R.string.condition_fair);
            case 2:
                return pContext.getResources().getString(R.string.condition_good);
            case 3:
                return pContext.getResources().getString(R.string.condition_near_mint);
            case 4:
                return pContext.getResources().getString(R.string.condition_mint);
            default:
                break;
        }
        return null;
    }

    public static int getConditionColor (Context pContext, int cond) {
        switch (cond) {
            case 0:
                return pContext.getResources().getColor(R.color.poor);
            case 1:
                return pContext.getResources().getColor(R.color.fair);
            case 2:
                return pContext.getResources().getColor(R.color.good);
            case 3:
                return pContext.getResources().getColor(R.color.near_mint);
            case 4:
                return pContext.getResources().getColor(R.color.mint);
            default:
                break;
        }
        return 0;
    }
}
