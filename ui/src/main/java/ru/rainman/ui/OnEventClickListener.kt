package ru.rainman.ui

import ru.rainman.domain.model.Event
import ru.rainman.domain.model.Post

interface OnEventClickListener : OnPublicationClickListener<Event> {
    fun onParticipateClicked(eventId: Long)
}

interface OnPostClickListener : OnPublicationClickListener<Post>