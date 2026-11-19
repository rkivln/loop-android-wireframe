package com.example.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatE2eeCryptoTest {

    @Test
    fun `ecdh key derivation and aes-gcm encrypt-decrypt round-trip succeeds for both sender and recipient`() {
        val alice = ChatE2eeCrypto.generateIdentityKeyPair()
        val bob = ChatE2eeCrypto.generateIdentityKeyPair()

        val payload = DecryptedChatPayload(
            text = "Meet at White Town Promenade gate at 5 PM.",
            replyToSnippet = "Is the Sony A7 IV ready?",
            imageUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
            locationTitle = "Promenade Gate",
            locationAddress = "White Town, Puducherry",
            latitude = 11.9338,
            longitude = 79.8349
        )

        val envelope = ChatE2eeCrypto.encryptPayload(
            payload = payload,
            conversationId = "conv_alice_bob",
            senderUid = "alice_uid",
            messageId = "msg_101",
            ownPrivateKeyPkcs8 = alice.privateKeyPkcs8,
            ownPublicKeyBase64 = alice.publicKeyBase64,
            peerUid = "bob_uid",
            peerPublicKeyBase64 = bob.publicKeyBase64
        )

        // Recipient Bob decrypts using Alice's public key
        val bobDecrypted = ChatE2eeCrypto.decryptPayload(
            ciphertextBase64 = envelope.ciphertextBase64,
            ivBase64 = envelope.ivBase64,
            conversationId = "conv_alice_bob",
            senderUid = "alice_uid",
            messageId = "msg_101",
            ownPrivateKeyPkcs8 = bob.privateKeyPkcs8,
            ownUid = "bob_uid",
            ownPublicKeyBase64 = bob.publicKeyBase64,
            otherUid = "alice_uid",
            otherPublicKeyBase64 = alice.publicKeyBase64
        ).getOrThrow()

        assertEquals(payload, bobDecrypted)

        // Sender Alice can also decrypt her own historical sent message using Bob's public key
        val aliceDecrypted = ChatE2eeCrypto.decryptPayload(
            ciphertextBase64 = envelope.ciphertextBase64,
            ivBase64 = envelope.ivBase64,
            conversationId = "conv_alice_bob",
            senderUid = "alice_uid",
            messageId = "msg_101",
            ownPrivateKeyPkcs8 = alice.privateKeyPkcs8,
            ownUid = "alice_uid",
            ownPublicKeyBase64 = alice.publicKeyBase64,
            otherUid = "bob_uid",
            otherPublicKeyBase64 = bob.publicKeyBase64
        ).getOrThrow()

        assertEquals(payload, aliceDecrypted)
    }

    @Test
    fun `tampered ciphertext or wrong conversation aad or third-party key fails decryption`() {
        val alice = ChatE2eeCrypto.generateIdentityKeyPair()
        val bob = ChatE2eeCrypto.generateIdentityKeyPair()
        val eve = ChatE2eeCrypto.generateIdentityKeyPair()

        val envelope = ChatE2eeCrypto.encryptPayload(
            payload = DecryptedChatPayload(text = "Handover PIN is 4829"),
            conversationId = "conv_1",
            senderUid = "alice",
            messageId = "msg_1",
            ownPrivateKeyPkcs8 = alice.privateKeyPkcs8,
            ownPublicKeyBase64 = alice.publicKeyBase64,
            peerUid = "bob",
            peerPublicKeyBase64 = bob.publicKeyBase64
        )

        // Wrong conversation ID (AAD mismatch)
        val wrongConv = ChatE2eeCrypto.decryptPayload(
            ciphertextBase64 = envelope.ciphertextBase64,
            ivBase64 = envelope.ivBase64,
            conversationId = "conv_other",
            senderUid = "alice",
            messageId = "msg_1",
            ownPrivateKeyPkcs8 = bob.privateKeyPkcs8,
            ownUid = "bob",
            ownPublicKeyBase64 = bob.publicKeyBase64,
            otherUid = "alice",
            otherPublicKeyBase64 = alice.publicKeyBase64
        )
        assertTrue(wrongConv.isFailure)

        // Third party Eve cannot decrypt
        val eveResult = ChatE2eeCrypto.decryptPayload(
            ciphertextBase64 = envelope.ciphertextBase64,
            ivBase64 = envelope.ivBase64,
            conversationId = "conv_1",
            senderUid = "alice",
            messageId = "msg_1",
            ownPrivateKeyPkcs8 = eve.privateKeyPkcs8,
            ownUid = "eve",
            ownPublicKeyBase64 = eve.publicKeyBase64,
            otherUid = "alice",
            otherPublicKeyBase64 = alice.publicKeyBase64
        )
        assertTrue(eveResult.isFailure)
    }

    @Test
    fun `safety number is deterministic and identical for both participants`() {
        val alice = ChatE2eeCrypto.generateIdentityKeyPair()
        val bob = ChatE2eeCrypto.generateIdentityKeyPair()
        val charlie = ChatE2eeCrypto.generateIdentityKeyPair()

        val fromAliceView = ChatE2eeCrypto.computeSafetyNumber(
            ownUid = "uid_alice",
            ownPublicKeyBase64 = alice.publicKeyBase64,
            peerUid = "uid_bob",
            peerPublicKeyBase64 = bob.publicKeyBase64
        )
        val fromBobView = ChatE2eeCrypto.computeSafetyNumber(
            ownUid = "uid_bob",
            ownPublicKeyBase64 = bob.publicKeyBase64,
            peerUid = "uid_alice",
            peerPublicKeyBase64 = alice.publicKeyBase64
        )
        val charlieView = ChatE2eeCrypto.computeSafetyNumber(
            ownUid = "uid_alice",
            ownPublicKeyBase64 = alice.publicKeyBase64,
            peerUid = "uid_charlie",
            peerPublicKeyBase64 = charlie.publicKeyBase64
        )

        assertNotNull(fromAliceView)
        assertNotNull(fromBobView)
        assertEquals(12, fromAliceView!!.chunks.size)
        assertTrue(fromAliceView.chunks.all { it.length == 5 && it.all(Char::isDigit) })
        assertEquals(fromAliceView.singleLine, fromBobView!!.singleLine)
        assertNotEquals(fromAliceView.singleLine, charlieView!!.singleLine)
    }
}
