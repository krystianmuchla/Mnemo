package com.github.krystianmuchla.mnemo.note

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity("note")
data class Note(
    @PrimaryKey val id: UUID,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("content") val content: String?,
    @ColumnInfo("creation_time") val creationTime: Instant,
    @ColumnInfo("modification_time") val modificationTime: Instant
)
