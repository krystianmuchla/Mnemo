package com.github.krystianmuchla.mnemo.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.krystianmuchla.mnemo.AppDatabase
import com.github.krystianmuchla.mnemo.R
import com.github.krystianmuchla.mnemo.databinding.NoteListViewBinding
import java.time.Instant
import java.util.LinkedList
import java.util.stream.Collectors

class NoteListFragment : Fragment() {
    private lateinit var noteDao: NoteDao
    private lateinit var notes: LinkedList<Note>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDao = AppDatabase.getInstance(requireContext()).noteDao()
        notes = getNotes()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onNewNoteResult { addNote(it) }
        val view = NoteListViewBinding.inflate(inflater)
        view.notes.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        view.notes.adapter =
            NoteListViewAdapter(notes)
        view.addNote.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, NewNoteFragment(javaClass.simpleName))
                .addToBackStack(null)
                .commit()
        }
        return view.root
    }

    private fun getNotes(): LinkedList<Note> {
        return noteDao.read()
            .stream()
            .filter { it.content != null }
            .sorted(Comparator.comparing<Note?, Instant?> { it.modificationTime }.reversed())
            .collect(Collectors.toCollection { LinkedList() })
    }

    private fun onNewNoteResult(listener: (Note) -> (Unit)) {
        parentFragmentManager.setFragmentResultListener(
            javaClass.simpleName,
            viewLifecycleOwner
        ) { requestId, bundle ->
            run {
                val note = bundle.getParcelable<Note>(requestId)
                note?.let(listener)
            }
        }
    }

    private fun addNote(note: Note) {
        val index = notes.indexOfFirst { it.modificationTime < note.modificationTime }
        notes.add(index, note)
        noteDao.create(note)
    }
}
