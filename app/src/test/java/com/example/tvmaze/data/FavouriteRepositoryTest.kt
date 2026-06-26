package com.example.tvmaze.data.repository

import com.example.tvmaze.data.database.FavouriteDao
import com.example.tvmaze.data.database.FavouriteEntity
import com.example.tvmaze.data.model.Show
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class FavouriteRepositoryTest {

    private val favouriteDao: FavouriteDao = mock()
    private val repository = FavouriteRepository(favouriteDao)

    @Test
    fun `initial state when no favourites should return empty list`() = runTest {
        `when`(favouriteDao.getAllFavourites()).thenReturn(flowOf(emptyList()))

        val result = repository.getAllFavourites().first()

        assertTrue(result.isEmpty())
        verify(favouriteDao, times(1)).getAllFavourites()
    }

    @Test
    fun `addToFavourites should insert correct entity with proper data conversion`() = runTest {
        val show = Show(
            id = 1,
            name = "Test Show",
            summary = "<p><b>Test</b> summary with <i>HTML</i></p>",
            genres = listOf("Drama", "Comedy"),
            rating = com.example.tvmaze.data.model.Rating(average = 8.5),
            image = null,
            language = "English",
            premiered = "2020-01-01",
            ended = null,
            status = "Running",
            officialSite = "https://example.com"
        )

        repository.addToFavourites(show)

        verify(favouriteDao, times(1)).addToFavourites(any())
    }

    @Test
    fun `removeFromFavourites should call DAO with correct ID`() = runTest {
        repository.removeFromFavourites(42)
        verify(favouriteDao, times(1)).removeFromFavourites(42)
    }

    @Test
    fun `getAllFavourites should return favourites list from DAO`() = runTest {
        val expected = listOf(
            FavouriteEntity(1, "Show 1", 8.0, "Drama", "Summary 1"),
            FavouriteEntity(2, "Show 2", 7.5, "Comedy", "Summary 2")
        )
        `when`(favouriteDao.getAllFavourites()).thenReturn(flowOf(expected))

        val result = repository.getAllFavourites().first()

        assertEquals(expected.size, result.size)
        assertEquals(expected[0].showId, result[0].showId)
        assertEquals(expected[0].showName, result[0].showName)
    }
}