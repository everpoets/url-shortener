package com.everpoets.urlshortener.data.remote

import com.everpoets.urlshortener.data.ShortenUrlModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShortenUrlResponse(
    @SerialName("alias") val alias: String,
    @SerialName("_links") val links: ShortenUrlLinksResponse
)

@Serializable
data class ShortenUrlLinksResponse(
    @SerialName("self") val self: String,
    @SerialName("short") val short: String,
)

fun ShortenUrlResponse.toShortenUrlModel(): ShortenUrlModel {
    return ShortenUrlModel(
        alias = this.alias,
        selfLink = this.links.self,
        shortLink = this.links.short
    )
}
