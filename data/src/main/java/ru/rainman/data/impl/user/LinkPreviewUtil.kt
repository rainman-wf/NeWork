package ru.rainman.data.impl.user

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.rainman.data.formatLink
import ru.rainman.common.LinkPreviewBuilder
import ru.rainman.data.impl.toEntity
import ru.rainman.data.local.entity.LinkPreviewEntity
import ru.rainman.data.remote.response.LinkedResponse
import ru.rainman.data.remote.response.PublicationResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class LinkPreviewUtil @Inject constructor() {

    private val converter = MutableSharedFlow<LinkPreviewPrepareDto>()

    private data class LinkPreviewPrepareDto(
        val response: LinkedResponse,
        val result: (LinkPreviewEntity) -> Unit
    )

    init {
        CoroutineScope(Dispatchers.IO).launch {
            converter.collect {
                it.result.invoke(
                    LinkPreviewBuilder.poll(it.response.link.formatLink()!!).toEntity()
                )
            }
        }
    }

    suspend fun getLinkPreviewEntity(publicationResponse: LinkedResponse): LinkPreviewEntity? =
        suspendCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                converter.emit(
                    LinkPreviewPrepareDto(publicationResponse) {
                        continuation.resume(it)
                    }
                )
            }
        }
}