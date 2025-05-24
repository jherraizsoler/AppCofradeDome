package com.example.CofradeDome.Models

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.nio.ByteBuffer
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

//import android.security.keystore.KeyGenParameterSpec;
object EmailEncrypter {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "cofrade_dome_email_key"
    private const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128 // bits


    fun isKeyGenerated(context: Context?): Boolean {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            return keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            Log.e("EmailEncrypter", "Error al verificar la existencia de la clave: " + e.message, e)
            return false // En caso de error, asumimos que la clave no está generada
        }
    }

    @Throws(Exception::class)
    fun encryptEmail(context: Context?, email: String): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            ?: throw Exception("Clave no encontrada en el Keystore. Genera la clave primero.")

        var cipher: Cipher? = null
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            Log.e("EmailEncrypter", "Error al obtener la instancia del Cipher: " + e.message, e)
            throw Exception("Error al inicializar el cifrado.", e)
        }

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(email.toByteArray(charset("UTF-8")))
        val iv = cipher.iv ?: throw Exception("Error al obtener el IV.")

        // Concatenar el IV y los bytes cifrados
        val byteBuffer = ByteBuffer.allocate(iv.size + encryptedBytes.size)
        byteBuffer.put(iv)
        byteBuffer.put(encryptedBytes)
        val combined = byteBuffer.array()

        Log.d("EmailEncrypter", "encryptEmail llamado con email: $email")

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    fun decryptEmail(context: Context?, encryptedEmailBase64: String?): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            ?: throw Exception("Clave no encontrada en el Keystore.")

        val combined = Base64.decode(encryptedEmailBase64, Base64.DEFAULT)
        val byteBuffer = ByteBuffer.wrap(combined)

        val iv = ByteArray(12) // El tamaño del IV por defecto para GCM suele ser 12 bytes
        byteBuffer[iv]
        val encryptedBytes = ByteArray(byteBuffer.remaining())
        byteBuffer[encryptedBytes]

        var cipher: Cipher? = null
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            Log.e("EmailEncrypter", "Error al obtener la instancia del Cipher: " + e.message, e)
            throw Exception("Error al inicializar el descifrado.", e)
        }

        val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, charset("UTF-8"))
        } catch (e: Exception) {
            Log.e("EmailEncrypter", "Error al descifrar el correo electrónico: " + e.message, e)
            throw Exception("Error al descifrar el correo electrónico.", e)
        }
    }

    @Throws(Exception::class)
    fun generateKey(context: Context?) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
            )

            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )

            keyGenerator.generateKey()
        }
    }
}
