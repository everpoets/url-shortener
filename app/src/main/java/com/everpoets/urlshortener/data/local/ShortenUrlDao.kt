package com.everpoets.urlshortener.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ShortenUrlDao {
    @Query(value = "SELECT * FROM shorten_urls")
    suspend fun getAll(): List<ShortenUrlEntity>

    @Insert
    suspend fun insertUrl(url: ShortenUrlEntity): Long
}