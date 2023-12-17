package com.github.krystianmuchla.mnemo.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.krystianmuchla.mnemo.AppDatabase
import com.github.krystianmuchla.mnemo.databinding.AddNoteViewBinding
import com.github.krystianmuchla.mnemo.instant.InstantFactory
import java.util.UUID

class AddNoteFragment : Fragment() {
    private lateinit var dao: NoteDao
    private lateinit var model: NoteViewModel
    private lateinit var view: AddNoteViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = AppDatabase.getInstance(requireContext()).noteDao()
        model = ViewModelProvider(this)[NoteViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = AddNoteViewBinding.inflate(inflater)
        view.title.requestFocus()
        view.title.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                requireActivity().getSystemService(InputMethodManager::class.java)
                    .showSoftInput(view.title, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        return view.root
    }

    override fun onDestroy() {
        super.onDestroy()
        val title = view.title.text.toString()
        val content = view.content.text.toString()
        if (title.isBlank() && content.isBlank()) return
        val creationTime = InstantFactory.create()
        val note = Note(UUID.randomUUID(), title, content, creationTime, creationTime)
        dao.create(note)
        model.notes = dao.read()
    }
}
