package com.example.android.lab1;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.algolia.instantsearch.ui.views.AlgoliaHitView;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

public class BookCoverHitView extends AppCompatImageView implements AlgoliaHitView{

    public BookCoverHitView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void onUpdateView(JSONObject result) {

        String coverURL = result.optString("thumbnail");
        if(!coverURL.equals(""))
            Glide.with(getContext()).load(coverURL).into(this);

    }
}
