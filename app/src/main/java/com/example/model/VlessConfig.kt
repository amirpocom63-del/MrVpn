package com.example.model

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
    val remarks: String
) {
    companion object {
        fun parse(url: String): VlessConfig? {
            try {
                if (!url.startsWith("vless://")) return null
                val temp = url.substring(8)
                val atIdx = temp.indexOf('@')
                if (atIdx == -1) return null
                val uuid = temp.substring(0, atIdx)
                val afterAt = temp.substring(atIdx + 1)
                
                // Find slash, question or hash which terminates host:port
                val endHostPortIdx = afterAt.indexOfAny(charArrayOf('?', '#', '/'))
                val hostPortStr = if (endHostPortIdx == -1) afterAt else afterAt.substring(0, endHostPortIdx)
                val remaining = if (endHostPortIdx == -1) "" else afterAt.substring(endHostPortIdx)
                
                val colonIdx = hostPortStr.indexOf(':')
                if (colonIdx == -1) return null
                val address = hostPortStr.substring(0, colonIdx)
                val port = hostPortStr.substring(colonIdx + 1).toIntOrNull() ?: 443
                
                var queryStr = ""
                var remarks = "Mrvpn Server"
                
                val hashIdx = remaining.indexOf('#')
                if (hashIdx != -1) {
                    remarks = remaining.substring(hashIdx + 1).trim()
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
                
                val path = queryParams["path"]?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                val security = queryParams["security"]
                val alpn = queryParams["alpn"]?.let { java.net.URLDecoder.decode(it, "UTF-8") }
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
                    remarks = remarks
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
}
