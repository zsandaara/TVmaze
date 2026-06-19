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
import org.junit.Assert.*
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
            id = 1, name = "Test Show", summary = "Test summary", genres = listOf("Drama"),
            rating = com.example.tvmaze.data.model.Rating(average = 8.5),
            image = null, language = "English", premiered = "2020-01-01",
            ended = null, status = "Running", officialSite = null
        )

        repository.addToFavourites(show)
        val favourites = repository.getAllFavourites().first()

        assertEquals(1, favourites.size)
        assertEquals("Test Show", favourites[0].showName)
        // Используем assertEquals с nullable типом
        assertEquals(8.5, favourites[0].showRating)
        assertEquals("Drama", favourites[0].showGenres)
        assertEquals("Test summary", favourites[0].showSummary)
    }

    @Test
    fun addingSameShowTwiceShouldNotCreateDuplicateAndShouldUpdateData() = runBlocking {
        val show1 = Show(
            id = 1, name = "Test Show", summary = "Old summary", genres = listOf("Drama"),
            rating = com.example.tvmaze.data.model.Rating(average = 8.0),
            image = null, language = "English", premiered = null,
            ended = null, status = null, officialSite = null
        )

        val show2 = Show(
            id = 1, name = "Updated Show", summary = "New summary", genres = listOf("Comedy"),
            rating = com.example.tvmaze.data.model.Rating(average = 9.0),
            image = null, language = "English", premiered = null,
            ended = null, status = null, officialSite = null
        )

        repository.addToFavourites(show1)
        repository.addToFavourites(show2)
        val favourites = repository.getAllFavourites().first()

        assertEquals(1, favourites.size)
        assertEquals("Updated Show", favourites[0].showName)
        assertEquals(9.0, favourites[0].showRating)
        assertEquals("Comedy", favourites[0].showGenres)
        assertEquals("New summary", favourites[0].showSummary)
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

    @Test
    fun isFavouriteShouldReturnCorrectState() = runBlocking {
        val show = Show(
            id = 1, name = "Test Show", summary = null, genres = listOf("Drama"),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )

        assertFalse(repository.isFavourite(1))

        repository.addToFavourites(show)
        assertTrue(repository.isFavourite(1))

        repository.removeFromFavourites(1)
        assertFalse(repository.isFavourite(1))
    }
}