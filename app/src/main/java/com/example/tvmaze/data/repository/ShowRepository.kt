package com.example.tvmaze.data.repository

import com.example.tvmaze.data.api.TvMazeApiService
import com.example.tvmaze.data.model.SearchResult
import com.example.tvmaze.data.model.Show
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowRepository @Inject constructor(
    private val apiService: TvMazeApiService
) {

    suspend fun getShows(page: Int): List<Show> {
        return apiService.getShows(page)
    }

    suspend fun searchShows(query: String): List<Show> {
        val results: List<SearchResult> = apiService.searchShows(query)
        return results.map { it.show }
    }

    suspend fun getShowById(id: Int): Show {
        return apiService.getShowById(id)
    }
}