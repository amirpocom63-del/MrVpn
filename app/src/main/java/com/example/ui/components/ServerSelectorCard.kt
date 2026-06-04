package com.example.ui.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VpnServer
import com.example.ui.theme.CyberCyan

@Composable
fun ServerSelectorCard(
    selectedServer: VpnServer?,
    servers: List<VpnServer>,
    serverPings: Map<String, Int?>,
    isPingingAll: Boolean,
    onTestAllPings: () -> Unit,
    onSmartConnect: () -> Unit,
    onServerSelected: (VpnServer) -> Unit,
    onManageAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("server_selector_card_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "لیست لوکیشن سرورها",
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "انتخاب سرور تونلینگ",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .background(CyberCyan.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onManageAdminClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("admin_panel_trigger_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "ورود به پنل مدیریت سرورها",
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "مدیریت سرور",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions Panel: Test All Pings and Smart Connect
        if (servers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestAllPings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan.copy(alpha = 0.12f),
                        contentColor = CyberCyan
                    ),
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    if (isPingingAll) {
                        CircularProgressIndicator(
                            color = CyberCyan,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("تست پینگ همه", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onSmartConnect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color(0xFF070B19)
                    ),
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("اتصال به بهترین", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Display beautiful list block of servers
        if (servers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "سروری یافت نشد. از پنل مدیریت سرور اضافه کنید.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("servers_quick_list")
            ) {
                servers.forEachIndexed { index, server ->
                    val isSelected = selectedServer?.id == server.id
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onServerSelected(server) }
                            .padding(vertical = 10.dp)
                            .testTag("server_row_${server.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = if (isSelected) "سرور فعال" else "سرور غیرفعال",
                                tint = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                modifier = Modifier.size(18.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = server.name,
                                    color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onBackground,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = server.remarks,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Right side: Live ping score indicator badge
                        val pingVal = serverPings[server.id]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (pingVal == null) {
                                Text(
                                    text = "--",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else if (pingVal < 0) {
                                Text(
                                    text = "خطا",
                                    color = Color(0xFFFF3366),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "${pingVal}ms",
                                    color = if (pingVal < 150) CyberCyan else Color(0xFFFFCC00),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    if (index < servers.size - 1 && index < 2) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 1.dp)
                    }
                }
            }
        }
    }
}
