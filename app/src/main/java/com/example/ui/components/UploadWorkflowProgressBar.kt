package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.github.UploadPhase
import com.example.data.github.UploadProgress
import com.example.ui.theme.GitBlue
import com.example.ui.theme.GitBorder
import com.example.ui.theme.GitGreen
import com.example.ui.theme.GitGreenBright
import com.example.ui.theme.GitRed
import com.example.ui.theme.GitSurface
import com.example.ui.theme.GitSurfaceVariant
import com.example.ui.theme.HiveGold

/**
 * Dedicated progress bar component visualizing the complete folder upload workflow:
 * file scanning, Git blob uploads, tree generation, and commit creation.
 */
@Composable
fun UploadWorkflowProgressBar(
    progress: UploadProgress,
    modifier: Modifier = Modifier
) {
    val isFailed = progress.phase == UploadPhase.FAILED
    val isCompleted = progress.phase == UploadPhase.COMPLETED
    val isRunning = progress.phase != UploadPhase.IDLE && !isCompleted && !isFailed

    val animatedPercent by animateFloatAsState(
        targetValue = when (progress.phase) {
            UploadPhase.IDLE -> 0f
            UploadPhase.SCANNING_FOLDER -> 0.15f
            UploadPhase.UPLOADING_BLOBS -> 0.15f + (progress.percent * 0.55f)
            UploadPhase.GENERATING_TREE -> 0.78f
            UploadPhase.CREATING_COMMIT -> 0.88f
            UploadPhase.UPDATING_BRANCH -> 0.95f
            UploadPhase.COMPLETED -> 1.0f
            UploadPhase.FAILED -> progress.percent
        },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "WorkflowProgressAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFailed -> GitRed
            isCompleted -> GitGreenBright
            isRunning -> GitBlue
            else -> GitBorder
        },
        label = "BorderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("upload_workflow_progress_bar"),
        colors = CardDefaults.cardColors(
            containerColor = if (isFailed) GitRed.copy(alpha = 0.08f) else GitSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Phase Name & Progress Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isFailed -> GitRed.copy(alpha = 0.2f)
                                    isCompleted -> GitGreen.copy(alpha = 0.2f)
                                    isRunning -> GitBlue.copy(alpha = 0.2f)
                                    else -> GitSurfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isFailed -> Icons.Default.Error
                                isCompleted -> Icons.Default.CheckCircle
                                progress.phase == UploadPhase.UPLOADING_BLOBS -> Icons.Default.CloudUpload
                                progress.phase == UploadPhase.GENERATING_TREE -> Icons.Default.AccountTree
                                progress.phase == UploadPhase.CREATING_COMMIT -> Icons.Default.Commit
                                else -> Icons.Default.Folder
                            },
                            contentDescription = null,
                            tint = when {
                                isFailed -> GitRed
                                isCompleted -> GitGreenBright
                                isRunning -> GitBlue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = getPhaseTitle(progress.phase),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isFailed) GitRed else MaterialTheme.colorScheme.onSurface
                        )
                        if (isRunning && progress.totalFiles > 0) {
                            Text(
                                text = "File ${progress.currentFile} of ${progress.totalFiles}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Percent Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isFailed -> GitRed.copy(alpha = 0.15f)
                        isCompleted -> GitGreen.copy(alpha = 0.15f)
                        else -> GitBlue.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = "${(animatedPercent * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isFailed -> GitRed
                            isCompleted -> GitGreenBright
                            else -> GitBlue
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Progress Bar Track
            LinearProgressIndicator(
                progress = { animatedPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("upload_linear_progress_indicator"),
                color = when {
                    isFailed -> GitRed
                    isCompleted -> GitGreenBright
                    else -> GitGreenBright
                },
                trackColor = GitSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Current File Processing Tag (if active)
            if (isRunning && progress.fileName.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GitSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GitBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = GitBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Processing: ${progress.fileName}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Workflow Step Indicators (4 Milestones)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkflowStepBadge(
                    stepNumber = "1",
                    label = "Scan",
                    isCurrent = progress.phase == UploadPhase.SCANNING_FOLDER,
                    isDone = progress.phase.ordinal > UploadPhase.SCANNING_FOLDER.ordinal
                )

                WorkflowStepBadge(
                    stepNumber = "2",
                    label = "Blobs",
                    isCurrent = progress.phase == UploadPhase.UPLOADING_BLOBS,
                    isDone = progress.phase.ordinal > UploadPhase.UPLOADING_BLOBS.ordinal
                )

                WorkflowStepBadge(
                    stepNumber = "3",
                    label = "Tree",
                    isCurrent = progress.phase == UploadPhase.GENERATING_TREE,
                    isDone = progress.phase.ordinal > UploadPhase.GENERATING_TREE.ordinal
                )

                WorkflowStepBadge(
                    stepNumber = "4",
                    label = "Commit",
                    isCurrent = progress.phase == UploadPhase.CREATING_COMMIT || progress.phase == UploadPhase.UPDATING_BRANCH,
                    isDone = isCompleted
                )
            }

            // Detailed Status Message
            if (progress.statusText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = progress.statusText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isFailed) GitRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WorkflowStepBadge(
    stepNumber: String,
    label: String,
    isCurrent: Boolean,
    isDone: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> GitGreen
                        isCurrent -> GitBlue
                        else -> GitBorder
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            } else {
                Text(
                    text = stepNumber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) Color.White else Color(0xFF8B949E)
                )
            }
        }

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isDone -> GitGreenBright
                isCurrent -> GitBlue
                else -> Color(0xFF6E7681)
            }
        )
    }
}

private fun getPhaseTitle(phase: UploadPhase): String {
    return when (phase) {
        UploadPhase.IDLE -> "Upload Workflow Idle"
        UploadPhase.SCANNING_FOLDER -> "Scanning Directory Tree"
        UploadPhase.UPLOADING_BLOBS -> "Uploading File Blobs"
        UploadPhase.GENERATING_TREE -> "Building Git Tree Structure"
        UploadPhase.CREATING_COMMIT -> "Creating Git Commit"
        UploadPhase.UPDATING_BRANCH -> "Updating Branch Head"
        UploadPhase.COMPLETED -> "Upload & Commit Complete"
        UploadPhase.FAILED -> "Upload Failed"
    }
}
