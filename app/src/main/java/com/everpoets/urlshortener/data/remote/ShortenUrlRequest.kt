package com.everpoets.urlshortener.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ShortenUrlRequest(val url: String)
