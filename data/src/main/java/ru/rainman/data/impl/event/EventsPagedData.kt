package ru.rainman.data.impl.event

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.rainman.data.local.dao.EventDao
import ru.rainman.data.impl.toModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventsPagedData @Inject constructor(
    eventDao: EventDao,
    eventsRemoteMediator: EventsRemoteMediator
) {

    @OptIn(ExperimentalPagingApi::class)
    val data = Pager(
        config = PagingConfig(
            pageSize = 10,
            prefetchDistance = 10,
            enablePlaceholders = true,
            maxSize = 30
        ),
        pagingSourceFactory = { eventDao.getPaged() },
        remoteMediator = eventsRemoteMediator
    ).flow.map {
        it.map { e -> e.toModel() }
    }.flowOn(Dispatchers.IO)
}