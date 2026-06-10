package com.example.tvmaze.data.repository

import com.example.tvmaze.data.database.FavouriteDao
import com.example.tvmaze.data.database.FavouriteEntity
import com.example.tvmaze.data.model.Show
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class FavouriteRepositoryTest {

    private val favouriteDao: FavouriteDao = mock()
    private val repository = FavouriteRepository(favouriteDao)

    @Test
    fun `addToFavourites should insert entity without duplicate`() = runTest {
        val show = Show(
            id = 1, name = "Test Show", summary = null, genres = listOf("Drama"),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )
        `when`(favouriteDao.isFavourite(1)).thenReturn(false)

        repository.addToFavourites(show)

        verify(favouriteDao, times(1)).addToFavourites(any())
    }

    @Test
    fun `removeFromFavourites should delete entity`() = runTest {
        repository.removeFromFavourites(1)
        verify(favouriteDao, times(1)).removeFromFavourites(1)
    }

    @Test
    fun `getAllFavourites should return favourites list`() = runTest {
        val expected = listOf(
            FavouriteEntity(1, "Show 1", null, "", null),
            FavouriteEntity(2, "Show 2", null, "", null)
        )
        `when`(favouriteDao.getAllFavourites()).thenReturn(flowOf(expected))

        val result = repository.getAllFavourites().first()
        assertEquals(expected.size, result.size)
    }
}