package com.github.krystianmuchla.mnemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.krystianmuchla.mnemo.note.NoteFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, NoteFragment())
                .commitNow()
        }
    }
}
