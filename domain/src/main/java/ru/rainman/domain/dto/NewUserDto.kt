package ru.rainman.domain.dto

import java.io.File

data class NewUserDto(
    val login: String,
    val password: String,
    val name: String,
    val avatar: File? = null
): NewObjectDto