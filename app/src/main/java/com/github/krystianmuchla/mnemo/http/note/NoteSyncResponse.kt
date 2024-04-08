package com.github.krystianmuchla.mnemo.http.note

import androidx.annotation.Keep

@Keep
data class NoteSyncResponse(val notes: List<NoteResponse>)
