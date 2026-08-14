package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.*
import com.example.data.intelligence.CalibrationStatus
import com.example.data.intelligence.TasteClusterEvidence
import com.example.ui.components.*
import com.example.ui.theme.*
import android.widget.Toast
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.example.util.MediaThumbnailFetcher

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = AuraMidnight,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp, top = 8.dp)
    )
}

@Composable
private fun PrivacyConsentCard(
    consentState: com.example.data.contribution.ConsentState,
    onConsentChange: (com.example.data.contribution.ConsentState) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = AuraSubtleSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AuraSubtleBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Global Recommendation Intelligence",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuraMidnight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (consentState) {
                            com.example.data.contribution.ConsentState.GRANTED -> "Status: Opted In (Active)"
                            com.example.data.contribution.ConsentState.REVOKED -> "Status: Opted Out (Queue Purged)"
                            else -> "Status: Disabled by Default"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (consentState == com.example.data.contribution.ConsentState.GRANTED) DiscoveryViolet else AuraMutedSlate
                    )
                }

                Switch(
                    checked = consentState == com.example.data.contribution.ConsentState.GRANTED,
                    onCheckedChange = { checked ->
                        val newState = if (checked) {
                            com.example.data.contribution.ConsentState.GRANTED
                        } else {
                            com.example.data.contribution.ConsentState.REVOKED
                        }
                        onConsentChange(newState)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AuraCrispWhite,
                        checkedTrackColor = DiscoveryViolet,
                        uncheckedThumbColor = AuraMutedSlate,
                        uncheckedTrackColor = AuraSubtleBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Participation is entirely optional. When enabled, Aura contributes anonymized mathematical preference signals to improve global models. Your personal intelligence stays 100% private and on-device.",
                fontSize = 11.sp,
                color = AuraMutedSlate,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun VisualTasteLearningCard() {
    Surface(
        color = DiscoveryViolet.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AuraSubtleBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Aura is learning your visual taste.",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(12.dp),
            color = DiscoveryViolet,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProfileScreen(
    repository: com.example.data.MediaRepository,
    onNavigateToFavorites: () -> Unit,
    onNavigateToCleanup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tasteDNA by repository.tasteDNA.collectAsStateWithLifecycle()
    val preferenceProfile by repository.preferenceProfile.collectAsStateWithLifecycle()
    val discoveryPolicy by repository.discoveryPolicy.collectAsStateWithLifecycle()
    val consentState by repository.consentState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val dashboardViewModel: IntelligenceDashboardViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return IntelligenceDashboardViewModel(repository.intelligenceRepository!!) as T
            }
        }
    )
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val report = dashboardState.report
    
    val fullLibrary by repository.mediaItems.collectAsStateWithLifecycle()
    
    val topGenres = remember(fullLibrary) {
        fullLibrary.groupBy { it.genre }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .let { list ->
                val total = list.sumOf { it.second }.coerceAtLeast(1)
                list.map { it.first to (it.second * 100 / total) }
            }
    }

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var selectedCluster by remember { mutableStateOf<TasteClusterEvidence?>(null) }
    var slidersExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AuraCrispWhite)
        ) {
            AuraSectionHeader(
                title = "Profile",
                subtitle = "Your intelligence profile and Taste DNA"
            )

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isLandscape = this.maxWidth > this.maxHeight
                
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Left Column: Core Identity & Intelligence
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // 1. Global Discovery Strategy
                            DiscoveryPolicyControl(
                                policy = discoveryPolicy,
                                onPolicyChange = { repository.updateDiscoveryPolicy(it) }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 2. Aura Style Analysis
                            AiDescriptionSection(aiDescription = report?.tasteProfile?.description)

                            Spacer(modifier = Modifier.height(24.dp))

                            // 3. Discovery Intelligence
                            IntelligenceWeightsSection(
                                preferenceProfile = preferenceProfile,
                                onPreferenceProfileUpdate = { repository.updatePreferenceProfile(it) }
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // 4. Tune Your Tastes
                            CollapsibleTasteSliders(
                                tasteDNA = tasteDNA,
                                expanded = slidersExpanded,
                                onToggle = { slidersExpanded = !slidersExpanded },
                                onTasteDnaUpdate = { repository.updateTasteDNA(it, isUserGenerated = true, evidenceCategory = "Profile Manual Tuning") }
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Right Column: Content & Statistics
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // 5. My Content
                            ProfileSectionTitle("My Content")
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp)),
                                color = AuraSubtleSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AuraSubtleBorder)
                            ) {
                                Column {
                                    SettingsClickRow(
                                        icon = Icons.Default.Favorite,
                                        title = "Favorites",
                                        subtitle = "View your favorited media",
                                        onClick = { onNavigateToFavorites() }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AuraSubtleBorder.copy(alpha = 0.5f))
                                    SettingsClickRow(
                                        icon = Icons.Default.Delete,
                                        title = "Smart Cleanup",
                                        subtitle = "Recover storage",
                                        onClick = { onNavigateToCleanup() }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 6. Your Visual Taste
                            if (report != null && report.tasteProfile.tasteClusters.isNotEmpty()) {
                                ProfileSectionTitle("Your Visual Taste")
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    report.tasteProfile.tasteClusters.forEach { evidence ->
                                        TasteClusterCard(
                                            evidence = evidence,
                                            onClick = { selectedCluster = evidence }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // 7. Top Genres
                            TopGenresSection(genres = topGenres)

                            Spacer(modifier = Modifier.height(24.dp))

                            // 8. Your Discovery Style
                            DiscoveryStyleSection(explorationPropensity = tasteDNA.effectiveExploration.toFloat())

                            Spacer(modifier = Modifier.height(24.dp))

                            // 9. Privacy
                            ProfileSectionTitle("Privacy")
                            PrivacyConsentCard(consentState) { repository.updateConsentState(it) }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 10. Feedback
                            ProfileSectionTitle("Feedback")
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp)),
                                color = AuraSubtleSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AuraSubtleBorder)
                            ) {
                                SettingsClickRow(
                                    icon = Icons.Outlined.Feedback,
                                    title = "Customer Feedback",
                                    subtitle = "Share your thoughts",
                                    onClick = { showFeedbackDialog = true }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                } else {
                    // Portrait Layout (Original 10-section hierarchy)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // 1. Global Discovery Strategy
                        DiscoveryPolicyControl(
                            policy = discoveryPolicy,
                            onPolicyChange = { repository.updateDiscoveryPolicy(it) }
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // 2. Aura Style Analysis
                        AiDescriptionSection(aiDescription = report?.tasteProfile?.description)

                        Spacer(modifier = Modifier.height(28.dp))

                        // 3. Discovery Intelligence
                        IntelligenceWeightsSection(
                            preferenceProfile = preferenceProfile,
                            onPreferenceProfileUpdate = { repository.updatePreferenceProfile(it) }
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // 4. Tune Your Tastes
                        CollapsibleTasteSliders(
                            tasteDNA = tasteDNA,
                            expanded = slidersExpanded,
                            onToggle = { slidersExpanded = !slidersExpanded },
                            onTasteDnaUpdate = { repository.updateTasteDNA(it, isUserGenerated = true, evidenceCategory = "Profile Manual Tuning") }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // 5. My Content (Favorites & Cleanup)
                        ProfileSectionTitle("My Content")
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp)),
                            color = AuraSubtleSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AuraSubtleBorder)
                        ) {
                            Column {
                                SettingsClickRow(
                                    icon = Icons.Default.Favorite,
                                    title = "Favorites",
                                    subtitle = "View and filter your favorited media",
                                    onClick = { onNavigateToFavorites() }
                                )
                                
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AuraSubtleBorder.copy(alpha = 0.5f))
                                
                                SettingsClickRow(
                                    icon = Icons.Default.Delete,
                                    title = "Smart Cleanup",
                                    subtitle = "Review and recover storage from low-value media",
                                    onClick = { onNavigateToCleanup() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // 6. Your Visual Taste
                        if (report != null) {
                            val tasteClusters = report.tasteProfile.tasteClusters
                            if (tasteClusters.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ProfileSectionTitle("Your Visual Taste")
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        tasteClusters.chunked(2).forEach { rowClusters ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                rowClusters.forEach { evidence ->
                                                    TasteClusterCard(
                                                        evidence = evidence,
                                                        onClick = { selectedCluster = evidence },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                if (rowClusters.size == 1) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                VisualTasteLearningCard()
                            }
                            Spacer(modifier = Modifier.height(28.dp))
                        }

                        // 7. Top Genres
                        TopGenresSection(genres = topGenres)
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // 8. Your Discovery Style
                        DiscoveryStyleSection(explorationPropensity = tasteDNA.effectiveExploration.toFloat())

                        Spacer(modifier = Modifier.height(28.dp))

                        // 9. Privacy and Global Intelligence
                        ProfileSectionTitle("Privacy and Global Intelligence")
                        PrivacyConsentCard(consentState) { repository.updateConsentState(it) }

                        Spacer(modifier = Modifier.height(28.dp))

                        // 10. Customer Feedback
                        ProfileSectionTitle("Customer Feedback")
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp)),
                            color = AuraSubtleSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AuraSubtleBorder)
                        ) {
                            SettingsClickRow(
                                icon = Icons.Outlined.Feedback,
                                title = "Customer Feedback",
                                subtitle = "Share your thoughts about Aura experience",
                                onClick = { showFeedbackDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }

        if (showFeedbackDialog) {
            var feedbackText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showFeedbackDialog = false },
                title = { Text("Customer Feedback", color = AuraMidnight, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "How can we improve your Aura experience?",
                            fontSize = 13.sp,
                            color = AuraMutedSlate,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            placeholder = { Text("Enter your feedback here...", fontSize = 14.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DiscoveryViolet,
                                unfocusedBorderColor = AuraSubtleBorder,
                                cursorColor = DiscoveryViolet
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (feedbackText.isNotBlank()) {
                                Toast.makeText(context, "Thank you for your feedback.", Toast.LENGTH_SHORT).show()
                                showFeedbackDialog = false
                                feedbackText = ""
                            }
                        }
                    ) {
                        Text("Submit", color = DiscoveryViolet, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFeedbackDialog = false }) {
                        Text("Cancel", color = AuraMutedSlate)
                    }
                },
                containerColor = AuraCrispWhite,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Task 1: Visual Taste Detail Overlay (Phase 1 Integrity)
        if (selectedCluster != null) {
            TasteClusterDetailView(
                evidence = selectedCluster!!,
                onDismiss = { selectedCluster = null }
            )
        }
    }
}

@Composable
fun TasteClusterDetailView(
    evidence: TasteClusterEvidence,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var thumbnailBitmap by remember(evidence.representativeMediaId, evidence.representativeMediaThumbnailUrl) { 
        mutableStateOf<Bitmap?>(null) 
    }

    LaunchedEffect(evidence.representativeMediaId, evidence.representativeMediaThumbnailUrl) {
        val targetUri = evidence.representativeMediaThumbnailUrl
        if (evidence.isVideo && !targetUri.isNullOrEmpty()) {
            thumbnailBitmap = MediaThumbnailFetcher.getThumbnail(context, targetUri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Visual Taste Insight",
                    style = MaterialTheme.typography.labelSmall,
                    color = DiscoveryViolet,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = evidence.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AuraMidnight
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Representative Media Example
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AuraSubtleSurface),
                    contentAlignment = Alignment.Center
                ) {
                    val imageModel = evidence.representativeMediaThumbnailUrl
                    if (imageModel != null) {
                        if (thumbnailBitmap != null) {
                            Image(
                                bitmap = thumbnailBitmap!!.asImageBitmap(),
                                contentDescription = "Visual evidence for ${evidence.title}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Visual evidence for ${evidence.title}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        
                        if (evidence.isVideo) {
                            VideoTilePreview(
                                itemId = evidence.representativeMediaId ?: "",
                                videoUri = imageModel,
                                imageUrl = imageModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Description & Analysis
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "AURA ANALYSIS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = AuraMutedSlate,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = evidence.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AuraMidnight,
                        lineHeight = 22.sp
                    )
                }

                // Metadata: Strength & Contributing Traits
                Surface(
                    color = DiscoveryViolet.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AFFINITY STRENGTH:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AuraMutedSlate
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = evidence.strengthLabel.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = DiscoveryViolet
                            )
                        }

                        if (evidence.contributingTraits.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "CONTRIBUTING TRAITS:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraMutedSlate
                                )
                                Text(
                                    text = evidence.contributingTraits.joinToString(" • "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AuraSlate
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DiscoveryViolet),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = AuraCrispWhite,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun SettingsClickRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = DiscoveryViolet, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AuraMidnight)
                Text(text = subtitle, fontSize = 12.sp, color = AuraMutedSlate)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = AuraMutedSlate)
    }
}
