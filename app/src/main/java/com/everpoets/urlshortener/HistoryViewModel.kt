package com.everpoets.urlshortener

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everpoets.urlshortener.data.ShortenUrlModel
import com.everpoets.urlshortener.data.ShortenUrlRepository
import com.everpoets.urlshortener.view.HistoryIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val shortenUrlRepository: ShortenUrlRepository
) : ViewModel() {
    private val _historyShortenUiState = MutableStateFlow(ShortenUiState())
    val historyShortenUiState: StateFlow<ShortenUiState> = _historyShortenUiState.asStateFlow()

    init {
        loadHistory()
    }

    fun refreshHistory() {
        viewModelScope.launch {
            _historyShortenUiState.update { it.copy(isLoading = true) }

            shortenUrlRepository.getAllShortenUrl()
                .onSuccess { urlList ->
                    println("HistoryViewModel: Loaded ${urlList?.size ?: 0} URLs from database")
                    _historyShortenUiState.update { current ->
                        current.copy(
                            isLoading = false,
                            list = urlList.orEmpty()
                        )
                    }
                    println("HistoryViewModel: State updated. Current list size: ${_historyShortenUiState.value.list.size}")
                }
                .onFailure { error ->
                    println("HistoryViewModel: Error loading history: ${error.message}")
                    _historyShortenUiState.update { current ->
                        current.copy(
                            isLoading = false,
                            userMessage = R.string.error_loading_history
                        )
                    }
                }
        }
    }

    private fun loadHistory() = refreshHistory()
}