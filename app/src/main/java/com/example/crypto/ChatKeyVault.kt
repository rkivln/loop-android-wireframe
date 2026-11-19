package com.example.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object ChatKeyVault {
    private const val TAG = "ChatKeyVault"
    private const val PREFS_NAME = "loop_e2ee_key_vault"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val WRAP_PREFIX = "loop_e2ee_wrap_"
    private const val AES_GCM = "AES/GCM/NoPadding"
    private const val GCM_TAG_BITS = 128

    private val memoryCache = ConcurrentHashMap<String, EcKeyPairEncoded>()

    fun getCachedIdentityKeyPair(uid: String): EcKeyPairEncoded? {
        return if (uid.isBlank()) null else memoryCache[uid]
    }

    fun getOrCreateIdentityKeyPair(context: Context, uid: String): EcKeyPairEncoded {
        require(uid.isNotBlank()) { "UID must not be blank" }
        memoryCache[uid]?.let { return it }

        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val pubB64 = prefs.getString("pub_$uid", null)
        val wrappedB64 = prefs.getString("priv_wrap_$uid", null)
        val wrapIvB64 = prefs.getString("priv_iv_$uid", null)

        if (!pubB64.isNullOrBlank() && !wrappedB64.isNullOrBlank() && !wrapIvB64.isNullOrBlank()) {
            try {
                val pkcs8 = unwrapWithKeystore(
                    uid = uid,
                    wrappedBytes = ChatE2eeCrypto.decodeBase64(wrappedB64),
                    iv = ChatE2eeCrypto.decodeBase64(wrapIvB64)
                )
                val fingerprint = ChatE2eeCrypto.computeKeyFingerprint(pubB64)
                val restored = EcKeyPairEncoded(
                    publicKeyBase64 = pubB64,
                    privateKeyPkcs8 = pkcs8,
                    fingerprint = fingerprint
                )
                memoryCache[uid] = restored
                return restored
            } catch (e: Exception) {
                Log.w(TAG, "Stored key unwrap failed for $uid, regenerating keypair", e)
            }
        }

        val fresh = ChatE2eeCrypto.generateIdentityKeyPair()
        try {
            val (wrapped, iv) = wrapWithKeystore(uid, fresh.privateKeyPkcs8)
            prefs.edit()
                .putString("pub_$uid", fresh.publicKeyBase64)
                .putString("priv_wrap_$uid", ChatE2eeCrypto.encodeBase64(wrapped))
                .putString("priv_iv_$uid", ChatE2eeCrypto.encodeBase64(iv))
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Unable to persist wrapped identity key in Keystore", e)
        }
        memoryCache[uid] = fresh
        return fresh
    }

    fun isPeerFingerprintVerified(
        context: Context,
        ownUid: String,
        peerUid: String,
        currentPeerFingerprint: String
    ): Boolean {
        if (ownUid.isBlank() || peerUid.isBlank() || currentPeerFingerprint.isBlank()) return false
        val saved = getVerifiedPeerFingerprint(context, ownUid, peerUid) ?: return false
        return saved.equals(currentPeerFingerprint, ignoreCase = true)
    }

    fun hasPeerKeyChanged(
        context: Context,
        ownUid: String,
        peerUid: String,
        currentPeerFingerprint: String
    ): Boolean {
        if (ownUid.isBlank() || peerUid.isBlank() || currentPeerFingerprint.isBlank()) return false
        val saved = getVerifiedPeerFingerprint(context, ownUid, peerUid) ?: return false
        return !saved.equals(currentPeerFingerprint, ignoreCase = true)
    }

    fun getVerifiedPeerFingerprint(context: Context, ownUid: String, peerUid: String): String? {
        if (ownUid.isBlank() || peerUid.isBlank()) return null
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("verified_${ownUid}_$peerUid", null)?.takeIf { it.isNotBlank() }
    }

    fun setPeerVerified(
        context: Context,
        ownUid: String,
        peerUid: String,
        peerFingerprint: String,
        verified: Boolean
    ) {
        if (ownUid.isBlank() || peerUid.isBlank()) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        if (verified && peerFingerprint.isNotBlank()) {
            editor.putString("verified_${ownUid}_$peerUid", peerFingerprint)
        } else {
            editor.remove("verified_${ownUid}_$peerUid")
        }
        editor.apply()
    }

    fun clearInMemoryCache() {
        memoryCache.clear()
    }

    private fun getOrCreateWrapKey(uid: String): SecretKey {
        val alias = "$WRAP_PREFIX$uid"
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(alias, null) as? SecretKey
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun wrapWithKeystore(uid: String, plaintext: ByteArray): Pair<ByteArray, ByteArray> {
        val key = getOrCreateWrapKey(uid)
        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(plaintext)
        return encrypted to cipher.iv
    }

    private fun unwrapWithKeystore(uid: String, wrappedBytes: ByteArray, iv: ByteArray): ByteArray {
        val key = getOrCreateWrapKey(uid)
        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(wrappedBytes)
    }
}
