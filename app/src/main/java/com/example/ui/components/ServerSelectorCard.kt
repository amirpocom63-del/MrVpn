package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
    onServerSelected: (VpnServer) -> Unit,
    onManageAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
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
                    color = Color.White,
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
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("servers_quick_list")
            ) {
                servers.take(3).forEachIndexed { index, server ->
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
                                tint = if (isSelected) CyberCyan else Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(18.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = server.name,
                                    color = if (isSelected) CyberCyan else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = server.remarks,
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    if (index < servers.size - 1 && index < 2) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                    }
                }
            }
        }
    }
}
