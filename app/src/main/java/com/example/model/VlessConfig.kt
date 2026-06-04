package com.example.model

import android.util.Base64
import java.net.URLDecoder

data class VlessConfig(
    val uuid: String,
    val address: String,
    val port: Int,
    val path: String?,
    val security: String?,
    val alpn: String?,
    val host: String?,
    val fp: String?,
    val type: String?,
    val sni: String?,
    val remarks: String,
    val protocol: String = "vless"
) {
    companion object {
        fun parse(url: String): VlessConfig? {
            val trimmedUrl = url.trim()
            try {
                if (trimmedUrl.startsWith("vless://")) {
                    return parseVlessOrTrojanOrHysteria(trimmedUrl, "vless")
                } else if (trimmedUrl.startsWith("trojan://")) {
                    return parseVlessOrTrojanOrHysteria(trimmedUrl, "trojan")
                } else if (trimmedUrl.startsWith("hysteria2://") || trimmedUrl.startsWith("hysteria://")) {
                    val proto = if (trimmedUrl.startsWith("hysteria2://")) "hysteria2" else "hysteria"
                    return parseVlessOrTrojanOrHysteria(trimmedUrl, proto)
                } else if (trimmedUrl.startsWith("vmess://")) {
                    return parseVmess(trimmedUrl)
                } else if (trimmedUrl.startsWith("ss://")) {
                    return parseShadowsocks(trimmedUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun parseVlessOrTrojanOrHysteria(url: String, protocol: String): VlessConfig? {
            val schemaLength = protocol.length + 3
            val temp = url.substring(schemaLength)
            val atIdx = temp.indexOf('@')
            if (atIdx == -1) return null
            val uuid = temp.substring(0, atIdx)
            val afterAt = temp.substring(atIdx + 1)
            
            val endHostPortIdx = afterAt.indexOfAny(charArrayOf('?', '#', '/'))
            val hostPortStr = if (endHostPortIdx == -1) afterAt else afterAt.substring(0, endHostPortIdx)
            val remaining = if (endHostPortIdx == -1) "" else afterAt.substring(endHostPortIdx)
            
            val colonIdx = hostPortStr.indexOf(':')
            if (colonIdx == -1) return null
            val address = hostPortStr.substring(0, colonIdx)
            val port = hostPortStr.substring(colonIdx + 1).toIntOrNull() ?: 443
            
            var queryStr = ""
            var remarks = "$protocol Server"
            
            val hashIdx = remaining.indexOf('#')
            if (hashIdx != -1) {
                val rawRemarks = remaining.substring(hashIdx + 1).trim()
                remarks = try {
                    URLDecoder.decode(rawRemarks, "UTF-8")
                } catch (e: Exception) {
                    rawRemarks
                }
                val beforeHash = remaining.substring(0, hashIdx)
                if (beforeHash.startsWith("?")) {
                    queryStr = beforeHash.substring(1)
                } else if (beforeHash.startsWith("/")) {
                    val qMark = beforeHash.indexOf('?')
                    if (qMark != -1) {
                        queryStr = beforeHash.substring(qMark + 1)
                    }
                }
            } else if (remaining.startsWith("?")) {
                queryStr = remaining.substring(1)
            } else if (remaining.startsWith("/")) {
                val qMark = remaining.indexOf('?')
                if (qMark != -1) {
                    queryStr = remaining.substring(qMark + 1)
                }
            }
            
            val queryParams = mutableMapOf<String, String>()
            if (queryStr.isNotEmpty()) {
                queryStr.split('&').forEach { pair ->
                    val parts = pair.split('=')
                    if (parts.size == 2) {
                        queryParams[parts[0]] = parts[1]
                    }
                }
            }
            
            val path = queryParams["path"]?.let { URLDecoder.decode(it, "UTF-8") }
            val security = queryParams["security"] ?: "tls"
            val alpn = queryParams["alpn"]?.let { URLDecoder.decode(it, "UTF-8") }
            val host = queryParams["host"]
            val fp = queryParams["fp"]
            val type = queryParams["type"]
            val sni = queryParams["sni"]
            
            return VlessConfig(
                uuid = uuid,
                address = address,
                port = port,
                path = path,
                security = security,
                alpn = alpn,
                host = host,
                fp = fp,
                type = type,
                sni = sni,
                remarks = remarks,
                protocol = protocol
            )
        }

        private fun parseVmess(url: String): VlessConfig? {
            val rawB64 = url.substring(8).trim()
            try {
                val decodedBytes = Base64.decode(rawB64, Base64.DEFAULT)
                val json = String(decodedBytes, Charsets.UTF_8)
                
                val ps = extractJsonValue(json, "ps") ?: "VMess Server"
                val add = extractJsonValue(json, "add") ?: ""
                val portStr = extractJsonValue(json, "port") ?: "443"
                val port = portStr.toIntOrNull() ?: 443
                val id = extractJsonValue(json, "id") ?: ""
                val net = extractJsonValue(json, "net") ?: "ws"
                val path = extractJsonValue(json, "path")
                val tls = extractJsonValue(json, "tls")
                val host = extractJsonValue(json, "host")
                val sni = extractJsonValue(json, "sni") ?: host
                
                return VlessConfig(
                    uuid = id,
                    address = add,
                    port = port,
                    path = path,
                    security = if (!tls.isNullOrEmpty() && tls != "none") "tls" else "none",
                    alpn = null,
                    host = host,
                    fp = null,
                    type = net,
                    sni = sni,
                    remarks = ps,
                    protocol = "vmess"
                )
            } catch (e: Exception) {
                return parseVlessOrTrojanOrHysteria(url, "vmess")
            }
        }

        private fun parseShadowsocks(url: String): VlessConfig? {
            val remain = url.substring(5)
            val hashIdx = remain.indexOf('#')
            val rawRemarks = if (hashIdx != -1) remain.substring(hashIdx + 1) else ""
            val remarks = if (rawRemarks.isNotEmpty()) {
                try { URLDecoder.decode(rawRemarks, "UTF-8") } catch (e: Exception) { rawRemarks }
            } else { "Shadowsocks Server" }
            
            val contentStr = if (hashIdx != -1) remain.substring(0, hashIdx) else remain
            
            try {
                val atIdx = contentStr.lastIndexOf('@')
                if (atIdx != -1) {
                    val methodPassB64 = contentStr.substring(0, atIdx)
                    val methodPass = try {
                        String(Base64.decode(methodPassB64, Base64.DEFAULT), Charsets.UTF_8)
                    } catch (e: Exception) {
                        methodPassB64
                    }
                    val hostPort = contentStr.substring(atIdx + 1)
                    val colonIdx = hostPort.indexOf(':')
                    if (colonIdx != -1) {
                        val address = hostPort.substring(0, colonIdx)
                        val port = hostPort.substring(colonIdx + 1).toIntOrNull() ?: 8388
                        return VlessConfig(
                            uuid = methodPass,
                            address = address,
                            port = port,
                            path = null,
                            security = "none",
                            alpn = null,
                            host = null,
                            fp = null,
                            type = "tcp",
                            sni = null,
                            remarks = remarks,
                            protocol = "shadowsocks"
                        )
                    }
                } else {
                    val decodedAll = String(Base64.decode(contentStr, Base64.DEFAULT), Charsets.UTF_8)
                    val lastAt = decodedAll.lastIndexOf('@')
                    if (lastAt != -1) {
                        val methodPass = decodedAll.substring(0, lastAt)
                        val hostPort = decodedAll.substring(lastAt + 1)
                        val colonIdx = hostPort.indexOf(':')
                        if (colonIdx != -1) {
                            val address = hostPort.substring(0, colonIdx)
                            val port = hostPort.substring(colonIdx + 1).toIntOrNull() ?: 8388
                            return VlessConfig(
                                uuid = methodPass,
                                address = address,
                                port = port,
                                path = null,
                                security = "none",
                                alpn = null,
                                host = null,
                                fp = null,
                                type = "tcp",
                                sni = null,
                                remarks = remarks,
                                protocol = "shadowsocks"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun extractJsonValue(json: String, key: String): String? {
            val patternStr = "\"$key\"\\s*:\\s*\"([^\"]*)\""
            val regex = patternStr.toRegex(RegexOption.IGNORE_CASE)
            val match = regex.find(json)
            if (match != null) {
                return match.groupValues[1]
            }
            val numberPatternStr = "\"$key\"\\s*:\\s*(\\d+)"
            val numberRegex = numberPatternStr.toRegex(RegexOption.IGNORE_CASE)
            val numMatch = numberRegex.find(json)
            if (numMatch != null) {
                return numMatch.groupValues[1]
            }
            return null
        }
    }
}
