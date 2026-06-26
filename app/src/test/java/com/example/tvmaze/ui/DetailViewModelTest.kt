package com.example.tvmaze.ui.detail

import com.example.tvmaze.data.model.Show
import com.example.tvmaze.data.repository.FavouriteRepository
import com.example.tvmaze.data.repository.ShowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
        whenever(repository.getShowById(1)).thenReturn(expectedShow)
        whenever(favouriteRepository.isFavourite(1)).thenReturn(false)

        viewModel = DetailViewModel(repository, favouriteRepository)
        viewModel.loadShowDetails(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is DetailUiState.Success)
        assertEquals(1, (state as DetailUiState.Success).show.id)
    }

    @Test
    fun `loadShowDetails should return Error state on network failure`() = runTest {
        whenever(repository.getShowById(1)).thenThrow(RuntimeException("Network error"))

        viewModel = DetailViewModel(repository, favouriteRepository)
        viewModel.loadShowDetails(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is DetailUiState.Error)
        assertTrue((state as DetailUiState.Error).message.contains("Network error"))
    }

    @Test
    fun `toggleFavourite should add when not favourite and remove when favourite`() = runTest {
        val show = Show(
            id = 1, name = "Test Show", summary = null, genres = listOf("Drama"),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )

        viewModel = DetailViewModel(repository, favouriteRepository)

        whenever(favouriteRepository.isFavourite(1)).thenReturn(false)
        viewModel.toggleFavourite(show)
        advanceUntilIdle()
        verify(favouriteRepository, times(1)).addToFavourites(show)
        verify(favouriteRepository, never()).removeFromFavourites(any())
        assertTrue(viewModel.isFavourite.value)

        whenever(favouriteRepository.isFavourite(1)).thenReturn(true)
        viewModel.toggleFavourite(show)
        advanceUntilIdle()
        verify(favouriteRepository, times(1)).removeFromFavourites(1)
        assertFalse(viewModel.isFavourite.value)
    }

    @Test
    fun `retry should initiate new request after error`() = runTest {
        val successShow = Show(
            id = 1, name = "Success Show", summary = "Success summary", genres = listOf("Comedy"),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )

        whenever(repository.getShowById(1))
            .thenThrow(RuntimeException("Network error"))
            .thenReturn(successShow)

        whenever(favouriteRepository.isFavourite(1)).thenReturn(false)

        viewModel = DetailViewModel(repository, favouriteRepository)

        viewModel.loadShowDetails(1)
        advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertTrue("State should be Error", errorState is DetailUiState.Error)

        viewModel.retry(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is DetailUiState.Success)
        if (state is DetailUiState.Success) {
            assertEquals("Success Show", state.show.name)
        }

        verify(repository, times(2)).getShowById(1)
    }

    @Test
    fun `old request should be cancelled when new request comes`() = runTest {
        val show1 = Show(
            id = 1, name = "Show 1", summary = null, genres = emptyList(),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )
        val show2 = Show(
            id = 2, name = "Show 2", summary = null, genres = emptyList(),
            rating = null, image = null, language = null, premiered = null,
            ended = null, status = null, officialSite = null
        )

        whenever(repository.getShowById(1)).thenAnswer {
            runBlocking {
                delay(5000)
                show1
            }
        }
        whenever(repository.getShowById(2)).thenReturn(show2)

        whenever(favouriteRepository.isFavourite(1)).thenReturn(false)
        whenever(favouriteRepository.isFavourite(2)).thenReturn(false)

        viewModel = DetailViewModel(repository, favouriteRepository)

        viewModel.loadShowDetails(1)

        delay(100)

        viewModel.loadShowDetails(2)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is DetailUiState.Success)
        if (state is DetailUiState.Success) {
            assertEquals("Should show second show", 2, state.show.id)
            assertEquals("Show 2", state.show.name)
        }

        verify(repository, times(1)).getShowById(1)
        verify(repository, times(1)).getShowById(2)
    }
}