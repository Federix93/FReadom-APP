package com.example.android.lab1.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.lab1.R
import com.example.android.lab1.model.Review
import com.example.android.lab1.model.User
import com.example.android.lab1.utils.NotificationUtilities
import com.example.android.lab1.utils.Utilities
import com.firebase.ui.auth.ui.ProgressDialogHolder
import com.google.firebase.firestore.FirebaseFirestore
import io.techery.properratingbar.ProperRatingBar


class ReviewActivity : AppCompatActivity() {
    private lateinit var mRatingBar: ProperRatingBar
    private lateinit var mCommentEditText: EditText
    private lateinit var mConfirmButton: AppCompatButton
    private lateinit var mToolbar: Toolbar
    private lateinit var mUserPhotoImageView: ImageView
    private lateinit var mUserName: TextView
    private var mProgressDialog : ProgressDialogHolder? = null

    private lateinit var mReviewerId: String
    private lateinit var mReviewedId: String
    private lateinit var mBookId: String

    private var mUploading: Boolean = false
    set(value) {
        field = value
        if (value) {
            mProgressDialog = ProgressDialogHolder(this)
            mProgressDialog?.showLoadingDialog(R.string.load_review)
        }
        else
        {
            if (mProgressDialog != null &&
                    mProgressDialog!!.isProgressDialogShowing)
                mProgressDialog!!.dismissDialog()
        }
    }
    private var mUserImageUrl: String? = null

    object Keys {
        const val REVIEWER_ID = "RERID"
        const val REVIEWED_ID = "REVIED"
        const val BOOK_ID = "BID"
        const val NUM_STARS = "NUMSTARS"
        const val NOTIFICATION_CMD = "NOTIFICATION_CMD"
        const val COMMENT_TEXT = "COMMENT"
        const val UPLOADING = "UPLOADING"
        const val USER_IMAGE = "USER_IMAGE"
        const val USERNAME = "USERNAME"
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null &&
                intent.hasExtra(Keys.REVIEWER_ID) &&
                intent.hasExtra(Keys.REVIEWED_ID) &&
                intent.hasExtra(Keys.BOOK_ID)) {
            mReviewerId = intent.getStringExtra(Keys.REVIEWER_ID)
            mReviewedId = intent.getStringExtra(Keys.REVIEWED_ID)
            mBookId = intent.getStringExtra(Keys.BOOK_ID)
            if (intent.hasExtra(Keys.NOTIFICATION_CMD))
            {
                if (NotificationUtilities.notificationExist(intent.getStringExtra(Keys.BOOK_ID)))
                    NotificationUtilities.removeNotification(intent.getStringExtra(Keys.BOOK_ID), this, false)
            }
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        setContentView(R.layout.activity_review)

        mRatingBar = findViewById(R.id.review_rating)
        mCommentEditText = findViewById(R.id.review_comment)
        mConfirmButton = findViewById(R.id.review_confirm)
        mToolbar = findViewById(R.id.review_toolbar)
        mUserPhotoImageView = findViewById(R.id.user_photo)
        mUserName = findViewById(R.id.user_name)

        setupToolbar()
        setupUser()
        setupConfirm()
    }

    private fun setupConfirm() {
        mConfirmButton.setOnClickListener {
            val failureFunction = { _: Exception ->
                mUploading = false
                Toast.makeText(applicationContext,
                        "Errore durante il caricamento, riprovare assicurandosi di avere una connessione ad internet attiva",
                        Toast.LENGTH_SHORT).show()
            }



            if (mRatingBar.rating > 0 && Utilities.isOnline(applicationContext)) {
                mUploading = true
                val firebaseFirestore = FirebaseFirestore.getInstance()
                val userReference = firebaseFirestore.collection("users")
                        .document(mReviewedId)
                val newRatingReference = userReference.collection("ratings")
                        .document()

                firebaseFirestore.runTransaction { transaction ->
                    val user = transaction[userReference].toObject(User::class.java)
                    //val batch = firebaseFirestore.batch()
                    if (user != null) {
                        val newReview = Review(reviewerId = mReviewerId,
                                bookId = mBookId,
                                text = mCommentEditText.text.toString().trimEnd(),
                                fiveStarRating = mRatingBar.rating.toFloat(),
                                timestamp = null)
                        transaction[newRatingReference] = newReview
                        val currentRating = user.rating ?: 0.0f
                        var numRatings = user.numRatings
                        val newRating = (currentRating * numRatings + newReview.fiveStarRating) /
                                (numRatings + 1)
                        user.rating = newRating
                        user.numRatings++
                        transaction[userReference] = user
                    }
                    //batch.commit()
                }.addOnSuccessListener {
                    Toast.makeText(applicationContext,
                            "Recensione pubblicata!",
                            Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }.addOnFailureListener(failureFunction)
            } else {
                Toast.makeText(applicationContext,
                        R.string.express_rating,
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUser() {
        FirebaseFirestore.getInstance().collection("users")
                .document(mReviewedId)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot != null && firebaseFirestoreException == null) {
                        val user = querySnapshot.toObject(User::class.java)
                        if (user != null) {
                            // load image and username
                            if (user.image != null) {
                                Glide.with(baseContext)
                                        .load(user.image)
                                        .apply(RequestOptions().circleCrop())
                                        .into(mUserPhotoImageView)
                                mUserImageUrl = user.image
                            } else
                                Glide.with(baseContext)
                                        .load(R.drawable.ic_account_circle_black_24dp)
                                        .apply(RequestOptions().circleCrop())
                                        .into(mUserPhotoImageView)
                            mUserName.text = user.username
                        }
                    }
                }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (outState != null) {
            with(outState) {
                putString(Keys.REVIEWER_ID, mReviewerId)
                putString(Keys.REVIEWED_ID, mReviewedId)
                putString(Keys.USERNAME, mUserName.text.toString())
                putString(Keys.USER_IMAGE, mUserImageUrl)
                putInt(Keys.NUM_STARS, mRatingBar.rating)
                putBoolean(Keys.UPLOADING, mUploading)
                if (mCommentEditText.text != null) {
                    putString(Keys.COMMENT_TEXT,
                            mCommentEditText.text.toString())
                }
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            with(savedInstanceState) {
                if (containsKey(Keys.REVIEWER_ID))
                    mReviewerId = getString(Keys.REVIEWER_ID)!!
                if (containsKey(Keys.REVIEWED_ID))
                    mReviewedId = getString(Keys.REVIEWED_ID)!!
                mUploading = getBoolean(Keys.UPLOADING)
                mCommentEditText.setText(getString(Keys.COMMENT_TEXT))
                mRatingBar.rating = getInt(Keys.NUM_STARS)
                mUserName.text = getString(Keys.USERNAME)
                mUserImageUrl = getString(Keys.USER_IMAGE)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupToolbar() {
        with(mToolbar) {
            Utilities.setupStatusBarColor(this@ReviewActivity)
            setTitleTextColor(ContextCompat.getColor(applicationContext,
                    R.color.black))
            setTitle(R.string.review_title)
            setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            setNavigationOnClickListener {
                if (!mUploading) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }
    }
}