package com.github.krystianmuchla.mnemo.id

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity("session")
data class Session(
    @PrimaryKey val id: UUID,
    @ColumnInfo("login") var login: String,
    @ColumnInfo("token") var token: String
) : Parcelable {
    companion object {
        fun from(cookies: List<String>): Session? {
            val login: String
            val token: String
            try {
                login = extractCookieValue(cookies, "login")
                token = extractCookieValue(cookies, "token")
            } catch (noSuchElementException: NoSuchElementException) {
                return null
            }
            return Session(login, token)
        }

        private fun extractCookieValue(cookies: List<String>, key: String): String {
            val prefix = "$key="
            val cookie = cookies.first { it.startsWith(prefix) }
            val cookieValue = cookie.drop(prefix.length)
            val delimiterIndex = cookieValue.indexOfFirst { it == ';' }
            if (delimiterIndex >= 0) {
                return cookieValue.substring(0, delimiterIndex)
            }
            return cookieValue
        }
    }

    constructor(login: String, token: String) : this(UUID.randomUUID(), login, token)

    fun asCookie(): String {
        return "login=$login; token=$token"
    }
}
