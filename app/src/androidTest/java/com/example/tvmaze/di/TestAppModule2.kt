package com.example.tvmaze.di

import android.content.Context
import androidx.room.Room
import com.example.tvmaze.data.database.FavouriteDao
import com.example.tvmaze.data.database.TvMazeDatabase
import com.example.tvmaze.data.repository.FavouriteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule2 {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TvMazeDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            TvMazeDatabase::class.java
        ).allowMainThreadQueries().build()
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