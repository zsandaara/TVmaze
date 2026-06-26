package com.example.tvmaze.data.repository

import com.example.tvmaze.data.database.FavouriteDao
import com.example.tvmaze.data.database.FavouriteEntity
import com.example.tvmaze.data.model.Show
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouriteRepository @Inject constructor(
    private val favouriteDao: FavouriteDao
) {

    fun getAllFavourites(): Flow<List<FavouriteEntity>> {
        return favouriteDao.getAllFavourites()
    }

    suspend fun addToFavourites(show: Show) {
        addToFavourites(show, System.currentTimeMillis())
    }

    suspend fun addToFavourites(show: Show, timestamp: Long) {
        val entity = FavouriteEntity(
            showId = show.id,
            showName = show.name,
            showRating = show.rating?.average,
            showGenres = show.genres.joinToString(","),
            showSummary = show.summary?.replace(Regex("<[^>]*>"), "")?.take(200),
            timestamp = timestamp
        )
        favouriteDao.addToFavourites(entity)
    }

    suspend fun removeFromFavourites(showId: Int) {
        favouriteDao.removeFromFavourites(showId)
    }

    suspend fun isFavourite(showId: Int): Boolean {
        return favouriteDao.isFavourite(showId)
    }
}