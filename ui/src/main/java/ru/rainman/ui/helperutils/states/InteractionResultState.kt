package ru.rainman.ui.helperutils.states

sealed interface InteractionResultState

object Loading : InteractionResultState
object Success : InteractionResultState
data class Error(val message: String) : InteractionResultState
