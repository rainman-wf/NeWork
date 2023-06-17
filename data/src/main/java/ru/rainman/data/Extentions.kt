package ru.rainman.data

import retrofit2.Response
import ru.rainman.data.remote.response.EventResponse
import ru.rainman.domain.model.ApiError
import ru.rainman.domain.model.DatabaseError
import ru.rainman.domain.model.NetworkError

fun String?.isUrl(): Boolean {
    if (this == null) return false
    if (!startsWith("http://") && !startsWith("https://")) return false
    val domain = "://([^/]+)".toRegex().find(this)?.groupValues?.get(1) ?: return false
    if (!domain.contains(".")) return false
    val domainParts = domain.split(".")
    val hasEmptyParts = domainParts.map { it.isEmpty() }.filterNot { it }.isEmpty()
    return (!hasEmptyParts && domainParts.last().length > 1)
}

fun EventResponse.hasCorrectLink(): Boolean {
    if (link == null) return false
    val url =
        if (!link.startsWith("http://") && !link.startsWith("https://")) "https://$link" else link

    return url.isUrl()
}

suspend fun <T> apiRequest(body: suspend () -> Response<T>): T {
    val response = try {
        body()
    } catch (e: Exception) {
        throw NetworkError(e.message.toString())
    }
    if (!response.isSuccessful) throw ApiError(response.code(), response.message())

    return response.body() ?: throw ApiError(response.code(), response.message())
}

suspend fun <T> dbQuery(query: suspend () -> T): T {
    return try {
        query()
    } catch (e: Exception) {
        throw DatabaseError(e.stackTraceToString())
    }
}

