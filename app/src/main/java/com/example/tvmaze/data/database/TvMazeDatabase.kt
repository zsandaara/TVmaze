package com.example.tvmaze.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FavouriteEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TvMazeDatabase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao
}