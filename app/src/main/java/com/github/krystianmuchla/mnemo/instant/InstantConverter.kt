package com.github.krystianmuchla.mnemo.instant

import androidx.room.TypeConverter
import java.time.Instant

class InstantConverter {
    @TypeConverter
    fun toString(entityValue: Instant?): String? {
        return entityValue?.toString()
    }

    @TypeConverter
    fun fromString(databaseValue: String?): Instant? {
        return databaseValue?.let { Instant.parse(it) }
    }
}
