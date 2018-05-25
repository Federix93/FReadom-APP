package com.example.android.lab1.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.Toolbar
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.lab1.R
import com.example.android.lab1.model.Book
import com.example.android.lab1.model.Review
import com.example.android.lab1.model.User
import com.example.android.lab1.utils.Utilities
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.FirebaseFirestore
import io.techery.properratingbar.ProperRatingBar


class ReviewActivity : AppCompatActivity() {
    private lateinit var mRatingBar: ProperRatingBar
    private lateinit var mCommentEditText: EditText
    private lateinit var mConfirmButton: AppCompatButton
    private lateinit var mToolbar: Toolbar
    private lateinit var mUserPhotoImageView: ImageView
    private lateinit var mUserName: TextView

    private lateinit var mReviewerId: String
    private lateinit var mReviewedId: String
    private lateinit var mBookId: String

    private var mUploading: Boolean = false
    private var mUserImageUrl: String? = null

    enum class ConstantKeys {
        REVIEWER_ID, REVIEWED_ID, BOOK_ID, NUM_STARS, COMMENT_TEXT, UPLOADING, USER_IMAGE, USERNAME
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null &&
                intent.hasExtra(ConstantKeys.REVIEWER_ID.toString()) &&
                intent.hasExtra(ConstantKeys.REVIEWED_ID.toString()) &&
                intent.hasExtra(ConstantKeys.BOOK_ID.toString())) {
            mReviewerId = intent.getStringExtra(ConstantKeys.REVIEWER_ID.toString())
            mReviewedId = intent.getStringExtra(ConstantKeys.REVIEWED_ID.toString())
            mBookId = intent.getStringExtra(ConstantKeys.BOOK_ID.toString())
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
            if (mRatingBar.rating > 0 && ! Utilities.isOnline(applicationContext)) {
                mUploading = true

                val failureFunction = {
                    mUploading = false
                    Toast.makeText(applicationContext,
                            "Errore durante il caricamento, riprovare assicurandosi di avere una connessione ad internet attiva",
                            Toast.LENGTH_SHORT).show()
                }

                val failureListener = OnFailureListener { failureFunction() }

                val firebaseFirestore = FirebaseFirestore.getInstance()

                firebaseFirestore.collection("books")
                        .document(mBookId)
                        .get().addOnCompleteListener { task ->
                            if (task.isSuccessful &&
                                    task.result.exists() &&
                                    task.result.toObject(Book::class.java) != null) {
                                val readBook = task.result.toObject(Book::class.java)!!
                                firebaseFirestore.collection("user-rating")
                                        .document(mReviewedId)
                                        .collection("ratings").add(Review(reviewerId = mReviewerId,
                                                bookId = mBookId,
                                                bookTitle = readBook.title,
                                                text = mCommentEditText.text.toString().trimEnd(),
                                                fiveStarRating = mRatingBar.rating.toFloat(),
                                                timestamp = null))
                                        .addOnCompleteListener { task2 ->
                                            if (task2.isSuccessful) {
                                                Toast.makeText(applicationContext,
                                                        "Recensione pubblicata!",
                                                        Toast.LENGTH_SHORT).show()
                                                setResult(RESULT_OK)
                                                finish()
                                            }
                                        }
                                        .addOnFailureListener(failureListener)
                            } else {
                                failureFunction()
                            }
                        }
                        .addOnFailureListener(failureListener)
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
                putString(ConstantKeys.REVIEWER_ID.toString(), mReviewerId)
                putString(ConstantKeys.REVIEWED_ID.toString(), mReviewedId)
                putString(ConstantKeys.USERNAME.toString(), mUserName.text.toString())
                putString(ConstantKeys.USER_IMAGE.toString(), mUserImageUrl)
                putInt(ConstantKeys.NUM_STARS.toString(), mRatingBar.rating)
                putBoolean(ConstantKeys.UPLOADING.toString(), mUploading)
                if (mCommentEditText.text != null) {
                    putString(ConstantKeys.COMMENT_TEXT.toString(),
                            mCommentEditText.text.toString())
                }
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            with(savedInstanceState) {
                if (containsKey(ConstantKeys.REVIEWER_ID.toString()))
                    mReviewerId = getString(ConstantKeys.REVIEWER_ID.toString())!!
                if (containsKey(ConstantKeys.REVIEWED_ID.toString()))
                    mReviewedId = getString(ConstantKeys.REVIEWED_ID.toString())!!
                mUploading = getBoolean(ConstantKeys.UPLOADING.toString())
                mCommentEditText.setText(getString(ConstantKeys.COMMENT_TEXT.toString()))
                mRatingBar.rating = getInt(ConstantKeys.NUM_STARS.toString())
                mUserName.text = getString(ConstantKeys.USERNAME.toString())
                mUserImageUrl = getString(ConstantKeys.USER_IMAGE.toString())
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