package com.everpoets.urlshortener.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.everpoets.urlshortener.MainViewModel
import com.everpoets.urlshortener.view.components.HistoryScreen
import com.everpoets.urlshortener.view.components.HomeScreen

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(Home)

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<Home> {
                val mainViewModel: MainViewModel = hiltViewModel()
                val uiState by mainViewModel.shortenUiState.collectAsState()

                HomeScreen(
                    uiState = uiState,
                    dispatchAction = mainViewModel::dispatchAction,
                    onNavigateToHistory = {
                        backStack.add(History)
                    }
                )
            }

            entry<History> {
                HistoryScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}
