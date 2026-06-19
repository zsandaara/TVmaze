package com.example.tvmaze.di

import com.example.tvmaze.data.api.TvMazeApiService
import com.example.tvmaze.data.model.Image
import com.example.tvmaze.data.model.Rating
import com.example.tvmaze.data.model.SearchResult
import com.example.tvmaze.data.model.Show
import com.example.tvmaze.data.repository.ShowRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.delay
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object TestAppModule {

    private val testShows = listOf(
        Show(
            id = 1,
            name = "Test Show 1",
            summary = "This is a test summary for show 1",
            genres = listOf("Drama", "Romance"),
            rating = Rating(8.5),
            image = Image("https://example.com/medium1.jpg", "https://example.com/original1.jpg"),
            language = "English",
            premiered = "2020-01-01",
            ended = null,
            status = "Running",
            officialSite = "https://example.com/show1"
        ),
        Show(
            id = 2,
            name = "Test Show 2",
            summary = "This is a test summary for show 2",
            genres = listOf("Comedy"),
            rating = Rating(7.5),
            image = Image("https://example.com/medium2.jpg", "https://example.com/original2.jpg"),
            language = "English",
            premiered = "2021-06-15",
            ended = null,
            status = "Running",
            officialSite = null
        ),
        Show(
            id = 3,
            name = "Test Show 3",
            summary = "This is a test summary for show 3",
            genres = listOf("Sci-Fi", "Adventure"),
            rating = Rating(9.0),
            image = Image("https://example.com/medium3.jpg", "https://example.com/original3.jpg"),
            language = "English",
            premiered = "2019-03-20",
            ended = "2022-12-31",
            status = "Ended",
            officialSite = "https://example.com/show3"
        )
    )

    @Provides
    @Singleton
    fun provideTvMazeApiService(): TvMazeApiService {
        return object : TvMazeApiService {
            override suspend fun getShows(page: Int): List<Show> {
                delay(100)
                return testShows
            }

            override suspend fun getShowById(id: Int): Show {
                delay(100)
                return testShows.find { it.id == id } ?: testShows.first()
            }

            override suspend fun searchShows(query: String): List<SearchResult> {
                delay(100)
                if (query.contains("nonexistent") || query.isEmpty()) {
                    return emptyList()
                }
                return testShows
                    .filter { it.name.contains(query, ignoreCase = true) }
                    .map { SearchResult(10.0, it) }
            }
        }
    }

    @Provides
    @Singleton
    fun provideShowRepository(apiService: TvMazeApiService): ShowRepository {
        return ShowRepository(apiService)
    }
}