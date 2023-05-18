package ru.rainman.data

import ru.rainman.data.remote.response.EventResponse

fun String?.isUrl(): Boolean {
    if (this == null) return false
    if (!startsWith("http://") && !startsWith("https://")) return false
    val domain = "://([^/]+)".toRegex().find(this)?.groupValues?.get(1) ?: return false
    if (!domain.contains(".")) return false
    val domainParts = domain.split(".")
    val hasEmptyParts = domainParts.map { it.isEmpty() }.filterNot { it }.isEmpty()
    return (!hasEmptyParts && domainParts.last().length > 1)
}

fun EventResponse.hasCorrectLink() : Boolean {
    if (link == null) return false
    val url = if (!link.startsWith("http://") && !link.startsWith("https://")) "https://$link" else link

    return url.isUrl()
}