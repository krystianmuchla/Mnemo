package com.github.krystianmuchla.mnemo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.krystianmuchla.mnemo.id.Cookie
import com.github.krystianmuchla.mnemo.id.CookieDao
import com.github.krystianmuchla.mnemo.instant.InstantConverter
import com.github.krystianmuchla.mnemo.note.Note
import com.github.krystianmuchla.mnemo.note.NoteDao

@Database(entities = [Note::class, Cookie::class], version = 1)
@TypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mnemo"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }

    abstract fun noteDao(): NoteDao

    abstract fun cookieDao(): CookieDao
}
