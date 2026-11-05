package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.WebsiteAdMode
import com.example.WebsiteAdState
import com.example.ui.theme.GitBlue
import com.example.ui.theme.GitBorder
import com.example.ui.theme.GitGreen
import com.example.ui.theme.GitGreenBright
import com.example.ui.theme.GitRed
import com.example.ui.theme.GitSurface
import com.example.ui.theme.GitSurfaceVariant
import com.example.ui.theme.HiveGold
import com.example.util.ChromeTabHelper
import kotlinx.coroutines.delay

data class SocialCommunityChannel(
    val name: String,
    val url: String,
    val badgeColor: Color
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OsunHiveWebsiteAdDialog(
    adState: WebsiteAdState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!adState.isVisible) return

    val context = LocalContext.current
    var secondsRemaining by remember(adState.url, adState.mode) {
        mutableIntStateOf(adState.requiredDurationSeconds)
    }
    var isTimerComplete by remember(adState.url, adState.mode) {
        mutableStateOf(false)
    }
    var isLoadingPage by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val socialChannels = remember {
        listOf(
            SocialCommunityChannel("Telegram", "https://t.me/Osunhive", GitBlue),
            SocialCommunityChannel("YouTube", "https://youtube.com/@osunhivetech", GitRed),
            SocialCommunityChannel("Yo.fan", "https://yo.fan/osunhivetech", Color(0xFFFF5722)),
            SocialCommunityChannel("TikTok", "https://www.tiktok.com/@osunhive.name.ng", Color(0xFF00F2FE))
        )
    }

    // Countdown Timer logic
    LaunchedEffect(adState.isVisible, adState.mode) {
        secondsRemaining = adState.requiredDurationSeconds
        isTimerComplete = false
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
        isTimerComplete = true
    }

    Dialog(
        onDismissRequest = {
            if (isTimerComplete || adState.mode == WebsiteAdMode.INTERSTITIAL) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = isTimerComplete || adState.mode == WebsiteAdMode.INTERSTITIAL,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .testTag("osunhive_website_ad_dialog"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = BorderStroke(1.5.dp, if (adState.mode == WebsiteAdMode.REWARDED) HiveGold else GitBlue),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar - Material 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GitSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (adState.mode == WebsiteAdMode.REWARDED) HiveGold.copy(alpha = 0.2f) else GitBlue.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                imageVector = if (adState.mode == WebsiteAdMode.REWARDED) Icons.Default.Star else Icons.Default.Language,
                                contentDescription = null,
                                tint = if (adState.mode == WebsiteAdMode.REWARDED) HiveGold else GitBlue,
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(6.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (adState.mode == WebsiteAdMode.REWARDED) "Reward Ad" else "Website View",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = HiveGold.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "AdSense",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HiveGold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "www.osunhive.name.ng",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Timer & Action Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Countdown Timer Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isTimerComplete) GitGreen.copy(alpha = 0.2f) else GitBlue.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (isTimerComplete) GitGreenBright else GitBlue)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isTimerComplete) Icons.Default.CheckCircle else Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isTimerComplete) GitGreenBright else GitBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTimerComplete) "Done" else "${secondsRemaining}s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTimerComplete) GitGreenBright else GitBlue
                                )
                            }
                        }

                        // Open in Chrome Tab Button
                        IconButton(
                            onClick = { ChromeTabHelper.openUrl(context, adState.url) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Open in Chrome Tab",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Reload Button
                        IconButton(
                            onClick = { webViewInstance?.reload() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            enabled = isTimerComplete || adState.mode == WebsiteAdMode.INTERSTITIAL,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = if (isTimerComplete || adState.mode == WebsiteAdMode.INTERSTITIAL) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Developer Attribution Banner - Material 3 Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable {
                            openEmail(context, "osunhiveteam@gmail.com")
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = GitSurfaceVariant,
                    border = BorderStroke(1.dp, HiveGold.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(HiveGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Developer",
                                    tint = HiveGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Developer: Olajide Sherif Oyinlola",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = GitBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = HiveGold,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "osunhiveteam@gmail.com",
                                        fontSize = 10.sp,
                                        color = HiveGold
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HiveGold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, HiveGold.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Contact",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = HiveGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Page Loading Indicator
                if (isLoadingPage) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = if (adState.mode == WebsiteAdMode.REWARDED) HiveGold else GitBlue,
                        trackColor = GitSurfaceVariant
                    )
                }

                // Embedded WebView with AdSense support
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    userAgentString = "${settings.userAgentString} OsunHiveApp/1.0"
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoadingPage = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoadingPage = false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        isLoadingPage = false
                                    }
                                }
                                webViewInstance = this
                                loadUrl(adState.url)
                            }
                        },
                        update = { view ->
                            if (view.url != adState.url) {
                                view.loadUrl(adState.url)
                            }
                        }
                    )
                }

                // Bottom Reward Action & Community Channels - Material 3
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GitSurface)
                        .padding(12.dp)
                ) {
                    // Rewarded Button (when completed) or Status Bar
                    if (adState.mode == WebsiteAdMode.REWARDED) {
                        if (isTimerComplete) {
                            Button(
                                onClick = {
                                    adState.onRewardGranted?.invoke()
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("claim_reward_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HiveGold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Claim Reward & Unlock Fast Mode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = GitSurfaceVariant,
                                border = BorderStroke(1.dp, GitBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = HiveGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "View sponsored site for ${secondsRemaining}s to unlock reward",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        if (isTimerComplete) {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GitBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Continue to App", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Social Channels Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Connect with Osun Hive:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            socialChannels.forEach { channel ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = channel.badgeColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, channel.badgeColor.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            ChromeTabHelper.openUrl(context, channel.url)
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = channel.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = channel.badgeColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openEmail(context: Context, email: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "GitMobile Uploader / Osun Hive Inquiry")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Contact: $email", Toast.LENGTH_SHORT).show()
    }
}
