package com.everpoets.urlshortener.di

import android.content.Context
import androidx.room.Room
import com.everpoets.urlshortener.data.local.AppDatabase
import com.everpoets.urlshortener.data.local.ShortenUrlDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "url_shortener_database"
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideUrlDao(database: AppDatabase): ShortenUrlDao = database.shortenUrlDao()
}
