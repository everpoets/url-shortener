package com.everpoets.urlshortener.toolkit.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

private const val TAG = "ApiLogging"
private const val MAX_CONTENT_LENGTH = 1024L // 1KB limit for logging content

class ApiLoggingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        val startTime = System.nanoTime()
        logRequest(request)
        
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "❌ HTTP FAILED: ${request.url}", e)
            throw e
        }
        
        val endTime = System.nanoTime()
        val duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime)
        
        logResponse(response, duration)
        
        return response
    }

    private fun logRequest(request: Request) {
        Log.d(TAG, "🚀 HTTP REQUEST")
        Log.d(TAG, "Method: ${request.method}")
        Log.d(TAG, "URL: ${request.url}")
        
        // Log headers
        if (request.headers.size > 0) {
            Log.d(TAG, "Headers:")
            for (i in 0 until request.headers.size) {
                Log.d(TAG, "  ${request.headers.name(i)}: ${request.headers.value(i)}")
            }
        }
        
        // Log request body
        request.body?.let { requestBody ->
            if (isPlaintext(requestBody.contentType())) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                
                val charset = requestBody.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                val content = if (buffer.size <= MAX_CONTENT_LENGTH) {
                    buffer.readString(charset)
                } else {
                    "${buffer.readString(MAX_CONTENT_LENGTH, charset)}... [TRUNCATED]"
                }
                
                Log.d(TAG, "Request Body (${requestBody.contentType()}):")
                Log.d(TAG, content)
            } else {
                Log.d(TAG, "Request Body: [Binary content, ${requestBody.contentLength()} bytes]")
            }
        }
        
        Log.d(TAG, "🚀 END REQUEST")
    }

    private fun logResponse(response: Response, duration: Long) {
        Log.i(TAG, "📥 HTTP RESPONSE")
        Log.i(TAG, "URL: ${response.request.url}")
        Log.i(TAG, "Status: ${response.code} ${response.message}")
        Log.i(TAG, "Duration: ${duration}ms")
        
        // Log response headers
        if (response.headers.size > 0) {
            Log.d(TAG, "Response Headers:")
            for (i in 0 until response.headers.size) {
                Log.d(TAG, "  ${response.headers.name(i)}: ${response.headers.value(i)}")
            }
        }
        
        // Log response body
        val responseBody = response.body
        if (responseBody != null && isPlaintext(responseBody.contentType())) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body
            val buffer = source.buffer
            
            val charset = responseBody.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            val content = if (buffer.size <= MAX_CONTENT_LENGTH) {
                buffer.clone().readString(charset)
            } else {
                "${buffer.clone().readString(MAX_CONTENT_LENGTH, charset)}... [TRUNCATED]"
            }
            
            Log.i(TAG, "Response Body (${responseBody.contentType()}):")
            Log.i(TAG, content)
        } else if (responseBody != null) {
            Log.i(TAG, "Response Body: [Binary content, ${responseBody.contentLength()} bytes]")
        }
        
        // Log summary
        val statusEmoji = when {
            response.isSuccessful -> "✅"
            response.code in 400..499 -> "⚠️"
            response.code >= 500 -> "❌"
            else -> "ℹ️"
        }
        
        Log.i(TAG, "$statusEmoji ${response.request.method} ${response.request.url} -> ${response.code} (${duration}ms)")
        Log.i(TAG, "📥 END RESPONSE")
    }

    private fun isPlaintext(mediaType: MediaType?): Boolean {
        if (mediaType == null) return false
        
        return when {
            mediaType.type == "text" -> true
            mediaType.subtype == "json" -> true
            mediaType.subtype == "xml" -> true
            mediaType.subtype == "html" -> true
            mediaType.subtype.endsWith("+json") -> true
            mediaType.subtype.endsWith("+xml") -> true
            else -> false
        }
    }
}
