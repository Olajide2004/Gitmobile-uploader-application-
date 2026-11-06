package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Material 3 Loading Styles:
 * - CIRCULAR_EXPRESSIVE: M3 circular spinner with active momentum sweep and pulsing ambient track
 * - LINEAR_WAVE: M3 indeterminate wave progress bar with gradient shimmer
 * - SKELETON_SHIMMER: M3 container shimmer placeholder effect
 */
@Composable
fun M3LoadingView(
    style: String = "CIRCULAR_EXPRESSIVE",
    message: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.testTag("m3_loading_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (style.uppercase()) {
            "LINEAR_WAVE" -> {
                M3LinearWaveIndicator(modifier = Modifier.fillMaxWidth(0.85f))
            }
            "SKELETON_SHIMMER" -> {
                M3SkeletonCardShimmer(modifier = Modifier.fillMaxWidth(0.9f))
            }
            else -> {
                // Default: CIRCULAR_EXPRESSIVE
                M3ExpressiveCircularIndicator(size = 48.dp)
            }
        }

        if (!message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Material 3 Expressive Circular Indicator with dual-track motion and pulsing glow.
 */
@Composable
fun M3ExpressiveCircularIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    strokeWidth: Dp = 4.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_circular_spin")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweepAngle"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("m3_expressive_circular_indicator"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Background Track
            drawCircle(
                color = trackColor,
                radius = (this.size.minDimension - strokeWidth.toPx()) / 2f,
                style = Stroke(width = strokeWidth.toPx())
            )

            // Animated Active Arc
            drawArc(
                color = primaryColor,
                startAngle = rotation,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Material 3 Linear Wave Progress Indicator with gradient wave animation.
 */
@Composable
fun M3LinearWaveIndicator(
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_linear_wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
            .testTag("m3_linear_wave_indicator")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val barWidth = size.width * 0.45f
            val startX = (size.width * waveOffset) - (barWidth / 2f)

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.2f),
                        color,
                        color.copy(alpha = 0.2f)
                    ),
                    startX = startX,
                    endX = startX + barWidth
                ),
                topLeft = Offset(x = startX.coerceAtLeast(0f), y = 0f),
                size = androidx.compose.ui.geometry.Size(
                    width = barWidth.coerceAtMost(size.width),
                    height = size.height
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height.toPx() / 2)
            )
        }
    }
}

/**
 * Material 3 Skeleton Shimmer Card with animated gradient transition.
 */
@Composable
fun M3SkeletonCardShimmer(
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    highlightColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
) {
    val transition = rememberInfiniteTransition(label = "m3_skeleton_shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
            .testTag("m3_skeleton_shimmer_card")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(shimmerBrush)
        )
    }
}
