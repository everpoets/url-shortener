package com.everpoets.urlshortener.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("alias")
    suspend fun shortenUrl(@Body url: ShortenUrlRequest): ShortenUrlResponse
}
