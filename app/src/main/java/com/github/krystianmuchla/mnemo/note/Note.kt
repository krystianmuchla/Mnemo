package com.github.krystianmuchla.mnemo.note

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.krystianmuchla.mnemo.instant.InstantFactory
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.UUID

@Parcelize
@Entity("note")
data class Note(
    @PrimaryKey val id: UUID,
    @ColumnInfo("title") var title: String?,
    @ColumnInfo("content") var content: String?,
    @ColumnInfo("creation_time") val creationTime: Instant?,
    @ColumnInfo("modification_time") var modificationTime: Instant
) : Parcelable {
    fun hasContent(): Boolean {
        return content != null
    }
}

fun note(title: String, content: String): Note {
    val creationTime = InstantFactory.create()
    return Note(UUID.randomUUID(), title, content, creationTime, creationTime)
}
