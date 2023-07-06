package ru.rainman.ui.fragments.publications.post

import ru.rainman.domain.model.Post
import ru.rainman.ui.fragments.publications.OnPublicationClickListener

interface OnPostClickListener : OnPublicationClickListener<Post> {
    fun onMentionedClicked(ids: List<Long>)
}