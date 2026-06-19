package com.example.tvmaze.ui.list

import com.example.tvmaze.data.model.Show
import com.example.tvmaze.data.repository.FavouriteRepository
import com.example.tvmaze.data.repository.ShowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SearchTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ListViewModel
    private val repository: ShowRepository = mock()
    private val favouriteRepository: FavouriteRepository = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty search result should give Empty state, not Success empty list`() = runTest {
        Mockito.`when`(repository.searchShows("nonexistent")).thenReturn(emptyList())

        viewModel = ListViewModel(repository, favouriteRepository)
        viewModel.searchShows("nonexistent")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        Assert.assertTrue("Search result should be Empty, not Success", state is ListUiState.Empty)
    }

    @Test
    fun `search with empty query should return to normal list view`() = runTest {
        val expectedShows = listOf(
            Show(
                id = 1, name = "Show 1", summary = null, genres = emptyList(),
                rating = null, image = null, language = null, premiered = null,
                ended = null, status = null, officialSite = null
            )
        )
        Mockito.`when`(repository.getShows(0)).thenReturn(expectedShows)

        viewModel = ListViewModel(repository, favouriteRepository)
        advanceUntilIdle()

        viewModel.searchShows("")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        Assert.assertTrue(state is ListUiState.Success)
        Assert.assertEquals(1, (state as ListUiState.Success).shows.size)
        Assert.assertEquals("Show 1", (state as ListUiState.Success).shows[0].name)
    }

    @Test
    fun `search results should replace existing list`() = runTest {
        val initialShows = listOf(
            Show(
                id = 1, name = "Initial Show", summary = null, genres = emptyList(),
                rating = null, image = null, language = null, premiered = null,
                ended = null, status = null, officialSite = null
            )
        )
        val searchResults = listOf(
            Show(
                id = 2, name = "Found Show", summary = null, genres = emptyList(),
                rating = null, image = null, language = null, premiered = null,
                ended = null, status = null, officialSite = null
            )
        )

        Mockito.`when`(repository.getShows(0)).thenReturn(initialShows)
        Mockito.`when`(repository.searchShows("found")).thenReturn(searchResults)

        viewModel = ListViewModel(repository, favouriteRepository)
        advanceUntilIdle()

        val initialState = viewModel.uiState.value
        Assert.assertTrue(initialState is ListUiState.Success)
        Assert.assertEquals("Initial Show", (initialState as ListUiState.Success).shows[0].name)

        viewModel.searchShows("found")
        advanceUntilIdle()

        val searchState = viewModel.uiState.value
        Assert.assertTrue(searchState is ListUiState.Success)
        Assert.assertEquals(1, (searchState as ListUiState.Success).shows.size)
        Assert.assertEquals("Found Show", (searchState as ListUiState.Success).shows[0].name)
    }
}