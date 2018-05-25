package com.example.android.lab1.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.lab1.R
import com.example.android.lab1.model.Review
import com.example.android.lab1.model.User
import com.example.android.lab1.utils.dateToWhenAgo
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.techery.properratingbar.ProperRatingBar
import java.util.*


class ReviewListFragment : Fragment() {

    private lateinit var mReviewsRecyclerView: RecyclerView
    private lateinit var mNoReviewsTextView: TextView
    private lateinit var mLoadMoreProgressBar: ProgressBar
    var mReviewedId: String? = null
    private var mReviewsPerPage: Int = 10
    private var mDownloading: Boolean = false

    companion object {
        fun newInstance(userId: String): ReviewListFragment {
            val args = Bundle()
            args.putString(Constants.REVIEWED_ID.toString(), userId)

            val reviewListFragment = ReviewListFragment()
            reviewListFragment.arguments = args

            return reviewListFragment
        }
    }

    enum class Constants {
        REVIEWED_ID
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mReviewsPerPage = context?.resources?.getInteger(R.integer.reviews_per_page) ?: 10
        val rootView = layoutInflater.inflate(R.layout.fragment_review_list, container, false)
        findViews(rootView)
        mReviewedId = arguments?.getString(Constants.REVIEWED_ID.toString())

        if (mReviewedId != null) {
            setupRecyclerView()
            getData(null)
        } else {
            mReviewsRecyclerView.visibility = View.GONE
            mNoReviewsTextView.visibility = View.VISIBLE
        }



        return rootView
    }

