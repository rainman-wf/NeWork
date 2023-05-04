package ru.rainman.data.local.entity

import ru.rainman.domain.model.LinkPreview

internal interface PublicationLinkPreviewEntity {
    val publicationId: Long
    val linkPreview: LinkPreview
}