package ru.rainman.data.remote.request

data class AuthenticationRequest(
    val login: String,
    val password: String
)
