package ru.rainman.data.impl

import androidx.paging.PagingSource


abstract class CustomPagingSource<K : Any, V : Any, I : Any> : PagingSource<K, V>() {

    suspend fun result(data: List<I>, nextPageNumber: Int, size: Int, map: suspend (I) -> V) =
        LoadResult.Page(
            data = data.map { map(it) },
            prevKey = if (nextPageNumber == 0) null else nextPageNumber - size,
            nextKey = if (data.isEmpty()) null else nextPageNumber + size
        )
}

