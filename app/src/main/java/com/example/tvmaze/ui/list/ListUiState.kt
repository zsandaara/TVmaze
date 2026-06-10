package com.example.tvmaze.ui.list

import com.example.tvmaze.data.model.Show

sealed class ListUiState {
    object Loading : ListUiState()
    data class Success(val shows: List<Show>, val isLoadingMore: Boolean = false) : ListUiState()
    data class Error(val message: String) : ListUiState()
    object Empty : ListUiState()
}