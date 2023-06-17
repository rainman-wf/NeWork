package ru.rainman.data.local.entity

sealed interface PublicationEntity {
    val id: Long
    val authorId: Long
    val content: String
    val published: String
    val linkKey: Long?
    val likedByMe: Boolean
    val ownedByMe: Boolean
    val attachmentKey: Long?
}

