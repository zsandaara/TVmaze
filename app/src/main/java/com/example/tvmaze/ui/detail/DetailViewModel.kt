package com.example.tvmaze.ui.detail

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
class DetailViewModel @Inject constructor(
    private val repository: ShowRepository,
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite: StateFlow<Boolean> = _isFavourite.asStateFlow()

    private var actualFavouriteState = false
    private var currentShow: Show? = null
    private var detailJob: Job? = null
    private var lastShowId: Int? = null

    fun loadShowDetails(showId: Int) {
        detailJob?.cancel()
        lastShowId = showId

        detailJob = viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val show = repository.getShowById(showId)
                if (lastShowId == showId) {
                    currentShow = show
                    actualFavouriteState = favouriteRepository.isFavourite(showId) ?: false
                    _isFavourite.value = actualFavouriteState
                    _uiState.value = DetailUiState.Success(show)
                }
            } catch (e: IOException) {
                if (lastShowId == showId) {
                    _uiState.value = DetailUiState.Error("Ошибка сети: ${e.message}")
                }
            } catch (e: HttpException) {
                if (lastShowId == showId) {
                    _uiState.value = DetailUiState.Error("Ошибка сервера: ${e.code()}")
                }
            } catch (e: Exception) {
                if (lastShowId == showId) {
                    _uiState.value = DetailUiState.Error("Неизвестная ошибка: ${e.message}")
                }
            }
        }
    }

    fun toggleFavourite(show: Show) {
        viewModelScope.launch {
            val newState = !_isFavourite.value
            val previousState = _isFavourite.value

            _isFavourite.value = newState

            try {
                if (newState) {
                    favouriteRepository.addToFavourites(show)
                    actualFavouriteState = true
                } else {
                    favouriteRepository.removeFromFavourites(show.id)
                    actualFavouriteState = false
                }
            } catch (e: Exception) {
                _isFavourite.value = previousState
                actualFavouriteState = previousState
                e.printStackTrace()
            }
        }
    }

    fun retry(showId: Int) {
        loadShowDetails(showId)
    }
}