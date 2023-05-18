package ru.rainman.data.remote

import retrofit2.Response
import ru.rainman.domain.model.ApiError
import ru.rainman.domain.model.NullBodyException

suspend fun <T> apiRequest(body: suspend () -> Response<T>): T {
    val response = try {
        body()
    } catch (e: Exception) {
        throw e
    }
    if (!response.isSuccessful) throw ApiError(
        response.code(),
        response.errorBody()?.string().toString()
    )
    return response.body() ?: throw NullBodyException()
}