package com.github.krystianmuchla.mnemo.note

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.krystianmuchla.mnemo.R
import com.google.android.material.card.MaterialCardView
import java.util.UUID

class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    lateinit var note: Note
    lateinit var onClick: (Note, MaterialCardView) -> Unit
    lateinit var onLongClick: (UUID, MaterialCardView) -> Boolean
    val title: TextView
    val content: TextView

    init {
        title = view.findViewById(R.id.title)
        content = view.findViewById(R.id.content)
        view.setOnClickListener {
            onClick(note, it as MaterialCardView)
        }
        view.setOnLongClickListener {
            val consumed = onLongClick(note.id, it as MaterialCardView)
            consumed
        }
    }
}
