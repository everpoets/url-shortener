package com.everpoets.urlshortener.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.everpoets.urlshortener.HistoryViewModel
import com.everpoets.urlshortener.ShortenUiState
import com.everpoets.urlshortener.ui.theme.SPACING_16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onClearMessage: () -> Unit = {}
) {
    val historyViewModel: HistoryViewModel = hiltViewModel()
    val uiState by historyViewModel.historyShortenUiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage = uiState.userMessage?.let { stringResource(it) }

    if (userMessage != null) {
        LaunchedEffect(userMessage, onClearMessage) {
            snackbarHostState.showSnackbar(userMessage)
            onClearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Histórico de URLs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
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
            verticalArrangement = Arrangement.spacedBy(SPACING_16)
        ) {
            ShortenUrlList(
                uiState = uiState
            )
        }
    }
}
