package com.everpoets.urlshortener.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun UrlShortenerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}
