package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VpnConnectionState
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SafeGreen

@Composable
fun MainStatusDisplay(
    connectionState: VpnConnectionState,
    serverName: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_handshake")
    val heartbeatAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("main_status_display_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Connected Server Location Display Banner styled like a frosted glass pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = when (connectionState) {
                            VpnConnectionState.CONNECTED -> SafeGreen
                            VpnConnectionState.CONNECTING -> Color(0xFFFFB300)
                            VpnConnectionState.DISCONNECTING -> Color(0xFFEF4444)
                            VpnConnectionState.DISCONNECTED -> Color.White.copy(alpha = 0.25f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            
            Text(
                text = serverName,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large high-contrast state label
        Box(
            modifier = Modifier.height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            when (connectionState) {
                VpnConnectionState.DISCONNECTED -> {
                    Text(
                        text = "محافظت قطع شده است",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("status_disconnected")
                    )
                }
                VpnConnectionState.CONNECTING -> {
                    Text(
                        text = "در حال ایجاد مسیر امن...",
                        color = Color(0xFFFFB300).copy(alpha = heartbeatAlpha),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("status_connecting")
                    )
                }
                VpnConnectionState.CONNECTED -> {
                    Text(
                        text = "درگاه امن متصل است",
                        color = CyberCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.testTag("status_connected")
                    )
                }
                VpnConnectionState.DISCONNECTING -> {
                    Text(
                        text = "در حال قطع ارتباط...",
                        color = Color(0xFFEF4444).copy(alpha = heartbeatAlpha),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("status_disconnecting")
                    )
                }
            }
        }
    }
}
