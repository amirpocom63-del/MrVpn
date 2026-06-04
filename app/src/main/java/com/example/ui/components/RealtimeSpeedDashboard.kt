package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.NetworkSpeed
import com.example.ui.theme.CyberCyan
import java.util.Locale

@Composable
fun RealtimeSpeedDashboard(
    networkSpeed: NetworkSpeed,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Download Speed Frosted Card
        SpeedCard(
            label = "DOWNLOAD",
            speedMbs = networkSpeed.downSpeedMbs,
            icon = Icons.Default.ArrowDownward,
            accentColor = CyberCyan,
            modifier = Modifier
                .weight(1f)
                .testTag("dl_speed_widget")
        )

        // 2. Upload Speed Frosted Card
        SpeedCard(
            label = "UPLOAD",
            speedMbs = networkSpeed.upSpeedMbs,
            icon = Icons.Default.ArrowUpward,
            accentColor = Color(0xFFF43F5E), // Rose-500 upload accent
            modifier = Modifier
                .weight(1f)
                .testTag("ul_speed_widget")
        )
    }
}

@Composable
private fun SpeedCard(
    label: String,
    speedMbs: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(24.dp))
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Top label with mini icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
            
            Text(
                text = label,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Large metric representation matching HTML stats display block
        val formattedSpeed = if (speedMbs >= 1.0f) {
            String.format(Locale.US, "%.1f", speedMbs)
        } else {
            String.format(Locale.US, "%.0f", speedMbs * 1024f)
        }
        
        val unit = if (speedMbs >= 1.0f) "MB/s" else "KB/s"

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formattedSpeed,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            
            Text(
                text = unit,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
