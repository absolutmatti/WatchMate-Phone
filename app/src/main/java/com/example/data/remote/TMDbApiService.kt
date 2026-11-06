package com.example.data.remote

import com.example.data.model.TMDbMovieDetails
import com.example.data.model.TMDbMultiSearchResponse
import com.example.data.model.TMDbTvDetails
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface TMDbApiService {

    @GET("3/search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = DEFAULT_API_KEY,
        @Query("language") language: String = "de-DE",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): TMDbMultiSearchResponse

    @GET("3/movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Long,
        @Query("api_key") apiKey: String = DEFAULT_API_KEY,
        @Query("language") language: String = "de-DE",
        @Query("append_to_response") appendToResponse: String = "videos,watch/providers"
    ): TMDbMovieDetails

    @GET("3/tv/{series_id}")
    suspend fun getTvDetails(
        @Path("series_id") seriesId: Long,
        @Query("api_key") apiKey: String = DEFAULT_API_KEY,
        @Query("language") language: String = "de-DE",
        @Query("append_to_response") appendToResponse: String = "videos,watch/providers"
    ): TMDbTvDetails

    @GET("3/movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(
        @Path("movie_id") movieId: Long,
        @Query("api_key") apiKey: String = DEFAULT_API_KEY
    ): com.example.data.model.TMDbWatchProvidersResult

    @GET("3/tv/{series_id}/watch/providers")
    suspend fun getTvWatchProviders(
        @Path("series_id") seriesId: Long,
        @Query("api_key") apiKey: String = DEFAULT_API_KEY
    ): com.example.data.model.TMDbWatchProvidersResult

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/"
        const val DEFAULT_API_KEY = "1b490ff6b2d76245e0c8503271f5f094"
        const val DEFAULT_READ_ACCESS_TOKEN =
            "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIxYjQ5MGZmNmIyZDc2MjQ1ZTBjODUwMzI3MWY1ZjA5NCIsIm5iZiI6MTc5MzgxNjkxNC40ODQsInN1YiI6IjZhZWI3OTUyZWQ2MDdhMzNkM2FhNDBkNiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.W9EY84OFlfK1GKAAez5q8zG1pokratCqh8w3Y19WCws"

        fun create(): TMDbApiService {
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url

                val effectiveApiKey = com.example.BuildConfig.TMDB_API_KEY.takeIf { it.isNotBlank() && !it.startsWith("YOUR_") } ?: DEFAULT_API_KEY
                val effectiveToken = com.example.BuildConfig.TMDB_READ_ACCESS_TOKEN.takeIf { it.isNotBlank() && !it.startsWith("YOUR_") } ?: DEFAULT_READ_ACCESS_TOKEN

                // Ensure api_key is present in query parameters if not supplied
                val urlBuilder = originalUrl.newBuilder()
                if (originalUrl.queryParameter("api_key").isNullOrBlank()) {
                    urlBuilder.addQueryParameter("api_key", effectiveApiKey)
                }

                val newRequest = originalRequest.newBuilder()
                    .url(urlBuilder.build())
                    .header("Authorization", "Bearer $effectiveToken")
                    .header("Accept", "application/json")
                    .build()

                chain.proceed(newRequest)
            }

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(TMDbApiService::class.java)
        }
    }
}
