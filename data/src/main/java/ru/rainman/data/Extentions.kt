package ru.rainman.data

import com.google.gson.Gson
import retrofit2.Response
import ru.rainman.data.remote.response.ErrorBody
import ru.rainman.domain.model.ApiError
import ru.rainman.domain.model.DatabaseError
import ru.rainman.domain.model.NetworkError
import ru.rainman.domain.model.UndefinedError

fun String?.isUrl(): Boolean {
    if (this == null) return false
    if (!startsWith("http://") && !startsWith("https://")) return false
    val domain = "://([^/]+)".toRegex().find(this)?.groupValues?.get(1) ?: return false
    if (!domain.contains(".")) return false
    val domainParts = domain.split(".")
    val hasEmptyParts = domainParts.map { it.isEmpty() }.filterNot { it }.isEmpty()
    return (!hasEmptyParts && domainParts.last().length > 1)
}

fun String?.formatLink(): String? {
    if (this == null) return null
    return if (!startsWith("http://") && !startsWith("https://")) "http://$this" else this
}

suspend fun <T> apiRequest(body: suspend () -> Response<T>): T {
    val response = try {
        body()
    } catch (e: Exception) {
        throw NetworkError(e.message.toString())
    }

    if (response.body() == null && response.errorBody() == null) throw UndefinedError

    return response.body() ?: throw ApiError(
        response.code(),
        Gson().fromJson(response.errorBody()!!.string(), ErrorBody::class.java).reason
    )
}

suspend fun <T> dbQuery(query: suspend () -> T): T {
    return try {
        query()
    } catch (e: Exception) {
        throw DatabaseError(e.stackTraceToString())
    }
}

