package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.PlaybackErrorLogEntity
import com.example.ui.components.AuraTopBar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlaybackDiagnosticsScreen(
    viewModel: PlaybackDiagnosticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errorLogs by viewModel.errorLogs.collectAsStateWithLifecycle()
    var selectedError by remember { mutableStateOf<PlaybackErrorLogEntity?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(AuraCrispWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AuraTopBar(
                title = "Playback Diagnostics",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AuraMidnight
                        )
                    }
                },
                actions = {
                    if (errorLogs.isNotEmpty()) {
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color(0xFFEF4444))
                        }
                    }
                }
            )

            if (errorLogs.isEmpty()) {
                EmptyDiagnosticsState()
            } else {
                Text(
                    text = "Playback errors recorded locally on this device.",
                    fontSize = 13.sp,
                    color = AuraMutedSlate,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(errorLogs, key = { it.id }) { error ->
                        PlaybackErrorCard(
                            error = error,
                            onClick = { selectedError = error }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Detail View Overlay
        AnimatedVisibility(
            visible = selectedError != null,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            selectedError?.let { error ->
                PlaybackErrorDetailView(
                    error = error,
                    onBack = { selectedError = null },
                    onDelete = {
                        viewModel.deleteError(error.id)
                        selectedError = null
                    }
                )
            }
        }

        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("Clear All Playback Diagnostics?", fontWeight = FontWeight.Bold) },
                text = { Text("All locally stored playback error records will be permanently deleted.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllLogs()
                            showDeleteAllDialog = false
                        }
                    ) {
                        Text("Clear All", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text("Cancel", color = AuraMidnight)
                    }
                },
                containerColor = AuraCrispWhite,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
private fun EmptyDiagnosticsState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            color = AuraSubtleSurface,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircleOutline,
                    contentDescription = null,
                    tint = DiscoveryViolet,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Playback Errors",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AuraMidnight
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aura hasn't recorded any playback failures on this device.",
            fontSize = 14.sp,
            color = AuraMutedSlate,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlaybackErrorCard(
    error: PlaybackErrorLogEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, AuraSubtleBorder, RoundedCornerShape(16.dp)),
        color = AuraSubtleSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = error.diagnosticSummary ?: "Playback Error",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraMidnight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error.mediaTitle ?: error.fileName ?: "Unknown Media",
                        fontSize = 13.sp,
                        color = AuraMidnight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (error.occurrenceCount > 1) {
                    Surface(
                        color = DiscoveryViolet.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "x${error.occurrenceCount}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DiscoveryViolet,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = AuraMutedSlate,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(error.timestamp),
                    fontSize = 11.sp,
                    color = AuraMutedSlate
                )
                if (error.errorCodeName != null) {
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = error.errorCodeName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaybackErrorDetailView(
    error: PlaybackErrorLogEntity,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AuraCrispWhite
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AuraTopBar(
                title = "Error Details",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AuraMidnight)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        copyToClipboard(context, formatDiagnosticForCopy(error))
                        Toast.makeText(context, "Diagnostics copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Details", tint = DiscoveryViolet)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Playback diagnostics are stored locally on this device.",
                    fontSize = 12.sp,
                    color = AuraMutedSlate,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                DiagnosticSection("Overview") {
                    DiagnosticRow("Summary", error.diagnosticSummary)
                    DiagnosticRow("First Occurrence", formatTimestampFull(error.timestamp))
                    if (error.occurrenceCount > 1) {
                        DiagnosticRow("Last Occurrence", formatTimestampFull(error.lastOccurrenceTimestamp))
                        DiagnosticRow("Total Count", error.occurrenceCount.toString())
                    }
                    DiagnosticRow("Session ID", error.sessionId)
                    DiagnosticRow("Recovery Attempted", error.recoveryAttempted.toString())
                    DiagnosticRow("Recovery Successful", error.recoverySuccessful?.toString() ?: "N/A")
                }

                DiagnosticSection("Media") {
                    DiagnosticRow("Title", error.mediaTitle)
                    DiagnosticRow("Filename", error.fileName)
                    DiagnosticRow("Media ID", error.mediaItemId)
                    DiagnosticRow("URI", error.mediaUri)
                    DiagnosticRow("MIME Type", error.mimeType)
                    DiagnosticRow("Duration", error.durationMs?.let { "${it}ms" })
                    DiagnosticRow("Playback Position", error.playbackPositionMs?.let { "${it}ms" })
                    DiagnosticRow("Local File", error.isLocalFile.toString())
                }

                DiagnosticSection("Playback") {
                    DiagnosticRow("State", error.playbackState)
                    DiagnosticRow("Play When Ready", error.playWhenReady?.toString())
                    DiagnosticRow("Renderer Index", error.rendererIndex?.toString())
                    DiagnosticRow("Renderer Name", error.rendererName)
                }

                if (error.codecName != null || error.codecMimeType != null) {
                    DiagnosticSection("Decoder / Codec") {
                        DiagnosticRow("Codec Name", error.codecName)
                        DiagnosticRow("Codec MIME", error.codecMimeType)
                    }
                }

                DiagnosticSection("Error") {
                    DiagnosticRow("Code", error.errorCode?.toString())
                    DiagnosticRow("Code Name", error.errorCodeName)
                    DiagnosticRow("Message", error.errorMessage)
                    DiagnosticRow("Exception Class", error.exceptionClass)
                }

                if (error.causeChain != null) {
                    DiagnosticSection("Cause Chain") {
                        val chains = error.causeChain.split(" -> ")
                        chains.forEachIndexed { index, chain ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (index > 0) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = AuraMutedSlate,
                                        modifier = Modifier.size(14.dp).padding(end = 8.dp)
                                    )
                                }
                                Text(
                                    text = chain,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AuraMidnight
                                )
                            }
                        }
                    }
                }

                if (error.stackTrace != null) {
                    var expanded by remember { mutableStateOf(false) }
                    DiagnosticSection("Stack Trace") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .border(1.dp, AuraSubtleBorder, RoundedCornerShape(8.dp)),
                            color = AuraSubtleSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (expanded) "Hide Stack Trace" else "Show Stack Trace",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DiscoveryViolet
                                    )
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = DiscoveryViolet,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (expanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = error.stackTrace,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = AuraMidnight,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())
                                    )
                                }
                            }
                        }
                    }
                }

                DiagnosticSection("Device") {
                    DiagnosticRow("Manufacturer", error.deviceManufacturer)
                    DiagnosticRow("Model", error.deviceModel)
                    DiagnosticRow("Android Version", error.androidVersion)
                    DiagnosticRow("SDK Int", error.sdkInt?.toString())
                    DiagnosticRow("App Version", error.appVersion)
                    DiagnosticRow("Media3 Version", error.media3Version)
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete Playback Error?", fontWeight = FontWeight.Bold) },
                text = { Text("This diagnostic record will be permanently removed from this device.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDelete()
                            showDeleteConfirm = false
                        }
                    ) {
                        Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel", color = AuraMidnight)
                    }
                },
                containerColor = AuraCrispWhite,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
