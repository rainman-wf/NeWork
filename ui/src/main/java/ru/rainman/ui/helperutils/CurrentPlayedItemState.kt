package ru.rainman.ui.helperutils

data class CurrentPlayedItemState(
    val type: PubType,
    val id: Long,
    val isPlaying: Boolean
)