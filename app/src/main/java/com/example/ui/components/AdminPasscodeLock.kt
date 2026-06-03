package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.SpaceDarkBg
import com.example.ui.theme.SpaceDarkCard
import com.example.ui.theme.AlertRed
import kotlinx.coroutines.delay

@Composable
fun AdminPasscodeLock(
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isShaking by remember { mutableStateOf(false) }

    val shakeOffset by animateFloatAsState(
        targetValue = if (isShaking) 15f else 0f,
        animationSpec = spring(dampingRatio = 0.25f, stiffness = Spring.StiffnessHigh),
        label = "shake_animation"
    )

    // Trigger error vibration trigger resets
    LaunchedEffect(isShaking) {
        if (isShaking) {
            delay(300)
            isShaking = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B19).copy(alpha = 0.98f))
            .padding(24.dp)
            .testTag("admin_passcode_lock_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Exit icon
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.05f), shape = CircleShape)
                .clickable { onDismiss() }
                .testTag("close_passcode_lock_btn"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "بستن پنل قفل",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = shakeOffset.dp)
        ) {
            // Glow lock icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(CyberCyan.copy(alpha = 0.1f), shape = CircleShape)
                    .border(1.dp, CyberCyan.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                     imageVector = Icons.Default.Lock,
                     contentDescription = "امنیتی",
                     tint = CyberCyan,
                     modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ورود امن به پنل مدیریت",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "دسترسی مدیریت فقط مخصوص کاربر رسمی:\namirpocom63@gmail.com",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Indicator circles
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { idx ->
                    val isFilled = idx < pinInput.length
                    val glowColor = if (errorMessage != null) AlertRed else CyberCyan
                    val targetAlpha = if (isFilled) 1.0f else 0.15f
                    val targetSize = if (isFilled) 16.dp else 12.dp

                    Box(
                        modifier = Modifier
                            .size(targetSize)
                            .background(
                                color = if (isFilled) glowColor else Color.White.copy(alpha = targetAlpha),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isFilled) glowColor else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.height(24.dp)) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = AlertRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("passcode_error_message")
                    )
                } else {
                    Text(
                        text = "رمز پیش‌فرض: ۱۳۸۸",
                        color = CyberCyan.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Numerical Pin Pad layout matching luxury dashboards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(280.dp)
            ) {
                val digits = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "◀")
                )

                digits.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { digit ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.5f)
                                    .background(SpaceDarkCard, shape = RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
                                    .clickable {
                                        errorMessage = null
                                        when (digit) {
                                            "C" -> {
                                                pinInput = ""
                                            }
                                            "◀" -> {
                                                if (pinInput.isNotEmpty()) {
                                                    pinInput = pinInput.dropLast(1)
                                                }
                                            }
                                            else -> {
                                                if (pinInput.length < 4) {
                                                    pinInput += digit
                                                    if (pinInput.length == 4) {
                                                        // Auto-verify PIN code on 4th tap
                                                        if (pinInput == "1388") {
                                                            onSuccess()
                                                            pinInput = ""
                                                        } else {
                                                            errorMessage = "رمز عبور نادرست است"
                                                            isShaking = true
                                                            pinInput = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("pin_pad_${digit}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit,
                                    color = if (digit == "C" || digit == "◀") AlertRed else Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
