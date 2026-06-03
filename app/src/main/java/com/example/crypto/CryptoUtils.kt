package com.example.crypto

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object CryptoUtils {
    fun generateKey(): String {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey = keyGen.generateKey()
        return Base64.encodeToString(secretKey.encoded, Base64.DEFAULT).trim()
    }

    fun encrypt(data: String, keyStr: String): String {
        return try {
            val keyBytes = Base64.decode(keyStr, Base64.DEFAULT)
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)
            val gcmSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
            val cipherText = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            Base64.encodeToString(combined, Base64.DEFAULT).trim()
        } catch (e: Exception) {
            e.printStackTrace()
            Base64.encodeToString(data.toByteArray(Charsets.UTF_8), Base64.DEFAULT).trim()
        }
    }

    fun decrypt(encryptedData: String, keyStr: String): String {
        return try {
            val keyBytes = Base64.decode(keyStr, Base64.DEFAULT)
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)
            if (combined.size <= 12) {
                return String(Base64.decode(encryptedData, Base64.DEFAULT), Charsets.UTF_8)
            }
            val iv = ByteArray(12)
            System.arraycopy(combined, 0, iv, 0, 12)
            val cipherText = ByteArray(combined.size - 12)
            System.arraycopy(combined, 12, cipherText, 0, cipherText.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                String(Base64.decode(encryptedData, Base64.DEFAULT), Charsets.UTF_8)
            } catch (ex: Exception) {
                encryptedData
            }
        }
    }
}
