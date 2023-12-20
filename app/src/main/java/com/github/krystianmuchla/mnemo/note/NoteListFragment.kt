package com.github.krystianmuchla.mnemo.note

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.krystianmuchla.mnemo.AppDatabase
import com.github.krystianmuchla.mnemo.R
import com.github.krystianmuchla.mnemo.databinding.NoteListViewBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.Instant
import java.util.LinkedList
import java.util.UUID
import java.util.stream.Collectors

class NoteListFragment : Fragment() {
    private val selectedNotes = HashSet<UUID>()
    private lateinit var noteDao: NoteDao
    private lateinit var notes: LinkedList<Note>
    private lateinit var adapter: NoteListViewAdapter

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
        val onNoteClick: (Note, MaterialCardView) -> Unit = { note, noteView ->
            if (selectedNotes.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Edit note: ${note.id}",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (selectedNotes.contains(note.id)) {
                if (selectedNotes.size == 1) {
                    view.action.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.add
                        )
                    )
                }
                deselectNote(note.id, noteView)
            } else {
                selectNote(note.id, noteView)
            }
        }
        val onNoteLongClick: (UUID, MaterialCardView) -> Boolean = { noteId, noteView ->
            if (selectedNotes.isEmpty()) {
                selectNote(noteId, noteView)
                view.action.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.remove
                    )
                )
                true
            } else {
                false
            }
        }
        view.notes.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        adapter = NoteListViewAdapter(notes, onNoteClick, onNoteLongClick)
        view.notes.adapter = adapter
        view.action.setOnClickListener {
            if (selectedNotes.isEmpty()) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, NewNoteFragment(javaClass.simpleName))
                    .addToBackStack(null)
                    .commit()
            } else {
                removeSelectedNotes(it as FloatingActionButton)
            }
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
        noteDao.create(note)
        val index = notes.indexOfFirst { it.modificationTime < note.modificationTime }
        if (index < 0) {
            notes.add(note)
        } else {
            notes.add(index, note)
        }
        adapter.notifyItemInserted(index)
    }

    private fun removeSelectedNotes(actionView: FloatingActionButton) {
        noteDao.delete(selectedNotes)
        selectedNotes.forEach {
            val index = notes.indexOfFirst { note -> note.id == it }
            notes.removeAt(index)
            adapter.notifyItemRemoved(index)
        }
        selectedNotes.clear()
        actionView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.add))
    }

    private fun selectNote(noteId: UUID, noteView: MaterialCardView) {
        noteView.strokeColor = Color.WHITE
        selectedNotes.add(noteId)
    }

    private fun deselectNote(noteId: UUID, noteView: MaterialCardView) {
        noteView.strokeColor = Color.TRANSPARENT
        selectedNotes.remove(noteId)
    }
}
