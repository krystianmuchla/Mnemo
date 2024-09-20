package com.github.krystianmuchla.mnemo.note

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.time.Instant
import java.util.UUID

@Dao
interface NoteDao {
    @Insert
    fun create(vararg note: Note)

    @Query("SELECT * FROM note")
    fun read(): List<Note>

    @Update
    fun update(vararg note: Note)

    @Query("UPDATE note SET content = NULL, modification_time = :modificationTime WHERE id = :id")
    fun remove(id: UUID, modificationTime: Instant)

    @Query("DELETE FROM note WHERE id = :id")
    fun delete(id: UUID)

    @Query("DELETE FROM note WHERE content IS NULL")
    fun deleteRemoved()
}
