package dev.voile.tunnel.crypto

import android.util.Base64
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.digests.SHA256Digest
import java.security.SecureRandom

/**
 * Utilitaires cryptographiques pour WireGuard.
 * Utilise BouncyCastle (audité, MIT) pour X25519.
 *
 * ⚠️ Curve25519 (utilisé par WireGuard) ≠ Ed25519 (utilisé par SSH/GPG).
 * WireGuard utilise Montgomery form X25519 pour ECDH.
 */
object CryptoUtils {

    /**
     * Génère une paire de clés WireGuard (Curve25519).
     *
     * @return Pair(privateKey, publicKey) en bytes bruts (32 octets chacun)
     */
    fun generateKeyPair(): KeyPair {
        val generator = X25519KeyPairGenerator()
        generator.init(X25519KeyGenerationParameters(SecureRandom()))

        val privateKeyParams = generator.generateKeyPair()
        val privateKey = (privateKeyParams.private as X25519PrivateKeyParameters).encoded
        val publicKey = (privateKeyParams.public as X25519PublicKeyParameters).encoded

        return KeyPair(privateKey, publicKey)
    }

    /**
     * Calcule un secret partagé via ECDH X25519.
     * Utile pour les tests ou futures extensions.
     */
    fun computeSharedSecret(privateKey: ByteArray, publicKey: ByteArray): ByteArray {
        val privateParams = X25519PrivateKeyParameters(privateKey, 0)
        val publicParams = X25519PublicKeyParameters(publicKey, 0)

        val agreement = X25519Agreement()
        agreement.init(privateParams)

        val sharedSecret = ByteArray(agreement.agreementSize)
        agreement.calculateAgreement(publicParams, sharedSecret, 0)
        return sharedSecret
    }

    /**
     * Calcule un hash SHA-256.
     */
    fun sha256(input: ByteArray): ByteArray {
        val digest = SHA256Digest()
        digest.update(input, 0, input.size)
        val output = ByteArray(digest.digestSize)
        digest.doFinal(output, 0)
        return output
    }

    /**
     * Encode en Base64 URL-safe (RFC 4648 §5).
     * WireGuard utilise ce format pour les clés.
     */
    fun encodeBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)
    }

    /**
     * Décode depuis Base64 URL-safe.
     */
    fun decodeBase64(encoded: String): ByteArray {
        return Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    data class KeyPair(val privateKey: ByteArray, val publicKey: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is KeyPair) return false
            return privateKey.contentEquals(other.privateKey) &&
                   publicKey.contentEquals(other.publicKey)
        }
        override fun hashCode(): Int =
            privateKey.contentHashCode() * 31 + publicKey.contentHashCode()
    }
}
