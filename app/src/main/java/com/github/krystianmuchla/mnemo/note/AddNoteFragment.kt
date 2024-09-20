package com.github.krystianmuchla.mnemo.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.github.krystianmuchla.mnemo.databinding.NoteViewBinding

class AddNoteFragment : Fragment() {
    companion object {
        private const val REQUEST_KEY = "request_key"

        fun newInstance(requestKey: String): AddNoteFragment {
            return AddNoteFragment().apply {
                arguments = Bundle().apply {
                    putString(REQUEST_KEY, requestKey)
                }
            }
        }
    }

    private lateinit var requestKey: String
    private lateinit var view: NoteViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = requireArguments()
        requestKey = arguments.getString(REQUEST_KEY)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = NoteViewBinding.inflate(inflater)
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
        if (title.isBlank() && content.isBlank()) {
            return
        }
        val note = note(title, content)
        val bundle = Bundle(1)
        bundle.putParcelable("note", note)
        parentFragmentManager.setFragmentResult(requestKey, bundle)
    }
}
