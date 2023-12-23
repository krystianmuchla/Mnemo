package com.github.krystianmuchla.mnemo.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.krystianmuchla.mnemo.AppDatabase
import com.github.krystianmuchla.mnemo.databinding.NoteViewBinding
import com.github.krystianmuchla.mnemo.instant.InstantFactory

class EditNoteFragment : Fragment() {
    companion object {
        private const val REQUEST_KEY = "request_key"
        private const val NOTE = "note"
        fun newInstance(requestKey: String, note: Note): EditNoteFragment {
            return EditNoteFragment().apply {
                arguments = Bundle().apply {
                    putString(REQUEST_KEY, requestKey)
                    putParcelable(NOTE, note)
                }
            }
        }
    }

    private lateinit var noteDao: NoteDao
    private lateinit var requestKey: String
    private lateinit var note: Note
    private lateinit var view: NoteViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDao = AppDatabase.getInstance(requireContext()).noteDao()
        val arguments = requireArguments()
        requestKey = arguments.getString(REQUEST_KEY)!!
        note = arguments.getParcelable(NOTE)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = NoteViewBinding.inflate(inflater)
        view.title.setText(note.title)
        view.content.setText(note.content)
        return view.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val title = view.title.text.toString()
        val content = view.content.text.toString()
        if (title == note.title && content == note.content) return
        note.title = title
        note.content = content
        note.modificationTime = InstantFactory.create()
        noteDao.update(note)
        val bundle = Bundle(1)
        bundle.putParcelable(requestKey, note)
        parentFragmentManager.setFragmentResult(requestKey, bundle)
    }
}
