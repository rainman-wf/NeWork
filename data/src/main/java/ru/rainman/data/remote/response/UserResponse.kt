package ru.rainman.data.remote.response

data class UserResponse(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?
)
