package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.WatchlistDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.remote.TMDbApiService
import com.example.data.repository.WatchlistRepository
import com.example.ui.NavTab
import com.example.ui.UiEvent
import com.example.ui.WatchlistViewModel
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.FilterDialog
import com.example.ui.components.SeriesFinishedDialog
import com.example.ui.components.StatusChangeDialog
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.remember
import com.example.data.sync.WatchlistFirestoreSyncService
import com.example.ui.components.TvPairingDialog
import com.example.ui.theme.WatchMateTvTheme
import android.app.Activity

class MainActivity : ComponentActivity() {

    private val syncService: WatchlistFirestoreSyncService by lazy {
        val db = WatchlistDatabase.getDatabase(applicationContext)
        WatchlistFirestoreSyncService(
            context = applicationContext,
            dao = db.watchlistDao(),
            scope = lifecycleScope
        )
    }

    private val viewModel: WatchlistViewModel by viewModels {
        val db = WatchlistDatabase.getDatabase(applicationContext)
        val apiService = TMDbApiService.create()
        val preferences = UserPreferences(applicationContext)
        val repository = WatchlistRepository(db.watchlistDao(), apiService, preferences)
        WatchlistViewModel.Factory(repository, preferences, syncService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Attempt silent auto-sign in if previously authenticated with Google
        syncService.attemptAutoSignIn(this)

        setContent {
            WatchMateTvTheme {
                WatchMateTvApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WatchMateTvApp(viewModel: WatchlistViewModel) {
    val context = LocalContext.current

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val displayedItems by viewModel.displayedItems.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val hasSubmittedSearch by viewModel.hasSubmittedSearch.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val subscribedProviders by viewModel.subscribedProviders.collectAsStateWithLifecycle()
    val currentRegion by viewModel.currentRegion.collectAsStateWithLifecycle()
    val tmdbApiKey by viewModel.tmdbApiKey.collectAsStateWithLifecycle()
    val availableGenres by viewModel.availableGenres.collectAsStateWithLifecycle()
    val rawItems by viewModel.rawItems.collectAsStateWithLifecycle()

    val seriesFinishedItem by viewModel.seriesFinishedDialogItem.collectAsStateWithLifecycle()
    val statusChangeItem by viewModel.statusChangeDialogItem.collectAsStateWithLifecycle()
    val deleteConfirmItem by viewModel.deleteConfirmDialogItem.collectAsStateWithLifecycle()
    val isFilterDialogVisible by viewModel.isFilterDialogVisible.collectAsStateWithLifecycle()
    val isPairingDialogVisible by viewModel.isPairingDialogVisible.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val syncStatusText by viewModel.syncStatusText.collectAsStateWithLifecycle()

    // Handle UI Events (Toasts, Trailer, Deep-Links)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.PlayTrailer -> {
                    com.example.util.StreamingAppLauncher.launchTrailer(
                        context = context,
                        youtubeKey = event.youtubeKey
                    )
                }
                is UiEvent.LaunchProviderApp -> {
                    com.example.util.StreamingAppLauncher.launch(
                        context = context,
                        providerName = event.providerName,
                        title = event.title,
                        explicitCandidates = event.candidatePackages,
                        fallbackUrl = event.fallbackUrl
                    )
                }
                is UiEvent.OpenUrl -> {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(event.url)))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Link konnte nicht geöffnet werden.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Main Navigation Flow
            when {
                selectedItem != null -> {
                    val item = selectedItem!!
                    val isInList = rawItems.any { it.id == item.id }
                    DetailScreen(
                        item = item,
                        isInWatchlist = isInList,
                        subscribedProviders = subscribedProviders,
                        onBack = { viewModel.selectItem(null) },
                        onAddToList = { viewModel.addToWatchlist(it) },
                        onPlayOnProvider = { viewModel.playOnProvider(it) },
                        onPlayOnProviderPlatform = { platformName -> viewModel.playOnProviderPlatform(item, platformName) },
                        onOpenTrailer = { viewModel.openTrailer(it) },
                        onAdvanceEpisode = { viewModel.advanceEpisode(it) },
                        onOpenStatusDialog = { viewModel.statusChangeDialogItem.value = it },
                        onOpenDeleteDialog = { viewModel.deleteConfirmDialogItem.value = it }
                    )
                }
                currentTab == NavTab.ADD -> {
                    val existingIds = rawItems.map { it.id }.toSet()
                    SearchScreen(
                        searchQuery = searchQuery,
                        searchSuggestions = searchSuggestions,
                        searchResults = searchResults,
                        isSearching = isSearching,
                        hasSubmittedSearch = hasSubmittedSearch,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onSearchSubmit = { viewModel.performSearch(it) },
                        onSuggestionClicked = { viewModel.onSuggestionClicked(it) },
                        onSearchItemClicked = { viewModel.onSearchItemClicked(it) },
                        onBack = { viewModel.setTab(NavTab.ALL) },
                        existingWatchlistIds = existingIds
                    )
                }
                currentTab == NavTab.SETTINGS -> {
                    SettingsScreen(
                        currentTab = currentTab,
                        subscribedProviders = subscribedProviders,
                        currentRegion = currentRegion,
                        apiKey = tmdbApiKey,
                        currentUser = currentUser,
                        syncStatusText = syncStatusText,
                        onTabSelected = { viewModel.setTab(it) },
                        onToggleProvider = { viewModel.toggleStreamingProvider(it) },
                        onSetRegion = { viewModel.setRegion(it) },
                        onSaveApiKey = { viewModel.setApiKey(it) },
                        onReloadSampleData = { viewModel.reloadSampleData() },
                        onOpenPairingDialog = { viewModel.isPairingDialogVisible.value = true }
                    )
                }
                else -> {
                    HomeScreen(
                        currentTab = currentTab,
                        items = displayedItems,
                        filterState = filterState,
                        subscribedProviders = subscribedProviders,
                        onTabSelected = { viewModel.setTab(it) },
                        onItemSelected = { viewModel.selectItem(it) },
                        onOpenFilterDialog = { viewModel.isFilterDialogVisible.value = true },
                        onOpenPairingDialog = { viewModel.isPairingDialogVisible.value = true },
                        onShufflePick = { viewModel.shufflePickRandom() },
                        onNavigateToSearch = { viewModel.setTab(NavTab.ADD) },
                        onLoadSampleData = { viewModel.reloadSampleData() }
                    )
                }
            }

            // Dialogs
            if (isPairingDialogVisible) {
                val pairingUrl = remember(tmdbApiKey, currentRegion) { viewModel.getPairingUrl() }
                TvPairingDialog(
                    pairingUrl = pairingUrl,
                    currentUser = currentUser,
                    syncStatusText = syncStatusText,
                    onSignInWithGoogle = {
                        (context as? Activity)?.let { viewModel.signInWithGoogle(it) }
                    },
                    onSignOut = {
                        viewModel.signOut(context)
                    },
                    onDismiss = { viewModel.isPairingDialogVisible.value = false }
                )
            }

            if (isFilterDialogVisible) {
                FilterDialog(
                    filterState = filterState,
                    availableGenres = availableGenres,
                    onToggleGenre = { viewModel.toggleGenreFilter(it) },
                    onSetRuntime = { viewModel.setRuntimeFilter(it) },
                    onToggleOnlySubscribed = { viewModel.toggleOnlySubscribedFilter() },
                    onResetFilters = { viewModel.resetFilters() },
                    onDismiss = { viewModel.isFilterDialogVisible.value = false }
                )
            }

            statusChangeItem?.let { item ->
                StatusChangeDialog(
                    item = item,
                    onStatusSelected = { newStatus -> viewModel.changeStatus(item, newStatus) },
                    onDismiss = { viewModel.statusChangeDialogItem.value = null }
                )
            }

            seriesFinishedItem?.let { item ->
                SeriesFinishedDialog(
                    item = item,
                    onConfirmWatched = {
                        viewModel.changeStatus(item, com.example.data.local.WatchlistItem.STATUS_WATCHED)
                        viewModel.seriesFinishedDialogItem.value = null
                    },
                    onDismiss = { viewModel.seriesFinishedDialogItem.value = null }
                )
            }

            deleteConfirmItem?.let { item ->
                DeleteConfirmDialog(
                    item = item,
                    onConfirmDelete = { viewModel.removeFromWatchlist(item) },
                    onDismiss = { viewModel.deleteConfirmDialogItem.value = null }
                )
            }
        }
    }
}
