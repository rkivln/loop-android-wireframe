package com.example.crypto

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class EcKeyPairEncoded(
    val publicKeyBase64: String,
    val privateKeyPkcs8: ByteArray,
    val fingerprint: String
)

data class DecryptedChatPayload(
    val v: Int = 1,
    val text: String = "",
    val replyToSnippet: String? = null,
    val imageUrl: String? = null,
    val locationTitle: String? = null,
    val locationAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class EncryptedMessageEnvelope(
    val ciphertextBase64: String,
    val ivBase64: String,
    val keyVersion: Int = CURRENT_KEY_VERSION
) {
    companion object {
        const val CURRENT_KEY_VERSION = 1
    }
}

data class SafetyNumberInfo(
    val chunks: List<String>,
    val formattedGrid: String,
    val singleLine: String,
    val ownFingerprint: String,
    val peerFingerprint: String
)

object ChatE2eeCrypto {

    private const val CURVE_NAME = "secp256r1"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val GCM_TAG_BITS = 128
    private const val GCM_IV_BYTES = 12
    private const val AES_KEY_BYTES = 32
    private const val HKDF_SALT = "LoopStudioDispatch-E2EE-v1"

    private val secureRandom = SecureRandom()

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val payloadAdapter by lazy {
        moshi.adapter(DecryptedChatPayload::class.java)
    }

    fun generateIdentityKeyPair(): EcKeyPairEncoded {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec(CURVE_NAME), secureRandom)
        val kp = kpg.generateKeyPair()
        val pubB64 = encodeBase64(kp.public.encoded)
        val fingerprint = computeKeyFingerprint(pubB64)
        return EcKeyPairEncoded(
            publicKeyBase64 = pubB64,
            privateKeyPkcs8 = kp.private.encoded,
            fingerprint = fingerprint
        )
    }

    fun decodePrivateKey(pkcs8Bytes: ByteArray): PrivateKey {
        val kf = KeyFactory.getInstance("EC")
        return kf.generatePrivate(PKCS8EncodedKeySpec(pkcs8Bytes))
    }

    fun decodePublicKey(publicKeyBase64: String): PublicKey {
        val raw = decodeBase64(publicKeyBase64.trim())
        val kf = KeyFactory.getInstance("EC")
        return kf.generatePublic(X509EncodedKeySpec(raw))
    }

    fun computeKeyFingerprint(publicKeyBase64: String): String {
        val clean = publicKeyBase64.trim()
        if (clean.isBlank()) return ""
        return try {
            val raw = decodeBase64(clean)
            val digest = MessageDigest.getInstance("SHA-256").digest(raw)
            // First 16 bytes -> 8 blocks of 4 uppercase hex characters
            (0 until 8).joinToString(" ") { idx ->
                val b0 = digest[idx * 2].toInt() and 0xFF
                val b1 = digest[idx * 2 + 1].toInt() and 0xFF
                "%02X%02X".format(b0, b1)
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun deriveConversationKey(
        ownPrivateKeyPkcs8: ByteArray,
        ownUid: String,
        ownPublicKeyBase64: String,
        peerUid: String,
        peerPublicKeyBase64: String
    ): ByteArray {
        val ownPrivate = decodePrivateKey(ownPrivateKeyPkcs8)
        val peerPublic = decodePublicKey(peerPublicKeyBase64)

        val agreement = KeyAgreement.getInstance("ECDH")
        agreement.init(ownPrivate)
        agreement.doPhase(peerPublic, true)
        val sharedSecret = agreement.generateSecret()

        val firstPair: Pair<String, String>
        val secondPair: Pair<String, String>
        if (ownUid <= peerUid) {
            firstPair = ownUid to ownPublicKeyBase64.trim()
            secondPair = peerUid to peerPublicKeyBase64.trim()
        } else {
            firstPair = peerUid to peerPublicKeyBase64.trim()
            secondPair = ownUid to ownPublicKeyBase64.trim()
        }

        val info = "${firstPair.first}|${secondPair.first}|${firstPair.second}|${secondPair.second}"
            .toByteArray(StandardCharsets.UTF_8)

        return hkdfSha256(
            ikm = sharedSecret,
            salt = HKDF_SALT.toByteArray(StandardCharsets.UTF_8),
            info = info,
            outputLen = AES_KEY_BYTES
        )
    }

    fun encryptPayload(
        payload: DecryptedChatPayload,
        conversationId: String,
        senderUid: String,
        messageId: String,
        ownPrivateKeyPkcs8: ByteArray,
        ownPublicKeyBase64: String,
        peerUid: String,
        peerPublicKeyBase64: String
    ): EncryptedMessageEnvelope {
        val aesKeyBytes = deriveConversationKey(
            ownPrivateKeyPkcs8 = ownPrivateKeyPkcs8,
            ownUid = senderUid,
            ownPublicKeyBase64 = ownPublicKeyBase64,
            peerUid = peerUid,
            peerPublicKeyBase64 = peerPublicKeyBase64
        )
        val jsonBytes = payloadAdapter.toJson(payload).toByteArray(StandardCharsets.UTF_8)
        val iv = ByteArray(GCM_IV_BYTES).also { secureRandom.nextBytes(it) }
        val aad = associatedData(conversationId, senderUid, messageId)

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(aesKeyBytes, "AES"),
            GCMParameterSpec(GCM_TAG_BITS, iv)
        )
        cipher.updateAAD(aad)
        val ciphertext = cipher.doFinal(jsonBytes)

        return EncryptedMessageEnvelope(
            ciphertextBase64 = encodeBase64(ciphertext),
            ivBase64 = encodeBase64(iv),
            keyVersion = EncryptedMessageEnvelope.CURRENT_KEY_VERSION
        )
    }

    fun decryptPayload(
        ciphertextBase64: String,
        ivBase64: String,
        conversationId: String,
        senderUid: String,
        messageId: String,
        ownPrivateKeyPkcs8: ByteArray,
        ownUid: String,
        ownPublicKeyBase64: String,
        otherUid: String,
        otherPublicKeyBase64: String
    ): Result<DecryptedChatPayload> {
        return try {
            val aesKeyBytes = deriveConversationKey(
                ownPrivateKeyPkcs8 = ownPrivateKeyPkcs8,
                ownUid = ownUid,
                ownPublicKeyBase64 = ownPublicKeyBase64,
                peerUid = otherUid,
                peerPublicKeyBase64 = otherPublicKeyBase64
            )
            val iv = decodeBase64(ivBase64.trim())
            val cipherBytes = decodeBase64(ciphertextBase64.trim())
            val aad = associatedData(conversationId, senderUid, messageId)

            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(aesKeyBytes, "AES"),
                GCMParameterSpec(GCM_TAG_BITS, iv)
            )
            cipher.updateAAD(aad)
            val plainBytes = cipher.doFinal(cipherBytes)
            val json = String(plainBytes, StandardCharsets.UTF_8)
            val parsed = payloadAdapter.fromJson(json)
                ?: return Result.failure(IllegalStateException("Malformed decrypted message payload."))
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun computeSafetyNumber(
        ownUid: String,
        ownPublicKeyBase64: String,
        peerUid: String,
        peerPublicKeyBase64: String
    ): SafetyNumberInfo? {
        val cleanOwn = ownPublicKeyBase64.trim()
        val cleanPeer = peerPublicKeyBase64.trim()
        if (ownUid.isBlank() || peerUid.isBlank() || cleanOwn.isBlank() || cleanPeer.isBlank()) {
            return null
        }
        return try {
            val ownBytes = decodeBase64(cleanOwn)
            val peerBytes = decodeBase64(cleanPeer)

            val firstUid: String
            val firstPub: ByteArray
            val secondUid: String
            val secondPub: ByteArray
            if (ownUid <= peerUid) {
                firstUid = ownUid
                firstPub = ownBytes
                secondUid = peerUid
                secondPub = peerBytes
            } else {
                firstUid = peerUid
                firstPub = peerBytes
                secondUid = ownUid
                secondPub = ownBytes
            }

            val sha512 = MessageDigest.getInstance("SHA-512")
            var digest = ByteArray(0)
            repeat(64) {
                sha512.reset()
                if (digest.isNotEmpty()) sha512.update(digest)
                sha512.update(firstUid.toByteArray(StandardCharsets.UTF_8))
                sha512.update(firstPub)
                sha512.update(secondUid.toByteArray(StandardCharsets.UTF_8))
                sha512.update(secondPub)
                digest = sha512.digest()
            }

            // SHA-512 produces 64 bytes -> 12 groups * 5 bytes = 60 bytes
            val chunks = (0 until 12).map { group ->
                val offset = group * 5
                var value = 0L
                for (i in 0 until 5) {
                    value = (value shl 8) or (digest[offset + i].toLong() and 0xFFL)
                }
                val fiveDigit = (value % 100_000L).toInt()
                "%05d".format(fiveDigit)
            }

            val grid = chunks.chunked(3).joinToString("\n") { row -> row.joinToString("  ") }
            val single = chunks.joinToString(" ")
            SafetyNumberInfo(
                chunks = chunks,
                formattedGrid = grid,
                singleLine = single,
                ownFingerprint = computeKeyFingerprint(cleanOwn),
                peerFingerprint = computeKeyFingerprint(cleanPeer)
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun associatedData(conversationId: String, senderUid: String, messageId: String): ByteArray {
        return "$conversationId|$senderUid|$messageId".toByteArray(StandardCharsets.UTF_8)
    }

    private fun hkdfSha256(
        ikm: ByteArray,
        salt: ByteArray,
        info: ByteArray,
        outputLen: Int
    ): ByteArray {
        val extractMac = Mac.getInstance("HmacSHA256")
        extractMac.init(SecretKeySpec(salt, "HmacSHA256"))
        val prk = extractMac.doFinal(ikm)

        val expandMac = Mac.getInstance("HmacSHA256")
        expandMac.init(SecretKeySpec(prk, "HmacSHA256"))

        val result = ByteArray(outputLen)
        var t = ByteArray(0)
        var offset = 0
        var counter = 1
        while (offset < outputLen) {
            expandMac.reset()
            expandMac.update(t)
            expandMac.update(info)
            expandMac.update(counter.toByte())
            t = expandMac.doFinal()
            val copyLen = minOf(t.size, outputLen - offset)
            System.arraycopy(t, 0, result, offset, copyLen)
            offset += copyLen
            counter++
        }
        return result
    }

    private const val B64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    fun encodeBase64(input: ByteArray): String {
        if (input.isEmpty()) return ""
        val out = StringBuilder(((input.size + 2) / 3) * 4)
        var i = 0
        while (i < input.size) {
            val b0 = input[i].toInt() and 0xFF
            val hasB1 = i + 1 < input.size
            val hasB2 = i + 2 < input.size
            val b1 = if (hasB1) input[i + 1].toInt() and 0xFF else 0
            val b2 = if (hasB2) input[i + 2].toInt() and 0xFF else 0

            val triple = (b0 shl 16) or (b1 shl 8) or b2
            out.append(B64_ALPHABET[(triple shr 18) and 0x3F])
            out.append(B64_ALPHABET[(triple shr 12) and 0x3F])
            out.append(if (hasB1) B64_ALPHABET[(triple shr 6) and 0x3F] else '=')
            out.append(if (hasB2) B64_ALPHABET[triple and 0x3F] else '=')
            i += 3
        }
        return out.toString()
    }

    fun decodeBase64(input: String): ByteArray {
        val cleaned = input.filter { !it.isWhitespace() }
        if (cleaned.isEmpty()) return ByteArray(0)
        require(cleaned.length % 4 == 0) { "Invalid Base64 length" }
        var padding = 0
        if (cleaned.endsWith("==")) padding = 2 else if (cleaned.endsWith("=")) padding = 1
        val outLen = (cleaned.length / 4) * 3 - padding
        val out = ByteArray(outLen)
        var outIdx = 0
        var i = 0
        while (i < cleaned.length) {
            val c0 = decodeChar(cleaned[i])
            val c1 = decodeChar(cleaned[i + 1])
            val c2 = if (cleaned[i + 2] == '=') 0 else decodeChar(cleaned[i + 2])
            val c3 = if (cleaned[i + 3] == '=') 0 else decodeChar(cleaned[i + 3])
            val triple = (c0 shl 18) or (c1 shl 12) or (c2 shl 6) or c3
            if (outIdx < outLen) out[outIdx++] = ((triple shr 16) and 0xFF).toByte()
            if (outIdx < outLen) out[outIdx++] = ((triple shr 8) and 0xFF).toByte()
            if (outIdx < outLen) out[outIdx++] = (triple and 0xFF).toByte()
            i += 4
        }
        return out
    }

    private fun decodeChar(c: Char): Int = when (c) {
        in 'A'..'Z' -> c.code - 'A'.code
        in 'a'..'z' -> c.code - 'a'.code + 26
        in '0'..'9' -> c.code - '0'.code + 52
        '+' -> 62
        '/' -> 63
        else -> throw IllegalArgumentException("Invalid Base64 character: $c")
    }
}
