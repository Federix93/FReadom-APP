package com.example.android.lab1.utils

import android.content.Context
import com.example.android.lab1.R
import java.util.*
import java.util.concurrent.TimeUnit

fun dateToWhenAgo(eventDate: Date,
                  context: Context): String? {
    // function that gets in input a date and returs the string indicating how much
    // [minutes | hours | days | week | months | years ] the input date is in respect to the current date
    val difference = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - eventDate.time)
    if (difference < 0) return null

    with(context) {
        return when (difference) {
            in 0..59 -> getString(R.string.less_than_a_minute)
            in 60..119 -> getString(R.string.a_minute_ago)
            in 120..3599 -> "${difference / 60} ${getString(R.string.minutes_ago).toLowerCase()}"
            in 3600..7199 -> getString(R.string.an_hour_ago)
            in 7200..85999 -> "${difference / 3600} ${getString(R.string.hours_ago).toLowerCase()}"
            in 86000..(86000 * 2 - 1) -> getString(R.string.a_day_ago)
            in (86000 * 2)..(86000 * 7 - 1) -> "${difference / 86000} ${getString(R.string.days_ago).toLowerCase()}"
            in (86000 * 7)..(86000 * 14 - 1) -> getString(R.string.a_week_ago)
            in (86000 * 14)..(86000 * 30 - 1) -> "${difference / (86000 * 7)} ${getString(R.string.weeks_ago).toLowerCase()}"
            in (86000 * 30)..(86000 * 30 * 2 - 1) -> getString(R.string.a_month_ago)
            in (86000 * 30 * 2)..(86000 * 30 * 12 - 1) -> "${difference / (86000 * 30)} ${getString(R.string.months_ago).toLowerCase()}"
            in (86000 * 30 * 12)..(86000 * 30 * 24 - 1) -> getString(R.string.a_year_ago)
            else -> {
                "${difference / (86000 * 30 * 12)} ${getString(R.string.years_ago).toLowerCase()}"
            }
        }
    }

}