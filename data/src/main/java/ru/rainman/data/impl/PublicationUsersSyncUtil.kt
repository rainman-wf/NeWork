package ru.rainman.data.impl

import ru.rainman.data.local.entity.crossref.CrossRef
import ru.rainman.data.local.utils.PublicationUsersDiff

abstract class PublicationUsersSyncUtil {
    protected fun <T : CrossRef> calcDiff(
        newList: List<T>,
        oldList: List<T>
    ): PublicationUsersDiff<T> {
        return PublicationUsersDiff(
            toDelete = oldList.minus(newList.toSet()),
            toInsert = newList.minus(oldList.toSet())
        )
    }
}