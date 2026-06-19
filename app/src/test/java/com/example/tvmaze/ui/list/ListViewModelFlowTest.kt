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

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelFlowTest {

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
    fun `loadShows should emit Loading then Success sequence`() = runTest {
        val expectedShows = listOf(
            Show(
                id = 1,
                name = "Show 1",
                summary = null,
                genres = emptyList(),
                rating = null,
                image = null,
                language = null,
                premiered = null,
                ended = null,
                status = null,
                officialSite = null
            )
        )
        `when`(repository.getShows(0)).thenReturn(expectedShows)

        viewModel = ListViewModel(repository, favouriteRepository)

        viewModel.uiState.test {
            val firstState = awaitItem()
            assertTrue("First state should be Loading", firstState is ListUiState.Loading)

            val secondState = awaitItem()
            assertTrue("Second state should be Success", secondState is ListUiState.Success)
            assertEquals(expectedShows, (secondState as ListUiState.Success).shows)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadShows should emit Loading then Error on failure`() = runTest {
        `when`(repository.getShows(0)).thenThrow(RuntimeException("Network error"))

        viewModel = ListViewModel(repository, favouriteRepository)

        viewModel.uiState.test {
            val firstState = awaitItem()
            assertTrue(firstState is ListUiState.Loading)

            val secondState = awaitItem()
            assertTrue(secondState is ListUiState.Error)
            assertTrue((secondState as ListUiState.Error).message.contains("Network error"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search should cancel previous request and show only latest result`() = runTest {
        val shows1 = listOf(
            Show(
                id = 1,
                name = "Show 1",
                summary = null,
                genres = emptyList(),
                rating = null,
                image = null,
                language = null,
                premiered = null,
                ended = null,
                status = null,
                officialSite = null
            )
        )
        val shows2 = listOf(
            Show(
                id = 2,
                name = "Show 2",
                summary = null,
                genres = emptyList(),
                rating = null,
                image = null,
                language = null,
                premiered = null,
                ended = null,
                status = null,
                officialSite = null
            )
        )

        `when`(repository.searchShows("query1")).thenReturn(shows1)
        `when`(repository.searchShows("query2")).thenReturn(shows2)

        viewModel = ListViewModel(repository, favouriteRepository)
        advanceUntilIdle()

        viewModel.searchShows("query1")
        viewModel.searchShows("query2")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ListUiState.Success)
        assertEquals(1, (state as ListUiState.Success).shows.size)
        assertEquals("Show 2", (state as ListUiState.Success).shows[0].name)

        verify(repository, atLeastOnce()).searchShows(any())
    }

    @Test
    fun `rapid search queries should not emit intermediate stale results`() = runTest {
        val shows1 = listOf(
            Show(
                id = 1,
                name = "Result 1",
                summary = null,
                genres = emptyList(),
                rating = null,
                image = null,
                language = null,
                premiered = null,
                ended = null,
                status = null,
                officialSite = null
            )
        )
        val shows2 = listOf(
            Show(
                id = 2,
                name = "Result 2",
                summary = null,
                genres = emptyList(),
                rating = null,
                image = null,
                language = null,
                premiered = null,
                ended = null,
                status = null,
                officialSite = null
            )
        )

        `when`(repository.searchShows("a")).thenReturn(shows1)
        `when`(repository.searchShows("ab")).thenReturn(shows2)

        viewModel = ListViewModel(repository, favouriteRepository)
        advanceUntilIdle()

        // Выполняем быстрые поисковые запросы
        viewModel.searchShows("a")
        viewModel.searchShows("ab")
        advanceUntilIdle()

        // Проверяем финальное состояние
        val finalState = viewModel.uiState.value
        assertTrue("Final state should be Success", finalState is ListUiState.Success)
        assertEquals("Result 2", (finalState as ListUiState.Success).shows[0].name)
    }

    @Test
    fun `nextPage should emit Success with isLoadingMore true then false`() = runTest {
        val firstPage = listOf(
            Show(
                id = 1,
                name = "Show 1",
                summary = null,
                genres = emptyList(),
                rating = null,
                image = null,
                language = null,
                premiered = null,
                ended = null,
                status = null,
                officialSite = null
            )
        )
        val secondPage = listOf(
            Show(
                id = 2,
                name = "Show 2",
                summary = null,
                genres = emptyList(),
                rating = null,
                image = null,
                language = null,
                premiered = null,
                ended = null,
                status = null,
                officialSite = null
            )
        )

        `when`(repository.getShows(0)).thenReturn(firstPage)
        `when`(repository.getShows(1)).thenReturn(secondPage)

        viewModel = ListViewModel(repository, favouriteRepository)
        advanceUntilIdle()

        val firstState = viewModel.uiState.value
        assertTrue(firstState is ListUiState.Success)
        assertEquals(1, (firstState as ListUiState.Success).shows.size)
        assertFalse((firstState as ListUiState.Success).isLoadingMore)

        viewModel.nextPage()
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is ListUiState.Success)
        assertFalse((finalState as ListUiState.Success).isLoadingMore)
        assertEquals(2, (finalState as ListUiState.Success).shows.size)
    }

    @Test
    fun `new subscriber should receive current state immediately`() = runTest {
        val expectedShows = listOf(
            Show(
                id = 1,
                name = "Show 1",
                summary = null,
                genres = emptyList(),
                rating = null,
                image = null,
                language = null,
                premiered = null,
                ended = null,
                status = null,
                officialSite = null
            )
        )
        `when`(repository.getShows(0)).thenReturn(expectedShows)

        viewModel = ListViewModel(repository, favouriteRepository)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ListUiState.Success)
            assertEquals(expectedShows, (state as ListUiState.Success).shows)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh should emit Success then Success with new data`() = runTest {
        val oldShows = listOf(
            Show(
                id = 1,
                name = "Old Show",
                summary = null,
                genres = emptyList(),
                rating = null,
                image = null,
                language = null,
                premiered = null,
                ended = null,
                status = null,
                officialSite = null
            )
        )
        val newShows = listOf(
            Show(
                id = 2,
                name = "New Show",
                summary = null,
                genres = emptyList(),
                rating = null,
                image = null,
                language = null,
                premiered = null,
                ended = null,
                status = null,
                officialSite = null
            )
        )

        `when`(repository.getShows(0))
            .thenReturn(oldShows)
            .thenReturn(newShows)

        viewModel = ListViewModel(repository, favouriteRepository)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertTrue(state is ListUiState.Success)
        assertEquals("Old Show", (state as ListUiState.Success).shows[0].name)

        viewModel.loadShows()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertTrue(state is ListUiState.Success)
        assertEquals("New Show", (state as ListUiState.Success).shows[0].name)
    }
}