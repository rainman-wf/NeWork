package ru.rainman.ui.storage.abstractions

import ru.rainman.domain.model.Attachment
import ru.rainman.ui.helperutils.SingleLiveEvent

object AttachmentSingleEvent {
    val value = SingleLiveEvent<Attachment>()
}