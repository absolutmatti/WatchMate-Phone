package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchlistItem
import com.example.ui.FilterState
import com.example.ui.NavTab
import com.example.ui.components.TvButton
import com.example.ui.components.TvPosterCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvCardBg
import com.example.ui.theme.TvCardFocused
import com.example.ui.theme.TvFocusBg
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceVariant

@Composable
fun HomeScreen(
    currentTab: NavTab,
    items: List<WatchlistItem>,
    filterState: FilterState,
    subscribedProviders: Set<String>,
    onTabSelected: (NavTab) -> Unit,
    onItemSelected: (WatchlistItem) -> Unit,
    onOpenFilterDialog: () -> Unit,
    onOpenPairingDialog: () -> Unit,
    onShufflePick: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onLoadSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top TV Navigation Bar
        TvNavigationBar(
            currentTab = currentTab,
            onTabSelected = onTabSelected,
            onOpenPairingDialog = onOpenPairingDialog
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Action Bar above the poster grid (Filter, Shuffle, Info)
        TvActionBar(
            filterState = filterState,
            totalItemsCount = items.size,
            onOpenFilterDialog = onOpenFilterDialog,
            onShufflePick = onShufflePick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Poster Grid or Empty State
        if (items.isEmpty()) {
            EmptyWatchlistState(
                currentTab = currentTab,
                hasActiveFilters = filterState.selectedGenre != null || filterState.maxRuntimeMinutes != null || filterState.onlySubscribed,
                onNavigateToSearch = onNavigateToSearch,
                onLoadSampleData = onLoadSampleData,
                onResetFilters = onOpenFilterDialog
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 145.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("watchlist_grid")
            ) {
                items(items, key = { it.id }, contentType = { "poster_card" }) { item ->
                    val isSubscribed = item.primaryProviderName != null &&
                            com.example.data.preferences.KnownProviders.matchesAny(
                                item.primaryProviderName,
                                subscribedProviders
                            )
                    TvPosterCard(
                        item = item,
                        isInUserSubscriptions = isSubscribed,
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun TvNavigationBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onOpenPairingDialog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TvSurface)
            .padding(start = 16.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Logo & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = null,
                tint = CyanNeon,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "WatchMate",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = " TV",
                color = CyanNeon,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Navigation Tabs (D-Pad navigable) with horizontal & vertical content padding
        // so that scaled focused tabs (especially outer tabs: Suche and Einstellungen)
        // are never clipped by the edge of the container.
        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(NavTab.values()) { tab ->
                val isSelected = currentTab == tab
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()
                val tabScale by animateFloatAsState(
                    targetValue = if (isFocused) 1.06f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tabScale_${tab.name}"
                )

                val bgColor = when {
                    isSelected -> CyanPrimary
                    isFocused -> TvFocusBg
                    else -> Color.Transparent
                }
                val textColor = when {
                    isSelected -> Color.Black
                    isFocused -> TvFocusBorder
                    else -> Color.White
                }

                Surface(
                    modifier = Modifier
                        .zIndex(if (isFocused) 2f else if (isSelected) 1f else 0f)
                        .graphicsLayer {
                            scaleX = tabScale
                            scaleY = tabScale
                        }
                        .focusable(interactionSource = interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        )
                        .testTag("nav_tab_${tab.name}"),
                    color = bgColor,
                    shape = RoundedCornerShape(10.dp),
                    border = when {
                        isFocused -> BorderStroke(3.5.dp, TvFocusBorder)
                        isSelected -> BorderStroke(1.dp, CyanPrimary)
                        else -> null
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tab.iconLabel,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.title,
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Web-Companion / Smartphone Kopplung Button
        val pairInteraction = remember { MutableInteractionSource() }
        val isPairFocused by pairInteraction.collectIsFocusedAsState()
        val pairScale by animateFloatAsState(
            targetValue = if (isPairFocused) 1.06f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "pairScale"
        )
        Surface(
            modifier = Modifier
                .padding(start = 8.dp, end = 6.dp)
                .graphicsLayer {
                    scaleX = pairScale
                    scaleY = pairScale
                }
                .focusable(interactionSource = pairInteraction)
                .clickable(
                    interactionSource = pairInteraction,
                    indication = null,
                    onClick = onOpenPairingDialog
                )
                .testTag("nav_button_pairing"),
            color = if (isPairFocused) TvFocusBg else TvCardBg,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(
                width = if (isPairFocused) 3.dp else 1.dp,
                color = if (isPairFocused) TvFocusBorder else CyanNeon
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📱", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Web-App",
                    color = if (isPairFocused) TvFocusBorder else CyanNeon,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TvActionBar(
    filterState: FilterState,
    totalItemsCount: Int,
    onOpenFilterDialog: () -> Unit,
    onShufflePick: () -> Unit
) {
    val hasFilters = filterState.selectedGenre != null ||
            filterState.maxRuntimeMinutes != null ||
            filterState.onlySubscribed

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter Button
            TvButton(
                onClick = onOpenFilterDialog,
                modifier = Modifier.testTag("filter_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filter",
                        tint = if (hasFilters) CyanNeon else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasFilters) "Filter (Aktiv)" else "Filter",
                        color = if (hasFilters) CyanNeon else Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (hasFilters) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Shuffle Button ("Überrasch mich")
            TvButton(
                onClick = onShufflePick,
                modifier = Modifier.testTag("shuffle_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Überrasch mich",
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Überrasch mich 🎲",
                        color = AmberAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Counter Info Badge
        Text(
            text = "$totalItemsCount ${if (totalItemsCount == 1) "Titel" else "Titel"}",
            color = Color.Gray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyWatchlistState(
    currentTab: NavTab,
    hasActiveFilters: Boolean,
    onNavigateToSearch: () -> Unit,
    onLoadSampleData: () -> Unit,
    onResetFilters: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(TvSurface)
                .padding(36.dp)
        ) {
            Text(
                text = if (hasActiveFilters) "🔍 Keine passenden Treffer" else "🍿 Deine Liste ist noch leer",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasActiveFilters) {
                    "Mit den aktuellen Filtern wurden keine Titel gefunden. Setze die Filter zurück."
                } else {
                    "Füge deine Lieblingsfilme und Serien hinzu oder starte direkt mit beliebten Beispieldaten!"
                },
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                if (hasActiveFilters) {
                    TvButton(onClick = onResetFilters, isPrimary = true) {
                        Text("Filter anpassen", fontWeight = FontWeight.Bold)
                    }
                } else {
                    TvButton(onClick = onNavigateToSearch, isPrimary = true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Titel suchen ➕", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    TvButton(onClick = onLoadSampleData) {
                        Text("Beispieldaten laden 📥")
                    }
                }
            }
        }
    }
}
