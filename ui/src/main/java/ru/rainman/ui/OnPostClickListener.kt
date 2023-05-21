package ru.rainman.ui

interface OnPostClickListener {
    fun onLikeClicked(postId: Long)
    fun onShareClicked(postId: Long)
    fun onMoreClicked(postId: Long)
    fun onAuthorClicked(postId: Long)
    fun onPostClicked(postId: Long)
}