package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.WatchlistItem
import com.example.data.model.TMDbSearchItem
import com.example.data.preferences.KnownProviders
import com.example.data.preferences.UserPreferences
import com.example.data.remote.SampleDataProvider
import com.example.data.repository.EpisodeAdvanceResult
import com.example.data.repository.ProviderDetails
import com.example.data.repository.WatchlistRepository
import com.example.util.StreamingAppLauncher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class NavTab(val title: String, val iconLabel: String) {
    ADD("Suche", "➕"),
    ALL("Alle", "🎬"),
    MOVIES("Filme", "🍿"),
    SERIES("Serien", "📺"),
    WATCHED("Gesehen", "✅"),
    DROPPED("Abgebrochen", "🗑️"),
    SETTINGS("Einstellungen", "⚙️")
}

data class FilterState(
    val selectedGenre: String? = null,
    val maxRuntimeMinutes: Int? = null, // e.g. 90, 120
    val onlySubscribed: Boolean = false
)

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class OpenUrl(val url: String) : UiEvent()
    data class PlayTrailer(val youtubeKey: String) : UiEvent()
    data class LaunchProviderApp(
        val providerName: String,
        val title: String,
        val candidatePackages: List<String>,
        val fallbackUrl: String?
    ) : UiEvent()
}

class WatchlistViewModel(
    private val repository: WatchlistRepository,
    private val preferences: UserPreferences,
    val syncService: com.example.data.sync.WatchlistFirestoreSyncService? = null
) : ViewModel() {

    private val _currentTab = MutableStateFlow(NavTab.ALL)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _selectedItem = MutableStateFlow<WatchlistItem?>(null)
    val selectedItem: StateFlow<WatchlistItem?> = _selectedItem.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<TMDbSearchItem>>(emptyList())
    val searchResults: StateFlow<List<TMDbSearchItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    val subscribedProviders = preferences.subscribedProviders
    val currentRegion = preferences.region
    val tmdbApiKey = preferences.apiKey

    // Cloud Sync & Auth States
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> =
        syncService?.currentUser ?: MutableStateFlow(null).asStateFlow()
    val syncStatusText: StateFlow<String> =
        syncService?.syncStatus ?: MutableStateFlow("Bereit").asStateFlow()

    // Dialog states
    val seriesFinishedDialogItem = MutableStateFlow<WatchlistItem?>(null)
    val statusChangeDialogItem = MutableStateFlow<WatchlistItem?>(null)
    val deleteConfirmDialogItem = MutableStateFlow<WatchlistItem?>(null)
    val isFilterDialogVisible = MutableStateFlow(false)
    val isPairingDialogVisible = MutableStateFlow(false)

    init {
        repository.syncService = syncService
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    fun getPairingUrl(): String {
        val baseUrl = "https://ais-pre-dshkttsmumjwxvx4ys56qq-721974800887.europe-west1.run.app/index.html"
        val key = preferences.apiKey.value.trim()
        val reg = preferences.region.value.trim()
        return "$baseUrl?tmdb_key=$key&region=$reg"
    }

    fun signInWithGoogle(activity: android.app.Activity) {
        syncService?.signInWithGoogle(
            activity = activity,
            onSuccess = {
                viewModelScope.launch {
                    _events.emit(UiEvent.ShowToast("Google-Konto erfolgreich verbunden!"))
                }
            },
            onError = { err ->
                viewModelScope.launch {
                    _events.emit(UiEvent.ShowToast(err))
                }
            }
        )
    }

    fun signOut(context: android.content.Context) {
        syncService?.signOut(context) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowToast("Erfolgreich abgemeldet."))
            }
        }
    }

    val rawItems: StateFlow<List<WatchlistItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamically filtered and strictly sorted titles according to user specs
    val displayedItems: StateFlow<List<WatchlistItem>> = combine(
        rawItems,
        _currentTab,
        _filterState,
        preferences.subscribedProviders
    ) { items, tab, filter, userSubs ->
        filterAndSortItems(items, tab, filter, userSubs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Available genres in current view for filter chip selection
    val availableGenres: StateFlow<List<String>> = rawItems.combine(_currentTab) { items, tab ->
        val tabFiltered = items.filter { item ->
            when (tab) {
                NavTab.ALL -> item.status != WatchlistItem.STATUS_WATCHED && item.status != WatchlistItem.STATUS_DROPPED
                NavTab.MOVIES -> item.isMovie && item.status != WatchlistItem.STATUS_WATCHED && item.status != WatchlistItem.STATUS_DROPPED
                NavTab.SERIES -> item.isTv && item.status != WatchlistItem.STATUS_WATCHED && item.status != WatchlistItem.STATUS_DROPPED
                NavTab.WATCHED -> item.status == WatchlistItem.STATUS_WATCHED
                NavTab.DROPPED -> item.status == WatchlistItem.STATUS_DROPPED
                else -> true
            }
        }
        tabFiltered.flatMap { it.genres.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun filterAndSortItems(
        items: List<WatchlistItem>,
        tab: NavTab,
        filter: FilterState,
        userSubs: Set<String>
    ): List<WatchlistItem> {
        // 1. Filter by Tab
        val tabItems = items.filter { item ->
            when (tab) {
                NavTab.ALL -> item.status != WatchlistItem.STATUS_WATCHED && item.status != WatchlistItem.STATUS_DROPPED
                NavTab.MOVIES -> item.isMovie && item.status != WatchlistItem.STATUS_WATCHED && item.status != WatchlistItem.STATUS_DROPPED
                NavTab.SERIES -> item.isTv && item.status != WatchlistItem.STATUS_WATCHED && item.status != WatchlistItem.STATUS_DROPPED
                NavTab.WATCHED -> item.status == WatchlistItem.STATUS_WATCHED
                NavTab.DROPPED -> item.status == WatchlistItem.STATUS_DROPPED
                NavTab.ADD, NavTab.SETTINGS -> false
            }
        }

        // 2. Filter by user criteria
        val filtered = tabItems.filter { item ->
            val matchesGenre = filter.selectedGenre == null ||
                    item.genres.lowercase().contains(filter.selectedGenre.lowercase())

            val matchesRuntime = filter.maxRuntimeMinutes == null ||
                    (item.runtime != null && item.runtime <= filter.maxRuntimeMinutes)

            val matchesSubs = !filter.onlySubscribed ||
                    isItemInUserSubscriptions(item, userSubs)

            matchesGenre && matchesRuntime && matchesSubs
        }

        // 3. Strict Sorting Rules:
        // Rule: "Gerade am ansehen" (WATCHING) are pinned at the top, sorted by lastWatchedAt DESC.
        // Below them follow all "Noch nicht gesehen" (PLAN_TO_WATCH), sorted by addedAt DESC.
        return when (tab) {
            NavTab.ALL, NavTab.SERIES -> {
                val watching = filtered
                    .filter { it.status == WatchlistItem.STATUS_WATCHING }
                    .sortedByDescending { it.lastWatchedAt }

                val other = filtered
                    .filter { it.status != WatchlistItem.STATUS_WATCHING }
                    .sortedByDescending { it.addedAt }

                watching + other
            }
            NavTab.MOVIES -> {
                filtered.sortedByDescending { it.addedAt }
            }
            NavTab.WATCHED -> {
                filtered.sortedByDescending { it.lastWatchedAt.takeIf { t -> t > 0 } ?: it.addedAt }
            }
            NavTab.DROPPED -> {
                filtered.sortedByDescending { it.addedAt }
            }
            else -> filtered
        }
    }

    fun isItemInUserSubscriptions(item: WatchlistItem, userSubs: Set<String>): Boolean {
        if (item.primaryProviderName != null && KnownProviders.matchesAny(item.primaryProviderName, userSubs)) {
            return true
        }
        val pJson = item.providersJson ?: return false
        return KnownProviders.ALL_PROVIDERS.any { prov ->
            userSubs.contains(prov.id) && pJson.lowercase().contains(prov.id)
        }
    }

    fun setTab(tab: NavTab) {
        _currentTab.value = tab
        _selectedItem.value = null
    }

    fun selectItem(item: WatchlistItem?) {
        _selectedItem.value = item
        if (item != null) {
            viewModelScope.launch {
                val refreshed = repository.refreshProviders(item)
                if (_selectedItem.value?.id == item.id) {
                    _selectedItem.value = refreshed
                }
            }
        }
    }

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(450) // 450ms debounce prevents freezing/stuttering while typing on TV keyboard
                _isSearching.value = true
                try {
                    val results = repository.searchTitles(trimmed)
                    _searchResults.value = results
                } catch (e: Exception) {
                    android.util.Log.e("WatchlistViewModel", "Search failed: ${e.message}")
                } finally {
                    _isSearching.value = false
                }
            }
        } else {
            _searchResults.value = emptyList()
            _isSearching.value = false
        }
    }

    fun performSearch(query: String) {
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            searchJob = viewModelScope.launch {
                _isSearching.value = true
                try {
                    val results = repository.searchTitles(trimmed)
                    _searchResults.value = results
                } catch (e: Exception) {
                    android.util.Log.e("WatchlistViewModel", "Immediate search failed: ${e.message}")
                } finally {
                    _isSearching.value = false
                }
            }
        }
    }

    fun onSearchItemClicked(searchItem: TMDbSearchItem) {
        viewModelScope.launch {
            _isSearching.value = true
            val item = repository.fetchOrCreateItem(searchItem)
            _selectedItem.value = item
            _isSearching.value = false
            val refreshed = repository.refreshProviders(item)
            if (_selectedItem.value?.id == item.id) {
                _selectedItem.value = refreshed
            }
        }
    }

    fun addToWatchlist(item: WatchlistItem) {
        viewModelScope.launch {
            repository.insertItem(item)
            _selectedItem.value = item
            _events.emit(UiEvent.ShowToast("Zu deiner Watchlist hinzugefügt!"))
        }
    }

    fun removeFromWatchlist(item: WatchlistItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _selectedItem.value = null
            deleteConfirmDialogItem.value = null
            _events.emit(UiEvent.ShowToast("Aus der Watchlist entfernt."))
        }
    }

    fun changeStatus(item: WatchlistItem, newStatus: String) {
        viewModelScope.launch {
            repository.updateStatus(item.id, newStatus)
            val updated = item.copy(status = newStatus)
            _selectedItem.value = updated
            statusChangeDialogItem.value = null
            _events.emit(UiEvent.ShowToast("Status geändert zu: ${getStatusLabel(newStatus)}"))
        }
    }

    fun advanceEpisode(item: WatchlistItem) {
        viewModelScope.launch {
            when (val result = repository.advanceEpisode(item)) {
                is EpisodeAdvanceResult.Advanced -> {
                    _selectedItem.value = result.item
                    _events.emit(
                        UiEvent.ShowToast(
                            "Fortschritt: S${result.newSeason.toString().padStart(2, '0')} E${result.newEpisode.toString().padStart(2, '0')}"
                        )
                    )
                }
                is EpisodeAdvanceResult.SeriesFinished -> {
                    _selectedItem.value = result.item
                    seriesFinishedDialogItem.value = result.item
                }
            }
        }
    }

    fun shufflePickRandom() {
        val current = displayedItems.value
        if (current.isNotEmpty()) {
            val randomItem = current[Random.nextInt(current.size)]
            _selectedItem.value = randomItem
            viewModelScope.launch {
                _events.emit(UiEvent.ShowToast("Zufällige Auswahl: ${randomItem.title}"))
            }
        } else {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowToast("Keine Titel in dieser Ansicht vorhanden."))
            }
        }
    }

    fun toggleGenreFilter(genre: String) {
        _filterState.value = _filterState.value.let {
            if (it.selectedGenre == genre) it.copy(selectedGenre = null)
            else it.copy(selectedGenre = genre)
        }
    }

    fun setRuntimeFilter(maxMinutes: Int?) {
        _filterState.value = _filterState.value.copy(maxRuntimeMinutes = maxMinutes)
    }

    fun toggleOnlySubscribedFilter() {
        _filterState.value = _filterState.value.copy(onlySubscribed = !_filterState.value.onlySubscribed)
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    // Settings actions
    fun toggleStreamingProvider(providerId: String) {
        preferences.toggleProvider(providerId)
    }

    fun setRegion(region: String) {
        preferences.setRegion(region)
    }

    fun setApiKey(key: String) {
        preferences.setApiKey(key)
        viewModelScope.launch {
            _events.emit(UiEvent.ShowToast("TMDb API-Schlüssel gespeichert!"))
        }
    }

    fun reloadSampleData() {
        viewModelScope.launch {
            val samples = SampleDataProvider.getInitialWatchlist()
            samples.forEach { repository.insertItem(it) }
            _events.emit(UiEvent.ShowToast("Beispieldaten erfolgreich geladen!"))
        }
    }

    fun openTrailer(key: String) {
        viewModelScope.launch {
            _events.emit(UiEvent.PlayTrailer(key))
        }
    }

    fun playOnProvider(item: WatchlistItem) {
        val providerName = item.primaryProviderName ?: "Streaming"
        playOnProviderPlatform(item, providerName)
    }

    fun playOnProviderPlatform(item: WatchlistItem, providerName: String) {
        val candidates = StreamingAppLauncher.getCandidatePackages(providerName)
        val known = KnownProviders.ALL_PROVIDERS.firstOrNull { prov ->
            prov.tmdbProviderNames.any { providerName.contains(it, ignoreCase = true) } ||
                    providerName.contains(prov.name, ignoreCase = true)
        }
        val fallback = known?.webFallbackUrl ?: "https://www.google.com/search?q=${item.title}+${providerName}+streamen"
        viewModelScope.launch {
            _events.emit(
                UiEvent.LaunchProviderApp(
                    providerName = providerName,
                    title = item.title,
                    candidatePackages = candidates,
                    fallbackUrl = fallback
                )
            )
        }
    }

    fun getStatusLabel(status: String): String = when (status) {
        WatchlistItem.STATUS_PLAN_TO_WATCH -> "Noch nicht gesehen"
        WatchlistItem.STATUS_WATCHING -> "Gerade am ansehen"
        WatchlistItem.STATUS_WATCHED -> "Gesehen"
        WatchlistItem.STATUS_DROPPED -> "Abgebrochen"
        else -> status
    }

    class Factory(
        private val repository: WatchlistRepository,
        private val preferences: UserPreferences,
        private val syncService: com.example.data.sync.WatchlistFirestoreSyncService? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WatchlistViewModel(repository, preferences, syncService) as T
        }
    }
}
