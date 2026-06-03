package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VpnConnectionState

@Composable
fun SmartConfigPingWidget(
    connectionState: VpnConnectionState,
    pingMs: Int?,
    isPinging: Boolean,
    onTestPingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color(0xFF0F172A), shape = RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("ping_widget_container"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.0f)) {
            // Highly Secure Configuration Status Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "اتصال دارای رمزگذاری AES-GCM ۲۵۶ متقارن است",
                    tint = Color(0xFF00FFCC),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "پروتکل رمزگذاری: AES-GCM-256",
                    color = Color(0xFF00FFCC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "رمزگذاری فوق‌امنیتی رشته کانفیگ با کلید اختصاصی",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Latency Measurement Core Display
        if (connectionState == VpnConnectionState.CONNECTED) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF1E293B), shape = RoundedCornerShape(10.dp))
                    .clickable(enabled = !isPinging, onClick = onTestPingClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("ping_trigger_action_box"),
                contentAlignment = Alignment.Center
            ) {
                if (isPinging) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF00FFCC),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        modifier = Modifier.testTag("ping_result_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnimatedContent(
                            targetState = pingMs,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "lat_fade"
                        ) { value ->
                            if (value != null) {
                                val latColor = when {
                                    value < 80 -> Color(0xFF00FFCC)
                                    value < 160 -> Color(0xFFFFCC00)
                                    else -> Color(0xFFFF3366)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "پینگ اتصال",
                                        tint = latColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "$value ms",
                                        color = latColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "اجرای تست پینگ",
                                        tint = Color.White.copy(alpha = 0.61f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "تست پینگ",
                                        color = Color.White.copy(alpha = 0.82f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Disabled state when VPN is disconnected
            Box(
                modifier = Modifier
                    .background(Color(0xFF1E293B).copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "آفلاین",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
