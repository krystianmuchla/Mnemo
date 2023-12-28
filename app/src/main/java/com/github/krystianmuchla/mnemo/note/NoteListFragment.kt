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
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request.Method
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.krystianmuchla.mnemo.AppDatabase
import com.github.krystianmuchla.mnemo.R
import com.github.krystianmuchla.mnemo.databinding.NoteListViewBinding
import com.github.krystianmuchla.mnemo.instant.InstantFactory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
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
    private lateinit var adapter: NoteListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDao = AppDatabase.getInstance(requireContext()).noteDao()
        notes = getNotes()
        requestQueue = Volley.newRequestQueue(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onNoteResult(ADD_NOTE_REQUEST_KEY) { addNote(it) }
        onNoteResult(EDIT_NOTE_REQUEST_KEY) { updateNote(it) }
        val view = NoteListViewBinding.inflate(inflater)
        val onNoteClick: (Note, MaterialCardView) -> Unit = { note, noteView ->
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
        view.refresh.setOnRefreshListener {
            val notes = noteDao.read()
            val request = JsonObjectRequest(
                Method.PUT,
                "http://192.168.0.127:8080/api/notes/sync",
                createRequestBody(notes),
                { response ->
                    val notesResponse = notes(response.getJSONArray("notes"))
                    notesResponse.forEach { noteResponse ->
                        if (noteResponse.content == null) {
                            noteDao.delete(noteResponse.id)
                            removeNote(noteResponse.id)
                        } else {
                            if (notes.indexOfFirst { it.id == noteResponse.id } < 0) {
                                noteDao.create(noteResponse)
                                addNote(noteResponse)
                            } else {
                                noteDao.update(noteResponse)
                                updateNote(noteResponse)
                            }
                        }
                    }
                    view.refresh.isRefreshing = false
                },
                {
                    view.refresh.isRefreshing = false
                }
            )
            request.setRetryPolicy(
                DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
            requestQueue.add(request)
        }
        view.action.setOnClickListener {
            if (selectedNotes.isEmpty()) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, AddNoteFragment.newInstance(ADD_NOTE_REQUEST_KEY))
                    .addToBackStack(null)
                    .commit()
            } else {
                noteDao.markAsDeleted(selectedNotes, InstantFactory.create())
                removeSelectedNotes()
                it as FloatingActionButton
                it.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.add))
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

    private fun createRequestBody(notes: List<Note>): JSONObject {
        val requestBody = JSONObject()
        val notesRequest = JSONArray()
        notes.forEach {
            val noteRequest = JSONObject()
            noteRequest.put(Note.ID, it.id.toString())
            noteRequest.put(Note.TITLE, it.title)
            noteRequest.put(Note.CONTENT, it.content)
            noteRequest.put(Note.CREATION_TIME, it.creationTime?.toString())
            noteRequest.put(Note.MODIFICATION_TIME, it.modificationTime.toString())
            notesRequest.put(noteRequest)
        }
        requestBody.put("notes", notesRequest)
        return requestBody
    }
}
