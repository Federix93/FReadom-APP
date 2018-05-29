package com.example.android.lab1.ui.listeners

import android.app.Activity
import android.view.View
import com.example.android.lab1.utils.Constants
import com.example.android.lab1.utils.Utilities

class RatingActivityOpener(private val mCurrentActivity: Activity,
                           private var mReviewerId: String?,
                           private var mReviewedId: String,
                           private var mBookId: String) : View.OnClickListener {

    override fun onClick(v: View?) {
        if (v != null) {
            val intent = Utilities.getRatingIntent(mCurrentActivity,
                    mReviewerId,
                    mReviewedId,
                    mBookId)
            if (intent != null)
                mCurrentActivity.startActivityForResult(intent, Constants.RATING_REQUEST)
        }
    }
}