package com.example.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.TMDbSearchItem
import com.example.ui.components.TvButton
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvCardBg
import com.example.ui.theme.TvCardFocused
import com.example.ui.theme.TvFocusBg
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvSurface

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

@Composable
fun SearchScreen(
    searchQuery: String,
    searchSuggestions: List<TMDbSearchItem>,
    searchResults: List<TMDbSearchItem>,
    isSearching: Boolean,
    hasSubmittedSearch: Boolean,
    onQueryChanged: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onSuggestionClicked: (TMDbSearchItem) -> Unit,
    onSearchItemClicked: (TMDbSearchItem) -> Unit,
    onBack: () -> Unit,
    existingWatchlistIds: Set<Long>,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    // Local decoupled text state for 0ms typing response without triggering heavy recomposition
    var localText by remember { mutableStateOf(searchQuery) }

    LaunchedEffect(searchQuery) {
        if (searchQuery != localText) {
            localText = searchQuery
        }
    }

    LaunchedEffect(localText) {
        if (localText != searchQuery) {
            delay(280)
            onQueryChanged(localText)
        }
    }

    // Voice search launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                localText = spoken
                onQueryChanged(spoken)
                onSearchSubmit(spoken)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Bar: Back Button & Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TvButton(
                onClick = onBack,
                modifier = Modifier.testTag("search_back_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Zurück",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Zurück", fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Titel suchen & hinzufügen",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Input Bar with Submit Button and Microphone Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = localText,
                onValueChange = { localText = it },
                placeholder = {
                    Text("Film oder Serie eingeben (z. B. Der Herr, Spid, Dune)...", color = Color.Gray, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CyanNeon)
                },
                trailingIcon = {
                    if (localText.isNotEmpty()) {
                        IconButton(onClick = {
                            localText = ""
                            onQueryChanged("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Löschen", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (localText.isNotBlank()) {
                        onQueryChanged(localText)
                        onSearchSubmit(localText)
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TvFocusBorder,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = TvFocusBg,
                    unfocusedContainerColor = TvSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = TvFocusBorder
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_input_field")
            )

            // Explicit Suchen Button for D-Pad / Remote Navigation
            TvButton(
                onClick = {
                    if (localText.isNotBlank()) {
                        onQueryChanged(localText)
                        onSearchSubmit(localText)
                    }
                },
                isPrimary = true,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("submit_search_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Suchen",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Suchen", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Voice Search Button
            TvButton(
                onClick = {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Film oder Serie sprechen...")
                        }
                        speechLauncher.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "Sprachsuche wird auf diesem Gerät nicht unterstützt.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                isPrimary = false,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("voice_search_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Sprachsuche",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sprachsuche", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subtle non-blocking progress bar during typing/search updates
        if (isSearching) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = TvFocusBorder,
                trackColor = Color(0x33FBBF24)
            )
            Spacer(modifier = Modifier.height(10.dp))
        } else {
            Spacer(modifier = Modifier.height(13.dp))
        }

        // --- DISPLAY LOGIC: GOOGLE-STYLE SUGGESTIONS vs. POSTER CARDS ---
        if (!hasSubmittedSearch) {
            // SUGGESTIONS MODE (No heavy poster image decodes while typing!)
            if (localText.trim().length >= 2) {
                if (searchSuggestions.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_suggestions_container")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Suchvorschläge (mit D-Pad auswählen oder Enter drücken):",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${searchSuggestions.size} Treffer",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("search_suggestions_list")
                        ) {
                            items(searchSuggestions, key = { "sugg_${it.id}_${it.resolvedMediaType}" }) { item ->
                                SearchSuggestionRow(
                                    item = item,
                                    onSelect = { onSuggestionClicked(item) }
                                )
                            }
                        }
                    }
                } else if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = TvFocusBorder, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Suche Vorschläge für \"$localText\"...", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Keine Vorschläge für \"$localText\".\nDrücke auf \"Suchen\" für die Volltextsuche.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TvButton(
                                onClick = {
                                    onQueryChanged(localText)
                                    onSearchSubmit(localText)
                                },
                                isPrimary = true
                            ) {
                                Text("Jetzt \"$localText\" suchen", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                // Idle hint
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Schnelle TV-Suche wie bei Google",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tippe mindestens 2 Buchstaben ein (z. B. 'Der Herr'), um sofort Vorschläge als Text zu sehen.\nDie Kacheln laden erst nach Bestätigung mit der Lupe oder Enter.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 520.dp)
                        )
                    }
                }
            }
        } else {
            // RESULTS GRID MODE (Shown only after submitting search or selecting suggestion)
            if (isSearching && searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TvFocusBorder, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Lade Filmergebnisse von TMDb...", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Keine Treffer für \"$searchQuery\" gefunden.\nVersuche einen anderen Titel oder prüfe die Schreibweise.",
                            color = Color.Gray,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ergebnisse für \"$searchQuery\" (${searchResults.size}):",
                            color = Color(0xFFE2E8F0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 145.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_results_grid")
                    ) {
                        items(searchResults, key = { it.id }, contentType = { "search_card" }) { item ->
                            val isAlreadyInList = existingWatchlistIds.contains(item.id)
                            SearchResultCard(
                                item = item,
                                isAlreadyInList = isAlreadyInList,
                                onClick = { onSearchItemClicked(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Super lightweight, zero-overhead text suggestion row for Google-like TV search.
 * No image decodes, instant render, smooth 60fps D-Pad navigation.
 */
@Composable
fun SearchSuggestionRow(
    item: TMDbSearchItem,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "suggestion_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            )
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)
                ) {
                    onSelect()
                    true
                } else {
                    false
                }
            }
            .testTag("search_suggestion_${item.id}"),
        color = if (isFocused) TvFocusBg else TvSurface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon matching Google search suggestions
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = if (isFocused) TvFocusBorder else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Title
            Text(
                text = item.displayTitle,
                color = if (isFocused) TvFocusBorder else Color.White,
                fontSize = 15.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Year
            if (item.displayYear.isNotBlank()) {
                Text(
                    text = item.displayYear,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Media Badge
            Surface(
                color = if (item.resolvedMediaType == "movie") Color(0x3338BDF8) else Color(0x33A855F7),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (item.resolvedMediaType == "movie") "Film" else "Serie",
                    color = if (item.resolvedMediaType == "movie") Color(0xFF7DD3FC) else Color(0xFFD8B4FE),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            if (isFocused) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Auswählen ↵",
                    color = TvFocusBorder,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SearchResultCard(
    item: TMDbSearchItem,
    isAlreadyInList: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale = if (isFocused) 1.05f else 1.0f

    val posterUrl = item.posterPath?.let {
        if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w342$it"
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(4.dp)
            .width(160.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isFocused) 3.5.dp else 1.dp,
                    color = if (isFocused) TvFocusBorder else Color(0xFF334155),
                    shape = RoundedCornerShape(12.dp)
                )
                .focusable(interactionSource = interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .testTag("search_card_${item.id}"),
            colors = CardDefaults.cardColors(
                containerColor = if (isFocused) TvFocusBg else TvCardBg
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = item.displayTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TvCardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.resolvedMediaType == "movie") Icons.Default.Movie else Icons.Default.Tv,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Type Badge (Film / Serie)
                Surface(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart),
                    color = Color(0xCC000000),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (item.resolvedMediaType == "movie") "Film" else "Serie",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                // In Watchlist Indicator
                if (isAlreadyInList) {
                    Surface(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.TopEnd),
                        color = CyanPrimary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "In Liste ✓",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Bottom Rating & Year
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color(0xDD090D16))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if ((item.voteAverage ?: 0.0) > 0.0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AmberGlow,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", item.voteAverage),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (item.displayYear.isNotBlank()) {
                        Text(
                            text = item.displayYear,
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.displayTitle,
            color = if (isFocused) TvFocusBorder else Color.White,
            fontSize = 13.sp,
            fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
