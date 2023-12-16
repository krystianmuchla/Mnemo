package com.github.krystianmuchla.mnemo.note

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {
    @Insert
    fun create(vararg note: Note)

    @Query("SELECT * FROM note")
    fun read(): List<Note>

    @Update
    fun update(vararg note: Note)

    @Delete
    fun delete(vararg note: Note)
}
