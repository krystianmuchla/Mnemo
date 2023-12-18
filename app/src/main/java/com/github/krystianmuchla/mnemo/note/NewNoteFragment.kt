package com.github.krystianmuchla.mnemo.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.github.krystianmuchla.mnemo.databinding.NewNoteViewBinding
import com.github.krystianmuchla.mnemo.instant.InstantFactory
import java.util.UUID

class NewNoteFragment(private val requestKey: String) : Fragment() {
    private lateinit var view: NewNoteViewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = NewNoteViewBinding.inflate(inflater)
        view.title.requestFocus()
        view.title.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                requireActivity().getSystemService(InputMethodManager::class.java)
                    .showSoftInput(view.title, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        return view.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val title = view.title.text.toString()
        val content = view.content.text.toString()
        if (title.isBlank() && content.isBlank()) return
        val bundle = Bundle(1)
        bundle.putParcelable(requestKey, createNote(title, content))
        parentFragmentManager.setFragmentResult(requestKey, bundle)
    }

    private fun createNote(title: String, content: String): Note {
        val creationTime = InstantFactory.create()
        return Note(UUID.randomUUID(), title, content, creationTime, creationTime)
    }
}
