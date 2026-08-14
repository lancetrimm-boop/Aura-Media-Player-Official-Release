package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaItem
import com.example.ui.theme.AuraOnSurface
import com.example.ui.theme.AuraOnSurfaceVariant
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraSurface

@Composable
fun AuraIntelligenceSummaryBar(
    mediaItems: List<MediaItem>,
    modifier: Modifier = Modifier
) {
    val totalCount = mediaItems.size
    val favoriteCount = mediaItems.count { it.isFavorite }
    val cleanupSuggestions = mediaItems.count { it.viewCount == 0 && it.exposureCount > 5 } // Example heuristic

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = AuraSurface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AuraPurple.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = AuraPurple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AURA INTELLIGENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = AuraPurple,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetric(label = "Analyzed", value = totalCount.toString())
                SummaryMetric(label = "Favorites", value = favoriteCount.toString())
                SummaryMetric(label = "Refinement", value = cleanupSuggestions.toString())
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String
) {
    Column {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AuraOnSurface
        )
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = AuraOnSurfaceVariant,
            letterSpacing = 0.5.sp
        )
    }
}
