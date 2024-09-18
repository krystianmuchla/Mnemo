package com.github.krystianmuchla.mnemo.http.note

import androidx.annotation.Keep
import com.github.krystianmuchla.mnemo.note.Note
import java.util.UUID

@Keep
data class NoteRequest(
    val id: UUID,
    val title: String?,
    val content: String?,
    val contentsModificationTime: String
) {
    companion object {
        fun from(note: Note): NoteRequest {
            return NoteRequest(
                note.id,
                note.title,
                note.content,
                note.contentsModificationTime.toString()
            )
        }
    }
}
