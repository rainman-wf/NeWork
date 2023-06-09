package ru.rainman.ui.helperutils

sealed interface Status {
    object Loading : Status
    object Success: Status
    data class Error(val message: String) : Status
}