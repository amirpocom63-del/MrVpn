package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VpnServer
import com.example.model.VlessConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerManagementPanel(
    servers: List<VpnServer>,
    onAddServer: (name: String, configUrl: String, remarks: String) -> Unit,
    onDeleteServer: (VpnServer) -> Unit,
    onClosePanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rawConfigUrl by remember { mutableStateOf("") }
    var serverName by remember { mutableStateOf("") }
    var serverRemarks by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isFormExpanded by remember { mutableStateOf(false) }

    // Admin Authorization State: Pin Protection
    var adminPin by remember { mutableStateOf("") }
    var isAuthorized by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Master Access definitions
    val targetPin = "138800" // Secure custom year PIN bypass code
    val adminEmail = "amirpocom63@gmail.com" // Automatically configured master email

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B19))
            .padding(16.dp)
            .testTag("server_management_panel_container")
    ) {
        // App header bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF00FFCC).copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        color = Color(0xFF00FFCC),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "پنل مدیریت سرورها",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }

            IconButton(
                onClick = onClosePanel,
                modifier = Modifier.testTag("admin_panel_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "بستن پنل مدیریت",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isAuthorized) {
            // Elegant secure cyber auth gate
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "احراز هویت مدیریت",
                    tint = Color(0xFF00FFCC),
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ورود مجهز به سیستم احراز هویت مدیریت",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "جهت ورود رمز عبور مدیریت مخصصوص خود را وارد کنید",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = adminPin,
                    onValueChange = { 
                        adminPin = it
                        authError = null
                        if (it == targetPin) {
                            isAuthorized = true
                        }
                    },
                    label = { Text("رمز عبور مدیریت (PIN)", color = Color.White.copy(alpha = 0.6f)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00FFCC),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = Color(0xFF00FFCC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .testTag("admin_pin_input_field"),
                    maxLines = 1
                )

                if (authError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = authError!!,
                        color = Color(0xFFFF3366),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (adminPin == targetPin) {
                            isAuthorized = true
                        } else {
                            authError = "رمز عبور وارد شده نادرست است."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FFCC),
                        contentColor = Color(0xFF070B19)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                        .testTag("submit_pin_button")
                ) {
                    Text("احراز هویت و ورود", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-authorization display badge for owner email
                Row(
                    modifier = Modifier
                        .background(Color(0xFF00FFCC).copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "صاحب امتیاز: $adminEmail",
                        color = Color(0xFF00FFCC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Authorized Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Verification badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF00FFCC).copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF00FFCC),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "هویت مدیریت با موفقیت تایید شد",
                            color = Color(0xFF00FFCC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { 
                            isAuthorized = false
                            adminPin = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("خروج", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Trigger expand add server form
                if (!isFormExpanded) {
                    Button(
                        onClick = { isFormExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00FFCC),
                            contentColor = Color(0xFF070B19)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("expand_add_server_form_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "افزودن سرور vless جدید",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isFormExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF131D35), shape = RoundedCornerShape(16.dp))
                            .padding(14.dp)
                            .testTag("add_server_form_box")
                    ) {
                        Text(
                            text = "افزودن سرور جدید",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Config Input Field
                        OutlinedTextField(
                            value = rawConfigUrl,
                            onValueChange = {
                                rawConfigUrl = it
                                validationError = null
                                val parsed = VlessConfig.parse(it)
                                if (parsed != null) {
                                    serverName = parsed.remarks
                                    serverRemarks = parsed.address
                                }
                            },
                            label = { Text("لینک کانفیگ ویتوری (vless://)", color = Color.White.copy(alpha = 0.5f)) },
                            placeholder = { Text("vless://...", color = Color.White.copy(alpha = 0.35f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                cursorColor = Color(0xFF00FFCC)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("config_url_input_field")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Name Input Field
                        OutlinedTextField(
                            value = serverName,
                            onValueChange = { serverName = it },
                            label = { Text("عنوان سرور (نام نمایشی)", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                cursorColor = Color(0xFF00FFCC)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("server_name_input_field")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Remarks Input Field
                        OutlinedTextField(
                            value = serverRemarks,
                            onValueChange = { serverRemarks = it },
                            label = { Text("آدرس سرور یا توضیحات", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                cursorColor = Color(0xFF00FFCC)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("server_remarks_input_field")
                        )

                        if (validationError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = validationError!!,
                                color = Color(0xFFFF3366),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("validation_error_message")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (rawConfigUrl.isBlank() || !rawConfigUrl.startsWith("vless://")) {
                                        validationError = "خالی یا نامعتبر است. فرمت معتبر vless:// آدرس‌دهی شده الزامیست."
                                        return@Button
                                    }
                                    
                                    val parsed = VlessConfig.parse(rawConfigUrl)
                                    if (parsed == null) {
                                        validationError = "فرمت کلی وب‌لینک vless نامعتبر است."
                                        return@Button
                                    }

                                    onAddServer(serverName, rawConfigUrl, serverRemarks)
                                    
                                    // Reset inputs
                                    rawConfigUrl = ""
                                    serverName = ""
                                    serverRemarks = ""
                                    validationError = null
                                    isFormExpanded = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00FFCC),
                                    contentColor = Color(0xFF070B19)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("submit_add_server_btn")
                            ) {
                                Text("ذخیره سرور", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    isFormExpanded = false
                                    validationError = null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("انصراف")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "لیست سرورهای ثبت شده",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Display existing configured servers with secure AES credentials
                if (servers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هیچ سروری اضافه نشده است. کانفیگ خود را وارد کنید.",
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                            .testTag("admin_servers_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(servers) { server ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF131D35), shape = RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                                    .testTag("admin_server_row_${server.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1.0f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = server.name,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (server.encryptionKey.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF00FFCC).copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "AES",
                                                    color = Color(0xFF00FFCC),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = server.remarks,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 10.sp
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteServer(server) },
                                    modifier = Modifier.testTag("delete_server_button_${server.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف سرور",
                                        tint = Color(0xFFFF3366).copy(alpha = 0.85f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
