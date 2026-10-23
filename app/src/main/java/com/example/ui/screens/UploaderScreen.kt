package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.data.github.UploadPhase
import com.example.ui.components.FolderPreviewTree
import com.example.ui.components.OsunHiveNativeSponsoredCard
import com.example.ui.theme.GitBlue
import com.example.ui.theme.GitBorder
import com.example.ui.theme.GitGreen
import com.example.ui.theme.GitGreenBright
import com.example.ui.theme.GitRed
import com.example.ui.theme.GitSurface
import com.example.ui.theme.GitSurfaceVariant
import com.example.ui.theme.HiveGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploaderScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val token by viewModel.githubToken.collectAsState()
    val username by viewModel.githubUsername.collectAsState()
    val authUser by viewModel.authenticatedUser.collectAsState()
    val isVerifying by viewModel.isVerifyingToken.collectAsState()

    val selectedRepo by viewModel.selectedRepo.collectAsState()
    val selectedBranch by viewModel.selectedBranch.collectAsState()
    val targetFolderPath by viewModel.targetFolderPath.collectAsState()
    val commitMessage by viewModel.commitMessage.collectAsState()

    val folderName by viewModel.folderName.collectAsState()
    val folderFiles by viewModel.folderFiles.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val lastSuccess by viewModel.lastUploadSuccess.collectAsState()

    var showTokenPassword by remember { mutableStateOf(false) }

    // Android Storage Access Framework (SAF) folder launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.onFolderSelected(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "GitMobile Uploader",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Fast mobile folder commits to GitHub via Git Trees API",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GitBlue.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GitBlue.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "Git Trees API",
                    fontSize = 11.sp,
                    color = GitBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Section 1: GitHub Authentication Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("github_auth_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = GitBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GitHub Credentials",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (authUser != null) {
                        Surface(
                            shape = CircleShape,
                            color = GitGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GitGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GitGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Connected",
                                    fontSize = 11.sp,
                                    color = GitGreenBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GitHub Token Field
                OutlinedTextField(
                    value = token,
                    onValueChange = { viewModel.updateGitHubToken(it) },
                    label = { Text("GitHub Personal Access Token (PAT) *") },
                    placeholder = { Text("ghp_... or github_pat_...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("github_token_input"),
                    singleLine = true,
                    visualTransformation = if (showTokenPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showTokenPassword = !showTokenPassword }) {
                            Icon(
                                imageVector = if (showTokenPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle token visibility"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitBlue,
                        unfocusedBorderColor = GitBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // GitHub Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.updateGitHubUsername(it) },
                    label = { Text("GitHub Username / Owner *") },
                    placeholder = { Text("e.g. torvalds or your-username") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("github_username_input"),
                    singleLine = true,
                    leadingIcon = {
                        if (authUser?.avatarUrl != null) {
                            AsyncImage(
                                model = authUser?.avatarUrl,
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitBlue,
                        unfocusedBorderColor = GitBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { viewModel.verifyToken() },
                        enabled = !isVerifying && token.isNotBlank(),
                        modifier = Modifier.testTag("verify_token_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GitBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verifying...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verify & Connect")
                        }
                    }
                }
            }
        }

        // Section 2: Repository and Target Folder Path
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("target_repo_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = null,
                        tint = HiveGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Destination Repository & Folder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Target Repository
                    OutlinedTextField(
                        value = selectedRepo,
                        onValueChange = { viewModel.updateSelectedRepo(it) },
                        label = { Text("Repository *") },
                        placeholder = { Text("e.g. awesome-project") },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("repo_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GitBlue,
                            unfocusedBorderColor = GitBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Target Branch
                    OutlinedTextField(
                        value = selectedBranch,
                        onValueChange = { viewModel.updateSelectedBranch(it) },
                        label = { Text("Branch") },
                        placeholder = { Text("main") },
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("branch_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GitBlue,
                            unfocusedBorderColor = GitBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Folder Path in Repository
                OutlinedTextField(
                    value = targetFolderPath,
                    onValueChange = { viewModel.updateTargetFolderPath(it) },
                    label = { Text("Folder Path in Repo *") },
                    placeholder = { Text("e.g. src/assets or docs or / for root") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_folder_path_input"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = HiveGold,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitBlue,
                        unfocusedBorderColor = GitBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Commit Message
                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { viewModel.updateCommitMessage(it) },
                    label = { Text("Commit Message") },
                    placeholder = { Text("e.g. Upload assets from GitMobile") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("commit_message_input"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Commit,
                            contentDescription = null,
                            tint = GitGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitBlue,
                        unfocusedBorderColor = GitBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Section 3: Folder Selection & Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("folder_selection_card"),
            colors = CardDefaults.cardColors(containerColor = GitSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GitBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = GitBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Folder Selection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Button to open Android Storage Access Framework folder picker
                    Button(
                        onClick = { folderPickerLauncher.launch(null) },
                        modifier = Modifier.testTag("select_device_folder_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GitSurfaceVariant,
                            contentColor = GitBlue
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GitBlue.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Device Folder", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Presets chips for quick testing
                Text(
                    text = "Or quick-load sample project folders:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = folderName == "my-web-assets",
                        onClick = { viewModel.loadPresetDemoFolder("WebStarter") },
                        label = { Text("🌐 Web Starter (4 files)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GitBlue.copy(alpha = 0.2f),
                            selectedLabelColor = GitBlue
                        )
                    )

                    FilterChip(
                        selected = folderName == "documentation",
                        onClick = { viewModel.loadPresetDemoFolder("DocsFolder") },
                        label = { Text("📄 Docs Folder (3 files)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GitGreen.copy(alpha = 0.2f),
                            selectedLabelColor = GitGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Folder Preview
                if (folderFiles.isNotEmpty()) {
                    FolderPreviewTree(
                        folderName = folderName,
                        files = folderFiles
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = GitSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GitBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No folder selected yet",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap 'Pick Device Folder' to choose any directory from storage, or tap a preset above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Upload Progress (Visible when uploading or failed)
        val isUploading = uploadProgress.phase != UploadPhase.IDLE && uploadProgress.phase != UploadPhase.COMPLETED
        AnimatedVisibility(visible = isUploading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_progress_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (uploadProgress.phase == UploadPhase.FAILED) GitRed.copy(alpha = 0.1f) else GitSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (uploadProgress.phase == UploadPhase.FAILED) GitRed else GitBlue
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when (uploadProgress.phase) {
                                UploadPhase.SCANNING_FOLDER -> "Scanning Directory Tree"
                                UploadPhase.UPLOADING_BLOBS -> "Uploading Git Blobs (${uploadProgress.currentFile}/${uploadProgress.totalFiles})"
                                UploadPhase.GENERATING_TREE -> "Building Git Tree"
                                UploadPhase.CREATING_COMMIT -> "Writing Git Commit"
                                UploadPhase.UPDATING_BRANCH -> "Updating Branch Ref"
                                UploadPhase.FAILED -> "Upload Failed"
                                else -> "Processing..."
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (uploadProgress.phase == UploadPhase.FAILED) GitRed else MaterialTheme.colorScheme.onSurface
                        )

                        if (uploadProgress.phase != UploadPhase.FAILED) {
                            Text(
                                text = "${(uploadProgress.percent * 100).toInt()}%",
                                fontWeight = FontWeight.Bold,
                                color = GitBlue,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uploadProgress.phase != UploadPhase.FAILED) {
                        LinearProgressIndicator(
                            progress = { uploadProgress.percent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = GitGreenBright,
                            trackColor = GitSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uploadProgress.statusText,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Upload Action Button
        Button(
            onClick = { viewModel.startFolderUpload() },
            enabled = !isUploading && folderFiles.isNotEmpty() && token.isNotBlank() && username.isNotBlank() && selectedRepo.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("git_upload_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = GitGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Commit & Upload Folder to GitHub",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        // Section 5: Osun Hive Native Sponsored Card
        OsunHiveNativeSponsoredCard(
            adManager = viewModel.adManager,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    // Success Commit Dialog
    if (lastSuccess != null) {
        val success = lastSuccess!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissLastUploadSuccess() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GitGreenBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Commit Successful!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Successfully uploaded ${success.fileCount} files to ${success.repo} at path '${success.folderPath}'.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GitSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GitBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Commit SHA:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = success.commitSha,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GitBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Branch: ${success.branch}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(success.url)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GitBlue,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Commit on GitHub")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLastUploadSuccess() }) {
                    Text("Done")
                }
            }
        )
    }
}
