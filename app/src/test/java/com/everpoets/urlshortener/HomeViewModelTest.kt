package com.everpoets.urlshortener

import com.everpoets.urlshortener.data.ShortenUrlModel
import com.everpoets.urlshortener.data.ShortenUrlRepository
import com.everpoets.urlshortener.utils.MainDispatcherRule
import com.everpoets.urlshortener.view.HomeIntent
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeViewModelTest {
    private val repository: ShortenUrlRepository = mockk()
    private lateinit var viewModel: MainViewModel

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testUrl = "https://example.com"

    @Before
    fun setup() {
        viewModel = MainViewModel(repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun initialState() = runTest {
        val initialState = viewModel.shortenUiState.value

        assertFalse(initialState.isLoading)
        assertTrue(initialState.list.isEmpty())
        assertNull(initialState.userMessage)
    }

    @Test
    fun dispatchAction_clearUserMessage_shouldClearUserMessage() = runTest {
        val mockModel = ShortenUrlModel(
            alias = "test-alias",
            shortLink = "https://short.url/abc",
            selfLink = testUrl
        )
        coEvery { repository.doShortenUrl(testUrl) } returns Result.success(mockModel)
        viewModel.dispatchAction(HomeIntent.ShortenUrl(testUrl))
        viewModel.dispatchAction(HomeIntent.ClearUserMessage)

        val finalState = viewModel.shortenUiState.value
        assertNull(finalState.userMessage)
    }

    @Test
    fun dispatchAction_shortenUrl_shouldShowSuccessMessage() = runTest {
        val mockModel = ShortenUrlModel(
            alias = "test-alias",
            shortLink = "https://short.url/abc",
            selfLink = testUrl
        )
        coEvery { repository.doShortenUrl(testUrl) } returns Result.success(mockModel)
        viewModel.dispatchAction(HomeIntent.ShortenUrl(testUrl))

        val finalState = viewModel.shortenUiState.value
        assertFalse(finalState.isLoading)
        assertEquals(1, finalState.list.size)
        assertEquals(mockModel, finalState.list.first())
        assertEquals(R.string.url_shortened_successfully, finalState.userMessage)
    }

    @Test
    fun dispatchAction_shortenUrl_shouldMoveExistingUrlToTop() = runTest {
        val existingModel = ShortenUrlModel(
            alias = "existing-alias",
            shortLink = "https://short.url/existing",
            selfLink = testUrl
        )
        val anotherModel = ShortenUrlModel(
            alias = "another-alias",
            shortLink = "https://short.url/another",
            selfLink = "https://another.com"
        )
        coEvery { repository.doShortenUrl("https://another.com") } returns Result.success(
            anotherModel
        )
        coEvery { repository.doShortenUrl(testUrl) } returns Result.success(existingModel)

        viewModel.dispatchAction(HomeIntent.ShortenUrl("https://another.com"))
        val firstState = viewModel.shortenUiState.value
        assertEquals(1, firstState.list.size)

        viewModel.dispatchAction(HomeIntent.ShortenUrl(testUrl))
        val secondState = viewModel.shortenUiState.value
        assertEquals(2, secondState.list.size)

        viewModel.dispatchAction(HomeIntent.ShortenUrl(testUrl))
        val finalState = viewModel.shortenUiState.value

        assertEquals(2, finalState.list.size)
        assertEquals(existingModel, finalState.list.first())
        assertEquals(R.string.url_already_exists, finalState.userMessage)
    }

    @Test
    fun dispatchAction_shortenUrl_whenRepositoryFails_shouldShowErrorMessage() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { repository.doShortenUrl(testUrl) } returns Result.failure(exception)

        viewModel.dispatchAction(HomeIntent.ShortenUrl(testUrl))
        val finalState = viewModel.shortenUiState.value

        assertFalse(finalState.isLoading)
        assertTrue(finalState.list.isEmpty())
        assertEquals(R.string.error_shortening_url, finalState.userMessage)
    }
}
