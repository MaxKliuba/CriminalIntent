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
) {
    fun getFormattedDataTime(context: Context?): String {
        val timePattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a"
        val pattern = "EEEE, MMM dd, yyyy, $timePattern"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return simpleDateFormat.format(date)
            .replaceFirstChar { it.uppercase() }
    }
}