package com.github.krystianmuchla.mnemo.http.note

import androidx.annotation.Keep

@Keep
data class SyncNotesRequest(val notes: List<NoteRequest>)
