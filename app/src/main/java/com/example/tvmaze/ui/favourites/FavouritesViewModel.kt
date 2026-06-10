package com.example.tvmaze.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvmaze.data.database.FavouriteEntity
import com.example.tvmaze.data.repository.FavouriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    private val _favourites = MutableStateFlow<List<FavouriteEntity>>(emptyList())
    val favourites: StateFlow<List<FavouriteEntity>> = _favourites.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFavourites()
    }

    private fun loadFavourites() {
        viewModelScope.launch {
            _isLoading.value = true
            favouriteRepository.getAllFavourites().collect { list ->
                _favourites.value = list
                _isLoading.value = false
            }
        }
    }
}