package com.example.tvmaze.data

import com.example.tvmaze.data.api.TvMazeApiService
import com.example.tvmaze.data.model.SearchResult
import com.example.tvmaze.data.model.Show
import com.example.tvmaze.data.repository.ShowRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ShowRepositoryTest {

    private val apiService: TvMazeApiService = mock()
    private val repository = ShowRepository(apiService)

    @Test
    fun `getShows should return list of shows on success`() = runTest {
        val expectedShows = listOf(
            Show(id = 1, name = "Show 1", summary = null, genres = emptyList(), rating = null,
                image = null, language = null, premiered = null, ended = null, status = null, officialSite = null)
        )
        `when`(apiService.getShows(0)).thenReturn(expectedShows)

        val result = repository.getShows(0)

        assertEquals(expectedShows, result)
        verify(apiService, times(1)).getShows(0)
    }

    @Test
    fun `getShows should throw exception on network error`() = runTest {
        // Используем RuntimeException вместо IOException
        `when`(apiService.getShows(0)).thenThrow(RuntimeException("Network error"))

        try {
            repository.getShows(0)
            assert(false) { "Expected exception was not thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `searchShows should return shows from search results`() = runTest {
        val searchResults = listOf(
            SearchResult(score = 10.0, show = Show(id = 1, name = "Found Show", summary = null,
                genres = emptyList(), rating = null, image = null, language = null, premiered = null,
                ended = null, status = null, officialSite = null))
        )
        `when`(apiService.searchShows("test")).thenReturn(searchResults)

        val result = repository.searchShows("test")

        assertEquals(1, result.size)
        assertEquals("Found Show", result[0].name)
    }

    @Test
    fun `getShowById should return single show`() = runTest {
        val expectedShow = Show(id = 5, name = "Detail Show", summary = null, genres = emptyList(),
            rating = null, image = null, language = null, premiered = null, ended = null,
            status = null, officialSite = null)
        `when`(apiService.getShowById(5)).thenReturn(expectedShow)

        val result = repository.getShowById(5)

        assertEquals(expectedShow, result)
    }
}