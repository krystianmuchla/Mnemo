package com.github.krystianmuchla.mnemo.http.id

import androidx.annotation.Keep

@Keep
data class SignInRequest(val login: String, val password: String)
