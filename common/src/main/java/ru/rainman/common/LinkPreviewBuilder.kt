package ru.rainman.common

import org.jsoup.Jsoup
import ru.rainman.domain.model.LinkPreview

class LinkPreviewBuilder {

    companion object {
        private const val AGENT = "Mozilla"
        private const val REFERRER = "http://www.yandex.ru"
        private const val DOC_SELECT_QUERY = "meta[property^=og:]"
        private const val OPEN_GRAPH_KEY = "content"
        private const val PROPERTY = "property"
        private const val OG_IMAGE = "og:image"
        private const val OG_DESCRIPTION = "og:description"
        private const val OG_TITLE = "og:title"
        private const val OG_SITE_NAME = "og:site_name"


        suspend fun poll(url: String): LinkPreview {

            val fullUrl = if (!url.startsWith("http")) "https://$url" else url

            val linkPreviewBuilder = LinkPreviewBuilder()

            val response = try {
                Jsoup.connect(fullUrl)
                    .ignoreContentType(true)
                    .referrer(REFERRER)
                    .userAgent(AGENT)
                    .timeout(30000)
                    .followRedirects(true)
                    .execute()

            } catch (e: Exception) {
                return linkPreviewBuilder.build(fullUrl) { }
            }

            return linkPreviewBuilder.build(fullUrl) {
                response.parse().select(DOC_SELECT_QUERY).map {
                    when (it.attr(PROPERTY)) {
                        OG_IMAGE -> image = (it.attr(OPEN_GRAPH_KEY))
                        OG_DESCRIPTION -> description = (it.attr(OPEN_GRAPH_KEY))
                        OG_TITLE -> title = (it.attr(OPEN_GRAPH_KEY))
                        OG_SITE_NAME -> siteName = (it.attr(OPEN_GRAPH_KEY))
                    }
                }
            }
        }
    }

    var title: String? = null
    var description: String? = null
    var image: String? = null
    var siteName: String? = null
    var type: String? = null

    private suspend fun build(
        url: String,
        builder: suspend LinkPreviewBuilder.() -> Unit
    ): LinkPreview {
        builder(this)
        return LinkPreview(
            url = url,
            title = title,
            description = description,
            image = image,
            siteName = siteName,
        )
    }
}