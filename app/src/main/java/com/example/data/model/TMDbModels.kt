package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TMDbMultiSearchResponse(
    val page: Int = 1,
    val results: List<TMDbSearchItem> = emptyList(),
    @Json(name = "total_results") val totalResults: Int = 0,
    @Json(name = "total_pages") val totalPages: Int = 0
)

@JsonClass(generateAdapter = true)
data class TMDbSearchItem(
    val id: Long,
    @Json(name = "media_type") val mediaType: String? = null, // "movie", "tv", "person"
    val title: String? = null, // Movie title
    val name: String? = null, // TV title
    @Json(name = "original_title") val originalTitle: String? = null,
    @Json(name = "original_name") val originalName: String? = null,
    val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "genre_ids") val genreIds: List<Int>? = null
) {
    val displayTitle: String
        get() = title ?: name ?: "Unbekannter Titel"

    val displayYear: String
        get() {
            val date = releaseDate ?: firstAirDate ?: return ""
            return if (date.length >= 4) date.substring(0, 4) else date
        }

    val resolvedMediaType: String
        get() = when {
            mediaType.equals("tv", ignoreCase = true) -> "tv"
            mediaType.equals("movie", ignoreCase = true) -> "movie"
            name != null && title == null -> "tv"
            else -> "movie"
        }
}

@JsonClass(generateAdapter = true)
data class TMDbMovieDetails(
    val id: Long,
    val title: String,
    @Json(name = "original_title") val originalTitle: String? = null,
    val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    val runtime: Int? = null,
    @Json(name = "vote_average") val voteAverage: Double? = 0.0,
    val genres: List<TMDbGenre> = emptyList(),
    val videos: TMDbVideoResults? = null,
    @Json(name = "watch/providers") val watchProviders: TMDbWatchProvidersResult? = null
)

@JsonClass(generateAdapter = true)
data class TMDbTvDetails(
    val id: Long,
    val name: String,
    @Json(name = "original_name") val originalName: String? = null,
    val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int? = null,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int? = null,
    val seasons: List<TMDbSeason> = emptyList(),
    @Json(name = "vote_average") val voteAverage: Double? = 0.0,
    val genres: List<TMDbGenre> = emptyList(),
    val videos: TMDbVideoResults? = null,
    @Json(name = "watch/providers") val watchProviders: TMDbWatchProvidersResult? = null
)

@JsonClass(generateAdapter = true)
data class TMDbGenre(
    val id: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class TMDbSeason(
    val id: Long,
    val name: String? = null,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "episode_count") val episodeCount: Int = 0,
    @Json(name = "poster_path") val posterPath: String? = null
)

@JsonClass(generateAdapter = true)
data class TMDbVideoResults(
    val results: List<TMDbVideoItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TMDbVideoItem(
    val id: String,
    val key: String,
    val name: String,
    val site: String, // "YouTube"
    val type: String, // "Trailer", "Teaser"
    val official: Boolean = false
)

@JsonClass(generateAdapter = true)
data class TMDbWatchProvidersResult(
    val results: Map<String, TMDbCountryProviders> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class TMDbCountryProviders(
    val link: String? = null,
    val flatrate: List<TMDbProviderInfo>? = null,
    val buy: List<TMDbProviderInfo>? = null,
    val rent: List<TMDbProviderInfo>? = null,
    val free: List<TMDbProviderInfo>? = null,
    val ads: List<TMDbProviderInfo>? = null
)

@JsonClass(generateAdapter = true)
data class TMDbProviderInfo(
    @Json(name = "provider_id") val providerId: Int,
    @Json(name = "provider_name") val providerName: String,
    @Json(name = "logo_path") val logoPath: String? = null,
    @Json(name = "display_priority") val displayPriority: Int = 0
) {
    val fullLogoUrl: String?
        get() = logoPath?.let {
            if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w185$it"
        }
}
