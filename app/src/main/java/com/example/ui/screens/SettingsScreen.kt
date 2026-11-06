package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.components.M3LoadingView
import com.example.ui.theme.GitBlue
import com.example.ui.theme.GitBorder
import com.example.ui.theme.GitGreen
import com.example.ui.theme.GitGreenBright
import com.example.ui.theme.GitSurface
import com.example.ui.theme.GitSurfaceVariant
import com.example.ui.theme.HiveAmber
import com.example.ui.theme.HiveGold
import com.example.util.ChromeTabHelper

data class ColorPresetOption(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color
)

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentThemeMode by viewModel.themeMode.collectAsState()
    val currentColorPreset by viewModel.colorPreset.collectAsState()
    val currentLoadingStyle by viewModel.m3LoadingStyle.collectAsState()
    val currentSyncInterval by viewModel.rssSyncIntervalMinutes.collectAsState()
    val isFastMode by viewModel.fastUploadMode.collectAsState()
    val isRssNotificationEnabled by viewModel.isRssNotificationEnabled.collectAsState()

    var customRemoteConfigUrl by remember { mutableStateOf(viewModel.getRemoteConfigUrl()) }

    val colorPresets = listOf(
        ColorPresetOption("HIVE_GOLD", "Hive & Git", GitGreenBright, HiveGold, Color(0xFF0D1117)),
        ColorPresetOption("DYNAMIC", "Material You", Color(0xFF6750A4), Color(0xFF625B71), Color(0xFF1C1B1F)),
        ColorPresetOption("OCEAN_BLUE", "Ocean Cyan", Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF0B1329)),
        ColorPresetOption("CYBER_PURPLE", "Cyber Violet", Color(0xFFA855F7), Color(0xFFEC4899), Color(0xFF120B24)),
        ColorPresetOption("SUNSET_AMBER", "Sunset Fire", Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF18100A)),
        ColorPresetOption("AMOLED_BLACK", "AMOLED Pitch", Color(0xFF00FF88), Color(0xFF38BDF8), Color(0xFF000000))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "App Preferences & Themes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Material 3 colors, expressive loading & Telegram releases",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section 1: Telegram Release & Distribution Hub
        TelegramReleaseCard(context = context)

        // Section 2: Material 3 Colour Palette Customization
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("m3_color_palette_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Material 3 Colour Schemes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your preferred M3 dynamic accent palette across all screens",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Color Presets Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorPresets.chunked(2).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPresets.forEach { preset ->
                                val isSelected = currentColorPreset.equals(preset.id, ignoreCase = true)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.updateColorPreset(preset.id) }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else GitBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    color = GitSurfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Color Preview Swatch
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(preset.backgroundColor)
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(preset.primaryColor)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = preset.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Theme Mode Switch (Dark / Light / System)
                Text(
                    text = "Display Mode",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DARK" to "Dark", "LIGHT" to "Light", "SYSTEM" to "System").forEach { (modeKey, label) ->
                        val isModeSelected = currentThemeMode.equals(modeKey, ignoreCase = true)
                        FilterChip(
                            selected = isModeSelected,
                            onClick = { viewModel.updateThemeMode(modeKey) },
                            label = { Text(label, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (modeKey == "LIGHT") Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section 3: Material 3 Like Loading Animations & Live Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("m3_loading_style_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Animation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Material 3 Loading Animations",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Customize the M3 indeterminate progress indicators and skeleton loaders",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Loading Style Selector
                val loadingStyles = listOf(
                    "CIRCULAR_EXPRESSIVE" to "M3 Expressive Circular",
                    "LINEAR_WAVE" to "M3 Linear Wave",
                    "SKELETON_SHIMMER" to "M3 Skeleton Shimmer"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    loadingStyles.forEach { (styleKey, styleTitle) ->
                        val isSelected = currentLoadingStyle.equals(styleKey, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.updateM3LoadingStyle(styleKey) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else GitBorder,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else GitSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = styleTitle,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Interactive M3 Loading Preview Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.background,
                    border = BorderStroke(1.dp, GitBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LIVE M3 LOADING PREVIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic live preview of selected loading animation
                        M3LoadingView(
                            style = currentLoadingStyle,
                            message = "Connecting to Osun Hive & GitHub...",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Section 4: Background RSS & Notification Preferences
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rss_preferences_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Background RSS Sync Frequency",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Background RSS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Background Feed Auto-Sync",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fetch new stories and post image notifications when closed",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isRssNotificationEnabled,
                        onCheckedChange = { viewModel.setRssNotificationEnabled(it, context) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sync Interval Selection
                Text(
                    text = "Sync Interval",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15 to "15m", 30 to "30m", 60 to "1 Hour", 180 to "3 Hours").forEach { (mins, label) ->
                        val isIntervalSelected = currentSyncInterval == mins
                        FilterChip(
                            selected = isIntervalSelected,
                            onClick = { viewModel.updateRssSyncInterval(mins, context) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section 5: Git Upload Performance & Remote JSON Configuration
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_performance_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Engine Optimization & Remote Links",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fast Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Parallel Multi-Worker Upload (6x)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Stream blobs concurrently to GitHub Trees API",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isFastMode,
                        onCheckedChange = { viewModel.updateFastUploadMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Remote Links Config URL Input
                Text(
                    text = "GitHub Remote Links JSON Endpoint",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = customRemoteConfigUrl,
                    onValueChange = { customRemoteConfigUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = GitBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { viewModel.updateRemoteConfigUrl(customRemoteConfigUrl) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save & Fetch Links", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 6: About & Osun Hive Technology Branding
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Osunhive Technology",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "GitMobile Uploader • v1.0.0 Open Source",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Powered by Osunhive Technology • Building mobile developer tools and youth digital empowerment.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { ChromeTabHelper.openUrl(context, "https://www.osunhive.name.ng") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Official Website", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { ChromeTabHelper.openUrl(context, "https://yo.fan/osunhivetech") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Yo.fan Page", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Dedicated Card answering: "Can this APK release to Telegram?" with 1-tap distribution actions.
 */
@Composable
private fun TelegramReleaseCard(context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("telegram_release_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E2E)),
        border = BorderStroke(1.dp, Color(0xFF2AABEE)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color(0xFF2AABEE),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Telegram Release & Distribution",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF2AABEE).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "100% Supported",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2AABEE),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Yes! You can freely release and distribute this APK directly on Telegram. Android allows instant installation of APK files downloaded from Telegram channels without Google Play Store restrictions.",
                fontSize = 12.sp,
                color = Color(0xFFC7D8E8),
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step-by-step checklist
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF08121C))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "HOW TO RELEASE ON TELEGRAM:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2AABEE)
                )
                Text(
                    text = "1. Export 'gitmobile-osunhive.apk' to your device",
                    fontSize = 11.sp,
                    color = Color(0xFFE2EDF8)
                )
                Text(
                    text = "2. In your Telegram channel/group, tap Attach 📎 -> File",
                    fontSize = 11.sp,
                    color = Color(0xFFE2EDF8)
                )
                Text(
                    text = "3. Attach the APK, add changelog/description, and Send",
                    fontSize = 11.sp,
                    color = Color(0xFFE2EDF8)
                )
                Text(
                    text = "4. Users tap the file to install instantly on any Android phone!",
                    fontSize = 11.sp,
                    color = Color(0xFF2AABEE),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        shareApkToTelegram(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2AABEE),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Send to Telegram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        openTelegram(context)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF2AABEE)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2AABEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Telegram", fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Triggers Android Share Sheet with Telegram package priority or generic file share
 */
private fun shareApkToTelegram(context: Context) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "GitMobile Uploader • Osunhive Technology APK Release")
            putExtra(
                Intent.EXTRA_TEXT,
                "🚀 Download GitMobile Uploader (by Osunhive Technology)\n" +
                "Direct APK release for GitHub uploads, live RSS news, and mobile dev tools!\n\n" +
                "Website: https://www.osunhive.name.ng\n" +
                "Yo.fan: https://yo.fan/osunhivetech"
            )
        }

        // Try to target Telegram app directly if installed
        val pm = context.packageManager
        val telegramIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            `package` = "org.telegram.messenger"
            putExtra(Intent.EXTRA_TEXT, shareIntent.getStringExtra(Intent.EXTRA_TEXT))
        }

        if (telegramIntent.resolveActivity(pm) != null) {
            context.startActivity(telegramIntent)
        } else {
            context.startActivity(Intent.createChooser(shareIntent, "Share APK to Telegram"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun openTelegram(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me"))
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
