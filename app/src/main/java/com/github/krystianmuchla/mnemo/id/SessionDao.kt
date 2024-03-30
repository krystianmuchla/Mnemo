package com.github.krystianmuchla.mnemo.id

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SessionDao {
    @Insert
    fun create(vararg session: Session)

    @Query("SELECT * FROM session")
    fun read(): Session?

    @Query("DELETE FROM session")
    fun delete()
}
