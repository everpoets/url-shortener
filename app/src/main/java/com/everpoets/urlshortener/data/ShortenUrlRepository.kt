package com.everpoets.urlshortener.data

import com.everpoets.urlshortener.data.local.ShortenUrlDao
import com.everpoets.urlshortener.data.local.ShortenUrlEntity
import com.everpoets.urlshortener.data.remote.ApiService
import com.everpoets.urlshortener.data.remote.ShortenUrlRequest
import com.everpoets.urlshortener.data.remote.toShortenUrlModel
import com.everpoets.urlshortener.toolkit.network.safeApiCall
import javax.inject.Inject

interface ShortenUrlRepository {
    suspend fun doShortenUrl(url: String): Result<ShortenUrlModel>
    suspend fun getAllShortenUrl(): Result<List<ShortenUrlModel>?>
}

class ShortenUrlDefaultRepository @Inject constructor(
    private val apiService: ApiService,
    private val shortenUrlDao: ShortenUrlDao,
) : ShortenUrlRepository {

    override suspend fun doShortenUrl(
        url: String
    ): Result<ShortenUrlModel> {
        return safeApiCall {
            apiService.shortenUrl(url = ShortenUrlRequest(url))
        }.fold(
            onSuccess = {
                val shortenModel = it.toShortenUrlModel()
                shortenUrlDao.insertUrl(
                    url = shortenModel.toShortenUrlEntity()
                )
                Result.success(value = shortenModel) 
                        },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun getAllShortenUrl(): Result<List<ShortenUrlModel>?> {
        return try {
            val shortenUrlEntitys = shortenUrlDao.getAll()
            println("Repository: Found ${shortenUrlEntitys.size} entities in database")
            val map = shortenUrlEntitys.map {
                ShortenUrlModel(
                    it.alias,
                    selfLink = it.selfLink,
                    shortLink = it.shortLink
                )
            }
            println("Repository: Mapped to ${map.size} models")
            Result.success(map)
        } catch (e: Exception) {
            println("Repository: Error getting URLs: ${e.message}")
            Result.failure(e)
        }
    }
}
