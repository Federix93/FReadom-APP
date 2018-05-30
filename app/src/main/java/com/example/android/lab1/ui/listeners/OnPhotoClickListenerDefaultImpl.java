package com.example.android.lab1.ui.listeners;


import android.app.Activity;
import android.content.Intent;

import com.example.android.lab1.ui.BookPhotoDetailActivity;
import com.example.android.lab1.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class OnPhotoClickListenerDefaultImpl implements OnPhotoClickListener {

    private Activity mContainer;
    private List<String> mPhotoUrls;
    private String mBookTitle;

    public OnPhotoClickListenerDefaultImpl(Activity mContainer, List<String> mPhotoUrls, String mBookTitle) {
        this.mContainer = mContainer;
        this.mPhotoUrls = mPhotoUrls;
        this.mBookTitle = mBookTitle;
    }

    @Override
    public void onPhotoClicked(int position) {
        if (position >= 0 && position <= mPhotoUrls.size()) {
            Intent i = new Intent(mContainer, BookPhotoDetailActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra(BookPhotoDetailActivity.Keys.BOOK_TITLE.toString(),
                    mBookTitle);
            i.putStringArrayListExtra(BookPhotoDetailActivity.Keys.URLS.toString(),
                    (ArrayList<String>) mPhotoUrls);
            i.putExtra(BookPhotoDetailActivity.Keys.SELECTED.toString(),
                    position);
            mContainer.startActivityForResult(i, Constants.SHOW_DETAIL);
        }
    }
}
