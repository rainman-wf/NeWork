package ru.rainman.domain.model

sealed class AppError : RuntimeException()

class ApiError(val code: Int, override val message: String) : AppError()
class DatabaseError(override val message: String): AppError()
class NetworkError(override val message: String): AppError()
class UndefinedError(override val message: String) : AppError()