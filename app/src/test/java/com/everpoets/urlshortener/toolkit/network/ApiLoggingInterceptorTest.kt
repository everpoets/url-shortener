package com.everpoets.urlshortener.toolkit.network

import okhttp3.MediaType.Companion.toMediaType
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApiLoggingInterceptorTest {

    private val interceptor = ApiLoggingInterceptor()

    @Test
    fun `isPlaintext should return true for JSON content type`() {
        val jsonMediaType = "application/json".toMediaType()

        // Use reflection to access private method for testing
        val method = ApiLoggingInterceptor::class.java.getDeclaredMethod("isPlaintext", okhttp3.MediaType::class.java)
        method.isAccessible = true
        val result = method.invoke(interceptor, jsonMediaType) as Boolean

        assertTrue(result)
    }

    @Test
    fun `isPlaintext should return true for text content type`() {
        val textMediaType = "text/plain".toMediaType()

        val method = ApiLoggingInterceptor::class.java.getDeclaredMethod("isPlaintext", okhttp3.MediaType::class.java)
        method.isAccessible = true
        val result = method.invoke(interceptor, textMediaType) as Boolean

        assertTrue(result)
    }

    @Test
    fun `isPlaintext should return false for binary content type`() {
        val binaryMediaType = "application/octet-stream".toMediaType()

        val method = ApiLoggingInterceptor::class.java.getDeclaredMethod("isPlaintext", okhttp3.MediaType::class.java)
        method.isAccessible = true
        val result = method.invoke(interceptor, binaryMediaType) as Boolean

        assertFalse(result)
    }

    @Test
    fun `isPlaintext should return false for null media type`() {
        val method = ApiLoggingInterceptor::class.java.getDeclaredMethod("isPlaintext", okhttp3.MediaType::class.java)
        method.isAccessible = true
        val result = method.invoke(interceptor, null) as Boolean

        assertFalse(result)
    }


}
