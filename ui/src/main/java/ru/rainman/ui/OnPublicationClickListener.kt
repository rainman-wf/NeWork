package ru.rainman.ui

import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Publication

interface OnPublicationClickListener<T: Publication> {
    fun onLikeClicked(id: Long)
    fun onShareClicked(id: Long)
    fun onEditClicked(id: Long)
    fun onDeleteClicked(id: Long)
    fun onAuthorClicked(id: Long)
    fun onBodyClicked(id: Long)
    fun onPlayClicked(id: Long, attachment: Attachment)
}