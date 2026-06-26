package com.example.tvmaze.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavourites(favourite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE showId = :showId")
    suspend fun removeFromFavourites(showId: Int)

    @Query("SELECT * FROM favourites ORDER BY timestamp DESC")
    fun getAllFavourites(): Flow<List<FavouriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE showId = :showId)")
    suspend fun isFavourite(showId: Int): Boolean
}