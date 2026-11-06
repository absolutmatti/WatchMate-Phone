package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StreamingProvider(
    val id: String,
    val name: String,
    val iconName: String,
    val tmdbProviderNames: List<String>,
    val appPackageName: String? = null,
    val webFallbackUrl: String? = null
)

object KnownProviders {
    val ALL_PROVIDERS = listOf(
        StreamingProvider(
            id = "netflix",
            name = "Netflix",
            iconName = "netflix",
            tmdbProviderNames = listOf("Netflix", "Netflix basic with Ads"),
            appPackageName = "com.netflix.ninja",
            webFallbackUrl = "https://www.netflix.com"
        ),
        StreamingProvider(
            id = "prime",
            name = "Amazon Prime Video",
            iconName = "prime",
            tmdbProviderNames = listOf("Amazon Prime Video", "Amazon Video", "Prime Video"),
            appPackageName = "com.amazon.amazonvideo.livingroom",
            webFallbackUrl = "https://www.amazon.de/primevideo"
        ),
        StreamingProvider(
            id = "disney",
            name = "Disney+",
            iconName = "disney",
            tmdbProviderNames = listOf("Disney Plus", "Disney+"),
            appPackageName = "com.disney.disneyplus",
            webFallbackUrl = "https://www.disneyplus.com"
        ),
        StreamingProvider(
            id = "wow",
            name = "WOW / Sky",
            iconName = "wow",
            tmdbProviderNames = listOf("WOW", "Sky Go", "Sky X", "Sky Ticket"),
            appPackageName = "de.sky.online",
            webFallbackUrl = "https://www.wowtv.de"
        ),
        StreamingProvider(
            id = "appletv",
            name = "Apple TV+",
            iconName = "appletv",
            tmdbProviderNames = listOf("Apple TV Plus", "Apple TV", "Apple TV+"),
            appPackageName = "com.apple.atve.androidtv.appletv",
            webFallbackUrl = "https://tv.apple.com"
        ),
        StreamingProvider(
            id = "paramount",
            name = "Paramount+",
            iconName = "paramount",
            tmdbProviderNames = listOf("Paramount Plus", "Paramount+"),
            appPackageName = "com.cbs.app",
            webFallbackUrl = "https://www.paramountplus.com"
        ),
        StreamingProvider(
            id = "rtl",
            name = "RTL+",
            iconName = "rtl",
            tmdbProviderNames = listOf("RTL+", "TVNOW"),
            appPackageName = "de.rtli.tvnow",
            webFallbackUrl = "https://plus.rtl.de"
        ),
        StreamingProvider(
            id = "joyn",
            name = "Joyn",
            iconName = "joyn",
            tmdbProviderNames = listOf("Joyn", "Joyn Plus+", "Joyn Plus"),
            appPackageName = "de.joyn.android.tv",
            webFallbackUrl = "https://www.joyn.de"
        ),
        StreamingProvider(
            id = "magentatv",
            name = "MagentaTV",
            iconName = "magentatv",
            tmdbProviderNames = listOf("MagentaTV", "Magenta TV", "Telekom"),
            appPackageName = "de.telekom.magentatv.tv",
            webFallbackUrl = "https://web.magentatv.de"
        ),
        StreamingProvider(
            id = "crunchyroll",
            name = "Crunchyroll",
            iconName = "crunchyroll",
            tmdbProviderNames = listOf("Crunchyroll"),
            appPackageName = "com.crunchyroll.crunchyroid",
            webFallbackUrl = "https://www.crunchyroll.com"
        )
    )

    fun matchesAny(providerName: String, subscribedIds: Set<String>): Boolean {
        val lower = providerName.lowercase()
        return ALL_PROVIDERS.any { provider ->
            subscribedIds.contains(provider.id) &&
                    provider.tmdbProviderNames.any { lower.contains(it.lowercase()) }
        }
    }
}

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("watchmate_tv_prefs", Context.MODE_PRIVATE)

    private val _subscribedProviders = MutableStateFlow(loadSubscribedProviders())
    val subscribedProviders: StateFlow<Set<String>> = _subscribedProviders.asStateFlow()

    private val _region = MutableStateFlow(prefs.getString(KEY_REGION, "DE") ?: "DE")
    val region: StateFlow<String> = _region.asStateFlow()

    private val _apiKey = MutableStateFlow(
        prefs.getString(KEY_API_KEY, null)
            ?: com.example.BuildConfig.TMDB_API_KEY.takeIf { it.isNotBlank() && !it.startsWith("YOUR_") }
            ?: DEFAULT_KEY
    )
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private fun loadSubscribedProviders(): Set<String> {
        val set = prefs.getStringSet(KEY_PROVIDERS, null)
        return set ?: setOf("netflix", "prime", "disney")
    }

    fun toggleProvider(providerId: String) {
        val current = _subscribedProviders.value.toMutableSet()
        if (current.contains(providerId)) {
            current.remove(providerId)
        } else {
            current.add(providerId)
        }
        prefs.edit().putStringSet(KEY_PROVIDERS, current).apply()
        _subscribedProviders.value = current
    }

    fun setRegion(newRegion: String) {
        prefs.edit().putString(KEY_REGION, newRegion).apply()
        _region.value = newRegion
    }

    fun setApiKey(newKey: String) {
        prefs.edit().putString(KEY_API_KEY, newKey.trim()).apply()
        _apiKey.value = newKey.trim()
    }

    var isInitialDataSeeded: Boolean
        get() = prefs.getBoolean(KEY_SEEDED, false)
        set(value) = prefs.edit().putBoolean(KEY_SEEDED, value).apply()

    companion object {
        const val DEFAULT_KEY = "1b490ff6b2d76245e0c8503271f5f094"
        private const val KEY_PROVIDERS = "subscribed_providers"
        private const val KEY_REGION = "tmdb_region"
        private const val KEY_API_KEY = "tmdb_api_key"
        private const val KEY_SEEDED = "initial_data_seeded"
    }
}
