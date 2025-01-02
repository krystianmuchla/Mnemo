package com.github.krystianmuchla.mnemo.id

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity("cookie")
data class Cookie(
    @PrimaryKey val id: UUID,
    @ColumnInfo("value") var value: String
) : Parcelable {
    companion object {
        fun from(cookies: List<String>): Cookie {
            val value = cookies.map {
                val endIndex = it.indexOf(';')
                if (endIndex < 0) {
                    return@map it
                } else {
                    return@map it.substring(0, endIndex)
                }
            }.joinToString("; ")
            return Cookie(value)
        }
    }

    constructor(value: String) : this(UUID.randomUUID(), value)

    override fun toString(): String {
        return value
    }
}
