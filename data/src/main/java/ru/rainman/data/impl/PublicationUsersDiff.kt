package ru.rainman.data.impl

import ru.rainman.data.local.entity.crossref.CrossRef

data class PublicationUsersDiff<T : CrossRef>(
    val toDelete: List<T>,
    val toInsert: List<T>
)