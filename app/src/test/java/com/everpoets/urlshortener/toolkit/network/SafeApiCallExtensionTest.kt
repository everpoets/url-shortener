package com.everpoets.urlshortener.toolkit.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.net.ConnectException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SafeApiCallExtensionTest {
    
    @Test
    fun `safeApiCall should return success when api call succeeds`() = runTest {
        val expectedResult = "Success Response"
        val apiCall: suspend () -> String = { expectedResult }

        val result = safeApiCall(apiCall = apiCall)

        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }

    @Test
    fun `safeApiCall should return NoInternetException when ConnectException is thrown`() = runTest {
        val apiCall: suspend () -> String = { throw ConnectException("Connection failed") }

        val result = safeApiCall(apiCall = apiCall)

        assertTrue(result.isFailure)
        assertIs<NoInternetException>(result.exceptionOrNull())
        assertEquals("No internet connection", result.exceptionOrNull()?.message)
    }

    @Test
    fun `safeApiCall should return original exception when generic exception is thrown`() = runTest {
        val originalException = RuntimeException("Something went wrong")
        val apiCall: suspend () -> String = { throw originalException }
        
        val result = safeApiCall(apiCall = apiCall)

        assertTrue(result.isFailure)
        assertEquals(originalException, result.exceptionOrNull())
        assertEquals("Something went wrong", result.exceptionOrNull()?.message)
    }
}
