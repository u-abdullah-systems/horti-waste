package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CollectionRecord
import com.example.data.model.ColonyOverview
import com.example.ui.components.MetricStatCard
import com.example.ui.components.ProgressBarWithLabels
import com.example.ui.components.StackedHistoryBarChart
import com.example.ui.theme.MixedRed
import com.example.ui.theme.MixedRedBg
import com.example.ui.theme.SegregatedGreen
import com.example.ui.theme.SegregatedGreenBg
import com.example.ui.viewmodel.CollectionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: CollectionViewModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val allDates by viewModel.allRecordedDates.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedReportDate.collectAsStateWithLifecycle()
    val selectedOverview by viewModel.selectedDateOverview.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val allDailySummaries by viewModel.allDailySummaries.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(0) } // 0 = By Date, 1 = Search Colony House
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Previous Records & Search",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Fatima Fertilizer Colony Collection History",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .testTag("history_subtabs")
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        activeSubTab = 0
                    },
                    text = { Text("Daily Archives", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        activeSubTab = 1
                    },
                    text = { Text("Search Colony House", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (activeSubTab == 0) {
            // Interactive Stacked Capsule Bar Chart
            item {
                StackedHistoryBarChart(
                    dailySummaries = allDailySummaries,
                    selectedDate = selectedDate,
                    onSelectDate = { viewModel.selectReportDate(it) }
                )
            }

            // By Date Quick Chips (Ordered to match graph left-to-right)
            item {
                val sortedDates = remember(allDates) { allDates.sorted() }

                Text(
                    text = "SELECT DATE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sortedDates) { date ->
                        val isSelected = date == selectedDate
                        val isToday = date == viewModel.todayDateString

                        val chipBgColor by animateColorAsState(
                            targetValue = if (isSelected) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface,
                            animationSpec = tween(150),
                            label = "chip_bg"
                        )
                        val chipBorderColor by animateColorAsState(
                            targetValue = if (isSelected) Color(0xFF1E293B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            animationSpec = tween(150),
                            label = "chip_border"
                        )
                        val chipContentColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            animationSpec = tween(150),
                            label = "chip_content"
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = chipBgColor,
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    chipBorderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.selectReportDate(date)
                                }
                                .testTag("date_chip_$date")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isToday) "Today ($date)" else date,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = chipContentColor
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Summary card for selected past date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Report for $selectedDate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .clickable { sharePastReport(context, selectedOverview) }
                            .padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Share",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricStatCard(
                        title = "TOTAL",
                        value = "${selectedOverview.totalHouses}",
                        subtitle = "Houses",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatCard(
                        title = "SEGREGATED",
                        value = "${selectedOverview.segregatedCount}",
                        subtitle = "${selectedOverview.segregationPercentage}%",
                        containerColor = SegregatedGreenBg,
                        contentColor = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatCard(
                        title = "MIXED",
                        value = "${selectedOverview.mixedCount}",
                        subtitle = "Trash",
                        containerColor = MixedRedBg,
                        contentColor = Color(0xFFB91C1C),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        ProgressBarWithLabels(
                            label = "Segregation Compliance",
                            percentage = selectedOverview.segregationPercentage,
                            segregatedCount = selectedOverview.segregatedCount,
                            mixedCount = selectedOverview.mixedCount,
                            totalCount = selectedOverview.totalHouses
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Sector Breakdown on $selectedDate",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val typeStatsWithHouses = selectedOverview.typeStats.filter { it.totalHouses > 0 }
            if (typeStatsWithHouses.isEmpty()) {
                item {
                    Text(
                        text = "No collection records on this date.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(typeStatsWithHouses, key = { "hist_stat_${it.houseType}" }) { stat ->
                    TypeStatCard(stat = stat)
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "HOUSES RECORDED (${selectedOverview.records.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(selectedOverview.records, key = { "record_${it.id}" }) { record ->
                HistoryHouseCard(record = record, modifier = Modifier.animateItem())
            }

        } else {
            // Search Colony House Tab
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search house e.g. G-14, B-3, or 12...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("house_search_input"),
                    singleLine = true
                )

                // Quick sector filter chips
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quick Sector Search:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.houseTypes) { type ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setSearchQuery("$type-")
                                }
                        ) {
                            Text(
                                text = "Type $type",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No records matching \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "Found ${searchResults.size} record(s):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(searchResults, key = { "search_${it.id}" }) { record ->
                    HistoryHouseCard(record = record, showDate = true, modifier = Modifier.animateItem())
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HistoryHouseCard(
    record: CollectionRecord,
    showDate: Boolean = false,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val formattedTime = remember(record.timestamp) { timeFormat.format(Date(record.timestamp)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Text(
                    text = record.fullAddress,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (record.isSegregated) SegregatedGreen else MixedRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (record.isSegregated) "Segregated" else "Mixed Waste",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (record.isSegregated) SegregatedGreen else MixedRed
                    )
                }
                Text(
                    text = if (showDate) "${record.dateString} at $formattedTime" else formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (record.notes.isNotEmpty()) {
                    Text(
                        text = record.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

private fun sharePastReport(context: Context, overview: ColonyOverview) {
    val builder = StringBuilder()
    builder.appendLine("🌿 Fatima Fertilizer Colony - Horticulture Waste Report")
    builder.appendLine("📅 Date: ${overview.dateString}")
    builder.appendLine("📊 Total Houses: ${overview.totalHouses}")
    builder.appendLine("✅ Segregated: ${overview.segregatedCount} (${overview.segregationPercentage}%)")
    builder.appendLine("❌ Mixed: ${overview.mixedCount}")
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, builder.toString())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Archive Report"))
}
