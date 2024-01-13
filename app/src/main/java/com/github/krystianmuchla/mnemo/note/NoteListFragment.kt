package com.github.krystianmuchla.mnemo.note

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.github.krystianmuchla.mnemo.AppDatabase
import com.github.krystianmuchla.mnemo.RequestFactory
import com.github.krystianmuchla.mnemo.R
import com.github.krystianmuchla.mnemo.databinding.NoteListViewBinding
import com.github.krystianmuchla.mnemo.instant.InstantFactory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.Instant
import java.util.LinkedList
import java.util.UUID
import java.util.stream.Collectors

class NoteListFragment : Fragment() {
    companion object {
        private val ADD_NOTE_REQUEST_KEY = UUID.randomUUID().toString()
        private val EDIT_NOTE_REQUEST_KEY = UUID.randomUUID().toString()
    }

    private val selectedNotes = HashSet<UUID>()
    private lateinit var noteDao: NoteDao
    private lateinit var notes: LinkedList<Note>
    private lateinit var requestQueue: RequestQueue
    private lateinit var view: NoteListViewBinding
    private lateinit var adapter: NoteListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDao = AppDatabase.getInstance(requireContext()).noteDao()
        notes = noteDao.read()
            .stream()
            .filter { it.content != null }
            .sorted(Comparator.comparing<Note?, Instant?> { it.modificationTime }.reversed())
            .collect(Collectors.toCollection { LinkedList() })
        requestQueue = Volley.newRequestQueue(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onNoteResult(ADD_NOTE_REQUEST_KEY) { addNote(it) }
        onNoteResult(EDIT_NOTE_REQUEST_KEY) { updateNote(it) }
        view = NoteListViewBinding.inflate(inflater)
        setUpNoteList()
        setUpListRefresh()
        setUpActionButton()
        return view.root
    }

    private fun setUpNoteList() {
        view.notes.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        adapter = NoteListViewAdapter(notes, ::onNoteClick, ::onNoteLongClick)
        view.notes.adapter = adapter
    }

    private fun setUpListRefresh() {
        view.refresh.isEnabled = false
        requestQueue.add(RequestFactory.createHealthRequest { view.refresh.isEnabled = true })
        view.refresh.setOnRefreshListener {
            val notes = noteDao.read()
            requestQueue.add(RequestFactory.createSyncNotesRequest(
                notes,
                { response ->
                    response.forEach { note ->
                        if (note.content == null) {
                            noteDao.delete(note.id)
                            removeNote(note.id)
                        } else {
                            if (notes.indexOfFirst { it.id == note.id } < 0) {
                                noteDao.create(note)
                                addNote(note)
                            } else {
                                noteDao.update(note)
                                updateNote(note)
                            }
                        }
                    }
                    noteDao.deleteEmptyNotes()
                    view.refresh.isRefreshing = false
                },
                {
                    view.refresh.isRefreshing = false
                }
            ))
        }
    }

    private fun setUpActionButton() {
        view.action.setOnClickListener {
            if (selectedNotes.isEmpty()) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, AddNoteFragment.newInstance(ADD_NOTE_REQUEST_KEY))
                    .addToBackStack(null)
                    .commit()
            } else {
                noteDao.emptyNotes(selectedNotes, InstantFactory.create())
                removeSelectedNotes()
                it as FloatingActionButton
                it.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.add))
            }
        }
    }

    private fun onNoteResult(requestKey: String, listener: (Note) -> (Unit)) {
        parentFragmentManager.setFragmentResultListener(
            requestKey,
            viewLifecycleOwner
        ) { requestId, bundle ->
            run {
                val note = bundle.getParcelable<Note>(requestId)
                note?.let(listener)
            }
        }
    }

    private fun onNoteClick(note: Note, noteView: MaterialCardView) {
        if (selectedNotes.isEmpty()) {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    EditNoteFragment.newInstance(EDIT_NOTE_REQUEST_KEY, note)
                )
                .addToBackStack(null)
                .commit()
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

    private fun onNoteLongClick(noteId: UUID, noteView: MaterialCardView): Boolean {
        if (selectedNotes.isEmpty()) {
            selectNote(noteId, noteView)
            view.action.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.remove
                )
            )
            return true
        }
        return false
    }

    private fun addNote(note: Note) {
        var index = notes.indexOfFirst { it.modificationTime < note.modificationTime }
        if (index < 0) index = notes.size
        notes.add(index, note)
        adapter.notifyItemInserted(index)
    }

    private fun updateNote(note: Note) {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index < 0) return
        notes.removeAt(index)
        var newIndex = notes.indexOfFirst { it.modificationTime < note.modificationTime }
        if (newIndex < 0) newIndex = notes.size
        notes.add(newIndex, note)
        adapter.notifyItemMoved(index, newIndex)
    }

    private fun removeSelectedNotes() {
        selectedNotes.forEach { removeNote(it) }
        selectedNotes.clear()
    }

    private fun removeNote(noteId: UUID) {
        val index = notes.indexOfFirst { it.id == noteId }
        if (index < 0) return
        notes.removeAt(index)
        adapter.notifyItemRemoved(index)
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
