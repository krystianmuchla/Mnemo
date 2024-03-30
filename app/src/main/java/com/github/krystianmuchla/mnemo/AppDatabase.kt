package com.github.krystianmuchla.mnemo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.krystianmuchla.mnemo.id.Session
import com.github.krystianmuchla.mnemo.id.SessionDao
import com.github.krystianmuchla.mnemo.instant.InstantConverter
import com.github.krystianmuchla.mnemo.note.Note
import com.github.krystianmuchla.mnemo.note.NoteDao

@Database(entities = [Note::class, Session::class], version = 2)
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
                ).addMigrations(MIGRATION_1_2).allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }

    abstract fun noteDao(): NoteDao

    abstract fun sessionDao(): SessionDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE `session` (`id` BLOB NOT NULL PRIMARY KEY, `login` TEXT NOT NULL, `token` TEXT NOT NULL)")
    }
}
