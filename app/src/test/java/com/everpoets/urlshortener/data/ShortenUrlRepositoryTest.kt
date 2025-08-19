package com.everpoets.urlshortener.data

import com.everpoets.urlshortener.data.local.ShortenUrlDao
import com.everpoets.urlshortener.data.remote.ApiService
import com.everpoets.urlshortener.data.remote.ShortenUrlLinksResponse
import com.everpoets.urlshortener.data.remote.ShortenUrlRequest
import com.everpoets.urlshortener.data.remote.ShortenUrlResponse
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ShortenUrlRepositoryTest {
    private val apiService: ApiService = mockk()
    private val shortenUrlDao: ShortenUrlDao = mockk(relaxed = true)
    private val repository = ShortenUrlDefaultRepository(apiService, shortenUrlDao)

    private val testUrl = "https://example.com"
    private val expectedRequest = ShortenUrlRequest(testUrl)

    @Before
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `doShortenUrl should call apiService with correct parameters`() = runTest {
        coEvery { apiService.shortenUrl(any()) } returns mockApiResponse()

        repository.doShortenUrl(testUrl)

        coVerify(exactly = 1) {
            apiService.shortenUrl(ShortenUrlRequest(testUrl))
        }
    }

    @Test
    fun `doShortenUrl should return success with correct model when api call succeeds`() = runTest {
        val mockResponse = mockApiResponse()
        val expectedModel = ShortenUrlModel(
            alias = mockResponse.alias,
            shortLink = mockResponse.links.short,
            selfLink = mockResponse.links.self
        )
        coEvery { apiService.shortenUrl(expectedRequest) } returns mockResponse

        val result = repository.doShortenUrl(testUrl)

        assertTrue(result.isSuccess)
        assertEquals(expectedModel, result.getOrNull())
    }

    @Test
    fun `doShortenUrl should return failure when api call throws exception`() = runTest {
        val expectedException = RuntimeException("Network error")
        coEvery { apiService.shortenUrl(expectedRequest) } throws expectedException

        val result = repository.doShortenUrl(testUrl)

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    private fun mockApiResponse() = ShortenUrlResponse(
        alias = "test-alias",
        links = ShortenUrlLinksResponse(
            short = "https://short.url/abc",
            self = testUrl
        )
    )
}
