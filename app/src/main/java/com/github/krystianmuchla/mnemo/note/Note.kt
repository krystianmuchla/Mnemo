package com.github.krystianmuchla.mnemo.note

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.krystianmuchla.mnemo.instant.InstantFactory
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.ArrayList
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
    companion object {
        const val ID = "id"
        const val TITLE = "title"
        const val CONTENT = "content"
        const val CREATION_TIME = "creationTime"
        const val MODIFICATION_TIME = "modificationTime"
    }
}

fun note(title: String, content: String): Note {
    val creationTime = InstantFactory.create()
    return Note(UUID.randomUUID(), title, content, creationTime, creationTime)
}

fun notes(notes: JSONArray): List<Note> {
    val result = ArrayList<Note>()
    for (index in 0..<notes.length()) {
        result.add(note(notes.getJSONObject(index)))
    }
    return result
}

fun note(note: JSONObject): Note {
    val id = UUID.fromString(note.getString(Note.ID))
    val title = if (note.has(Note.TITLE)) note.getString(Note.TITLE) else null
    val content = if (note.has(Note.CONTENT)) note.getString(Note.CONTENT) else null
    val creationTime = if (note.has(Note.CREATION_TIME)) {
        Instant.parse(note.getString(Note.CREATION_TIME))
    } else {
        null
    }
    val modificationTime = Instant.parse(note.getString(Note.MODIFICATION_TIME))
    return Note(id, title, content, creationTime, modificationTime)
}
