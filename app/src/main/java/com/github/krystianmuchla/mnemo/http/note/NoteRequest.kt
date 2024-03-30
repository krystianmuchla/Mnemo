package com.github.krystianmuchla.mnemo.http.note

import com.github.krystianmuchla.mnemo.note.Note
import java.util.UUID

data class NoteRequest(
    val id: UUID,
    val title: String?,
    val content: String?,
    val creationTime: String?,
    val modificationTime: String
) {
    companion object {
        fun from(note: Note): NoteRequest {
            return NoteRequest(
                note.id,
                note.title,
                note.content,
                note.creationTime?.toString(),
                note.modificationTime.toString()
            )
        }
    }
}
