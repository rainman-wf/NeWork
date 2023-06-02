package ru.rainman.ui.helperutils

import ru.rainman.domain.model.Token
import ru.rainman.domain.model.User

data class Me(
    val token: Token,
    val user: User
)
