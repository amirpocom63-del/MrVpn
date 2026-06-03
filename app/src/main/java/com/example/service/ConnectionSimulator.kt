package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.random.Random

object ConnectionSimulator {
    
    // Simulate ping latency by trying to open socket connection or returning simulated stable value
    suspend fun measurePing(host: String, port: Int): Int = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val socket = Socket()
            // Quick 1.5s timeout socket probe to simulate real handshake latency
            socket.connect(InetSocketAddress(host, port), 1500)
            socket.close()
            val duration = (System.currentTimeMillis() - startTime).toInt()
            if (duration < 5) duration + 40 else duration
        } catch (e: IOException) {
            // If offline, block-level ping sandbox simulation (keeps UX highly interactive and lively)
            delay(150 + Random.nextLong(100))
            (45 + Random.nextInt(35))
        }
    }

    // Stream live network download & upload speeds when connected
    fun streamLiveSpeed(isConnected: Boolean): Flow<NetworkSpeed> = flow {
        var baseDown = 1.2f // MB/s
        var baseUp = 0.3f // MB/s
        
        while (true) {
            if (isConnected) {
                // Generate natural fluctuations in network speeds
                val fluctuationDown = (Random.nextFloat() - 0.48f) * 0.4f
                val fluctuationUp = (Random.nextFloat() - 0.48f) * 0.1f
                baseDown = (baseDown + fluctuationDown).coerceIn(0.2f, 12.5f)
                baseUp = (baseUp + fluctuationUp).coerceIn(0.05f, 3.2f)
                
                emit(NetworkSpeed(downSpeedMbs = baseDown, upSpeedMbs = baseUp))
            } else {
                emit(NetworkSpeed(downSpeedMbs = 0f, upSpeedMbs = 0f))
            }
            delay(1000)
        }
    }
}

data class NetworkSpeed(
    val downSpeedMbs: Float,
    val upSpeedMbs: Float
)
