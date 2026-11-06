package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.WatchlistItem
import com.example.ui.FilterState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenWatched
import com.example.ui.theme.RedDropped
import com.example.ui.theme.TvCardBg
import com.example.ui.theme.TvCardFocused
import com.example.ui.theme.TvFocusBg
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    filterState: FilterState,
    availableGenres: List<String>,
    onToggleGenre: (String) -> Unit,
    onSetRuntime: (Int?) -> Unit,
    onToggleOnlySubscribed: () -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 400.dp, max = 640.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, CyanPrimary, RoundedCornerShape(16.dp))
                .testTag("filter_dialog"),
            color = TvSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Watchlist filtern",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TvButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFF334155)
                )

                // Nur meine Abos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleOnlySubscribed() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = filterState.onlySubscribed,
                        onCheckedChange = { onToggleOnlySubscribed() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = CyanNeon,
                            checkmarkColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Nur meine Streaming-Abos",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Nur Titel anzeigen, die in deinen konfigurierten Abos enthalten sind",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Maximale Laufzeit
                Text(
                    text = "Maximale Laufzeit (Filme):",
                    color = CyanNeon,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        null to "Alle",
                        90 to "< 90 Min",
                        120 to "< 120 Min",
                        150 to "< 150 Min"
                    ).forEach { (minutes, label) ->
                        val isSelected = filterState.maxRuntimeMinutes == minutes
                        FilterChip(
                            label = label,
                            isSelected = isSelected,
                            onClick = { onSetRuntime(minutes) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Genres
                if (availableGenres.isNotEmpty()) {
                    Text(
                        text = "Genre:",
                        color = CyanNeon,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableGenres.forEach { genre ->
                            val isSelected = filterState.selectedGenre == genre
                            FilterChip(
                                label = genre,
                                isSelected = isSelected,
                                onClick = { onToggleGenre(genre) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TvButton(onClick = onResetFilters) {
                        Text("Filter zurücksetzen", fontSize = 13.sp)
                    }
                    TvButton(onClick = onDismiss, isPrimary = true) {
                        Text("Fertig", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val chipScale = if (isFocused) 1.05f else 1.0f

    val bgColor = when {
        isFocused -> TvFocusBg
        isSelected -> CyanPrimary
        else -> TvCardBg
    }
    val textColor = when {
        isFocused -> TvFocusBorder
        isSelected -> Color.Black
        else -> Color.White
    }

    Surface(
        modifier = Modifier
            .graphicsLayer {
                scaleX = chipScale
                scaleY = chipScale
            }
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isFocused) 3.5.dp else if (isSelected) 1.5.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else if (isSelected) CyanPrimary else Color(0xFF334155),
                shape = RoundedCornerShape(8.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun StatusChangeDialog(
    item: WatchlistItem,
    onStatusSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 360.dp, max = 460.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, CyanPrimary, RoundedCornerShape(16.dp)),
            color = TvSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Status ändern",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.title,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                val statuses = mutableListOf(
                    WatchlistItem.STATUS_PLAN_TO_WATCH to ("Noch nicht gesehen" to Color.White)
                )
                if (item.isTv) {
                    statuses.add(WatchlistItem.STATUS_WATCHING to ("Gerade am ansehen" to AmberAccent))
                }
                statuses.add(WatchlistItem.STATUS_WATCHED to ("Gesehen" to GreenWatched))
                statuses.add(WatchlistItem.STATUS_DROPPED to ("Abgebrochen" to RedDropped))

                statuses.forEach { (statusKey, pair) ->
                    val (label, color) = pair
                    val isCurrent = item.status == statusKey
                    val itemInteraction = remember { MutableInteractionSource() }
                    val isItemFocused by itemInteraction.collectIsFocusedAsState()
                    val itemScale = if (isItemFocused) 1.04f else 1.0f

                    val itemBorderColor = when {
                        isItemFocused -> TvFocusBorder
                        isCurrent -> CyanNeon
                        else -> Color(0xFF334155)
                    }
                    val itemBorderWidth = if (isItemFocused) 3.5.dp else if (isCurrent) 2.dp else 1.dp
                    val itemBg = when {
                        isItemFocused -> TvFocusBg
                        isCurrent -> TvCardFocused
                        else -> TvCardBg
                    }

                    Surface(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = itemScale
                                scaleY = itemScale
                            }
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = itemBorderWidth,
                                color = itemBorderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .focusable(interactionSource = itemInteraction)
                            .clickable(
                                interactionSource = itemInteraction,
                                indication = null
                            ) { onStatusSelected(statusKey) },
                        color = itemBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                color = if (isItemFocused) TvFocusBorder else color,
                                fontSize = 14.sp,
                                fontWeight = if (isCurrent || isItemFocused) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isItemFocused) TvFocusBorder else CyanNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TvButton(onClick = onDismiss) {
                        Text("Abbrechen", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesFinishedDialog(
    item: WatchlistItem,
    onConfirmWatched: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 360.dp, max = 460.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, AmberAccent, RoundedCornerShape(16.dp)),
            color = TvSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 Serie beendet!",
                    color = AmberAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Du hast die letzte Folge von \"${item.title}\" geschaut!",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Möchtest du diese Serie jetzt als 'Gesehen' markieren?",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TvButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Später", fontSize = 13.sp)
                    }
                    TvButton(
                        onClick = onConfirmWatched,
                        modifier = Modifier.weight(1.3f),
                        isPrimary = true
                    ) {
                        Text("✅ Als 'Gesehen' markieren", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    item: WatchlistItem,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 340.dp, max = 420.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, RedDropped, RoundedCornerShape(16.dp)),
            color = TvSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = RedDropped,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Titel entfernen?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Möchtest du \"${item.title}\" wirklich aus deiner Watchlist entfernen?",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TvButton(onClick = onDismiss) {
                        Text("Abbrechen", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    TvButton(onClick = onConfirmDelete, isPrimary = true) {
                        Text("Löschen", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TvPairingDialog(
    pairingUrl: String,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    syncStatusText: String,
    onSignInWithGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    val qrImageBitmap = remember(pairingUrl) {
        com.example.util.QrCodeGenerator.generateQrBitmap(pairingUrl, 400)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 520.dp, max = 680.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, CyanNeon, RoundedCornerShape(20.dp))
                .testTag("tv_pairing_dialog"),
            color = TvSurface,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📱", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Web-Companion Kopplung",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    TvButton(onClick = onDismiss) {
                        Text("✕", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // QR Code Box
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                bitmap = qrImageBitmap,
                                contentDescription = "QR Code zur Kopplung mit der Web-App",
                                modifier = Modifier.size(190.dp)
                            )
                        }
                    }

                    // Instructions & Auth Status
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1. Kamera auf Smartphone auf QR-Code richten",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. Web-App öffnet sich & übernimmt TMDb + Region",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "3. Filme/Serien suchen und 1-Klick auf den TV senden",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Cloud Sync Badge / Account
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TvCardBg,
                            border = BorderStroke(1.dp, if (currentUser != null) CyanPrimary else AmberAccent)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (currentUser != null) "☁️ TV Cloud-Sync aktiv" else "☁️ TV Cloud-Sync (Optional)",
                                    color = if (currentUser != null) CyanNeon else AmberAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentUser?.email ?: syncStatusText,
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🔗 $pairingUrl",
                            color = Color(0xFFAAAAAA),
                            fontSize = 10.sp,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentUser == null) {
                        TvButton(
                            onClick = onSignInWithGoogle,
                            isPrimary = true,
                            modifier = Modifier.testTag("google_signin_button")
                        ) {
                            Text(
                                text = "Mit Google anmelden",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        TvButton(
                            onClick = onSignOut,
                            modifier = Modifier.testTag("google_signout_button")
                        ) {
                            Text(
                                text = "Abmelden",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    TvButton(onClick = onDismiss, isPrimary = currentUser != null) {
                        Text(
                            text = "Fertig",
                            color = if (currentUser != null) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

