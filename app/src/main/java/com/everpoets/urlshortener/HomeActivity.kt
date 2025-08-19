package com.everpoets.urlshortener

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.everpoets.urlshortener.navigation.AppNavigation
import com.everpoets.urlshortener.ui.theme.UrlShortenerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UrlShortenerTheme {
                AppNavigation()
            }
        }
    }
}
