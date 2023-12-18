package com.github.krystianmuchla.mnemo.note

import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.github.krystianmuchla.mnemo.R

class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    lateinit var note: Note
    val title: TextView
    val content: TextView

    init {
        title = view.findViewById(R.id.title)
        content = view.findViewById(R.id.content)
        view.setOnClickListener {
            Toast.makeText(view.context, "Edit note: ${note.id}", Toast.LENGTH_SHORT).show()
        }
    }
}
