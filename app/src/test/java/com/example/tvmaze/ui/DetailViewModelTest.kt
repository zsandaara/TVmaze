package com.example.tvmaze.ui.detail

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
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DetailViewModel
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
        viewModel = DetailViewModel(repository, favouriteRepository)
        assertTrue(viewModel.uiState.value is DetailUiState.Loading)
    }

    @Test
    fun `loadShowDetails should return Success state with show`() = runTest {
        val expectedShow = Show(
            id = 1, name = "Test Show", summary = null, genres = listOf("Drama"),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )
        `when`(repository.getShowById(1)).thenReturn(expectedShow)
        `when`(favouriteRepository.isFavourite(1)).thenReturn(false)

        viewModel = DetailViewModel(repository, favouriteRepository)
        viewModel.loadShowDetails(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DetailUiState.Success)
        assertEquals(1, (state as DetailUiState.Success).show.id)
    }

    @Test
    fun `loadShowDetails should return Error state on network failure`() = runTest {
        `when`(repository.getShowById(1)).thenThrow(RuntimeException("Network error"))

        viewModel = DetailViewModel(repository, favouriteRepository)
        viewModel.loadShowDetails(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DetailUiState.Error)
    }

    @Test
    fun `toggleFavourite should add and remove from favourites`() = runTest {
        val show = Show(
            id = 1, name = "Test Show", summary = null, genres = listOf("Drama"),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )

        viewModel = DetailViewModel(repository, favouriteRepository)

        `when`(favouriteRepository.isFavourite(1)).thenReturn(false)

        viewModel.toggleFavourite(show)
        advanceUntilIdle()
        verify(favouriteRepository, times(1)).addToFavourites(show)
    }
}