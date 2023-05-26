package ru.rainman.ui

interface OnEventClickListener {
    fun onLikeClicked(eventId: Long)
    fun onParticipateClicked(eventId: Long)
    fun onShareClicked(eventId: Long)
    fun onMoreClicked(eventId: Long)
    fun onAuthorClicked(eventId: Long)
    fun onEventClicked(eventId: Long)
    fun onPlayClicked(uri: String)
}