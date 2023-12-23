package com.github.krystianmuchla.mnemo.note

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.krystianmuchla.mnemo.R
import com.google.android.material.card.MaterialCardView
import java.util.UUID

class NoteListViewAdapter(
    private val notes: List<Note>,
    private val onClick: (Note, MaterialCardView) -> Unit,
    private val onLongClick: (UUID, MaterialCardView) -> Boolean
) : RecyclerView.Adapter<NoteViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_row_view, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.title.text = notes[position].title
        holder.content.text = notes[position].content
        holder.note = notes[position]
        holder.onClick = onClick
        holder.onLongClick = onLongClick
    }
}
