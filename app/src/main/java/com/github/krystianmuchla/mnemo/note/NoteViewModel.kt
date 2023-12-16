package com.github.krystianmuchla.mnemo.note

import androidx.lifecycle.ViewModel

class NoteViewModel : ViewModel() {
    lateinit var notes: List<Note>
}
