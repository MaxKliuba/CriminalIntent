package com.maxclub.android.criminalintent.database

import androidx.room.TypeConverter
import java.util.*

class CrimeTypeConverters {
    @TypeConverter
    fun fromDate(date: Date?) = date?.time

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?) = millisSinceEpoch?.let { Date(it) }

    @TypeConverter
    fun fromUUID(uuid: UUID?) = uuid?.toString()

    @TypeConverter
    fun toUUID(uuid: String?) = uuid?.let { UUID.fromString(it) }
}