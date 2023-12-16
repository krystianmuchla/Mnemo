package com.github.krystianmuchla.mnemo.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.krystianmuchla.mnemo.AppDatabase
import com.github.krystianmuchla.mnemo.databinding.NoteViewBinding

class NoteFragment : Fragment() {
    private lateinit var model: NoteViewModel
    private lateinit var view: NoteViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this)[NoteViewModel::class.java]
        model.notes = AppDatabase.getInstance(requireContext()).noteDao().read()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = NoteViewBinding.inflate(inflater)
        return view.root
    }
}
