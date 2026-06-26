package com.example.tvmaze.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvmaze.data.model.Show
import com.example.tvmaze.data.repository.FavouriteRepository
import com.example.tvmaze.data.repository.ShowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: ShowRepository,
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var isSearchMode = false
    private var lastQuery = ""

    private var searchJob: Job? = null
    private var isLoadingMore = false

    private var loadedPages = mutableListOf<List<Show>>()
    private var currentShows = mutableListOf<Show>()

    init {
        loadShows()
    }

    fun loadShows() {
        if (isSearchMode && lastQuery.isNotBlank()) {
            searchShows(lastQuery)
        } else {
            resetPagination()
            loadShowsFromApi()
        }
    }

    private fun resetPagination() {
        currentPage = 0
        loadedPages.clear()
        currentShows.clear()
        isLoadingMore = false
    }

    private fun loadShowsFromApi() {
        if (isLoadingMore) return

        viewModelScope.launch {
            val currentState = _uiState.value
            val isFirstLoad = currentState !is ListUiState.Success

            if (isFirstLoad) {
                _uiState.value = ListUiState.Loading
            } else if (currentState is ListUiState.Success) {
                isLoadingMore = true
                _uiState.value = ListUiState.Success(
                    currentState.shows,
                    isLoadingMore = true,
                    hasError = false
                )
            }

            try {
                val shows = repository.getShows(currentPage)
                if (shows.isEmpty() && currentPage == 0) {
                    _uiState.value = ListUiState.Empty
                } else {
                    if (currentPage > 0) {
                        loadedPages.add(shows)
                    }

                    if (currentPage == 0) {
                        currentShows = shows.distinctBy { it.id }.toMutableList()
                    } else {
                        currentShows.addAll(shows)
                        currentShows = currentShows.distinctBy { it.id }.toMutableList()
                    }

                    isLoadingMore = false
                    _uiState.value = ListUiState.Success(
                        currentShows.toList(),
                        isLoadingMore = false,
                        hasError = false
                    )
                }
            } catch (e: IOException) {
                handlePaginationError(e.message ?: "Ошибка сети")
            } catch (e: HttpException) {
                handlePaginationError("Ошибка сервера: ${e.code()}")
            } catch (e: Exception) {
                handlePaginationError("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    private fun handlePaginationError(message: String) {
        isLoadingMore = false

        val currentState = _uiState.value
        if (currentState is ListUiState.Success) {
            _uiState.value = ListUiState.Success(
                currentState.shows,
                isLoadingMore = false,
                hasError = true
            )
        } else {
            _uiState.value = ListUiState.Error(message)
        }
    }

    fun retryNextPage() {
        val currentState = _uiState.value
        if (currentState is ListUiState.Success && currentState.hasError) {
            _uiState.value = ListUiState.Success(
                currentState.shows,
                isLoadingMore = true,
                hasError = false
            )
            loadShowsFromApi()
        }
    }

    fun searchShows(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            isSearchMode = false
            lastQuery = ""
            resetPagination()
            loadShowsFromApi()
            return
        }

        isSearchMode = true
        lastQuery = query
        currentPage = 0
        loadedPages.clear()
        currentShows.clear()

        searchJob = viewModelScope.launch {
            _uiState.value = ListUiState.Loading
            try {
                val shows = repository.searchShows(query)
                if (lastQuery == query && isSearchMode) {
                    if (shows.isEmpty()) {
                        _uiState.value = ListUiState.Empty
                    } else {
                        val uniqueShows = shows.distinctBy { it.id }
                        _uiState.value = ListUiState.Success(uniqueShows, isLoadingMore = false)
                    }
                }
            } catch (e: IOException) {
                if (lastQuery == query && isSearchMode) {
                    _uiState.value = ListUiState.Error("Ошибка сети: ${e.message}")
                }
            } catch (e: HttpException) {
                if (lastQuery == query && isSearchMode) {
                    _uiState.value = ListUiState.Error("Ошибка сервера: ${e.code()}")
                }
            } catch (e: Exception) {
                if (lastQuery == query && isSearchMode) {
                    _uiState.value = ListUiState.Error("Неизвестная ошибка: ${e.message}")
                }
            }
        }
    }

    fun nextPage() {
        if (!isSearchMode && !isLoadingMore) {
            val currentState = _uiState.value
            if (currentState is ListUiState.Success && !currentState.isLoadingMore) {
                if (!currentState.hasError) {
                    currentPage++
                    loadShowsFromApi()
                }
            }
        }
    }

    fun retry() {
        resetPagination()
        loadShows()
    }
}