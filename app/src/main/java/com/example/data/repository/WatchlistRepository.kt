package com.example.data.repository

import android.util.Log
import com.example.data.local.WatchlistDao
import com.example.data.local.WatchlistItem
import com.example.data.model.TMDbCountryProviders
import com.example.data.model.TMDbMovieDetails
import com.example.data.model.TMDbSearchItem
import com.example.data.model.TMDbTvDetails
import com.example.data.preferences.KnownProviders
import com.example.data.preferences.UserPreferences
import com.example.data.remote.SampleDataProvider
import com.example.data.remote.TMDbApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed class EpisodeAdvanceResult {
    data class Advanced(val item: WatchlistItem, val newSeason: Int, val newEpisode: Int) : EpisodeAdvanceResult()
    data class SeriesFinished(val item: WatchlistItem) : EpisodeAdvanceResult()
}

data class ProviderEntry(
    val name: String,
    val logo: String?,
    val type: String // "flatrate", "ads", "free", "buy_rent"
)

data class ProviderDetails(
    val inUserSubscriptions: List<ProviderEntry>,
    val otherFlatrates: List<ProviderEntry>,
    val buyOrRent: List<ProviderEntry>,
    val primaryProviderName: String?,
    val primaryProviderLogo: String?,
    val watchLink: String?
)

