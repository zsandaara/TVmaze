package com.example.tvmaze.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        loadShows()
    }

    fun loadShows() {
        if (isSearchMode && lastQuery.isNotBlank()) {
            searchShows(lastQuery)
        } else {
            loadShowsFromApi()
        }
    }

    private fun loadShowsFromApi() {
        viewModelScope.launch {
            val currentState = _uiState.value

            val isFirstLoad = currentState !is ListUiState.Success

            if (isFirstLoad) {
                _uiState.value = ListUiState.Loading
            } else if (currentState is ListUiState.Success) {
                _uiState.value = ListUiState.Success(currentState.shows, isLoadingMore = true)
            }

            try {
                val shows = repository.getShows(currentPage)
                if (shows.isEmpty() && currentPage == 0) {
                    _uiState.value = ListUiState.Empty
                } else {
                    val existingShows = if (currentPage == 0) emptyList()
                    else (currentState as? ListUiState.Success)?.shows ?: emptyList()

                    val allShows = (existingShows + shows).distinctBy { it.id }
                    _uiState.value = ListUiState.Success(allShows, isLoadingMore = false)
                }
            } catch (e: IOException) {
                val previousState = _uiState.value
                if (previousState is ListUiState.Success) {
                    _uiState.value = ListUiState.Success(previousState.shows, isLoadingMore = false)
                } else {
                    _uiState.value = ListUiState.Error("Ошибка сети: ${e.message}")
                }
            } catch (e: HttpException) {
                val previousState = _uiState.value
                if (previousState is ListUiState.Success) {
                    _uiState.value = ListUiState.Success(previousState.shows, isLoadingMore = false)
                } else {
                    _uiState.value = ListUiState.Error("Ошибка сервера: ${e.code()}")
                }
            } catch (e: Exception) {
                val previousState = _uiState.value
                if (previousState is ListUiState.Success) {
                    _uiState.value = ListUiState.Success(previousState.shows, isLoadingMore = false)
                } else {
                    _uiState.value = ListUiState.Error("Неизвестная ошибка: ${e.message}")
                }
            }
        }
    }

    fun searchShows(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            isSearchMode = false
            lastQuery = ""
            currentPage = 0
            loadShowsFromApi()
            return
        }

        isSearchMode = true
        lastQuery = query
        currentPage = 0

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
        if (!isSearchMode) {
            val currentState = _uiState.value
            // Проверяем, что не идет загрузка и есть данные
            if (currentState is ListUiState.Success && !currentState.isLoadingMore) {
                currentPage++
                loadShowsFromApi()
            }
        }
    }

    fun retry() {
        loadShows()
    }
}