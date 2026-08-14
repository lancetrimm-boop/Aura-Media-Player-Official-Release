package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IntelligenceRepository
import com.example.ui.components.AuraTopBar
import com.example.ui.theme.AuraCrispWhite
import com.example.ui.theme.AuraMidnight
import com.example.ui.theme.AuraMutedSlate
import com.example.ui.theme.AuraOnSurfaceVariant
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.DiscoveryViolet

enum class IntelligenceSection(val title: String) {
    OVERVIEW("Overview"),
    INBOX("Inbox"),
    FINDINGS("Findings"),
    IMPROVEMENTS("Recommendations"),
    DECISIONS("Decisions"),
    EXECUTION("Execution"),
    REPORTS("Analysis"),
    DEBUGGER("Debugger"),
    WORKSPACE("Blueprint"),
    HISTORY("History")
}

@Composable
fun AuraIntelligenceScreen(
    repository: IntelligenceRepository,
    onNavigateToImprovement: (String) -> Unit,
    onNavigateToFinding: (String) -> Unit,
    onNavigateToCleanupDebug: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(IntelligenceSection.OVERVIEW) }

    val viewModel: IntelligenceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return IntelligenceViewModel(repository) as T
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraCrispWhite)
    ) {
        AuraTopBar(
            title = if (selectedSection == IntelligenceSection.DECISIONS) "Decision Center" else "Aura Intelligence",
            showLogo = selectedSection != IntelligenceSection.DECISIONS,
            actions = {
                if (selectedSection == IntelligenceSection.DECISIONS) {
                    TextButton(onClick = { viewModel.markAllAsReviewed() }) {
                        Text("Mark All Reviewed", color = DiscoveryViolet, fontSize = 12.sp)
                    }
                }
            }
        )

        ScrollableTabRow(
            selectedTabIndex = selectedSection.ordinal,
            containerColor = AuraCrispWhite,
            contentColor = DiscoveryViolet,
            edgePadding = 20.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection.ordinal]),
                    color = DiscoveryViolet
                )
            }
        ) {
            IntelligenceSection.entries.forEach { section ->
                Tab(
                    selected = selectedSection == section,
                    onClick = { selectedSection = section },
                    text = {
                        Text(
                            text = section.title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSection == section) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.4.sp
                        )
                    },
                    selectedContentColor = DiscoveryViolet,
                    unselectedContentColor = AuraMutedSlate
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedSection) {
                IntelligenceSection.OVERVIEW -> {
                    IntelligenceOverviewScreen(
                        repository = repository,
                        onNavigateToImprovement = onNavigateToImprovement,
                        onNavigateToFinding = onNavigateToFinding
                    ) { selectedSection = IntelligenceSection.REPORTS }
                }
                IntelligenceSection.INBOX -> {
                    IntelligenceInboxScreen(
                        repository = repository,
                        onNavigateToImprovement = onNavigateToImprovement,
                        onNavigateToFinding = onNavigateToFinding
                    )
                }
                IntelligenceSection.FINDINGS -> {
                    FindingsScreen(
                        repository = repository,
                        onNavigateToFinding = onNavigateToFinding
                    )
                }
                IntelligenceSection.IMPROVEMENTS -> {
                    SuggestedImprovementsScreen(
                        repository = repository,
                        onNavigateToImprovement = onNavigateToImprovement
                    )
                }
                IntelligenceSection.DECISIONS -> {
                    AuraDecisionCenterScreen(
                        repository = repository,
                        onNavigateToImprovement = onNavigateToImprovement,
                        onNavigateToFinding = onNavigateToFinding,
                        onNavigateToMonitoring = { /* */ },
                        onBack = { selectedSection = IntelligenceSection.OVERVIEW }
                    )
                }
                IntelligenceSection.EXECUTION -> {
                    IntelligenceExecutionDashboard(
                        repository = repository,
                        onNavigateToImprovement = onNavigateToImprovement
                    )
                }
                IntelligenceSection.REPORTS -> {
                    MasterIntelligenceReportScreen(
                        repository = repository,
                        onNavigateToWorkspace = { /* */ },
                        onNavigateToImprovement = onNavigateToImprovement,
                        onNavigateToFinding = onNavigateToFinding
                    )
                }
                IntelligenceSection.DEBUGGER -> {
                    EngagementDebuggerScreen(
                        repository = repository.mediaRepository,
                        onBack = { selectedSection = IntelligenceSection.OVERVIEW },
                        onNavigateToCleanupDebug = onNavigateToCleanupDebug
                    )
                }
                IntelligenceSection.WORKSPACE -> {
                    BlueprintWorkspaceScreen(
                        repository = repository.mediaRepository,
                        onBack = { selectedSection = IntelligenceSection.OVERVIEW },
                        onNavigateToImprovement = onNavigateToImprovement,
                        onNavigateToFinding = onNavigateToFinding
                    )
                }
                IntelligenceSection.HISTORY -> {
                    IntelligenceHistoryScreen(repository = repository)
                }
            }
        }
    }
}
