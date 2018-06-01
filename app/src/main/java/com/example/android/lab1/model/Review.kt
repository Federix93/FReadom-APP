package com.example.android.lab1.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class Review(){

    lateinit var reviewerId : String
    lateinit var bookId : String
    var text : String? = null
    var fiveStarRating: Float = 0f
    @ServerTimestamp var timestamp : Date? = null

    constructor(reviewerId: String,
                bookId: String,
                text: String?,
                fiveStarRating: Float,
                timestamp: Date?) : this()
    {
        this.reviewerId = reviewerId
        this.bookId = bookId
        this.text = text
        this.fiveStarRating = fiveStarRating
        this.timestamp = timestamp
    }
}