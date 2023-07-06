package ru.rainman.ui.fragments.publications.event

import ru.rainman.domain.model.Event
import ru.rainman.ui.fragments.publications.OnPublicationClickListener

interface OnEventClickListener : OnPublicationClickListener<Event> {
    fun onParticipateClicked(eventId: Long)
    fun onParticipantsCountClicked(ids: List<Long>)
}

