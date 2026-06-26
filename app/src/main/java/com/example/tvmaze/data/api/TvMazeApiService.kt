package com.example.tvmaze.data.api

import com.example.tvmaze.data.model.SearchResult
import com.example.tvmaze.data.model.Show
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TvMazeApiService {

    @GET("shows")
    suspend fun getShows(
        @Query("page") page: Int
    ): List<Show>

    @GET("shows/{id}")
    suspend fun getShowById(
        @Path("id") id: Int
    ): Show

    @GET("search/shows")
    suspend fun searchShows(
        @Query("q") query: String
    ): List<SearchResult>
}