private fun DiagnosticSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = AuraMutedSlate,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String?) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AuraMutedSlate
        )
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "Unavailable",
            fontSize = 13.sp,
            color = AuraMidnight,
            lineHeight = 18.sp
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { time = date }
    
    return if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR) && 
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
    }
}

private fun formatTimestampFull(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Playback Diagnostic", text)
    clipboard.setPrimaryClip(clip)
}

private fun formatDiagnosticForCopy(error: PlaybackErrorLogEntity): String {
    val sb = StringBuilder()
    sb.append("Aura Media Player\n")
    sb.append("Playback Diagnostic\n\n")
    
    sb.append("Summary: ${error.diagnosticSummary ?: "Unavailable"}\n")
    sb.append("First Occurrence: ${formatTimestampFull(error.timestamp)}\n")
    if (error.occurrenceCount > 1) {
        sb.append("Last Occurrence: ${formatTimestampFull(error.lastOccurrenceTimestamp)}\n")
        sb.append("Occurrence Count: ${error.occurrenceCount}\n")
    }
    sb.append("Media: ${error.mediaTitle ?: "Unavailable"}\n")
    sb.append("Filename: ${error.fileName ?: "Unavailable"}\n")
    sb.append("Media ID: ${error.mediaItemId ?: "Unavailable"}\n")
    sb.append("URI: ${error.mediaUri ?: "Unavailable"}\n")
    sb.append("MIME Type: ${error.mimeType ?: "Unavailable"}\n")
    sb.append("Playback Position: ${error.playbackPositionMs?.let { "${it}ms" } ?: "Unavailable"}\n")
    sb.append("Playback State: ${error.playbackState ?: "Unavailable"}\n\n")
    
    sb.append("Error Code: ${error.errorCode ?: "Unavailable"}\n")
    sb.append("Error Code Name: ${error.errorCodeName ?: "Unavailable"}\n")
    sb.append("Error Message: ${error.errorMessage ?: "Unavailable"}\n")
    sb.append("Exception: ${error.exceptionClass ?: "Unavailable"}\n\n")
    
    sb.append("Codec: ${error.codecName ?: "Unavailable"}\n")
    sb.append("Renderer: ${error.rendererName ?: "Unavailable"}\n\n")
    
    sb.append("Device: ${error.deviceManufacturer} ${error.deviceModel}\n")
    sb.append("Android: ${error.androidVersion}\n")
    sb.append("SDK: ${error.sdkInt}\n")
    sb.append("App Version: ${error.appVersion}\n")
    sb.append("Media3 Version: ${error.media3Version}\n\n")
    
    sb.append("Cause Chain:\n${error.causeChain ?: "Unavailable"}\n\n")
    sb.append("Stack Trace:\n${error.stackTrace ?: "Unavailable"}\n")
    
    return sb.toString()
}
