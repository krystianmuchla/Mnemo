package com.github.krystianmuchla.mnemo.http.note

import com.github.krystianmuchla.mnemo.note.Note
import java.time.Instant
import java.util.UUID

data class NoteResponse(
    val id: UUID,
    val title: String?,
    val content: String?,
    val creationTime: String?,
    val modificationTime: String
) {
    fun asNote(): Note {
        return Note(
            id,
            title,
            content,
            creationTime?.let { Instant.parse(it) },
            Instant.parse(modificationTime)
        )
    }
}
