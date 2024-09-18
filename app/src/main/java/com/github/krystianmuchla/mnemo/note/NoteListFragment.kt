package com.github.krystianmuchla.mnemo.note

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.krystianmuchla.mnemo.AppDatabase
import com.github.krystianmuchla.mnemo.R
import com.github.krystianmuchla.mnemo.databinding.NoteListViewBinding
import com.github.krystianmuchla.mnemo.http.ApiService
import com.github.krystianmuchla.mnemo.http.ServiceFactory
import com.github.krystianmuchla.mnemo.http.id.SignInRequest
import com.github.krystianmuchla.mnemo.http.note.NoteRequest
import com.github.krystianmuchla.mnemo.http.note.SyncNotesRequest
import com.github.krystianmuchla.mnemo.id.Session
import com.github.krystianmuchla.mnemo.id.SessionDao
import com.github.krystianmuchla.mnemo.id.SignInDialogFragment
import com.github.krystianmuchla.mnemo.instant.InstantFactory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Instant
import java.util.LinkedList
import java.util.UUID
import java.util.stream.Collectors

class NoteListFragment : Fragment() {
    companion object {
        private val ADD_NOTE_REQUEST_KEY = UUID.randomUUID().toString()
        private val EDIT_NOTE_REQUEST_KEY = UUID.randomUUID().toString()
        private val SIGN_IN_REQUEST_KEY = UUID.randomUUID().toString()
    }

    private val selectedNotes = HashSet<UUID>()
    private lateinit var noteDao: NoteDao
    private lateinit var notes: LinkedList<Note>
    private lateinit var sessionDao: SessionDao
    private lateinit var apiService: ApiService
    private lateinit var view: NoteListViewBinding
    private lateinit var adapter: NoteListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appDatabase = AppDatabase.getInstance(requireContext())
        noteDao = appDatabase.noteDao()
        notes = noteDao.read()
            .stream()
            .filter { it.hasContent() }
            .sorted(Comparator.comparing<Note?, Instant?> { it.contentsModificationTime }
                .reversed())
            .collect(Collectors.toCollection { LinkedList() })
        sessionDao = appDatabase.sessionDao()
        apiService = ServiceFactory.getInstance().create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onNoteResult(ADD_NOTE_REQUEST_KEY) { addNote(it) }
        onNoteResult(EDIT_NOTE_REQUEST_KEY) { updateNote(it) }
        onSignInResult()
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
        lifecycleScope.launch {
            try {
                val response = apiService.getHealth()
                if (response.isSuccessful) {
                    view.refresh.isEnabled = true
                }
            } catch (_: IOException) {
            }
        }
        view.refresh.setOnRefreshListener {
            val session = sessionDao.read()
            val request = SyncNotesRequest(noteDao.read().map { NoteRequest.from(it) })
            lifecycleScope.launch {
                try {
                    val response = apiService.syncNotes(session?.asCookie(), request)
                    if (response.isSuccessful) {
                        val responseBody = response.body()!!
                        val externalNotes = responseBody.notes.map { it.asNote() }
                        externalNotes.forEach { externalNote ->
                            if (notes.indexOfFirst { it.id == externalNote.id } < 0) {
                                if (externalNote.hasContent()) {
                                    noteDao.create(externalNote)
                                    addNote(externalNote)
                                }
                            } else {
                                if (externalNote.hasContent()) {
                                    noteDao.update(externalNote)
                                    updateNote(externalNote)
                                } else {
                                    noteDao.delete(externalNote.id)
                                    removeNote(externalNote.id)
                                }
                            }
                        }
                        noteDao.deleteEmptyNotes()
                    } else if (response.code() == 401) {
                        SignInDialogFragment.newInstance(SIGN_IN_REQUEST_KEY)
                            .show(childFragmentManager)
                    } else {
                        Toast.makeText(requireContext(), "External error", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (_: Exception) {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                } finally {
                    view.refresh.isRefreshing = false
                }
            }
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
        ) { _, bundle ->
            val note = bundle.getParcelable<Note>("note")
            note?.let(listener)
        }
    }

    private fun onSignInResult() {
        childFragmentManager.setFragmentResultListener(
            SIGN_IN_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            view.refresh.isRefreshing = true
            val login = bundle.getString("login")!!
            val password = bundle.getString("password")!!
            val request = SignInRequest(login, password)
            lifecycleScope.launch {
                try {
                    val response = apiService.signIn(request)
                    if (response.isSuccessful) {
                        val headers = response.headers().toMultimap()
                        val cookies = headers["Set-Cookie"]!!
                        val session = Session.from(cookies)!!
                        sessionDao.delete()
                        sessionDao.create(session)
                    } else if (response.code() == 401) {
                        Toast.makeText(requireContext(), "Bad credentials", Toast.LENGTH_SHORT)
                            .show()
                        SignInDialogFragment.newInstance(SIGN_IN_REQUEST_KEY)
                            .show(childFragmentManager)
                    } else {
                        Toast.makeText(requireContext(), "External error", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (_: IOException) {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                } finally {
                    view.refresh.isRefreshing = false
                }
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
        var index =
            notes.indexOfFirst { it.contentsModificationTime < note.contentsModificationTime }
        if (index < 0) index = notes.size
        notes.add(index, note)
        adapter.notifyItemInserted(index)
    }

    private fun updateNote(note: Note) {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index < 0) return
        notes.removeAt(index)
        var newIndex =
            notes.indexOfFirst { it.contentsModificationTime < note.contentsModificationTime }
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
