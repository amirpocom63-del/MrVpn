package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VpnConnectionState
import com.example.ui.theme.CyberCyan

@Composable
fun PowerButton(
    connectionState: VpnConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnecting = connectionState == VpnConnectionState.CONNECTING || connectionState == VpnConnectionState.DISCONNECTING
    val isConnected = connectionState == VpnConnectionState.CONNECTED

    // Spring scaling on down press / active states
    val scaleFactor by animateFloatAsState(
        targetValue = if (isConnecting) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btn_scale"
    )

    // Glowing pulsator animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_handshake")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isConnecting) 1.5f else if (isConnected) 1.35f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isConnecting || isConnected) 0.35f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val mainAccentColor = when (connectionState) {
        VpnConnectionState.CONNECTED -> CyberCyan
        VpnConnectionState.CONNECTING -> Color(0xFFFFB300) // Amber status
        VpnConnectionState.DISCONNECTING -> Color(0xFFEF4444) // Soft red alert
        VpnConnectionState.DISCONNECTED -> Color(0xFF475569) // Muted slate grey
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .scale(scaleFactor)
            .testTag("power_button_container")
    ) {
        // 1. Large Blurred Ambient Glow Background (Simulating Tailwind's blur-3xl)
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(1.2f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            mainAccentColor.copy(alpha = if (isConnected) 0.12f else if (isConnecting) 0.08f else 0.02f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 2. Active Pulsating Ring Layer
        if (isConnected || isConnecting) {
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .scale(pulseScale)
                    .border(
                        width = 2.dp,
                        brush = Brush.radialGradient(
                            colors = listOf(mainAccentColor.copy(alpha = pulseAlpha), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }

        // 3. Central Slate Cylinder Container (The w-48 h-48 container matching tailwind layout)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(192.dp)
                .border(4.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                .background(Color(0xFF0B0E14), shape = CircleShape)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Custom visual feedback without generic ripple
                    onClick = onClick
                )
                .testTag("power_button_click_surface")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 4. Glowing core inner orb (Cyan circle from design template)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    mainAccentColor,
                                    mainAccentColor.copy(alpha = 0.75f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Power Icon
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "دکمه اتصال به وی پی ان",
                        tint = Color(0xFF0F1115), // Deep dark contrasting icon fill color
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Connection indicator status tag
                val tagText = when (connectionState) {
                    VpnConnectionState.DISCONNECTED -> "آفلاین"
                    VpnConnectionState.CONNECTING -> "در حال اتصال"
                    VpnConnectionState.CONNECTED -> "متصل"
                    VpnConnectionState.DISCONNECTING -> "قطع اتصال"
                }

                Text(
                    text = tagText.uppercase(),
                    color = mainAccentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.testTag("power_button_status_label")
                )
            }
        }
    }
}
