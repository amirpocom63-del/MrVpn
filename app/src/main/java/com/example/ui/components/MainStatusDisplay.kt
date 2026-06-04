package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VpnConnectionState
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SafeGreen

@Composable
fun LiveWaveformCanvas(
    connectionState: VpnConnectionState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_waves")
    
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_2"
    )

    val pulseStrength by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_strength"
    )

    val activeColor = when (connectionState) {
        VpnConnectionState.CONNECTED -> CyberCyan
        VpnConnectionState.CONNECTING -> Color(0xFFFFB300)
        VpnConnectionState.DISCONNECTING -> Color(0xFFEF4444)
        VpnConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    }

    val waveHeight = when (connectionState) {
        VpnConnectionState.CONNECTED -> 24.dp
        VpnConnectionState.CONNECTING -> 14.dp
        VpnConnectionState.DISCONNECTING -> 16.dp
        VpnConnectionState.DISCONNECTED -> 3.dp
    }

    val waveFrequency = when (connectionState) {
        VpnConnectionState.CONNECTED -> 2.5f
        VpnConnectionState.CONNECTING -> 4.5f
        VpnConnectionState.DISCONNECTING -> 5.5f
        VpnConnectionState.DISCONNECTED -> 1.0f
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val amp = waveHeight.toPx() * pulseStrength

        val path1 = Path()
        val path2 = Path()

        path1.moveTo(0f, centerY)
        path2.moveTo(0f, centerY)

        val steps = 120
        for (i in 0..steps) {
            val x = (i.toFloat() / steps) * width
            // Taper waves at edges so they don't break at borders
            val envelope = Math.sin((i.toFloat() / steps) * Math.PI).toFloat()
            
            // Primary Sine trace
            val angle1 = (i.toFloat() / steps) * 2 * Math.PI.toFloat() * waveFrequency + phase1
            val y1 = centerY + (Math.sin(angle1.toDouble()).toFloat() * amp * envelope)
            path1.lineTo(x, y1)

            // Secondary Cosine trace
            val angle2 = (i.toFloat() / steps) * 2 * Math.PI.toFloat() * (waveFrequency * 0.75f) + phase2
            val y2 = centerY + (Math.cos(angle2.toDouble()).toFloat() * (amp * 0.55f) * envelope)
            path2.lineTo(x, y2)
        }

        drawPath(
            path = path1,
            color = activeColor,
            style = Stroke(width = 2.2.dp.toPx())
        )

        drawPath(
            path = path2,
            color = activeColor.copy(alpha = 0.45f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

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
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
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
                            VpnConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            
            Text(
                text = serverName,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
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
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
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

        Spacer(modifier = Modifier.height(8.dp))

        // Live Connection Waveform Status Analyzer (حالت زنده)
        LiveWaveformCanvas(
            connectionState = connectionState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}
