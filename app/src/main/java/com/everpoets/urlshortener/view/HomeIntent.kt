package com.everpoets.urlshortener.view

sealed interface HomeIntent {
    data class ShortenUrl(val url: String) : HomeIntent
    data object ClearUserMessage : HomeIntent
}