    private fun setupRecyclerView() {
        val rvRecyclerManager = LinearLayoutManager(context)
        rvRecyclerManager.orientation = LinearLayoutManager.VERTICAL
        mReviewsRecyclerView.layoutManager = rvRecyclerManager
        val reviewsAdapter = ReviewsAdapter(context!!)
        mReviewsRecyclerView.adapter = reviewsAdapter

        class ReviewsScrollListener : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val itemCount = rvRecyclerManager.itemCount
                val lastVisiblePosition = rvRecyclerManager.findLastVisibleItemPosition()

                if (!mDownloading && itemCount <= (lastVisiblePosition
                                + mReviewsPerPage)) {
                    getData(reviewsAdapter.lastId)
                    mDownloading = true
                    mLoadMoreProgressBar.visibility = View.VISIBLE
                }

            }

        }
        mNoReviewsTextView.visibility = View.GONE
        mReviewsRecyclerView.visibility = View.VISIBLE
        mReviewsRecyclerView.addOnScrollListener(ReviewsScrollListener())
    }

    private fun findViews(rootView: View) {
        with(rootView)
        {
            mReviewsRecyclerView = findViewById(R.id.reviews_rv)
            mNoReviewsTextView = findViewById(R.id.reviews_no_reviews)
            mLoadMoreProgressBar = findViewById(R.id.reviews_load_new_content)
        }
    }

    fun getData(nodeId: String?) {

        val query = if (nodeId == null) // first query get firsts reviews
            FirebaseFirestore.getInstance()
                    .collection("user-rating")
                    .document(mReviewedId!!)
                    .collection("ratings")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(mReviewsPerPage.toLong())
        else {
            val documentReference = FirebaseFirestore.getInstance()
                    .collection("user-rating")
                    .document(mReviewedId!!)
                    .collection("rating")
            documentReference.orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(nodeId)
                    .limit(mReviewsPerPage.toLong())
        }

        query.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (querySnapshot != null && firebaseFirestoreException == null) {
                if (!querySnapshot.isEmpty) {
                    val reviews = mutableListOf<Review>()
                    val reviewsIDs = mutableListOf<String>()
                    val usersMap = mutableMapOf<String, User?>()
                    val reviewsAdapter = mReviewsRecyclerView.adapter as ReviewsAdapter
                    val currentUsers = reviewsAdapter.mUsers.keys

                    for (document in querySnapshot.documents) {
                        reviewsIDs.add(document.id)
                        val currentReview = document.toObject(Review::class.java)
                        if (currentReview != null) {
                            reviews.add(currentReview)
                            if (!currentUsers.contains(currentReview.reviewerId)) {
                                usersMap[currentReview.reviewerId] = null
                            }
                        }

                    }

                    var successfulTasks = 0
                    val numTask = usersMap.keys.size
                    for (userID in usersMap.keys) {
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userID)
                                .get()
                                .addOnCompleteListener {
                                    usersMap[userID] = it.result.toObject(User::class.java)
                                    successfulTasks++
                                    if (successfulTasks >= numTask) {
                                        // last user downloaded
                                        mDownloading = false
                                        mLoadMoreProgressBar.visibility = View.GONE
                                        reviewsAdapter.addAll(reviews,
                                                reviewsIDs,
                                                usersMap)
                                    }
                                }
                    }


                } else if (nodeId == null) {
                    // if first query didn't return anything
                    mDownloading = false
                    mLoadMoreProgressBar.visibility = View.GONE
                    mNoReviewsTextView.visibility = View.VISIBLE
                    mReviewsRecyclerView.visibility = View.GONE
                }
            } else {
                mDownloading = false
                if (nodeId == null) {
                    mNoReviewsTextView.visibility = View.VISIBLE
                    mReviewsRecyclerView.visibility = View.GONE
                    mLoadMoreProgressBar.visibility = View.GONE
                    mNoReviewsTextView.text = firebaseFirestoreException.toString()
                }
            }
        }

    }

    class ReviewsAdapter(private val mContext: Context) : RecyclerView.Adapter<ReviewViewHolder>() {

        private val mReviews: MutableList<Review> = mutableListOf()
        private val mReviewsIds: MutableList<String> = mutableListOf()
        val mUsers: MutableMap<String, User> = mutableMapOf()

        val lastId: String?
            get() {
                return if (mReviewsIds.isEmpty()) null else mReviewsIds[mReviewsIds.size - 1]
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
            val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val rootView = layoutInflater.inflate(R.layout.item_review, parent, false)
            return ReviewViewHolder(rootView, mContext)
        }

        override fun getItemCount(): Int {
            return mReviews.size
        }

        override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
            // find user
            holder.bind(mReviews[position], mUsers[mReviews[position].reviewerId])
        }

        fun addAll(reviews: List<Review>, reviewsID: List<String>, users: MutableMap<String, User?>) {
            val initialSize = reviews.size
            mReviews.addAll(reviews)
            mReviewsIds.addAll(reviewsID)
            for ((key, value) in users)
                if (value != null)
                    mUsers[key] = value
            notifyItemRangeInserted(initialSize, initialSize + reviews.size)
        }
    }

    class ReviewViewHolder(rootView: View?,
                           private val mContext: Context) : ViewHolder(rootView) {
        private lateinit var mUserImage: ImageView
        private lateinit var mReviewRating: ProperRatingBar
        private lateinit var mReviewTime: TextView
        private lateinit var mUserNameTextView: TextView
        private lateinit var mTitleTextView: TextView
        private lateinit var mUserReviewTextView: TextView

        init {
            if (rootView != null)
                with(rootView) {
                    mUserImage = findViewById(R.id.user_image)
                    mReviewRating = findViewById(R.id.review_rating)
                    mReviewTime = findViewById(R.id.review_when)
                    mUserNameTextView = findViewById(R.id.user_name)
                    mTitleTextView = findViewById(R.id.review_book_title)
                    mUserReviewTextView = findViewById(R.id.user_review)
                }
        }

        fun bind(review: Review,
                 user: User?) {
            Glide.with(mContext)
                    .load(user?.image ?: R.drawable.ic_account_circle_black_24dp)
                    .apply(RequestOptions().circleCrop())
                    .into(mUserImage)

            mUserNameTextView.text = user?.username
            mTitleTextView.text = review.bookTitle
            mReviewRating.rating = review.fiveStarRating.toInt()
            mTitleTextView.text = review.bookTitle

            with(mReviewTime) {
                if (review.timestamp != null) {
                    text = dateToWhenAgo(review.timestamp as Date, mContext)
                    if (text == null || text == "")
                        visibility = View.GONE
                } else
                    visibility = View.GONE
            }

            with(mUserNameTextView) {
                text = user?.username
                visibility = if (user != null && user.username != "")
                    View.VISIBLE else View.GONE
            }

            with(mUserReviewTextView)
            {
                text = review.text?.trimEnd()
                visibility = if (review.text != null) View.VISIBLE else View.GONE
            }
        }
    }
}

