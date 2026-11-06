package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_items")
data class WatchlistItem(
    @PrimaryKey val id: Long, // TMDb ID
    val mediaType: String, // "movie" or "tv"
    val title: String,
    val originalTitle: String? = null,
    val overview: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val releaseYear: String? = null,
    val voteAverage: Double = 0.0,
    val runtime: Int? = null, // in minutes for movies
    val numberOfSeasons: Int? = null,
    val numberOfEpisodes: Int? = null,
    val genres: String = "", // e.g. "Action, Sci-Fi"
    val status: String = STATUS_PLAN_TO_WATCH,
    val currentSeason: Int = 1,
    val currentEpisode: Int = 0,
    val seasonEpisodeCountsJson: String? = null, // e.g. "{\"1\":8,\"2\":10}"
    val addedAt: Long = System.currentTimeMillis(),
    val lastWatchedAt: Long = System.currentTimeMillis(),
    val trailerYoutubeKey: String? = null,
    val primaryProviderName: String? = null,
    val primaryProviderLogo: String? = null,
    val providersJson: String? = null
) {
    companion object {
        const val STATUS_PLAN_TO_WATCH = "PLAN_TO_WATCH"
        const val STATUS_WATCHING = "WATCHING"
        const val STATUS_WATCHED = "WATCHED"
        const val STATUS_DROPPED = "DROPPED"
    }

    val isMovie: Boolean get() = mediaType.equals("movie", ignoreCase = true)
    val isTv: Boolean get() = mediaType.equals("tv", ignoreCase = true)

    val posterUrl: String?
        get() = posterPath?.let {
            if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w342$it"
        }

    val backdropUrl: String?
        get() = backdropPath?.let {
            if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w1280$it"
        }
}
