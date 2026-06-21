package com.gestormei.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Criptografia local (AES/GCM via Android Keystore) usada no cofre de senhas.
 * Valores antigos em texto puro continuam legíveis (sem o prefixo são retornados
 * como estão), permitindo migração transparente.
 */
object Cripto {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "gestor_mei_vault"
    private const val TRANSFORMACAO = "AES/GCM/NoPadding"
    private const val PREFIXO = "enc:v1:"
    private const val TAM_IV = 12

    private fun chave(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (ks.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val gerador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        gerador.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return gerador.generateKey()
    }

    fun cifrar(texto: String): String {
        if (texto.isEmpty()) return texto
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMACAO)
            cipher.init(Cipher.ENCRYPT_MODE, chave())
            val iv = cipher.iv
            val cifrado = cipher.doFinal(texto.toByteArray(Charsets.UTF_8))
            PREFIXO + Base64.encodeToString(iv + cifrado, Base64.NO_WRAP)
        }.getOrDefault(texto)
    }

    fun decifrar(texto: String): String {
        if (!texto.startsWith(PREFIXO)) return texto // legado em texto puro
        return runCatching {
            val dados = Base64.decode(texto.removePrefix(PREFIXO), Base64.NO_WRAP)
            val iv = dados.copyOfRange(0, TAM_IV)
            val cifrado = dados.copyOfRange(TAM_IV, dados.size)
            val cipher = Cipher.getInstance(TRANSFORMACAO)
            cipher.init(Cipher.DECRYPT_MODE, chave(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(cifrado), Charsets.UTF_8)
        }.getOrDefault(texto)
    }
}
