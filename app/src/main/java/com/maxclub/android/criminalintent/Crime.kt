package com.maxclub.android.criminalintent

import android.content.Context
import android.text.format.DateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class Crime(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var suspect: String = "",
) {
    fun getFormattedDate(): String {
        val pattern = "EEEE, MMM dd, yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return simpleDateFormat.format(date)
            .replaceFirstChar { it.uppercase() }
    }

    fun getFormattedTime(context: Context?): String {
        val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return simpleDateFormat.format(date)
    }

    fun getFormattedDateTime(context: Context?): String {
        return "${getFormattedDate()}, ${getFormattedTime(context)}"
    }

    val photoFileMame
        get() = "IMG_$id.jpg"
}