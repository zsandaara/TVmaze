package com.example.tvmaze.di

import com.example.tvmaze.data.api.TvMazeApiService
import com.example.tvmaze.data.repository.ShowRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.tvmaze.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTvMazeApiService(retrofit: Retrofit): TvMazeApiService {
        return retrofit.create(TvMazeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideShowRepository(apiService: TvMazeApiService): ShowRepository {
        return ShowRepository(apiService)
    }
}