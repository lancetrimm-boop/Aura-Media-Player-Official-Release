package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DiscoveryStyleSection(
    explorationPropensity: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "YOUR DISCOVERY STYLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = DiscoveryViolet,
            letterSpacing = 1.2.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Slider track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(AuraSubtleBorder)
        ) {
            // Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth(explorationPropensity)
                    .fillMaxHeight()
                    .background(DiscoveryGradient)
            )
            
            // Knob (visually centered on the value)
            Box(
                modifier = Modifier
                    .offset(x = (explorationPropensity * 300).dp) // Approximate for preview, will be dynamic in real layout
                    .size(16.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (-8).dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .background(DiscoveryViolet)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Familiar", fontSize = 12.sp, color = AuraSlate, fontWeight = FontWeight.Medium)
            Text("Exploratory", fontSize = 12.sp, color = AuraSlate, fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        val styleTitle = when {
            explorationPropensity > 0.7f -> "Adventurous Explorer"
            explorationPropensity > 0.4f -> "Balanced Explorer"
            else -> "Familiarity Focused"
        }
        
        val styleDesc = when {
            explorationPropensity > 0.7f -> "You love discovering new styles and unexpected content."
            explorationPropensity > 0.4f -> "You enjoy a mix of familiar favorites and new discoveries."
            else -> "You prefer sticking to the styles and creators you know best."
        }
        
        Text(
            text = styleTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AuraMidnight
        )
        
        Text(
            text = styleDesc,
            fontSize = 14.sp,
            color = AuraSlate
        )
    }
}
