package com.github.krystianmuchla.mnemo.note

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.UUID

@Parcelize
@Entity("note")
data class Note(
    @PrimaryKey val id: UUID,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("content") val content: String?,
    @ColumnInfo("creation_time") val creationTime: Instant,
    @ColumnInfo("modification_time") val modificationTime: Instant
) : Parcelable
