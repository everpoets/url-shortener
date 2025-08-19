package com.everpoets.urlshortener.di

import com.everpoets.urlshortener.data.ShortenUrlDefaultRepository
import com.everpoets.urlshortener.data.ShortenUrlRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindShortenUrlRepository(
        dataRepository: ShortenUrlDefaultRepository
    ): ShortenUrlRepository
}
