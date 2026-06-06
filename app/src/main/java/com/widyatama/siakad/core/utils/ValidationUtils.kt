package com.widyatama.siakad.core.utils

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

object ValidationUtils {
    fun isValidCampusEmail(email: String): Boolean {
        return email.endsWith("@student.widyatama.ac.id") || email.endsWith("@widyatama.ac.id")
    }

    fun isNotEmpty(vararg values: String): Boolean {
        return values.none { it.isEmpty() }
    }

    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun isValidNpmFormat(npm: String): Boolean {
        return npm.length >= 8 && npm.all { it.isDigit() }
    }

    fun hashPassword(password: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashBase64 = Base64.encodeToString(hashBytes, Base64.NO_WRAP)
        return "$saltBase64:$hashBase64"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false
            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val expectedHash = Base64.decode(parts[1], Base64.NO_WRAP)
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt)
            val actualHash = digest.digest(password.toByteArray(Charsets.UTF_8))
            MessageDigest.isEqual(expectedHash, actualHash)
        } catch (e: Exception) {
            false
        }
    }
}
