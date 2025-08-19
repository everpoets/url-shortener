package com.everpoets.urlshortener.toolkit.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    apiCall: suspend () -> T
): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.success(apiCall.invoke())
        } catch (e: ConnectException) {
            Result.failure(NoInternetException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class NoInternetException : Exception("No internet connection")
