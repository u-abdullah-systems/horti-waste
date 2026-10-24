package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailySummary
import com.example.ui.theme.MixedRed
import com.example.ui.theme.MixedRedBg
import com.example.ui.theme.SegregatedGreen
import com.example.ui.theme.SegregatedGreenBg
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun StackedHistoryBarChart(
    dailySummaries: List<DailySummary>,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (dailySummaries.isEmpty()) return

    // Find maximum total houses among the days to scale bar heights
    val maxTotal = remember(dailySummaries) {
        val max = dailySummaries.maxOfOrNull { it.totalHouses } ?: 1
        if (max > 0) max else 1
    }

    // Date formatting helper
    val inputFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val outputFormat = remember { SimpleDateFormat("dd MMM", Locale.US) }
    val dayOfWeekFormat = remember { SimpleDateFormat("EEE", Locale.US) }

    val scrollState = rememberScrollState()

    // Automatically scroll to selected date on load
    LaunchedEffect(dailySummaries.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    // Active tooltip item (defaults to selectedDate)
    val activeSummary = remember(selectedDate, dailySummaries) {
        dailySummaries.find { it.dateString == selectedDate } ?: dailySummaries.lastOrNull()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .testTag("stacked_history_barchart"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header with title and Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Daily Segregation Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap a bar to see numbers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Legend (Stacked: Top = Mixed Red, Bottom = Segregated Green)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(SegregatedGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Segregated",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MixedRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mixed",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // POPUP BUBBLE TOOLTIP CARD (Spacious 2-row layout, NEVER squishes or wraps vertically)
            activeSummary?.let { summary ->
                val formattedDate = try {
                    val parsed = inputFormat.parse(summary.dateString)
                    parsed?.let { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US).format(it) } ?: summary.dateString
                } catch (_: Exception) {
                    summary.dateString
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("chart_tooltip_bubble")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${summary.segregationPercentage}% Segregated",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (summary.segregationPercentage >= 70) SegregatedGreen else MixedRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Segregated Pill (clean full width share, never squished)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SegregatedGreenBg,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SegregatedGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${summary.segregatedCount} Segregated",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Mixed Trash Pill (clean full width share, never squished)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MixedRedBg,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MixedRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${summary.mixedCount} Mixed",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB91C1C),
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BAR CHART AREA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Subtle horizontal grid lines
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                }

                // Scrollable row of capsule stacked bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailySummaries.forEach { summary ->
                        val isSelected = summary.dateString == selectedDate

                        val dayText = try {
                            val parsed = inputFormat.parse(summary.dateString)
                            parsed?.let { outputFormat.format(it) } ?: summary.dateString
                        } catch (_: Exception) {
                            summary.dateString
                        }

                        val dayOfWeek = try {
                            val parsed = inputFormat.parse(summary.dateString)
                            parsed?.let { dayOfWeekFormat.format(it) } ?: ""
                        } catch (_: Exception) {
                            ""
                        }

                        StackedCapsuleBarItem(
                            summary = summary,
                            maxTotal = maxTotal,
                            isSelected = isSelected,
                            dayLabel = dayText,
                            dayOfWeek = dayOfWeek,
                            onClick = { onSelectDate(summary.dateString) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual vertical capsule stacked bar:
 * - Rounded top and bottom like the user's reference image
 * - Mixed waste stacked on top (Red)
 * - Segregated waste stacked on bottom (Green)
 * - Compact popup bubble indicator right above the bar when selected
 */
@Composable
private fun StackedCapsuleBarItem(
    summary: DailySummary,
    maxTotal: Int,
    isSelected: Boolean,
    dayLabel: String,
    dayOfWeek: String,
    onClick: () -> Unit
) {
    val total = summary.totalHouses
    val chartMaxHeight = 110.dp
    val minBarHeight = 28.dp

    // Proportional height
    val targetHeight = if (maxTotal > 0 && total > 0) {
        val fraction = total.toFloat() / maxTotal.toFloat()
        minBarHeight + (chartMaxHeight - minBarHeight) * fraction
    } else {
        minBarHeight
    }

    val animatedHeight by animateFloatAsState(
        targetValue = targetHeight.value,
        animationSpec = spring(),
        label = "bar_height"
    )

    val segregatedFraction = if (total > 0) summary.segregatedCount.toFloat() / total.toFloat() else 1f
    val mixedFraction = if (total > 0) summary.mixedCount.toFloat() / total.toFloat() else 0f

    val barScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "bar_scale"
    )

    val haptic = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 4.dp)
            .testTag("bar_${summary.dateString}")
    ) {
        // Floating mini bubble directly over the active bar with smooth entrance
        Box(
            modifier = Modifier.height(22.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0F172A),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "${summary.segregatedCount}✓",
                            color = Color(0xFF4ADE80),
                            fontWeight = FontWeight.Bold,
                            softWrap = false,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${summary.mixedCount}✕",
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.Bold,
                            softWrap = false,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Capsule / Pill Bar - Clean look with subtle bouncy scale on selection
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(animatedHeight.dp)
                .graphicsLayer {
                    scaleX = barScale
                    scaleY = barScale
                }
                .clip(RoundedCornerShape(percent = 50)) // Pill capsule shape
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(percent = 50)
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                // Top portion: Mixed Trash (Red)
                if (mixedFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (mixedFraction > 0.05f) mixedFraction else 0.05f)
                            .background(MixedRed)
                    )
                }

                // Bottom portion: Segregated (Green)
                if (segregatedFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (segregatedFraction > 0.05f) segregatedFraction else 0.05f)
                            .background(SegregatedGreen)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Day of week (e.g., "Mon")
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 10.sp
        )

        // Date (e.g., "24 Oct")
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp
        )

        // Subtle indicator dot under selected day
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(12.dp)
                .padding(top = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
