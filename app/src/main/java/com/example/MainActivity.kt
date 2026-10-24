package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ColonyAppHeader
import com.example.ui.screens.CollectionScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.TodaysReportScreen
import com.example.ui.theme.HortiWasteTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CollectionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainApp()
        }
    }
}

@Composable
fun MainApp(
    viewModel: CollectionViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val todaySummary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFeedback()
        }
    }

    // Determine whether dark theme is effectively active
    val isDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Handle back button: if on Report or History, navigate back to Collection tab
    BackHandler(enabled = selectedTab != 0) {
        viewModel.selectTab(0)
    }

    HortiWasteTheme(themeMode = themeMode) {
        val todayProgressText = if (todaySummary.totalHouses > 0) {
            "Today: ${todaySummary.segregatedCount}/${todaySummary.totalHouses} Segregated (${todaySummary.segregationPercentage}%)"
        } else {
            "Today: Ready to collect"
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                ColonyAppHeader(
                    themeMode = themeMode,
                    onThemeToggle = {
                        val nextMode = when (themeMode) {
                            ThemeMode.SYSTEM -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.SYSTEM
                        }
                        viewModel.setThemeMode(nextMode)
                    },
                    todayProgressText = todayProgressText
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val navColors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        indicatorColor = androidx.compose.ui.graphics.Color(0xFF1E293B),
                        selectedIconColor = androidx.compose.ui.graphics.Color.White,
                        selectedTextColor = androidx.compose.ui.graphics.Color(0xFF1E293B)
                    )

                    val haptic = LocalHapticFeedback.current

                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            if (selectedTab != 0) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.selectTab(0)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PostAdd,
                                contentDescription = "Log Trash"
                            )
                        },
                        label = { Text("Collect Log") },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_tab_collect")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            if (selectedTab != 1) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.selectTab(1)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Today's Report"
                            )
                        },
                        label = { Text("Today's Report") },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_tab_report")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            if (selectedTab != 2) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.selectTab(2)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History & Search"
                            )
                        },
                        label = { Text("History") },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_tab_history")
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition",
                modifier = Modifier.padding(innerPadding)
            ) { tab ->
                when (tab) {
                    0 -> CollectionScreen(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onNavigateToReport = { viewModel.selectTab(1) }
                    )
                    1 -> TodaysReportScreen(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onNavigateToLog = { viewModel.selectTab(0) }
                    )
                    2 -> HistoryScreen(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}
