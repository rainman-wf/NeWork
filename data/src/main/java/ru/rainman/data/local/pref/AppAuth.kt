package ru.rainman.data.local.pref

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.rainman.domain.model.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    private val prefs: SharedPreferences
) {

    private val idKey = "id_key"
    private val tokenKey = "token_key"

    private val _token: MutableStateFlow<Token?> = MutableStateFlow(getToken())
    val token: StateFlow<Token?> get() = _token
    val tokenValue: Token? get() = _token.value

    suspend fun putAuth(token: Token) {
        _token.emit(token)
        with(prefs.edit()) {
            putLong(idKey, token.id)
            putString(tokenKey, token.token)
            apply()
        }
    }

    suspend fun removeAuth() {
        _token.emit(null)
        with(prefs.edit()) {
            clear()
            commit()
        }
    }

    private fun getToken(): Token? {
        return prefs.getString(tokenKey, null)?.let {
            val id = prefs.getLong(idKey, 0)
            if (id == 0L) null
            else Token(id, it)
        }
    }
}