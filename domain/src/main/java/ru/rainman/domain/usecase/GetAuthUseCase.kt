package ru.rainman.domain.usecase

import ru.rainman.domain.model.Token
import ru.rainman.domain.repository.UserRepository

class GetAuthUseCase(
    private val userRepository: UserRepository
) {

    operator fun invoke(): Token {
        TODO()
    }
}