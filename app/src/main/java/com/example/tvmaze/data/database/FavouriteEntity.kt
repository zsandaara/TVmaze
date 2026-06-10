package com.example.tvmaze.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey
    val showId: Int,
    val showName: String,
    val showRating: Double?,
    val showGenres: String,
    val showSummary: String?,
    val timestamp: Long = System.currentTimeMillis()
)