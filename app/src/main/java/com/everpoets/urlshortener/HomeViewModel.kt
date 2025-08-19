package com.everpoets.urlshortener

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everpoets.urlshortener.data.ShortenUrlModel
import com.everpoets.urlshortener.data.ShortenUrlRepository
import com.everpoets.urlshortener.toolkit.network.NoInternetException
import com.everpoets.urlshortener.view.HomeIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShortenUiState(
    val isLoading: Boolean = false,
    val list: List<ShortenUrlModel> = emptyList(),
    @StringRes val userMessage: Int? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val shortenUrlRepository: ShortenUrlRepository
) : ViewModel() {
    private val _shortenUiState = MutableStateFlow(ShortenUiState())
    val shortenUiState: StateFlow<ShortenUiState> = _shortenUiState.asStateFlow()

    fun dispatchAction(action: HomeIntent) {
        when (action) {
            is HomeIntent.ClearUserMessage -> clearUserMessage()
            is HomeIntent.ShortenUrl -> handleShortenUrlAction(action.url)
        }
    }

    private fun handleShortenUrlAction(url: String) {
        viewModelScope.launch {
            setLoading()

            val existingItem = findItemInList(url)

            if (existingItem != null) {
                handleExistingUrl(existingItem)
            } else {
                performShortenUrl(url)
            }
        }
    }

    private fun setLoading() {
        _shortenUiState.update { it.copy(isLoading = true) }
    }

    private fun findItemInList(url: String): ShortenUrlModel? {
        return _shortenUiState.value.list.find { it.selfLink == url }
    }

    private fun handleExistingUrl(existingItem: ShortenUrlModel) {
        _shortenUiState.update { current ->
            val updatedList = current.list.toMutableList().apply {
                remove(existingItem)
                add(0, existingItem) // Move to top
            }
            current.copy(
                list = updatedList,
                isLoading = false,
                userMessage = R.string.url_already_exists
            )
        }
    }

    private suspend fun performShortenUrl(url: String) {
        shortenUrlRepository.doShortenUrl(url)
            .onSuccess { model ->
                updateStateWithNewUrl(model)
            }
            .onFailure {
                updateStateWithError(it)
            }
    }

    private fun updateStateWithNewUrl(model: ShortenUrlModel) {
        _shortenUiState.update { current ->
            val updatedList = listOf(model) + current.list
            current.copy(
                isLoading = false,
                list = updatedList,
                userMessage = R.string.url_shortened_successfully
            )
        }
    }

    private fun updateStateWithError(throwable: Throwable) {
        _shortenUiState.update {
            it.copy(
                isLoading = false,
                userMessage = getErrorMessage(throwable)
            )
        }
    }

    private fun getErrorMessage(throwable: Throwable): Int {
        return when (throwable) {
            is NoInternetException -> {
                R.string.error_no_internet_connection
            }

            else -> R.string.error_shortening_url
        }
    }

    private fun clearUserMessage() {
        _shortenUiState.update { it.copy(userMessage = null) }
    }
}
