package com.github.krystianmuchla.mnemo.id

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CookieDao {
    @Insert
    fun create(vararg cookie: Cookie)

    @Query("SELECT * FROM cookie")
    fun read(): Cookie?

    @Query("DELETE FROM cookie")
    fun delete()
}
