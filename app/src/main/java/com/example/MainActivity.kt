package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OsunHiveStickyBanner
import com.example.ui.components.OsunHiveWebsiteAdDialog
import com.example.ui.screens.ChromeTabScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.RepositoriesScreen
import com.example.ui.screens.RssFeedScreen
import com.example.ui.screens.UploaderScreen
import com.example.ui.theme.GitBackground
import com.example.ui.theme.GitBlue
import com.example.ui.theme.GitGreen
import com.example.ui.theme.GitGreenBright
import com.example.ui.theme.GitSurface
import com.example.ui.theme.HiveGold
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize background RSS synchronization if enabled
        if (viewModel.appPreferences.isRssNotificationEnabled()) {
            com.example.data.rss.RssBackgroundReceiver.schedulePeriodicSync(this)
        }

        setContent {
            MyApplicationTheme {
                GitMobileApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GitMobileApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val uiNotification by viewModel.uiNotification.collectAsState()
    val websiteAdState by viewModel.websiteAdState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back button: return to UPLOAD tab if on another tab
    BackHandler(enabled = currentTab != AppTab.UPLOAD) {
        viewModel.setTab(AppTab.UPLOAD)
    }

    // Automatically pop up the website view dialog on startup
    LaunchedEffect(Unit) {
        viewModel.showInterstitialAd(
            url = "https://www.osunhive.name.ng",
            title = "Osun Hive • Official Website"
        )
    }

    LaunchedEffect(uiNotification) {
        uiNotification?.let { notif ->
            snackbarHostState.showSnackbar(notif.message)
            viewModel.dismissNotification()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = GitBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Osun Hive Sticky Monetization Banner
                OsunHiveStickyBanner(
                    adManager = viewModel.adManager
                )

                // Navigation Bar
                NavigationBar(
                    containerColor = GitSurface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.UPLOAD,
                        onClick = { viewModel.setTab(AppTab.UPLOAD) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.UPLOAD) Icons.Filled.CloudUpload else Icons.Outlined.CloudUpload,
                                contentDescription = "Upload"
                            )
                        },
                        label = { Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GitGreen,
                            selectedTextColor = GitGreen,
                            indicatorColor = GitGreen.copy(alpha = 0.2f),
                            unselectedIconColor = Color(0xFF8B949E),
                            unselectedTextColor = Color(0xFF8B949E)
                        ),
                        modifier = Modifier.testTag("nav_item_upload")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.REPOSITORIES,
                        onClick = { viewModel.setTab(AppTab.REPOSITORIES) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.REPOSITORIES) Icons.Filled.Book else Icons.Outlined.Book,
                                contentDescription = "Repositories"
                            )
                        },
                        label = { Text("Repos", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GitBlue,
                            selectedTextColor = GitBlue,
                            indicatorColor = GitBlue.copy(alpha = 0.2f),
                            unselectedIconColor = Color(0xFF8B949E),
                            unselectedTextColor = Color(0xFF8B949E)
                        ),
                        modifier = Modifier.testTag("nav_item_repos")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.RSS_FEED,
                        onClick = { viewModel.setTab(AppTab.RSS_FEED) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.RSS_FEED) Icons.Filled.RssFeed else Icons.Outlined.RssFeed,
                                contentDescription = "Hive News"
                            )
                        },
                        label = { Text("News", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GitGreenBright,
                            selectedTextColor = GitGreenBright,
                            indicatorColor = GitGreenBright.copy(alpha = 0.2f),
                            unselectedIconColor = Color(0xFF8B949E),
                            unselectedTextColor = Color(0xFF8B949E)
                        ),
                        modifier = Modifier.testTag("nav_item_rss")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.CHROME_TAB,
                        onClick = { viewModel.setTab(AppTab.CHROME_TAB) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.CHROME_TAB) Icons.Filled.Language else Icons.Outlined.Language,
                                contentDescription = "Osunhive Web"
                            )
                        },
                        label = { Text("Osunhive", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HiveGold,
                            selectedTextColor = HiveGold,
                            indicatorColor = HiveGold.copy(alpha = 0.2f),
                            unselectedIconColor = Color(0xFF8B949E),
                            unselectedTextColor = Color(0xFF8B949E)
                        ),
                        modifier = Modifier.testTag("nav_item_chrome")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.HISTORY,
                        onClick = { viewModel.setTab(AppTab.HISTORY) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = "History"
                            )
                        },
                        label = { Text("History", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GitBlue,
                            selectedTextColor = GitBlue,
                            indicatorColor = GitBlue.copy(alpha = 0.2f),
                            unselectedIconColor = Color(0xFF8B949E),
                            unselectedTextColor = Color(0xFF8B949E)
                        ),
                        modifier = Modifier.testTag("nav_item_history")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    AppTab.UPLOAD -> UploaderScreen(viewModel = viewModel)
                    AppTab.REPOSITORIES -> RepositoriesScreen(viewModel = viewModel)
                    AppTab.CHROME_TAB -> ChromeTabScreen(viewModel = viewModel)
                    AppTab.RSS_FEED -> RssFeedScreen(viewModel = viewModel)
                    AppTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                }
            }

            // Global Website View Dialog (Interstitial & Rewarded ads with timer & AdSense)
            val remoteConfig by viewModel.remoteConfig.collectAsState()
            OsunHiveWebsiteAdDialog(
                adState = websiteAdState,
                onDismiss = { viewModel.dismissWebsiteAd() },
                remoteSocialLinks = remoteConfig.socialLinks
            )
        }
    }
}
