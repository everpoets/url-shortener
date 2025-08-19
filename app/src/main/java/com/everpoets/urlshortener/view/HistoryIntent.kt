package com.everpoets.urlshortener.view

sealed interface HistoryIntent {
    data object Init : HistoryIntent
}