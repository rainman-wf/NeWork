package ru.rainman.ui.helperutils

data class TimeUnitsWrapper(
    val hours: Int,
    val minutes: Int
) {
    override fun toString(): String {
        return "$hours:$minutes"
    }
}