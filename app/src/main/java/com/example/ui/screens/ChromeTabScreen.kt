package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.GitBlue
import com.example.ui.theme.GitBorder
import com.example.ui.theme.GitGreen
import com.example.ui.theme.GitGreenBright
import com.example.ui.theme.GitRed
import com.example.ui.theme.GitSurface
import com.example.ui.theme.GitSurfaceVariant
import com.example.ui.theme.HiveGold
import com.example.util.ChromeTabHelper

data class QuickBookmark(
    val title: String,
    val description: String,
    val url: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun ChromeTabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var inputUrl by remember { mutableStateOf("https://www.osunhive.name.ng") }

    val bookmarks = remember {
        listOf(
            QuickBookmark(
                title = "Osunhive Technology",
                description = "Official tech platform, news & community",
                url = "https://www.osunhive.name.ng",
                icon = Icons.Default.Language,
                color = HiveGold
            ),
            QuickBookmark(
                title = "GitHub",
                description = "Explore repositories & activity",
                url = "https://github.com",
                icon = Icons.Default.Code,
                color = GitBlue
            ),
            QuickBookmark(
                title = "GitHub Tokens",
                description = "Generate or manage Personal Access Tokens",
                url = "https://github.com/settings/tokens",
                icon = Icons.Default.VpnKey,
                color = HiveGold
            ),
            QuickBookmark(
                title = "GitHub REST API Docs",
                description = "Reference for Git Blobs, Trees, and Commits",
                url = "https://docs.github.com/en/rest/git",
                icon = Icons.Default.Book,
                color = GitBlue
            ),
            QuickBookmark(
                title = "Git Reference Guide",
                description = "Official Git documentation & syntax",
                url = "https://git-scm.com/docs",
                icon = Icons.Default.Public,
                color = Color(0xFFF05032)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("osunhive_tab_header")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(HiveGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Osunhive Web",
                        tint = HiveGold,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "Osunhive Web Browser",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "High-speed in-app web browsing powered by Osunhive Technology",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // URL Input & Launch Bar Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("chrome_url_bar_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Web Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    label = { Text("Web Address") },
                    placeholder = { Text("https://www.osunhive.name.ng") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("osunhive_url_input"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = HiveGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (inputUrl.isNotBlank()) {
                            IconButton(onClick = { inputUrl = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear URL"
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (inputUrl.isNotBlank()) {
                                ChromeTabHelper.openUrl(context, inputUrl)
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HiveGold,
                        unfocusedBorderColor = GitBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (inputUrl.isNotBlank()) {
                            ChromeTabHelper.openUrl(context, inputUrl)
                        }
                    },
                    enabled = inputUrl.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_in_osunhive_browser_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HiveGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open with Osunhive Browser",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Quick Developer Bookmarks Section
        Text(
            text = "Developer Quick Bookmarks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        bookmarks.forEach { bookmark ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        inputUrl = bookmark.url
                        ChromeTabHelper.openUrl(context, bookmark.url)
                    }
                    .testTag("bookmark_${bookmark.title.lowercase().replace(" ", "_")}"),
                colors = CardDefaults.cardColors(containerColor = GitSurface),
                border = BorderStroke(1.dp, GitBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bookmark.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = bookmark.icon,
                            contentDescription = null,
                            tint = bookmark.color,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bookmark.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = bookmark.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = bookmark.url,
                            fontSize = 11.sp,
                            color = GitBlue,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Osun Hive Official Channels
        Text(
            text = "Osun Hive Official Channels",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("osunhive_channels_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Telegram
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { ChromeTabHelper.openUrl(context, "https://t.me/Osunhive") }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = GitBlue.copy(alpha = 0.2f)) {
                        Text(
                            text = "TG",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GitBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Telegram Community", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("t.me/Osunhive", fontSize = 11.sp, color = GitBlue)
                    }
                    Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = GitBlue, modifier = Modifier.size(16.dp))
                }

                // YouTube
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { ChromeTabHelper.openUrl(context, "https://youtube.com/@osunhivetech") }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = GitRed.copy(alpha = 0.2f)) {
                        Text(
                            text = "YT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GitRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("YouTube: osunhivetech", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("youtube.com/@osunhivetech", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = GitRed, modifier = Modifier.size(16.dp))
                }

                // Instagram
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { ChromeTabHelper.openUrl(context, "https://instagram.com/osunhive.olajidesherif") }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = Color(0xFFE1306C).copy(alpha = 0.2f)) {
                        Text(
                            text = "IG",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFE1306C),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Instagram: osunhive.olajidesherif", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("instagram.com/osunhive.olajidesherif", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = Color(0xFFE1306C), modifier = Modifier.size(16.dp))
                }

                // TikTok
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { ChromeTabHelper.openUrl(context, "https://www.tiktok.com/@osunhive.name.ng") }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = Color(0xFF00F2FE).copy(alpha = 0.2f)) {
                        Text(
                            text = "TT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF00F2FE),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TikTok: osunhive.name.ng", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("tiktok.com/@osunhive.name.ng", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = Color(0xFF00F2FE), modifier = Modifier.size(16.dp))
                }
            }
        }

        // Sponsored Website View Dialog (Reward & Interstitial Ad with Timer & AdSense)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sponsored_ad_view_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = BorderStroke(1.dp, HiveGold.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = HiveGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sponsored Website View (AdSense)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Visit https://www.osunhive.name.ng with countdown timer to support the developer and unlock speed rewards.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.showRewardedAd(
                                url = "https://www.osunhive.name.ng",
                                title = "Osun Hive Rewarded Ad"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HiveGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reward Ad (Timer)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.showInterstitialAd(
                                url = "https://www.osunhive.name.ng",
                                title = "Osun Hive Interstitial Ad"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GitBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Interstitial Ad", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Technology Card Info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            colors = CardDefaults.cardColors(containerColor = GitSurfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = HiveGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Powered by Osunhive Technology",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Osunhive Technology provides high-speed, secure in-app browsing with fast rendering, saved credentials, and smooth web integration without leaving the app.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
