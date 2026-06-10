package com.example.tvmaze.ui.list

import app.cash.turbine.test
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

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
    fun `initial state should be Loading`() = runTest {
        viewModel = ListViewModel(repository, favouriteRepository)
        assertTrue(viewModel.uiState.value is ListUiState.Loading)
    }

    @Test
    fun `loadShows should return Success state with shows`() = runTest {
        val expectedShows = listOf(
            Show(id = 1, name = "Show 1", summary = null, genres = emptyList(), rating = null,
                image = null, language = null, premiered = null, ended = null, status = null, officialSite = null)
        )
        `when`(repository.getShows(0)).thenReturn(expectedShows)

        viewModel = ListViewModel(repository, favouriteRepository)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ListUiState.Success)
            assertEquals(expectedShows, (state as ListUiState.Success).shows)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadShows should return Error state on network failure`() = runTest {
        `when`(repository.getShows(0)).thenThrow(RuntimeException("Network error"))

        viewModel = ListViewModel(repository, favouriteRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ListUiState.Error)
    }

    @Test
    fun `retry should reload data after error`() = runTest {
        val expectedShows = listOf(
            Show(id = 1, name = "Show 1", summary = null, genres = emptyList(), rating = null,
                image = null, language = null, premiered = null, ended = null, status = null, officialSite = null)
        )

        `when`(repository.getShows(0))
            .thenThrow(RuntimeException("Network error"))
            .thenReturn(expectedShows)

        viewModel = ListViewModel(repository, favouriteRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ListUiState.Error)

        viewModel.retry()
        advanceUntilIdle()

        val successState = viewModel.uiState.value
        assertTrue(successState is ListUiState.Success)
    }

    @Test
    fun `empty search result should return Empty state`() = runTest {
        `when`(repository.searchShows("nonexistent")).thenReturn(emptyList())

        viewModel = ListViewModel(repository, favouriteRepository)
        viewModel.searchShows("nonexistent")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ListUiState.Empty)
    }
}