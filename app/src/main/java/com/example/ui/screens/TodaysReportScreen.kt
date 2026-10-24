package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ColonyOverview
import com.example.data.model.HouseTypeStat
import com.example.ui.components.MetricStatCard
import com.example.ui.components.ProgressBarWithLabels
import com.example.ui.theme.MixedRed
import com.example.ui.theme.MixedRedBg
import com.example.ui.theme.SegregatedGreen
import com.example.ui.theme.SegregatedGreenBg
import com.example.ui.viewmodel.CollectionViewModel

@Composable
fun TodaysReportScreen(
    viewModel: CollectionViewModel,
    isDarkTheme: Boolean,
    onNavigateToLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overview by viewModel.todayOverview.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val mixedHouses = overview.records.filter { !it.isSegregated }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Title & Share Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Today's Collection Report",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${overview.dateString} • Fatima Fertilizer Colony",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        shareReport(context, overview)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("share_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Report",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            // High-Level Stat Cards Grid - Clean White Theme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricStatCard(
                    title = "VISITED",
                    value = "${overview.totalHouses}",
                    subtitle = "Houses today",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = "SEGREGATED",
                    value = "${overview.segregatedCount}",
                    subtitle = "${overview.segregationPercentage}% clean",
                    containerColor = SegregatedGreenBg,
                    contentColor = Color(0xFF15803D),
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = "MIXED TRASH",
                    value = "${overview.mixedCount}",
                    subtitle = "Unsegregated",
                    containerColor = MixedRedBg,
                    contentColor = Color(0xFFB91C1C),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Overall Compliance Progress Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    ProgressBarWithLabels(
                        label = "Colony Overall Segregation Rate",
                        percentage = overview.segregationPercentage,
                        segregatedCount = overview.segregatedCount,
                        mixedCount = overview.mixedCount,
                        totalCount = overview.totalHouses
                    )
                }
            }
        }

        item {
            // Sector / House Type Breakdown
            Text(
                text = "Breakdown by Sector / House Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        val activeTypeStats = overview.typeStats.filter { it.totalHouses > 0 }
        if (activeTypeStats.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No houses logged yet today. Tap below to start logging!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(activeTypeStats, key = { it.houseType }) { stat ->
                TypeStatCard(stat = stat, modifier = Modifier.animateItem())
            }
        }

        // Show remaining house types with 0 count in compact pills
        val emptyTypes = overview.typeStats.filter { it.totalHouses == 0 }
        if (emptyTypes.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "Pending Sectors (Not visited yet today):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        emptyTypes.forEach { typeStat ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "Type ${typeStat.houseType}: 0",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Non-compliant / Mixed waste houses section
        if (mixedHouses.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MixedRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Houses with Mixed Waste Today (${mixedHouses.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MixedRed
                    )
                }
                Text(
                    text = "Requires green segregation bags / awareness follow-up:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                )
            }

            items(mixedHouses, key = { "mixed_${it.id}" }) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .border(1.dp, MixedRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MixedRedBg.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = record.fullAddress,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            if (record.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${record.notes})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "Mixed Waste",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MixedRed
                        )
                    }
                }
            }
        }

        item {
            // Action to go back to logging
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNavigateToLog()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("collect_more_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOG MORE HOUSES", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TypeStatCard(stat: HouseTypeStat, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            ProgressBarWithLabels(
                label = "House Type ${stat.houseType}",
                percentage = stat.compliancePercentage,
                segregatedCount = stat.segregatedCount,
                mixedCount = stat.mixedCount,
                totalCount = stat.totalHouses
            )
        }
    }
}

private fun shareReport(context: Context, overview: ColonyOverview) {
    val builder = StringBuilder()
    builder.appendLine("🌿 Fatima Fertilizer Colony - Horticulture Waste Report")
    builder.appendLine("📅 Date: ${overview.dateString}")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("📊 Total Houses Visited: ${overview.totalHouses}")
    builder.appendLine("✅ Segregated (Organic): ${overview.segregatedCount} (${overview.segregationPercentage}%)")
    builder.appendLine("❌ Mixed Trash: ${overview.mixedCount} (${100 - overview.segregationPercentage}%)")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("Sector Breakdown:")
    overview.typeStats.filter { it.totalHouses > 0 }.forEach { stat ->
        builder.appendLine("• Type ${stat.houseType}: ${stat.segregatedCount} Segregated / ${stat.mixedCount} Mixed (${stat.compliancePercentage}%)")
    }

    val mixedList = overview.records.filter { !it.isSegregated }
    if (mixedList.isNotEmpty()) {
        builder.appendLine("━━━━━━━━━━━━━━━━━━━━━")
        builder.appendLine("Mixed Trash Houses:")
        builder.appendLine(mixedList.joinToString(", ") { it.fullAddress })
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, builder.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Horticulture Report")
    context.startActivity(shareIntent)
}
