package com.github.krystianmuchla.mnemo.http

import com.github.krystianmuchla.mnemo.http.id.SignInRequest
import com.github.krystianmuchla.mnemo.http.note.NoteSyncResponse
import com.github.krystianmuchla.mnemo.http.note.SyncNotesRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @GET("api/health")
    suspend fun getHealth(): Response<Void>

    @PUT("api/notes/sync")
    suspend fun syncNotes(
        @Header("Cookie") cookie: String?,
        @Body request: SyncNotesRequest
    ): Response<NoteSyncResponse>

    @POST("api/id/sign_in")
    suspend fun signIn(@Body request: SignInRequest): Response<Void>
}
