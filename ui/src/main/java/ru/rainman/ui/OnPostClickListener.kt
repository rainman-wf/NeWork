package ru.rainman.ui

import ru.rainman.domain.model.Attachment

interface OnPostClickListener {
    fun onLikeClicked(postId: Long)
    fun onShareClicked(postId: Long)
    fun onEditClicked(postId: Long)
    fun onDeleteClicked(postId: Long)
    fun onAuthorClicked(postId: Long)
    fun onPostClicked(postId: Long)
    fun onPlayClicked(postId: Long, attachment: Attachment)
}