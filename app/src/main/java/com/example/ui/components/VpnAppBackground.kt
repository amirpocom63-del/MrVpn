package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan

@Composable
fun VpnAppBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isDark) {
                    Modifier.background(Color(0xFF04060E)) // deep cyber-indigo dark space
                } else {
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF1F5F9),
                                Color(0xFFE2E8F0)
                            )
                        )
                    )
                }
            )
    ) {
        if (isDark) {
            // Draw dual orbital fusion highlights for the cyber look & feel
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Top-right CyberCyan subtle glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CyberCyan.copy(alpha = 0.18f),
                            CyberCyan.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.9f, size.height * 0.1f),
                        radius = size.width * 0.7f
                    ),
                    center = Offset(size.width * 0.9f, size.height * 0.1f),
                    radius = size.width * 0.7f
                )

                // Bottom-left Deep Purple/Indigo secondary glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6366F1).copy(alpha = 0.15f), // Indigo highlight
                            Color(0xFF4F46E5).copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.1f, size.height * 0.85f),
                        radius = size.width * 0.8f
                    ),
                    center = Offset(size.width * 0.1f, size.height * 0.85f),
                    radius = size.width * 0.8f
                )

                // Central glowing matrix path (subtle tech grid)
                val gridAlpha = 0.025f
                val stepX = 40.dp.toPx()
                val stepY = 40.dp.toPx()
                
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = CyberCyan.copy(alpha = gridAlpha),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += stepX
                }
                
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = CyberCyan.copy(alpha = gridAlpha),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += stepY
                }
            }
        } else {
            // Light tech grid line pattern
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridAlpha = 0.03f
                val stepX = 50.dp.toPx()
                val stepY = 50.dp.toPx()
                
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color.Black.copy(alpha = gridAlpha),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 0.5.dp.toPx()
                    )
                    x += stepX
                }
                
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color.Black.copy(alpha = gridAlpha),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.5.dp.toPx()
                    )
                    y += stepY
                }
            }
        }

        // Render child components over the beautiful backdrop overlay
        content()
    }
}
