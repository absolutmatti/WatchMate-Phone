package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.KnownProviders
import com.example.ui.NavTab
import com.example.ui.components.TvButton
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenWatched
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvCardBg
import com.example.ui.theme.TvFocusBg
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentTab: NavTab,
    subscribedProviders: Set<String>,
    currentRegion: String,
    apiKey: String,
    onTabSelected: (NavTab) -> Unit,
    onToggleProvider: (String) -> Unit,
    onSetRegion: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onReloadSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var inputKey by remember(apiKey) { mutableStateOf(apiKey) }

    BackHandler {
        onTabSelected(NavTab.ALL)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Navigation Bar
        TvNavigationBar(
            currentTab = currentTab,
            onTabSelected = onTabSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .testTag("settings_scroll_container")
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Einstellungen & Streaming-Dienste",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: Meine Streaming-Dienste (Subscriptions)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                color = TvSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "📺 Meine Streaming-Dienste (Eigene Abos)",
                        color = CyanNeon,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Markiere deine aktiven Abonnements. WatchMate TV hebt diese Anbieter auf Postern und Info-Karten hervor.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        KnownProviders.ALL_PROVIDERS.forEach { provider ->
                            val isChecked = subscribedProviders.contains(provider.id)
                            val provInteraction = remember { MutableInteractionSource() }
                            val isProvFocused by provInteraction.collectIsFocusedAsState()
                            val chipScale = if (isProvFocused) 1.06f else 1.0f

                            val chipBorderColor = when {
                                isProvFocused -> TvFocusBorder
                                isChecked -> CyanNeon
                                else -> Color(0xFF334155)
                            }
                            val chipBorderWidth = if (isProvFocused) 3.5.dp else if (isChecked) 2.dp else 1.dp
                            val chipBg = when {
                                isProvFocused -> TvFocusBg
                                isChecked -> Color(0xFF1E2D4A)
                                else -> TvCardBg
                            }

                            Surface(
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = chipScale
                                        scaleY = chipScale
                                    }
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = chipBorderWidth,
                                        color = chipBorderColor,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .focusable(interactionSource = provInteraction)
                                    .clickable(
                                        interactionSource = provInteraction,
                                        indication = null
                                    ) { onToggleProvider(provider.id) }
                                    .testTag("provider_check_${provider.id}"),
                                color = chipBg,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { onToggleProvider(provider.id) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = if (isProvFocused) TvFocusBorder else CyanNeon,
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = provider.name,
                                        color = if (isProvFocused) TvFocusBorder else if (isChecked) Color.White else Color.LightGray,
                                        fontWeight = if (isChecked || isProvFocused) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: Region Auswahl
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                color = TvSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Streaming-Region",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Wähle dein Land, damit die Streaming-Lizenzen (Flatrates, Kauf- und Leihrechte) exakt bestimmt werden.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(
                            "DE" to "🇩🇪 Deutschland",
                            "AT" to "🇦🇹 Österreich",
                            "CH" to "🇨🇭 Schweiz"
                        ).forEach { (code, label) ->
                            val isSelected = currentRegion.equals(code, ignoreCase = true)
                            val regInteraction = remember { MutableInteractionSource() }
                            val isRegFocused by regInteraction.collectIsFocusedAsState()
                            val regScale = if (isRegFocused) 1.06f else 1.0f

                            val regBorderColor = when {
                                isRegFocused -> TvFocusBorder
                                isSelected -> AmberAccent
                                else -> Color(0xFF334155)
                            }
                            val regBorderWidth = if (isRegFocused) 3.5.dp else if (isSelected) 2.dp else 1.dp
                            val regBg = when {
                                isRegFocused -> TvFocusBg
                                isSelected -> Color(0xFF2C2416)
                                else -> TvCardBg
                            }

                            Surface(
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = regScale
                                        scaleY = regScale
                                    }
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = regBorderWidth,
                                        color = regBorderColor,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .focusable(interactionSource = regInteraction)
                                    .clickable(
                                        interactionSource = regInteraction,
                                        indication = null
                                    ) { onSetRegion(code) },
                                color = regBg,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isRegFocused) TvFocusBorder else AmberAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = label,
                                        color = if (isRegFocused) TvFocusBorder else if (isSelected) AmberAccent else Color.White,
                                        fontWeight = if (isSelected || isRegFocused) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 3: TMDb API Key
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                color = TvSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TMDb API-Konfiguration",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Gib hier deinen persönlichen TMDb v3 API-Key ein, um unbegrenzt weltweit in Echtzeit Filme, Serien und Streaming-Anbieter abzufragen. (Bleibt das Feld leer, greift die App auf den kuratierten Katalog zurück).",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it },
                            placeholder = { Text("TMDb API-Key einfügen...", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = TvCardBg,
                                unfocusedContainerColor = TvCardBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("api_key_input")
                        )

                        TvButton(
                            onClick = { onSaveApiKey(inputKey) },
                            isPrimary = true,
                            modifier = Modifier.testTag("save_api_key_button")
                        ) {
                            Text("Speichern", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 4: Beispieldaten laden
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                color = TvSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "📥 Beispieldaten & Demo",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lade beliebte Serien und Filme (Severance, Stranger Things, Dune 2, Arcane, Inception, Breaking Bad) direkt in die lokale TV-Datenbank.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    TvButton(
                        onClick = onReloadSampleData,
                        modifier = Modifier.testTag("reload_sample_data_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = CyanNeon)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Beispieldaten jetzt laden", color = CyanNeon, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
