package com.example.tvmaze.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tvmaze.data.database.FavouriteDao
import com.example.tvmaze.data.database.TvMazeDatabase
import com.example.tvmaze.data.model.Show
import com.example.tvmaze.data.repository.FavouriteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouriteRepositoryIntegrationTest {

    private lateinit var database: TvMazeDatabase
    private lateinit var favouriteDao: FavouriteDao
    private lateinit var repository: FavouriteRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, TvMazeDatabase::class.java
        ).allowMainThreadQueries().build()
        favouriteDao = database.favouriteDao()
        repository = FavouriteRepository(favouriteDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun addToFavouritesThenGetAllFavouritesShouldReturnAddedShow() = runBlocking {
        val show = Show(
            id = 1, name = "Test Show", summary = null, genres = listOf("Drama"),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )

        repository.addToFavourites(show)
        val favourites = repository.getAllFavourites().first()

        assertEquals(1, favourites.size)
        assertEquals("Test Show", favourites[0].showName)
    }

    @Test
    fun addingSameShowTwiceShouldNotCreateDuplicate() = runBlocking {
        val show = Show(
            id = 1, name = "Test Show", summary = null, genres = listOf("Drama"),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )

        repository.addToFavourites(show)
        repository.addToFavourites(show)
        val favourites = repository.getAllFavourites().first()

        assertEquals(1, favourites.size)
    }

    @Test
    fun removeFromFavouritesShouldRemoveShowFromFavourites() = runBlocking {
        val show = Show(
            id = 1, name = "Test Show", summary = null, genres = listOf("Drama"),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )

        repository.addToFavourites(show)
        repository.removeFromFavourites(1)
        val favourites = repository.getAllFavourites().first()

        assertTrue(favourites.isEmpty())
    }
}