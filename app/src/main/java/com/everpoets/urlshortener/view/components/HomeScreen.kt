package com.everpoets.urlshortener.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.everpoets.urlshortener.ShortenUiState
import com.everpoets.urlshortener.ui.theme.SPACING_10
import com.everpoets.urlshortener.ui.theme.SPACING_16
import com.everpoets.urlshortener.view.HomeIntent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: ShortenUiState,
    dispatchAction: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToHistory: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage = uiState.userMessage?.let { stringResource(it) }

    if (userMessage != null) {
        LaunchedEffect(userMessage, dispatchAction) {
            snackbarHostState.showSnackbar(userMessage)
            dispatchAction(HomeIntent.ClearUserMessage)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("URL Shortener") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ver histórico"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = SPACING_16),
            verticalArrangement = Arrangement.spacedBy(SPACING_10)
        ) {
            InputTextButton(
                onClick = { dispatchAction(HomeIntent.ShortenUrl(it)) }
            )
            ShortenUrlList(
                uiState = uiState
            )
        }
    }
}
