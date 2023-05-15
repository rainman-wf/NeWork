package ru.rainman.ui.helperutils

import ru.rainman.domain.model.User

data class SelectableUser(
    val user: User,
    val selected: Boolean = false
)
