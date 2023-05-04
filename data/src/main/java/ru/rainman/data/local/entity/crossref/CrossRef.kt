package ru.rainman.data.local.entity.crossref

internal interface CrossRef {
    val parentId: Long
    val childId: Long
}