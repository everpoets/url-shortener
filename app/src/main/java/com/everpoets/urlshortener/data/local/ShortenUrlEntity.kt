package com.everpoets.urlshortener.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shorten_urls")
data class ShortenUrlEntity(
    @PrimaryKey val alias: String,
    @ColumnInfo(name = "self_link") val selfLink: String,
    @ColumnInfo(name = "short_link") val shortLink: String,
)