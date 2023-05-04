package ru.rainman.domain.model

data class LinkPreview(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val siteName: String? = null,
) {
    val isEmpty: Boolean
        get() = title == null && description == null && siteName == null && image == null
}
