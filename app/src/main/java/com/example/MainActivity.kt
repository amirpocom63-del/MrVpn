package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.VpnServer
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.VpnConnectionState
import com.example.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: VpnViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.startVpnService()
        }
    }

    private fun handleVpnToggle() {
        if (viewModel.connectionState.value == VpnConnectionState.DISCONNECTED) {
            val vpnIntent = android.net.VpnService.prepare(this)
            if (vpnIntent != null) {
                vpnPermissionLauncher.launch(vpnIntent)
            } else {
                viewModel.startVpnService()
            }
        } else {
            viewModel.stopVpnService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fully edge-to-edge full bleed support
        enableEdgeToEdge()
        
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            MyApplicationTheme(darkTheme = isDarkTheme) {
                val servers by viewModel.servers.collectAsStateWithLifecycle()
                val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
                val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
                val pingMs by viewModel.pingMs.collectAsStateWithLifecycle()
                val isPinging by viewModel.isPinging.collectAsStateWithLifecycle()
                val networkSpeed by viewModel.networkSpeed.collectAsStateWithLifecycle()

                var showAdminPanel by remember { mutableStateOf(false) }
                var showPasscodeLock by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Main Dashboard Client Surface
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AppHeader(
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { isDarkTheme = !isDarkTheme },
                                onAdminClick = { showPasscodeLock = true }
                            )

                            Spacer(modifier = Modifier.weight(0.2f))

                            // Large Glowing VPN Connection Hub
                            PowerButton(
                                connectionState = connectionState,
                                onClick = { handleVpnToggle() }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Status textual display
                            val activeServerName = selectedServer?.name ?: "سروری انتخاب نشده است"
                            MainStatusDisplay(
                                connectionState = connectionState,
                                serverName = activeServerName
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Speed indicator panel
                            RealtimeSpeedDashboard(networkSpeed = networkSpeed)

                            Spacer(modifier = Modifier.height(14.dp))

                            // Encryption metadata + Latency Checker
                            SmartConfigPingWidget(
                                connectionState = connectionState,
                                pingMs = pingMs,
                                isPinging = isPinging,
                                onTestPingClick = { viewModel.triggerPing() }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Server selections and administration entry
                            ServerSelectorCard(
                                selectedServer = selectedServer,
                                servers = servers,
                                onServerSelected = { viewModel.selectServer(it) },
                                onManageAdminClick = { showPasscodeLock = true }
                            )

                            Spacer(modifier = Modifier.weight(1.0f))
                        }

                        // Fully animated overlay containing server credentials administrator configuration
                        AnimatedVisibility(
                            visible = showAdminPanel,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ServerManagementPanel(
                                servers = servers,
                                onAddServer = { name, configUrl, remarks ->
                                    viewModel.addServer(name, configUrl, remarks)
                                },
                                onDeleteServer = { server ->
                                    viewModel.deleteServer(server)
                                },
                                onClosePanel = { showAdminPanel = false }
                            )
                        }

                        // Secure Admin Passcode Authentication Screen
                        AnimatedVisibility(
                            visible = showPasscodeLock,
                            enter = fadeIn() + scaleIn(initialScale = 0.9f),
                            exit = fadeOut() + scaleOut(targetScale = 0.9f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AdminPasscodeLock(
                                onSuccess = {
                                    showPasscodeLock = false
                                    showAdminPanel = true
                                },
                                onDismiss = {
                                    showPasscodeLock = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
