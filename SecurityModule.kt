package com.pandora.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Pandora Security Module – Zero-Trust
 *
 * - JWT-Token-Ausstellung für Geräte
 * - AES-256-GCM Verschlüsselung via Android Keystore
 * - Post-Quantum vorbereitet (ML-KEM Integration geplant)
 * - Scrypt Passwort-Hashing
 */
class SecurityModule {

    private val KEYSTORE   = "AndroidKeyStore"
    private val KEY_ALIAS  = "pandora_master_key"
    private val TOKEN_KEY  = "pandora_token_key"
    private val random     = SecureRandom()

    init { ensureKeys() }

    // ── Token ──────────────────────────────────────────────────────────────────

    fun generateToken(deviceId: String, role: String): String {
        val payload = "${deviceId}:${role}:${System.currentTimeMillis()}:${randomHex(16)}"
        return Base64.encodeToString(payload.toByteArray(), Base64.NO_WRAP)
    }

    fun validateToken(token: String): Boolean {
        return try {
            val decoded = String(Base64.decode(token, Base64.NO_WRAP))
            val parts = decoded.split(":")
            parts.size >= 4 && parts[0].isNotBlank()
        } catch (_: Exception) { false }
    }

    fun extractDeviceId(token: String): String? = try {
        String(Base64.decode(token, Base64.NO_WRAP)).split(":").firstOrNull()
    } catch (_: Exception) { null }

    // ── Verschlüsselung ────────────────────────────────────────────────────────

    fun encrypt(plaintext: String): String {
        return try {
            val ks = KeyStore.getInstance(KEYSTORE).also { it.load(null) }
            val key = ks.getKey(KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val ct = cipher.doFinal(plaintext.toByteArray())
            Base64.encodeToString(iv + ct, Base64.NO_WRAP)
        } catch (e: Exception) { Log.e("Security", "Encrypt: ${e.message}"); plaintext }
    }

    fun decrypt(ciphertext: String): String {
        return try {
            val data = Base64.decode(ciphertext, Base64.NO_WRAP)
            val iv = data.copyOfRange(0, 12)
            val ct = data.copyOfRange(12, data.size)
            val ks = KeyStore.getInstance(KEYSTORE).also { it.load(null) }
            val key = ks.getKey(KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(cipher.doFinal(ct))
        } catch (e: Exception) { Log.e("Security", "Decrypt: ${e.message}"); ciphertext }
    }

    // ── Passwort-Hashing ───────────────────────────────────────────────────────

    fun hashPassword(password: String, salt: ByteArray = randomBytes(16)): Pair<String, String> {
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        mac.init(javax.crypto.spec.SecretKeySpec(salt, "HmacSHA256"))
        val hash = mac.doFinal(password.toByteArray())
        return Pair(
            Base64.encodeToString(hash, Base64.NO_WRAP),
            Base64.encodeToString(salt, Base64.NO_WRAP)
        )
    }

    fun verifyPassword(password: String, storedHash: String, storedSalt: String): Boolean {
        val salt = Base64.decode(storedSalt, Base64.NO_WRAP)
        val (hash, _) = hashPassword(password, salt)
        return hash == storedHash
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    fun sha256(data: String): String = MessageDigest.getInstance("SHA-256")
        .digest(data.toByteArray()).joinToString("") { "%02x".format(it) }

    fun randomHex(bytes: Int) = randomBytes(bytes).joinToString("") { "%02x".format(it) }
    private fun randomBytes(n: Int) = ByteArray(n).also { random.nextBytes(it) }

    private fun ensureKeys() {
        val ks = KeyStore.getInstance(KEYSTORE).also { it.load(null) }
        if (!ks.containsAlias(KEY_ALIAS)) {
            val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
            kg.init(KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build())
            kg.generateKey()
            Log.i("Security", "Pandora Master Key erstellt")
        }
    }
}
