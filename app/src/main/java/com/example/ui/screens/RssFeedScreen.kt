package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.components.OsunHiveRssFeedComponent
import com.example.ui.theme.GitBlue
import com.example.ui.theme.GitBorder
import com.example.ui.theme.GitGreen
import com.example.ui.theme.GitSurface
import com.example.ui.theme.HiveAmber
import com.example.ui.theme.HiveGold

@Composable
fun RssFeedScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val feedState by viewModel.rssFeedState.collectAsState()
    val isBackgroundSyncEnabled by viewModel.isRssNotificationEnabled.collectAsState()
    val currentLoadingStyle by viewModel.m3LoadingStyle.collectAsState()

    var hasNotificationPermission by remember {
        mutableStateOf(viewModel.notificationHelper.hasNotificationPermission())
    }

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            viewModel.setRssNotificationEnabled(true, context)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Action Bar for Live Notifications & Monetization
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.triggerTestNotification()
                        }
                    },
                    modifier = Modifier.testTag("rss_notification_test_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Trigger Post Alert Notification",
                        tint = HiveGold
                    )
                }
                Text(
                    text = "Test Post Alert",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = HiveAmber
                )
            }

            Button(
                onClick = {
                    viewModel.showRewardedAd(
                        url = "https://www.osunhive.name.ng",
                        title = "Osun Hive Rewarded Ad"
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = HiveGold, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reward Ad", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Notification Permission Banner (if not granted on Android 13+)
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261D0C)),
                border = BorderStroke(1.dp, HiveAmber),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = HiveGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enable notifications to receive background Osun Hive post alerts",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        colors = ButtonDefaults.buttonColors(containerColor = HiveGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Background RSS & Post Image Control Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("background_rss_control_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isBackgroundSyncEnabled) GitGreen else Color(0xFF8B949E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Background RSS Sync",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBackgroundSyncEnabled) "Active • Checks every 30m" else "Disabled",
                                fontSize = 11.sp,
                                color = if (isBackgroundSyncEnabled) GitGreen else Color(0xFF8B949E)
                            )
                        }
                    }

                    Switch(
                        checked = isBackgroundSyncEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setRssNotificationEnabled(isChecked, context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = HiveGold,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = GitSurface
                        ),
                        modifier = Modifier.testTag("background_rss_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Post Image Badge & Sync Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HiveGold.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, HiveGold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = HiveGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Post Image RSS Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HiveGold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.syncRssInBackgroundNow(context) },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = GitBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 10.sp, color = GitBlue)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dedicated Osun Hive RSS Feed Component with loading, error, and scrollable list states
        OsunHiveRssFeedComponent(
            feedState = feedState,
            onRefresh = { viewModel.refreshRssFeed() },
            onToggleBookmark = { viewModel.toggleBookmark(it) },
            modifier = Modifier.weight(1f),
            loadingStyle = currentLoadingStyle
        )
    }
}