class WatchlistRepository(
    private val dao: WatchlistDao,
    private val apiService: TMDbApiService,
    private val preferences: UserPreferences
) {
    var syncService: com.example.data.sync.WatchlistFirestoreSyncService? = null

    val allItems: Flow<List<WatchlistItem>> = dao.getAllFlow()

    fun getItemByIdFlow(id: Long): Flow<WatchlistItem?> = dao.getItemByIdFlow(id)

    suspend fun getItemById(id: Long): WatchlistItem? = dao.getItemByIdDirect(id)

    suspend fun checkAndSeedInitialData() {
        withContext(Dispatchers.IO) {
            if (!preferences.isInitialDataSeeded) {
                val initial = SampleDataProvider.getInitialWatchlist()
                dao.insertAll(initial)
                preferences.isInitialDataSeeded = true
            }
        }
    }

    suspend fun insertItem(item: WatchlistItem) {
        withContext(Dispatchers.IO) {
            dao.insert(item)
            syncService?.syncItemToCloud(item)
        }
    }

    suspend fun deleteItem(item: WatchlistItem) {
        withContext(Dispatchers.IO) {
            dao.delete(item)
            syncService?.deleteItemFromCloud(item.id)
        }
    }

    suspend fun deleteById(id: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteById(id)
            syncService?.deleteItemFromCloud(id)
        }
    }

    suspend fun updateStatus(id: Long, newStatus: String) {
        withContext(Dispatchers.IO) {
            dao.updateStatus(id, newStatus)
            dao.getItemByIdDirect(id)?.let { syncService?.syncItemToCloud(it) }
        }
    }

    /**
     * Advances episode for a TV series:
     * - S01 E00 -> S01 E01
     * - S01 E08 -> S02 E01 (if S01 has 8 episodes)
     * - Returns SeriesFinished if the last episode of the series is reached
     */
    suspend fun advanceEpisode(item: WatchlistItem): EpisodeAdvanceResult {
        return withContext(Dispatchers.IO) {
            val seasonCounts = parseSeasonEpisodeCounts(item.seasonEpisodeCountsJson)
            val currentSeason = item.currentSeason.coerceAtLeast(1)
            val currentEpisode = item.currentEpisode

            val episodesInCurrentSeason = seasonCounts[currentSeason] ?: 10
            val maxSeasons = item.numberOfSeasons ?: seasonCounts.keys.maxOrNull() ?: 1

            val newSeason: Int
            val newEpisode: Int
            val isFinalEpisode: Boolean

            if (currentEpisode < episodesInCurrentSeason) {
                newSeason = currentSeason
                newEpisode = currentEpisode + 1
                isFinalEpisode = (newSeason == maxSeasons && newEpisode == episodesInCurrentSeason)
            } else {
                if (currentSeason < maxSeasons) {
                    newSeason = currentSeason + 1
                    newEpisode = 1
                    val episodesInNext = seasonCounts[newSeason] ?: 10
                    isFinalEpisode = (newSeason == maxSeasons && newEpisode == episodesInNext)
                } else {
                    // Already at or past the final episode
                    return@withContext EpisodeAdvanceResult.SeriesFinished(item)
                }
            }

            val now = System.currentTimeMillis()
            dao.updateEpisodeProgress(
                id = item.id,
                season = newSeason,
                episode = newEpisode,
                status = WatchlistItem.STATUS_WATCHING,
                lastWatchedAt = now
            )

            val updatedItem = item.copy(
                currentSeason = newSeason,
                currentEpisode = newEpisode,
                status = WatchlistItem.STATUS_WATCHING,
                lastWatchedAt = now
            )
            syncService?.syncItemToCloud(updatedItem)

            if (isFinalEpisode) {
                EpisodeAdvanceResult.SeriesFinished(updatedItem)
            } else {
                EpisodeAdvanceResult.Advanced(updatedItem, newSeason, newEpisode)
            }
        }
    }

    private fun parseSeasonEpisodeCounts(json: String?): Map<Int, Int> {
        if (json.isNullOrBlank()) return emptyMap()
        val map = mutableMapOf<Int, Int>()
        try {
            val obj = JSONObject(json)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val seasonNum = key.toIntOrNull()
                if (seasonNum != null) {
                    map[seasonNum] = obj.optInt(key, 8)
                }
            }
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "Error parsing season episodes: ${e.message}")
        }
        return map
    }

    suspend fun searchTitles(query: String): List<TMDbSearchItem> {
        if (query.isBlank()) return emptyList()
        val key = preferences.apiKey.value.ifBlank { TMDbApiService.DEFAULT_API_KEY }
        val region = preferences.region.value

        return withContext(Dispatchers.IO) {
            try {
                val lang = if (region.equals("AT", true)) "de-AT" else if (region.equals("CH", true)) "de-CH" else "de-DE"
                val response = apiService.searchMulti(query = query, apiKey = key, language = lang)
                val filtered = response.results.filter {
                    it.mediaType == "movie" || it.mediaType == "tv"
                }
                if (filtered.isNotEmpty()) {
                    return@withContext filtered
                }
            } catch (e: Exception) {
                Log.e("WatchlistRepository", "TMDb API search failed: ${e.message}")
            }
            // Fallback to sample catalog search
            SampleDataProvider.searchSampleCatalog(query).map { item ->
                TMDbSearchItem(
                    id = item.id,
                    mediaType = item.mediaType,
                    title = if (item.isMovie) item.title else null,
                    name = if (item.isTv) item.title else null,
                    originalTitle = item.originalTitle,
                    originalName = item.originalTitle,
                    overview = item.overview,
                    posterPath = item.posterPath,
                    backdropPath = item.backdropPath,
                    releaseDate = if (item.isMovie) item.releaseYear else null,
                    firstAirDate = if (item.isTv) item.releaseYear else null,
                    voteAverage = item.voteAverage
                )
            }
        }
    }

    suspend fun fetchOrCreateItem(searchItem: TMDbSearchItem): WatchlistItem {
        val existing = dao.getItemByIdDirect(searchItem.id)
        if (existing != null) return existing

        val key = preferences.apiKey.value.ifBlank { TMDbApiService.DEFAULT_API_KEY }
        val region = preferences.region.value
        val isTv = searchItem.resolvedMediaType == "tv"

        return withContext(Dispatchers.IO) {
            try {
                val lang = if (region.equals("AT", true)) "de-AT" else if (region.equals("CH", true)) "de-CH" else "de-DE"
                if (isTv) {
                    val tv = apiService.getTvDetails(searchItem.id, key, language = lang)
                    return@withContext convertTvToWatchlist(tv)
                } else {
                    val movie = apiService.getMovieDetails(searchItem.id, key, language = lang)
                    return@withContext convertMovieToWatchlist(movie)
                }
            } catch (e: Exception) {
                Log.e("WatchlistRepository", "Failed to fetch details from TMDb: ${e.message}")
            }

            // Fallback construction
            val sample = SampleDataProvider.searchSampleCatalog(searchItem.displayTitle).firstOrNull { it.id == searchItem.id }
            if (sample != null) return@withContext sample

            val now = System.currentTimeMillis()
            WatchlistItem(
                id = searchItem.id,
                mediaType = searchItem.resolvedMediaType,
                title = searchItem.displayTitle,
                originalTitle = searchItem.originalTitle ?: searchItem.originalName,
                overview = searchItem.overview,
                posterPath = searchItem.posterPath,
                backdropPath = searchItem.backdropPath,
                releaseYear = searchItem.displayYear,
                voteAverage = searchItem.voteAverage ?: 0.0,
                status = WatchlistItem.STATUS_PLAN_TO_WATCH,
                addedAt = now,
                lastWatchedAt = 0,
                currentSeason = if (isTv) 1 else 0,
                currentEpisode = 0
            )
        }
    }

    private fun convertMovieToWatchlist(movie: TMDbMovieDetails): WatchlistItem {
        val now = System.currentTimeMillis()
        val genresStr = movie.genres.joinToString(", ") { it.name }
        val trailerKey = movie.videos?.results?.firstOrNull {
            it.site.equals("YouTube", ignoreCase = true) &&
                    (it.type.equals("Trailer", ignoreCase = true) || it.type.equals("Teaser", ignoreCase = true))
        }?.key

        val providers = extractProviderDetails(movie.watchProviders?.results)

        return WatchlistItem(
            id = movie.id,
            mediaType = "movie",
            title = movie.title,
            originalTitle = movie.originalTitle,
            overview = movie.overview,
            posterPath = movie.posterPath,
            backdropPath = movie.backdropPath,
            releaseYear = movie.releaseDate?.take(4),
            voteAverage = movie.voteAverage ?: 0.0,
            runtime = movie.runtime,
            genres = genresStr,
            status = WatchlistItem.STATUS_PLAN_TO_WATCH,
            addedAt = now,
            lastWatchedAt = 0,
            trailerYoutubeKey = trailerKey,
            primaryProviderName = providers.primaryProviderName,
            primaryProviderLogo = providers.primaryProviderLogo,
            providersJson = serializeProvidersJson(providers)
        )
    }

    private fun convertTvToWatchlist(tv: TMDbTvDetails): WatchlistItem {
        val now = System.currentTimeMillis()
        val genresStr = tv.genres.joinToString(", ") { it.name }
        val trailerKey = tv.videos?.results?.firstOrNull {
            it.site.equals("YouTube", ignoreCase = true) &&
                    (it.type.equals("Trailer", ignoreCase = true) || it.type.equals("Teaser", ignoreCase = true))
        }?.key

        val seasonCounts = JSONObject()
        tv.seasons.forEach { season ->
            if (season.seasonNumber > 0) {
                seasonCounts.put(season.seasonNumber.toString(), season.episodeCount)
            }
        }

        val providers = extractProviderDetails(tv.watchProviders?.results)

        return WatchlistItem(
            id = tv.id,
            mediaType = "tv",
            title = tv.name,
            originalTitle = tv.originalName,
            overview = tv.overview,
            posterPath = tv.posterPath,
            backdropPath = tv.backdropPath,
            releaseYear = tv.firstAirDate?.take(4),
            voteAverage = tv.voteAverage ?: 0.0,
            numberOfSeasons = tv.numberOfSeasons ?: tv.seasons.filter { it.seasonNumber > 0 }.size,
            numberOfEpisodes = tv.numberOfEpisodes,
            genres = genresStr,
            status = WatchlistItem.STATUS_PLAN_TO_WATCH,
            currentSeason = 1,
            currentEpisode = 0,
            seasonEpisodeCountsJson = seasonCounts.toString(),
            addedAt = now,
            lastWatchedAt = 0,
            trailerYoutubeKey = trailerKey,
            primaryProviderName = providers.primaryProviderName,
            primaryProviderLogo = providers.primaryProviderLogo,
            providersJson = serializeProvidersJson(providers)
        )
    }

    fun extractProviderDetails(results: Map<String, TMDbCountryProviders>?): ProviderDetails {
        if (results == null) {
            return ProviderDetails(emptyList(), emptyList(), emptyList(), null, null, null)
        }
        val region = preferences.region.value.uppercase()
        val country = results[region] ?: results["DE"] ?: results["US"]
            ?: return ProviderDetails(emptyList(), emptyList(), emptyList(), null, null, null)

        val userSubs = preferences.subscribedProviders.value
        val inUserSubs = mutableListOf<ProviderEntry>()
        val otherFlats = mutableListOf<ProviderEntry>()

        // 1. Flatrate
        country.flatrate.orEmpty().forEach { prov ->
            val isUserSub = KnownProviders.matchesAny(prov.providerName, userSubs)
            val entry = ProviderEntry(prov.providerName, prov.fullLogoUrl, "flatrate")
            if (isUserSub) inUserSubs.add(entry) else otherFlats.add(entry)
        }

        // 2. Free (Mediatheken, etc.)
        country.free.orEmpty().forEach { prov ->
            val isUserSub = KnownProviders.matchesAny(prov.providerName, userSubs)
            val entry = ProviderEntry(prov.providerName, prov.fullLogoUrl, "free")
            if (isUserSub) inUserSubs.add(entry) else otherFlats.add(entry)
        }

        // 3. Ads (e.g. Joyn, Freevee, Pluto TV, RTL+ Free)
        country.ads.orEmpty().forEach { prov ->
            val isUserSub = KnownProviders.matchesAny(prov.providerName, userSubs)
            val entry = ProviderEntry(prov.providerName, prov.fullLogoUrl, "ads")
            if (isUserSub) inUserSubs.add(entry) else otherFlats.add(entry)
        }

        val inUserSubsDistinct = inUserSubs.distinctBy { it.name }
        val otherFlatsDistinct = otherFlats
            .filterNot { other -> inUserSubsDistinct.any { it.name.equals(other.name, ignoreCase = true) } }
            .distinctBy { it.name }

        val buyRent = mutableListOf<ProviderEntry>()
        (country.buy.orEmpty() + country.rent.orEmpty()).distinctBy { it.providerId }.forEach { prov ->
            buyRent.add(ProviderEntry(prov.providerName, prov.fullLogoUrl, "buy_rent"))
        }

        val primary = inUserSubsDistinct.firstOrNull() ?: otherFlatsDistinct.firstOrNull() ?: buyRent.firstOrNull()

        return ProviderDetails(
            inUserSubscriptions = inUserSubsDistinct,
            otherFlatrates = otherFlatsDistinct,
            buyOrRent = buyRent,
            primaryProviderName = primary?.name,
            primaryProviderLogo = primary?.logo,
            watchLink = country.link
        )
    }

    suspend fun refreshProviders(item: WatchlistItem): WatchlistItem {
        return withContext(Dispatchers.IO) {
            try {
                val key = preferences.apiKey.value.ifBlank { TMDbApiService.DEFAULT_API_KEY }
                val results = if (item.isTv) {
                    apiService.getTvWatchProviders(item.id, key).results
                } else {
                    apiService.getMovieWatchProviders(item.id, key).results
                }
                val details = extractProviderDetails(results)
                val json = serializeProvidersJson(details)
                val updated = item.copy(
                    providersJson = json,
                    primaryProviderName = details.primaryProviderName ?: item.primaryProviderName,
                    primaryProviderLogo = details.primaryProviderLogo ?: item.primaryProviderLogo
                )
                dao.update(updated)
                updated
            } catch (e: Exception) {
                Log.e("WatchlistRepository", "Error refreshing providers: ${e.message}")
                item
            }
        }
    }

    private fun serializeProvidersJson(details: ProviderDetails): String {
        val array = JSONArray()
        // Save flatrate / ads / free streaming providers
        (details.inUserSubscriptions + details.otherFlatrates).distinctBy { it.name }.forEach { entry ->
            array.put(JSONObject().put("name", entry.name).put("logo", entry.logo).put("type", entry.type))
        }
        // Save buy/rent providers
        details.buyOrRent.distinctBy { it.name }.forEach { entry ->
            array.put(JSONObject().put("name", entry.name).put("logo", entry.logo).put("type", entry.type))
        }
        return array.toString()
    }
}
