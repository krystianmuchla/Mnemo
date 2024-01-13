package com.github.krystianmuchla.mnemo

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request.Method
import com.android.volley.Response.ErrorListener
import com.android.volley.Response.Listener
import com.android.volley.toolbox.JsonObjectRequest
import com.github.krystianmuchla.mnemo.note.Note
import com.github.krystianmuchla.mnemo.note.notes
import org.json.JSONArray
import org.json.JSONObject

class RequestFactory {
    companion object {
        private const val URL = "http://192.168.0.69:80"

        fun createHealthRequest(
            listener: Listener<JSONObject>,
        ): JsonObjectRequest {
            val url = "$URL/api/health"
            val request = JsonObjectRequest(url, listener) {}
            request.setRetryPolicy(DefaultRetryPolicy(5000, 0, 1f))
            return request
        }

        fun createSyncNotesRequest(
            notes: List<Note>,
            listener: (List<Note>) -> Unit,
            errorListener: ErrorListener
        ): JsonObjectRequest {
            val url = "$URL/api/notes/sync"
            val requestBody = JSONObject()
            val notesRequest = JSONArray()
            notes.forEach {
                val noteRequest = JSONObject()
                noteRequest.put(Note.ID, it.id.toString())
                noteRequest.put(Note.TITLE, it.title)
                noteRequest.put(Note.CONTENT, it.content)
                noteRequest.put(Note.CREATION_TIME, it.creationTime?.toString())
                noteRequest.put(Note.MODIFICATION_TIME, it.modificationTime.toString())
                notesRequest.put(noteRequest)
            }
            requestBody.put("notes", notesRequest)
            val request = JsonObjectRequest(
                Method.PUT,
                url,
                requestBody,
                { response -> listener(notes(response.getJSONArray("notes"))) },
                errorListener
            )
            request.setRetryPolicy(DefaultRetryPolicy(10000, 0, 1f))
            return request
        }
    }
}
