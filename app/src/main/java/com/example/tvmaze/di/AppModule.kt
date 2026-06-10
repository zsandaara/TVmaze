package com.example.tvmaze.di

import android.content.Context
import androidx.room.Room
import com.example.tvmaze.data.database.FavouriteDao
import com.example.tvmaze.data.database.TvMazeDatabase
import com.example.tvmaze.data.repository.FavouriteRepository
import com.example.tvmaze.data.repository.ShowRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TvMazeDatabase {
        return Room.databaseBuilder(
            context,
            TvMazeDatabase::class.java,
            "tvmaze_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavouriteDao(database: TvMazeDatabase): FavouriteDao {
        return database.favouriteDao()
    }

    @Provides
    @Singleton
    fun provideFavouriteRepository(favouriteDao: FavouriteDao): FavouriteRepository {
        return FavouriteRepository(favouriteDao)
    }
}