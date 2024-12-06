package com.example.passvault

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object SQLCipherUtils {
    fun getBytes(passPhrase: String): ByteArray {
        return try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.digest(passPhrase.toByteArray(Charsets.UTF_8))
        }
        catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Algorithm not found!", e)
        }
    }
}