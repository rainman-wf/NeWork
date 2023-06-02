package ru.rainman.domain.model

sealed class AppError(message: String) : RuntimeException(message)

class ApiError(val code: Int, override val message: String) : AppError("code $code : \"$message\"")
class NullBodyException : AppError("Response body is null")