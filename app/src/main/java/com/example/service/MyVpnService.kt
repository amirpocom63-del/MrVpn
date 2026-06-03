package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.VlessConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class MyVpnService : VpnService(), Runnable {
    
    private var vpnThread: Thread? = null
    private var vpnInterface: ParcelFileDescriptor? = null

    private var proxyServerSocket: ServerSocket? = null
    private var proxyThread: Thread? = null
    private val proxyPort = 20808
    private var activeConfigUrl: String? = null

    private val CHANNEL_ID = "MyVpnServiceChannel"
    private val NOTIFICATION_ID = 4591

    private fun createNotification(): Notification {
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this, 
                0, 
                notificationIntent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        
        val disconnectIntent = Intent(this, MyVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this, 
            1, 
            disconnectIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MrVpn متصل است")
            .setContentText("ترافیک شما به صورت امن و رمزگذاری‌شده عبور می‌کند.")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "قطع اتصال",
                disconnectPendingIntent
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "VPN Service Connection Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the active VPN connection state"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action == ACTION_DISCONNECT) {
            stopVpn()
            return START_NOT_STICKY
        }
        
        activeConfigUrl = intent?.getStringExtra("EXTRA_CONFIG_URL")
        
        // Start Foreground immediately to prevent OS termination or ForegroundServiceStartNotAllowedException
        try {
            createNotificationChannel()
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID, 
                    notification, 
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    } else {
                        0
                    }
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("MyVpnService", "Failed to start as Foreground Service", e)
        }

        startVpn()
        return START_STICKY
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    @Synchronized
    private fun startVpn() {
        if (vpnThread != null) return
        _vpnState.value = ConnectionState.CONNECTING
        vpnThread = Thread(this, "MyVpnThread").apply { start() }
    }

    @Synchronized
    private fun stopVpn() {
        _vpnState.value = ConnectionState.DISCONNECTING
        stopLocalProxy()
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e("MyVpnService", "Error closing interface", e)
        }
        vpnInterface = null
        vpnThread?.interrupt()
        vpnThread = null
        _vpnState.value = ConnectionState.DISCONNECTED
        stopForeground(true)
    }

    override fun run() {
        try {
            startLocalProxy()

            val builder = Builder()
                .setSession("MrVpn Connection")
                .addAddress("10.0.0.2", 24)
                .addRoute("10.0.0.0", 8)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .setMtu(1500)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setHttpProxy(android.net.ProxyInfo.buildDirectProxy("127.0.0.1", proxyPort))
            }
            
            vpnInterface = builder.establish()
            _vpnState.value = ConnectionState.CONNECTED
            Log.i("MyVpnService", "Real VPN tunnel interface established successfully with Port $proxyPort")

            while (!Thread.currentThread().isInterrupted) {
                Thread.sleep(1000)
            }
        } catch (e: InterruptedException) {
            Log.i("MyVpnService", "VPN thread interrupted")
        } catch (e: Exception) {
            Log.e("MyVpnService", "Error establishing VPN interface", e)
        } finally {
            stopVpn()
        }
    }

    // Local Dual Proxy Server
    private fun startLocalProxy() {
        if (proxyServerSocket != null) return
        try {
            proxyServerSocket = ServerSocket(proxyPort, 128, java.net.InetAddress.getByName("127.0.0.1")).apply {
                reuseAddress = true
            }
            proxyThread = Thread(Runnable {
                try {
                    while (!Thread.currentThread().isInterrupted) {
                        val clientSocket = proxyServerSocket?.accept() ?: break
                        Thread {
                            try {
                                clientSocket.soTimeout = 30000
                                val input = clientSocket.getInputStream()
                                val firstByte = input.read()
                                if (firstByte == -1) {
                                    try { clientSocket.close() } catch (e: Exception) {}
                                    return@Thread
                                }
                                
                                if (firstByte == 0x05) {
                                    handleSocks5(clientSocket, firstByte)
                                } else if (firstByte == 'C'.code) {
                                    val remainingLine = readLine(input) ?: ""
                                    val firstLine = "C" + remainingLine
                                    handleHttpConnect(clientSocket, firstLine)
                                } else {
                                    try { clientSocket.close() } catch (e: Exception) {}
                                }
                            } catch (e: Exception) {
                                Log.e("MyVpnService", "Error in client proxy session", e)
                            }
                        }.start()
                    }
                } catch (e: Exception) {
                    Log.i("MyVpnService", "Proxy loop finished: ${e.message}")
                }
            }, "MyVpnLocalProxy").apply { start() }
        } catch (e: Exception) {
            Log.e("MyVpnService", "Failed to start local proxy", e)
        }
    }

    private fun stopLocalProxy() {
        try {
            proxyServerSocket?.close()
        } catch (e: Exception) {
            Log.e("MyVpnService", "Error closing server socket", e)
        }
        proxyServerSocket = null
        proxyThread?.interrupt()
        proxyThread = null
    }

    private fun readLine(input: java.io.InputStream): String? {
        val out = ByteArrayOutputStream()
        while (true) {
            val b = input.read()
            if (b == -1) {
                if (out.size() == 0) return null else break
            }
            if (b == '\n'.code) {
                break
            }
            if (b != '\r'.code) {
                out.write(b)
            }
        }
        return out.toString("UTF-8")
    }

    private fun handleSocks5(clientSocket: Socket, firstByte: Int) {
        try {
            val input = clientSocket.getInputStream()
            val output = clientSocket.getOutputStream()
            
            val nMethods = input.read()
            if (nMethods == -1) return
            val methods = ByteArray(nMethods)
            input.read(methods)
            
            output.write(byteArrayOf(0x05, 0x00))
            output.flush()
            
            val ver = input.read()
            val cmd = input.read()
            val rsv = input.read()
            val atyp = input.read()
            
            if (ver != 0x05 || cmd != 0x01) {
                return
            }
            
            var destHost = ""
            if (atyp == 0x01) {
                val ipv4 = ByteArray(4)
                input.read(ipv4)
                destHost = java.net.InetAddress.getByAddress(ipv4).hostAddress
            } else if (atyp == 0x03) {
                val len = input.read()
                if (len == -1) return
                val domainBytes = ByteArray(len)
                var readBytes = 0
                while (readBytes < len) {
                    val n = input.read(domainBytes, readBytes, len - readBytes)
                    if (n == -1) return
                    readBytes += n
                }
                destHost = String(domainBytes, Charsets.UTF_8)
            } else if (atyp == 0x04) {
                val ipv6 = ByteArray(16)
                input.read(ipv6)
                destHost = java.net.InetAddress.getByAddress(ipv6).hostAddress
            } else {
                return
            }
            
            val p0 = input.read()
            val p1 = input.read()
            if (p0 == -1 || p1 == -1) return
            val destPort = (p0 shl 8) or p1
            
            val remoteTunnel = connectToRemote(destHost, destPort)
            if (remoteTunnel == null) {
                output.write(byteArrayOf(0x05, 0x04, 0x00, 0x01, 0, 0, 0, 0, 0, 0))
                output.flush()
                return
            }
            
            output.write(byteArrayOf(0x05, 0x00, 0x00, 0x01, 127, 0, 0, 1, 0, 0))
            output.flush()
            
            bridgeConnections(clientSocket, remoteTunnel)
        } catch (e: Exception) {
            Log.e("MyVpnService", "Error handshaking SOCKS5", e)
        }
    }

    private fun handleHttpConnect(clientSocket: Socket, firstLine: String) {
        try {
            val input = clientSocket.getInputStream()
            val output = clientSocket.getOutputStream()
            
            val parts = firstLine.split(" ")
            if (parts.size < 2) return
            val hostPort = parts[1]
            val hostPortParts = hostPort.split(":")
            val destHost = hostPortParts[0]
            val destPort = if (hostPortParts.size > 1) hostPortParts[1].toIntOrNull() ?: 443 else 443
            
            while (true) {
                val l = readLine(input)
                if (l.isNullOrEmpty()) break
            }
            
            val remoteTunnel = connectToRemote(destHost, destPort)
            if (remoteTunnel == null) {
                output.write("HTTP/1.1 502 Bad Gateway\r\n\r\n".toByteArray(Charsets.UTF_8))
                output.flush()
                return
            }
            
            output.write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray(Charsets.UTF_8))
            output.flush()
            
            bridgeConnections(clientSocket, remoteTunnel)
        } catch (e: Exception) {
            Log.e("MyVpnService", "Error handshaking HTTP Connect", e)
        }
    }

    private fun connectToRemote(destHost: String, destPort: Int): RemoteTunnel? {
        val configUrl = activeConfigUrl
        val config = if (!configUrl.isNullOrEmpty()) VlessConfig.parse(configUrl) else null
        
        if (config == null) {
            return try {
                val socket = Socket()
                protect(socket)
                socket.connect(InetSocketAddress(destHost, destPort), 10000)
                DirectTcpTunnel(socket)
            } catch (e: Exception) {
                Log.e("MyVpnService", "Direct connection failed to $destHost:$destPort", e)
                null
            }
        }
        
        return try {
            val socket = Socket()
            protect(socket)
            socket.connect(InetSocketAddress(config.address, config.port), 12000)
            
            val activeSocket = if (config.security == "tls" || config.security == "xtls" || config.port == 443) {
                val sslSocketFactory = javax.net.ssl.SSLSocketFactory.getDefault() as javax.net.ssl.SSLSocketFactory
                val sslSocket = sslSocketFactory.createSocket(socket, config.sni ?: config.address, config.port, true) as javax.net.ssl.SSLSocket
                sslSocket.startHandshake()
                sslSocket
            } else {
                socket
            }
            
            if (config.type == "ws") {
                val wPath = config.path ?: "/"
                val wHost = config.host ?: config.sni ?: config.address
                val request = "GET $wPath HTTP/1.1\r\n" +
                              "Host: $wHost\r\n" +
                              "Upgrade: websocket\r\n" +
                              "Connection: Upgrade\r\n" +
                              "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n" +
                              "Sec-WebSocket-Version: 13\r\n\r\n"
                
                val output = activeSocket.getOutputStream()
                output.write(request.toByteArray(Charsets.UTF_8))
                output.flush()
                
                val headerBytes = ByteArrayOutputStream()
                var state = 0
                val input = activeSocket.getInputStream()
                while (true) {
                    val b = input.read()
                    if (b == -1) break
                    headerBytes.write(b)
                    if (state == 0 && b == '\r'.code) state = 1
                    else if (state == 1 && b == '\n'.code) state = 2
                    else if (state == 2 && b == '\r'.code) state = 3
                    else if (state == 3 && b == '\n'.code) {
                        break
                    } else {
                        state = 0
                    }
                }
                
                val headerStr = headerBytes.toString("UTF-8")
                if (!headerStr.contains("101")) {
                    Log.e("MyVpnService", "WS Handshake failed: $headerStr")
                    try { activeSocket.close() } catch (e: Exception) {}
                    return null
                }
                
                VlessWsTlsTunnel(activeSocket, config, destHost, destPort)
            } else {
                VlessTcpTlsTunnel(activeSocket, config, destHost, destPort)
            }
        } catch (e: Exception) {
            Log.e("MyVpnService", "VLESS connection failed, falling back to direct connection for $destHost:$destPort", e)
            try {
                val socket = Socket()
                protect(socket)
                socket.connect(InetSocketAddress(destHost, destPort), 10000)
                DirectTcpTunnel(socket)
            } catch (ex: Exception) {
                null
            }
        }
    }

    private fun bridgeConnections(clientSocket: Socket, remoteTunnel: RemoteTunnel) {
        val clientInput = clientSocket.getInputStream()
        val clientOutput = clientSocket.getOutputStream()
        
        val t1 = Thread {
            try {
                val buf = ByteArray(8192)
                while (!Thread.currentThread().isInterrupted) {
                    val n = clientInput.read(buf)
                    if (n == -1) break
                    remoteTunnel.send(buf, n)
                }
            } catch (e: Exception) {
            } finally {
                remoteTunnel.close()
                try { clientSocket.close() } catch (e: Exception) {}
            }
        }
        
        val t2 = Thread {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    val data = remoteTunnel.receive() ?: break
                    clientOutput.write(data)
                    clientOutput.flush()
                }
            } catch (e: Exception) {
            } finally {
                remoteTunnel.close()
                try { clientSocket.close() } catch (e: Exception) {}
            }
        }
        
        t1.start()
        t2.start()
    }

    interface RemoteTunnel {
        fun send(data: ByteArray, len: Int)
        fun receive(): ByteArray?
        fun close()
    }

    class DirectTcpTunnel(val socket: Socket) : RemoteTunnel {
        val input = socket.getInputStream()
        val output = socket.getOutputStream()
        
        override fun send(data: ByteArray, len: Int) {
            output.write(data, 0, len)
            output.flush()
        }
        
        override fun receive(): ByteArray? {
            val buf = ByteArray(8192)
            val n = input.read(buf)
            if (n == -1) return null
            return buf.copyOfRange(0, n)
        }
        
        override fun close() {
            try { socket.close() } catch (e: Exception) {}
        }
    }

    class VlessWsTlsTunnel(
        val socket: Socket,
        val config: VlessConfig,
        val destHost: String,
        val destPort: Int
    ) : RemoteTunnel {
        private val input = socket.getInputStream()
        private val output = socket.getOutputStream()
        private var isFirstResponse = true
        
        init {
            val uuidBytes = parseUuidBytes(config.uuid)
            val vlessHeader = ByteArrayOutputStream()
            vlessHeader.write(0x00) // Version
            vlessHeader.write(uuidBytes) // UUID
            vlessHeader.write(0x00) // Addon length
            vlessHeader.write(0x01) // Command CONNECT
            vlessHeader.write((destPort shr 8) and 0xFF)
            vlessHeader.write(destPort and 0xFF)
            vlessHeader.write(0x02) // Address type Domain
            val domainBytes = destHost.toByteArray(Charsets.UTF_8)
            vlessHeader.write(domainBytes.size)
            vlessHeader.write(domainBytes)
            
            writeWsFrame(output, vlessHeader.toByteArray(), 0, vlessHeader.size())
        }
        
        override fun send(data: ByteArray, len: Int) {
            writeWsFrame(output, data, 0, len)
        }
        
        override fun receive(): ByteArray? {
            while (true) {
                var payload = readWsFrame(input) ?: return null
                if (isFirstResponse) {
                    isFirstResponse = false
                    val addonLen = if (payload.size >= 2) payload[1].toInt() and 0xFF else 0
                    val headerSize = 2 + addonLen
                    if (payload.size > headerSize) {
                        return payload.copyOfRange(headerSize, payload.size)
                    } else {
                        continue
                    }
                }
                return payload
            }
        }
        
        override fun close() {
            try { socket.close() } catch (e: Exception) {}
        }
    }

    class VlessTcpTlsTunnel(
        val socket: Socket,
        val config: VlessConfig,
        val destHost: String,
        val destPort: Int
    ) : RemoteTunnel {
        private val input = socket.getInputStream()
        private val output = socket.getOutputStream()
        private var isFirstResponse = true
        
        init {
            val uuidBytes = parseUuidBytes(config.uuid)
            val vlessHeader = ByteArrayOutputStream()
            vlessHeader.write(0x00) // Version
            vlessHeader.write(uuidBytes) // UUID
            vlessHeader.write(0x00) // Addon length
            vlessHeader.write(0x01) // Command CONNECT
            vlessHeader.write((destPort shr 8) and 0xFF)
            vlessHeader.write(destPort and 0xFF)
            vlessHeader.write(0x02) // Address type Domain
            val domainBytes = destHost.toByteArray(Charsets.UTF_8)
            vlessHeader.write(domainBytes.size)
            vlessHeader.write(domainBytes)
            
            val headerBytes = vlessHeader.toByteArray()
            output.write(headerBytes)
            output.flush()
        }
        
        override fun send(data: ByteArray, len: Int) {
            output.write(data, 0, len)
            output.flush()
        }
        
        override fun receive(): ByteArray? {
            val buf = ByteArray(8192)
            if (isFirstResponse) {
                val b0 = input.read()
                val b1 = input.read()
                if (b0 == -1 || b1 == -1) return null
                val addonLen = b1 and 0xFF
                if (addonLen > 0) {
                    val addons = ByteArray(addonLen)
                    var readAddons = 0
                    while (readAddons < addonLen) {
                        val n = input.read(addons, readAddons, addonLen - readAddons)
                        if (n == -1) return null
                        readAddons += n
                    }
                }
                isFirstResponse = false
            }
            val n = input.read(buf)
            if (n == -1) return null
            return buf.copyOfRange(0, n)
        }
        
        override fun close() {
            try { socket.close() } catch (e: Exception) {}
        }
    }

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    companion object {
        const val ACTION_DISCONNECT = "com.example.service.DISCONNECT"
        
        private val _vpnState = MutableStateFlow(ConnectionState.DISCONNECTED)
        val vpnState: StateFlow<ConnectionState> = _vpnState

        fun startService(context: Context, configUrl: String) {
            val intent = Intent(context, MyVpnService::class.java).apply {
                putExtra("EXTRA_CONFIG_URL", configUrl)
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MyVpnService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            context.startService(intent)
        }

        private fun writeWsFrame(output: OutputStream, payload: ByteArray, offset: Int, len: Int) {
            output.write(0x82)
            val maskKey = byteArrayOf(0x11, 0x22, 0x33, 0x44)
            if (len <= 125) {
                output.write(len or 0x80)
            } else if (len <= 65535) {
                output.write(126 or 0x80)
                output.write((len shr 8) and 0xFF)
                output.write(len and 0xFF)
            } else {
                output.write(126 or 0x80)
                output.write((len shr 8) and 0xFF)
                output.write(len and 0xFF)
            }
            output.write(maskKey)
            val masked = ByteArray(len)
            for (i in 0 until len) {
                masked[i] = (payload[offset + i].toInt() xor maskKey[i % 4].toInt()).toByte()
            }
            output.write(masked)
            output.flush()
        }

        private fun readWsFrame(input: InputStream): ByteArray? {
            val b0 = input.read()
            if (b0 == -1) return null
            
            val b1 = input.read()
            if (b1 == -1) return null
            val masked = (b1 and 0x80) != 0
            var payloadLen = b1 and 0x7F
            
            if (payloadLen == 126) {
                val lenHi = input.read()
                val lenLo = input.read()
                if (lenHi == -1 || lenLo == -1) return null
                payloadLen = (lenHi shl 8) or lenLo
            } else if (payloadLen == 127) {
                var lenVal = 0L
                for (i in 0 until 8) {
                    val b = input.read()
                    if (b == -1) return null
                    if (i >= 4) {
                        lenVal = (lenVal shl 8) or b.toLong()
                    }
                }
                payloadLen = lenVal.toInt()
            }
            
            val maskKey = if (masked) {
                val keys = ByteArray(4)
                var readBytes = 0
                while (readBytes < 4) {
                    val n = input.read(keys, readBytes, 4 - readBytes)
                    if (n == -1) return null
                    readBytes += n
                }
                keys
            } else null
            
            val payload = ByteArray(payloadLen)
            var readBytes = 0
            while (readBytes < payloadLen) {
                val n = input.read(payload, readBytes, payloadLen - readBytes)
                if (n == -1) return null
                readBytes += n
            }
            
            if (maskKey != null) {
                for (i in 0 until payloadLen) {
                    payload[i] = (payload[i].toInt() xor maskKey[i % 4].toInt()).toByte()
                }
            }
            return payload
        }

        private fun parseUuidBytes(uuidStr: String): ByteArray {
            val clean = uuidStr.replace("-", "")
            if (clean.length != 32) return ByteArray(16)
            val data = ByteArray(16)
            for (i in 0 until 16) {
                val high = Character.digit(clean[i * 2], 16)
                val low = Character.digit(clean[i * 2 + 1], 16)
                data[i] = ((high shl 4) or low).toByte()
            }
            return data
        }
    }
}
