package ru.rainman.ui

import ru.rainman.domain.model.Attachment

interface OnEventClickListener {
    fun onLikeClicked(eventId: Long)
    fun onParticipateClicked(eventId: Long)
    fun onShareClicked(eventId: Long)
    fun onEditClicked(postId: Long)
    fun onDeleteClicked(postId: Long)
    fun onAuthorClicked(eventId: Long)
    fun onEventClicked(eventId: Long)
    fun onPlayClicked(postId: Long, attachment: Attachment)
}