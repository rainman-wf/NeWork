package ru.rainman.data.local.entity

import ru.rainman.domain.model.LinkPreview

interface PublicationLinkPreviewEntity {
    val publicationId: Long
    val linkPreview: LinkPreview
}