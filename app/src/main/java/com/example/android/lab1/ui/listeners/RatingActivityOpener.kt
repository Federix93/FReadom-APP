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

    fun openActivity() {
        // TODO REMOVE ALL FUNCTION
        // aside function for testing reaso
        mReviewedId = "SMc8vP0GD8eBUK0Wty8zfTYEB7l1" //TODO remove
        mReviewerId = "SMc8vP0GD8eBUK0Wty8zfTYEB7l1" // TODO REMOVE
        val bookId = "4OGgy0KCkBBg4q8yb5mW"
        val intent = Utilities.getRatingIntent(mCurrentActivity,
                mReviewerId,
                mReviewedId,
                bookId)
        if (intent != null)
            mCurrentActivity.startActivityForResult(intent, Constants.RATING_REQUEST)
    }

}