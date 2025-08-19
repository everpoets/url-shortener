package com.everpoets.urlshortener.data

import com.everpoets.urlshortener.data.local.ShortenUrlEntity

data class ShortenUrlModel(
    val alias: String,
    val selfLink: String,
    val shortLink: String
)

fun ShortenUrlModel.toShortenUrlEntity(): ShortenUrlEntity {
    return ShortenUrlEntity(
        alias = this.alias,
        selfLink = this.selfLink,
        shortLink = this.shortLink
    )
}