package ru.rainman.domain.dto

import ru.rainman.domain.model.UploadMedia

data class NewUserDto(
    val login: String,
    val password: String,
    val name: String,
    val avatar: UploadMedia?
): NewObjectDto