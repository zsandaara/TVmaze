package com.example.tvmaze.ui.detail

import com.example.tvmaze.data.model.Show

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val show: Show) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}