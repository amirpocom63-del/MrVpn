package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.CryptoUtils
import com.example.db.AppDatabase
import com.example.model.VpnServer
import com.example.model.VlessConfig
import com.example.service.ConnectionSimulator
import com.example.service.NetworkSpeed
import com.example.service.MyVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val vpnDao = database.vpnServerDao()

    // Server State
    val servers: StateFlow<List<VpnServer>> = vpnDao.getAllServers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedServer = MutableStateFlow<VpnServer?>(null)
    val selectedServer: StateFlow<VpnServer?> = _selectedServer.asStateFlow()

    // Connection States
    val connectionState: StateFlow<VpnConnectionState> = MyVpnService.vpnState
        .map { serviceState ->
            when (serviceState) {
                MyVpnService.ConnectionState.CONNECTED -> VpnConnectionState.CONNECTED
                MyVpnService.ConnectionState.CONNECTING -> VpnConnectionState.CONNECTING
                MyVpnService.ConnectionState.DISCONNECTED -> VpnConnectionState.DISCONNECTED
                MyVpnService.ConnectionState.DISCONNECTING -> VpnConnectionState.DISCONNECTING
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VpnConnectionState.DISCONNECTED)

    private val _pingMs = MutableStateFlow<Int?>(null)
    val pingMs: StateFlow<Int?> = _pingMs.asStateFlow()

    private val _isPinging = MutableStateFlow(false)
    val isPinging: StateFlow<Boolean> = _isPinging.asStateFlow()

    // Real-time speed
    private val _networkSpeed = MutableStateFlow(NetworkSpeed(0f, 0f))
    val networkSpeed: StateFlow<NetworkSpeed> = _networkSpeed.asStateFlow()

    private var speedJob: Job? = null
    private var pingJob: Job? = null

    init {
        // Hydrate initial defaults
        viewModelScope.launch {
            servers.collectLatest { list ->
                if (list.isEmpty()) {
                    seedDefaultConfig()
                } else if (_selectedServer.value == null) {
                    val defaultServer = list.find { it.isDefault } ?: list.first()
                    _selectedServer.value = defaultServer
                }
            }
        }

        // Keep connection speed updated dynamically based on real-time state
        viewModelScope.launch {
            connectionState.collectLatest { state ->
                speedJob?.cancel()
                if (state == VpnConnectionState.CONNECTED) {
                    speedJob = viewModelScope.launch {
                        ConnectionSimulator.streamLiveSpeed(true).collect { speed ->
                            _networkSpeed.value = speed
                        }
                    }
                    // Trigger initial ping upon successful connection
                    triggerPing()
                } else {
                    _networkSpeed.value = NetworkSpeed(0f, 0f)
                    _pingMs.value = null
                }
            }
        }
    }

    private suspend fun seedDefaultConfig() {
        val initialConfig = "vless://c35050ce-01ab-45a9-91b8-54cec637d0a8@188.114.97.6:443?path=%2Fudfyfws&security=tls&alpn=h3%2Ch2%2Chttp%2F1.1&encryption=none&insecure=0&host=octopusss5.info&fp=chrome&type=ws&allowInsecure=0&sni=octopusss5.info#mrvpn294"
        val parsed = VlessConfig.parse(initialConfig)
        val name = parsed?.remarks ?: "Official server (mrvpn294)"
        
        // Highly secure setup: generate AES-GCM 256 key specifically for encrypting configuration string
        val aesKey = CryptoUtils.generateKey()
        val encryptedUrl = CryptoUtils.encrypt(initialConfig, aesKey)

        val server = VpnServer(
            name = name,
            configUrl = "SECURED_ENCRYPTED_FIELD",
            remarks = "Primary high-speed secure trunk",
            isDefault = true,
            encryptedConfig = encryptedUrl,
            encryptionKey = aesKey
        )
        withContext(Dispatchers.IO) {
            vpnDao.insertServer(server)
        }
    }

    fun selectServer(server: VpnServer) {
        viewModelScope.launch {
            _selectedServer.value = server
            if (connectionState.value != VpnConnectionState.DISCONNECTED) {
                // Reconnect to new server if currently connected
                disconnectVpn()
                connectVpn()
            } else {
                _pingMs.value = null
            }
        }
    }

    fun toggleConnection() {
        if (connectionState.value == VpnConnectionState.DISCONNECTED) {
            connectVpn()
        } else {
            disconnectVpn()
        }
    }

    fun startVpnService() {
        val server = _selectedServer.value
        val configUrl = if (server != null) {
            try {
                CryptoUtils.decrypt(server.encryptedConfig, server.encryptionKey)
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
        MyVpnService.startService(getApplication(), configUrl)
    }

    fun stopVpnService() {
        MyVpnService.stopService(getApplication())
    }

    private fun connectVpn() {
        startVpnService()
    }

    fun disconnectVpn() {
        stopVpnService()
    }

    fun triggerPing() {
        val server = _selectedServer.value ?: return
        if (connectionState.value != VpnConnectionState.CONNECTED) return
        
        pingJob?.cancel()
        pingJob = viewModelScope.launch {
            _isPinging.value = true
            _pingMs.value = null
            
            // Decrypt secured URL parameters at point of connection handshake
            val cipherText = server.encryptedConfig
            val key = server.encryptionKey
            var host = "188.114.97.6"
            var port = 443

            if (cipherText.isNotEmpty() && key.isNotEmpty()) {
                try {
                    val rawUrl = CryptoUtils.decrypt(cipherText, key)
                    val parsed = VlessConfig.parse(rawUrl)
                    if (parsed != null) {
                        host = parsed.address
                        port = parsed.port
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val result = ConnectionSimulator.measurePing(host, port)
            _pingMs.value = result
            _isPinging.value = false
        }
    }

    // Server Management operations
    fun addServer(name: String, rawConfigUrl: String, remarks: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val parsed = VlessConfig.parse(rawConfigUrl)
            val finalName = if (parsed != null) parsed.remarks else name
            
            val aesKey = CryptoUtils.generateKey()
            val encryptedUrl = CryptoUtils.encrypt(rawConfigUrl, aesKey)

            val newServer = VpnServer(
                name = finalName.ifEmpty { "Custom Server" },
                configUrl = "SECURED_ENCRYPTED_FIELD",
                remarks = remarks.ifEmpty { parsed?.address ?: "Manual VLESS config" },
                isDefault = false,
                encryptedConfig = encryptedUrl,
                encryptionKey = aesKey
            )
            vpnDao.insertServer(newServer)
        }
    }

    fun deleteServer(server: VpnServer) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_selectedServer.value?.id == server.id) {
                _selectedServer.value = null
            }
            vpnDao.deleteServer(server)
        }
    }
}

enum class VpnConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    DISCONNECTING
}
